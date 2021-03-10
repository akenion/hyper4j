package com.alexkenion.hyper4j.server;

import com.alexkenion.hyper4j.http.BufferHttpResponseWriter;
import com.alexkenion.hyper4j.http.HttpException;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.HttpResponse;
import com.alexkenion.hyper4j.http.HttpResponseWriter;
import com.alexkenion.hyper4j.logging.LogLevel;

public class SessionWorker implements Runnable{
	
	private Server server;
	private Session session;
	private HttpResponseWriter writer;
	
	public SessionWorker(Server server, Session session) {
		this.server=server;
		this.session=session;
		this.writer=new BufferHttpResponseWriter(server.getSettings().getBufferSize(), session.getOutputBufferConsumer());
	}
	
	private void writeResponse(HttpResponse response) {
		try {
			writer.write(session.getCurrentProtocolVersion(), response);
		}
		catch(HttpException e) {
			server.getLogger().log(LogLevel.ERROR, String.format("Unable to write response to client %s, caught: %s", session.getClientAddress(), e.getMessage()));
			try {
				session.terminate();
			} catch (LockException lockException) {
				server.getLogger().log(LogLevel.ERROR, String.format("Failed to disconnect session %s with write error: unable to acquire session lock", session.getClientAddress()));
			}
		}
	}

	@Override
	public void run() {
		try {
			session.lock();
		}
		catch(LockException e) {		
			server.getLogger().log(LogLevel.ERROR, String.format("Unable to acquire session lock for %s", session.getClientAddress()));
			return;
		}
		try {
			HttpRequest request=session.processInput();
			if(request==null) {
				server.getLogger().log(LogLevel.DEBUG, "No request");
			}
			else {
				server.getLogger().log(LogLevel.INFO, String.format("Request for %s with method %s", request.getUrl(), request.getMethod()));
				HttpResponse response=server.handleRequest(request);
				writeResponse(response);
				if(request.shouldClose())
					session.terminate();
			}
		} catch (HttpException e) {
			System.err.println("Received malformed request");
			e.printStackTrace();
			writeResponse(server.generateErrorResponse((short)400));
			try {
				session.terminate();
			} catch (LockException lockException) {
				server.getLogger().log(LogLevel.ERROR, String.format("Failed to disconnect session %s with protocol error: unable to acquire session lock", session.getClientAddress()));
			}
		} catch (LockException e) {
			server.getLogger().log(LogLevel.ERROR, String.format("Unable to acquire session lock for %s", session.getClientAddress()));
		}
		session.unlock();
	}

}
