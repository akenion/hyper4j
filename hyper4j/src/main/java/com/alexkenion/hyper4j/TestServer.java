package com.alexkenion.hyper4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class TestServer {
	
	public TestServer() {
		//
	}
	
	public void serve() {
		try {
			Selector selector = Selector.open();
			ServerSocketChannel channel = ServerSocketChannel.open();
			channel.bind(new InetSocketAddress(8080));
			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_ACCEPT);
			while(true) {
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				for(SelectionKey key:selectedKeys) {
					if(key.isAcceptable()) {
						SelectableChannel client = channel.accept();
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);
					}
					if(key.isReadable()) {
						SocketChannel client = (SocketChannel)key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(512);
						client.read(buffer);
						buffer.flip();
						client.write(buffer);
						client.close();
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Failed!");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
