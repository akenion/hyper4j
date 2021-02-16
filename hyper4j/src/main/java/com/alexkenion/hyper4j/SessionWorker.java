package com.alexkenion.hyper4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SessionWorker implements Runnable{
	
	private HttpServer server;
	private Session session;
	
	public SessionWorker(HttpServer server, Session session) {
		this.server=server;
		this.session=session;
	}
	
	private void writeResponse(HttpResponse response) {
		response.setHeader("Server", "Hyper4J/0.0.0");
		String body=response.getBody();
		response.setHeader("Content-Length", body.length()+"");
		Charset charset=Charset.forName("ISO-8859-1");
		StringBuilder out=new StringBuilder();
		out.append("HTTP/1.1 "+response.getStatus()+" OK\r\n");
		for(String field:response.getHeaders().keySet()) {
			String value=response.getHeader(field);
			out.append(field+": "+value+"\r\n");
		}
		out.append("\r\n");
		out.append(body);
		System.out.println("Sending response: "+out);
		SocketChannel client=session.getChannel();
		try {
			client.write(charset.encode(out.toString()));
			//client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
			writeResponse(server.handleRequest(request));
		} catch (HttpException e1) {
			System.err.println("Received malformed request");
			e1.printStackTrace();
			writeResponse(new HttpResponse(400));
		}
		session.unlock();
	}

}
