package com.alexkenion.hyper4j.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.alexkenion.hyper4j.http.Http;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.HttpResponse;
import com.alexkenion.hyper4j.logging.LogLevel;
import com.alexkenion.hyper4j.logging.Logger;
import com.alexkenion.hyper4j.logging.NullLogger;

/**
 * An HTTP server implementation that aims to be compliant with RFC 7230
 * @author Alex Kenion
 */
public class Server implements SessionObserver {
	
	private Set<SocketAddress> addresses;
	private ServerSettings settings;
	private Logger logger;
	private boolean running=false;
	private RequestHandler handler;
	private ExecutorService threadPool;
	private Set<Session> sessions;
	
	public Server(ServerSettings settings, Set<SocketAddress> addresses) {
		this.settings=settings;
		this.addresses=addresses;
		this.logger=new NullLogger();
	}
	
	public Server(ServerSettings settings) {
		this(settings, new HashSet<SocketAddress>());
	}
	
	public Server(Set<SocketAddress> addresses) {
		this(new ServerSettings(), addresses);
	}
	
	public Server() {
		this(new HashSet<SocketAddress>());
	}
	
	public ServerSettings getSettings() {
		return this.settings;
	}
	
	public void bind(SocketAddress address) {
		addresses.add(address);
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
			response=new HttpResponse(500);
		}
		return postProcessResponse(response);
	}
	
	public void start() throws IOException {
		running=true;
		sessions=new HashSet<Session>();
		threadPool=Executors.newFixedThreadPool(8);
		Selector selector=Selector.open();
		for(SocketAddress address:addresses) {
			ServerSocketChannel server=ServerSocketChannel.open();
			server.bind(address);
			server.configureBlocking(false);
			server.register(selector, SelectionKey.OP_ACCEPT);
		}
		while(running) {
			selector.select(settings.getIdleTimeout());
			Set<SelectionKey> selected=selector.selectedKeys();
			Iterator<SelectionKey> iterator=selected.iterator();
			while(iterator.hasNext()) {
				SelectionKey key=iterator.next();
				iterator.remove();
				if(!key.isValid())
					continue;
				if(key.isValid()&&key.isAcceptable()) {
					SocketChannel client=((ServerSocketChannel)key.channel()).accept();
					if(client==null) {
						logger.log(LogLevel.DEBUG, "Client is null");
						continue;
					}
					logger.log(LogLevel.DEBUG, String.format("Accepted connection from %s", client.getRemoteAddress()));
					client.configureBlocking(false);
					Session session=new Session(settings, client);
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
							threadPool.submit(new SessionWorker(this, session));
							break;
						}
					}
					if(read==-1) {
						session.terminate();
					}
					session.unlock();
				}
			}
			long currentTime=System.currentTimeMillis();
			for(Session session:sessions) {
				if((currentTime-session.getLastInteraction())/1000>settings.getIdleTimeout()) {
					logger.log(LogLevel.WARNING, String.format("Client %s has been idle for too long, disconnecting...", session.getClientAddress()));
					session.terminate();
				}
			}
		}
		selector.close();
		try {
			threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.log(LogLevel.ERROR, "Thread interrupted");
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

	@Override
	public void onTermination(Session session) {
		logger.log(LogLevel.DEBUG, String.format("Client %s disconnected", session.getClientAddress()));
		sessions.remove(session);
	}

}
