package com.alexkenion.hyper4j.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.alexkenion.hyper4j.http.Http;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.HttpResponse;
import com.alexkenion.hyper4j.logging.LogLevel;
import com.alexkenion.hyper4j.logging.Logger;
import com.alexkenion.hyper4j.logging.NullLogger;
import com.alexkenion.hyper4j.tls.TlsSettings;

/**
 * An HTTP server implementation that aims to be compliant with RFC 7230
 * @author Alex Kenion
 */
public class Server implements SessionObserver {
	
	private Map<SocketAddress, SessionManager> addresses;
	private ServerSettings settings;
	private Logger logger;
	private boolean running=false, stopped=false;
	private RequestHandler handler;
	private ExecutorService threadPool;
	private Set<Session> sessions;
	private ReentrantLock sessionsLock;
	private ServerObserver observer=null;
	private Thread thread = null;
	
	public Server(ServerSettings settings) {
		this.settings=settings;
		this.addresses=new HashMap<SocketAddress, SessionManager>();
		this.logger=new NullLogger();
		this.sessionsLock=new ReentrantLock();
		this.observer=new NullServerObserver();
	}
	
	public Server() {
		this(new ServerSettings());
	}
	
	public ServerSettings getSettings() {
		return this.settings;
	}
	
	public void bind(SocketAddress address) {
		addresses.put(address, new StandardSessionManager(settings, logger));
	}
	
	public void bind(SocketAddress address, TlsSettings tlsSettings) {
		addresses.put(address, new SecureSessionManager(settings, tlsSettings, logger));
	}
	
	public void setLogger(Logger logger) {
		this.logger=logger;
	}
	
	public Logger getLogger() {
		return logger;
	}

	public void setHandler(RequestHandler handler) {
		this.handler=handler;
	}
	
	public void setObserver(ServerObserver observer) {
		this.observer=observer;
	}
	
	private HttpResponse postProcessResponse(HttpResponse response) {
		if(response==null)
			response=new HttpResponse(404);
		if(settings.hasServerIdentifier())
			response.setHeader(Http.HEADER_SERVER, settings.getServerIdentifier());
		return response;
	}
	
	public HttpResponse generateErrorResponse(short status, Exception exception) {
		return postProcessResponse(new HttpResponse(status));
	}
	
	public HttpResponse generateErrorResponse(short status) {
		return generateErrorResponse(status, null);
	}
	
	public HttpResponse handleRequest(HttpRequest request) {
		HttpResponse response;
		try {
			response=this.handler.handle(request);
		}
		catch(Exception e) {
			logger.log(LogLevel.ERROR, "Exception while processing request: "+e.getMessage());;
			response=new HttpResponse(500);
		}
		return postProcessResponse(response);
	}
	
	public void start() throws IOException {
		try {
			thread = Thread.currentThread();
			running=true;
			stopped=false;
			sessions=new HashSet<Session>();
			//TODO: Ensure exceptions thrown in the worker threads do not go unnoticed
			threadPool=Executors.newFixedThreadPool(settings.getWorkerCount());
			Selector selector=Selector.open();
			for(SocketAddress address:addresses.keySet()) {
				ServerSocketChannel server=ServerSocketChannel.open();
				try {
					server.getClass().getMethod("bind", address.getClass());
					server.bind(address);
				} catch (NoSuchMethodException e) {
					server.socket().bind(address);
				} catch (SecurityException e) {
					logger.log(LogLevel.ERROR, "Unable to bind to address: "+address);
				}	
				server.configureBlocking(false);
				SelectionKey key=server.register(selector, SelectionKey.OP_ACCEPT);
				key.attach(addresses.get(address));
			}
			while(running) {
				selector.select(settings.hasReadTimeout() ? settings.getReadTimeout() : 0);
				Set<SelectionKey> selected=selector.selectedKeys();
				Iterator<SelectionKey> iterator=selected.iterator();
				while(iterator.hasNext()) {
					SelectionKey key=iterator.next();
					iterator.remove();
					if(!key.isValid())
						continue;
					try {
						if(key.isValid()&&key.isAcceptable()) {
							SocketChannel client=((ServerSocketChannel)key.channel()).accept();
							if(client==null) {
								logger.log(LogLevel.DEBUG, "Client is null");
								continue;
							}
							client.configureBlocking(false);
							Session session=((SessionManager)key.attachment()).createSession(client);
							sessions.add(session);
							session.setObserver(this);
							client.register(selector, SelectionKey.OP_READ, session);
						}
						else if(key.isValid()&&key.isReadable()) {
							Session session=(Session)key.attachment();
							session.lock();
							SocketChannel client=(SocketChannel)key.channel();
							int read=0;
							while(client.isOpen()&&session.getBuffer().hasRemaining()&&(read=client.read(session.getBuffer()))>0) {
								logger.log(LogLevel.DEBUG, String.format("Read %d bytes from %s", read, session.getClientAddress()));
								if(read>0) {
									threadPool.submit(new MonitoredRunnable(new SessionWorker(this, session), logger));
									break;
								}
							}
							if(read==-1) {
								session.terminate();
							}
							session.unlock();
						}
					}
					catch(CancelledKeyException e) {
						logger.log(LogLevel.WARNING, "Key cancelled");
						key.channel().close();
					}
					catch(LockException e) {
						logger.log(LogLevel.ERROR, "Failed to acquire session lock");
						key.channel().close();
					}
				}
				long currentTime=System.currentTimeMillis();
				sessionsLock.lock();
				for(Session session:sessions) {
					if((currentTime-session.getLastInteraction())>settings.getIdleTimeoutMilliseconds()) {
						logger.log(LogLevel.WARNING, String.format("Client %s has been idle for too long, disconnecting...", session.getClientAddress()));
						try{
							session.terminate();
						}
						catch(LockException e) {
							logger.log(LogLevel.WARNING, String.format("Failed to disconnect idle client %s: unable to acquire session lock", session.getClientAddress()));
						}
					}
				}
				sessionsLock.unlock();
			}
			for(SelectionKey key : selector.keys()) {
				key.channel().close();
			}
			selector.close();
		}
		catch (ClosedByInterruptException | ClosedSelectorException e) {
			logger.log(LogLevel.DEBUG, "Selector closed: " + e.getMessage());
		}
		finally {
			threadPool.shutdown();
			if (!thread.isInterrupted()) {
				try {
					threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					logger.log(LogLevel.WARNING, "Thread interrupted while awaiting termination");
				}
			}
			stopped=true;
			observer.onStop();
		}
	}
	
	public void startInThread() {
		Thread thread=new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					start();
				} catch (IOException e) {
					logger.log(LogLevel.ERROR, String.format("Server threw IOException: %s", e.getMessage()));
					running=false;
				}
			}
			
		});
		thread.start();
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public boolean isStopped() {
		return stopped;
	}
	
	public void stop() {
		running=false;
		thread.interrupt();
	}
	
	public void awaitStop(long sleep) {
		stop();
		while(!stopped) {
			try {
				Thread.sleep(sleep);
			}
			catch (InterruptedException e) {
				break;
			}
		}
	}
	
	public void awaitStop() {
		awaitStop(10);
	}

	@Override
	public void onTermination(Session session) {
		threadPool.submit(new MonitoredRunnable(new SessionTerminator(this, session), logger));
	}
	
	public void removeSession(Session session) {
		sessionsLock.lock();
		logger.log(LogLevel.DEBUG, String.format("Client %s disconnected", session.getClientAddress()));
		sessions.remove(session);
		sessionsLock.unlock();
	}

}
