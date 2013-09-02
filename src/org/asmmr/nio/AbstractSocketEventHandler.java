package org.asmmr.nio;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
/**
 * @author asim.ali
 *
 */
public class AbstractSocketEventHandler implements ISocketEventHandler {

	public void OnAccept(SelectionKey key, ServerSocketChannel channel) {
		// TODO Auto-generated method stub
		
	}
	public void OnConnect(SelectionKey key, SocketChannel channel) {
		// TODO Auto-generated method stub

	}

	public void OnRead(SelectionKey key, SocketChannel channel) {
		// TODO Auto-generated method stub

	}

	public void OnWrite(SelectionKey key, SocketChannel channel) {
		// TODO Auto-generated method stub

	}

	public void Registered(SelectionKey key, AbstractSelectableChannel channel) {
		// TODO Auto-generated method stub
		
	}
}
