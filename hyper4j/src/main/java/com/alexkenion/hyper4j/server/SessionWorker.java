package com.alexkenion.hyper4j.server;

import com.alexkenion.hyper4j.http.ChannelHttpResponseWriter;
import com.alexkenion.hyper4j.http.HttpException;
import com.alexkenion.hyper4j.http.HttpRequest;
import com.alexkenion.hyper4j.http.HttpResponse;
import com.alexkenion.hyper4j.http.HttpResponseWriter;

public class SessionWorker implements Runnable{
	
	private Server server;
	private Session session;
	private HttpResponseWriter writer;
	
	public SessionWorker(Server server, Session session) {
		this.server=server;
		this.session=session;
		this.writer=new ChannelHttpResponseWriter(session.getChannel(), server.getSettings().getBufferSize());
	}
	
	private void writeResponse(HttpResponse response) {
		try {
			writer.write(session.getCurrentProtocolVersion(), response);
		}
		catch(HttpException e) {
			//TODO: Implement proper error handling
			System.err.println("Unable to write response");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		session.lock();
		try {
			HttpRequest request=session.processInput();
			System.out.println("Processing request: "+request);
			if(request==null) {
				System.out.println("No request");
				return;
			}
			System.out.println("Request for "+request.getUrl()+" with method "+request.getMethod());
			HttpResponse response=server.handleRequest(request);
			writeResponse(response);
		} catch (HttpException e) {
			System.err.println("Received malformed request");
			e.printStackTrace();
			writeResponse(server.generateErrorResponse((short)400));
		}
		session.unlock();
	}

}
