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
import com.alexkenion.hyper4j.http.HttpException;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.HttpResponse;

public class Server{
	
	private Set<SocketAddress> addresses;
	private ServerSettings settings;
	private boolean running=false;
	private RequestHandler handler;
	private ExecutorService threadPool;
	
	public Server(ServerSettings settings, Set<SocketAddress> addresses) {
		this.settings=settings;
		this.addresses=addresses;
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
		threadPool=Executors.newFixedThreadPool(8);
		Selector selector=Selector.open();
		for(SocketAddress address:addresses) {
			ServerSocketChannel server=ServerSocketChannel.open();
			server.bind(address);
			server.configureBlocking(false);
			server.register(selector, SelectionKey.OP_ACCEPT);
		}
		while(running) {
			selector.select();
			Set<SelectionKey> selected=selector.selectedKeys();
			Iterator<SelectionKey> iterator=selected.iterator();
			while(iterator.hasNext()) {
				SelectionKey key=iterator.next();
				//System.out.println("Processing key...");
				iterator.remove();
				if(!key.isValid())
					continue;
				if(key.isValid()&&key.isAcceptable()) {
					SocketChannel client=((ServerSocketChannel)key.channel()).accept();
					if(client==null) {
						System.out.println("Client is null");
						continue;
					}
					System.out.println("Accepted connection from "+client.getRemoteAddress());
					client.configureBlocking(false);
					client.register(selector, SelectionKey.OP_READ, new Session(settings, client));
				}
				else if(key.isValid()&&key.isReadable()) {
					Session session=(Session)key.attachment();
					session.lock();
					SocketChannel client=(SocketChannel)key.channel();
					int read=0;
					while(client.isOpen()&&session.getBuffer().hasRemaining()&&(read=client.read(session.getBuffer()))>0) {
						System.out.println("Read "+read+" bytes from "+client.getRemoteAddress());
						if(read>0) {
							threadPool.submit(new SessionWorker(this, session));
							break;
						}
					}
					if(read==-1) {
						client.close();
						System.out.println("Client disconnected");
					}
					session.unlock();
				}
			}
		}
		selector.close();
		try {
			threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startInThread() {
		Thread thread=new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		thread.start();
	}

}
