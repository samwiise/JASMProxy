package org.asmmr.nio;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
/**
 * Created by IntelliJ IDEA.
 * User: Asim Ali
 * Date: Jul 13, 2006
 * Time: 8:40:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ISocketEventHandler {
	public void OnAccept(SelectionKey key,ServerSocketChannel channel);
    public void OnConnect(SelectionKey key,SocketChannel channel);
    public void OnRead(SelectionKey key,SocketChannel channel);
    public void OnWrite(SelectionKey key,SocketChannel channel);
    public void Registered(SelectionKey key,AbstractSelectableChannel channel);
}

