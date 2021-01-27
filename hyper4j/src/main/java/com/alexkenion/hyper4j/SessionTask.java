package com.alexkenion.hyper4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SessionTask implements Runnable{
	
	private HttpServer server;
	private Session session;
	
	public SessionTask(HttpServer server, Session session) {
		this.server=server;
		this.session=session;
	}

	@Override
	public void run() {
		if(!session.lock())
			return;
		Request request=session.processInput();
		Response response=server.handleRequest(request);//handleRequest(request);
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
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		session.unlock();
	}

}
