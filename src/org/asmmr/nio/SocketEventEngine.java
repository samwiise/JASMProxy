package org.asmmr.nio;

/**
 * Created by IntelliJ IDEA.
 * User: Asim Ali
 * Date: Jul 14, 2006
 * Time: 12:38:06 AM
 * To change this template use File | Settings | File Templates.
 */

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;
import java.util.LinkedList;
/**
 * @author asim.ali
 *
 */
public class SocketEventEngine {

    private LinkedList<SCRegistrationRequest> regrequests;
    private Selector selector;
    private boolean isrunning;

    public SocketEventEngine() {
        regrequests = new LinkedList<SCRegistrationRequest>();
        isrunning=false;
    }

    public synchronized void queueForRegister(SCRegistrationRequest request) throws IOException
    {
    	request.getChannel().configureBlocking(false);
    	regrequests.offer(request);
    }
    public int getLoad() 
    {
    	return selector.keys().size();
    }
    public boolean isRunning() {
    	return isrunning;
    }
    public void wakeup(){
        if(isrunning)selector.wakeup();
    }
    public SelectionKey getKey(AbstractSelectableChannel channel) {
    	return channel.keyFor(selector);
    }
    private synchronized void processRegisterRequests()
    {
        SCRegistrationRequest temp;
        while((temp=(SCRegistrationRequest)regrequests.poll())!=null)
        {
            try
            {
                SelectionKey key=temp.getChannel().register(selector,
                        temp.getOperationset(),temp.getHandler());
                temp.getHandler().Registered(key,temp.getChannel());
            }catch(ClosedChannelException e)
            {
                //do nothing
            }catch(IOException e){
            	e.printStackTrace();
            }
        }
    }
    public void stop() 
    {
    	if(isrunning) {
    		isrunning=false;
    		selector.wakeup();
    	}
    }
    public void Run() throws IOException
    {
        selector = Selector.open();
        isrunning=true;
        while (isrunning) {
            processRegisterRequests();
            int a;
            if ((a = selector.select()) > 0) {
                Iterator it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    it.remove();
                    if (key.isValid()) {
						ISocketEventHandler handler = (ISocketEventHandler) key
								.attachment();
						if (key.isReadable()) {
							// System.out.println("Incoming Data #" + ((Integer)
							// key.attachment()).intValue());
							handler.OnRead(key, (SocketChannel) key.channel());
						} else if (key.isWritable()) {
							// System.out.println("Writing Data #" +
							// ((Integer) key.attachment()).intValue());
							handler.OnWrite(key, (SocketChannel) key.channel());
						} else if (key.isConnectable()) {
							handler.OnConnect(key, (SocketChannel) key
									.channel());
						} else if (key.isAcceptable()) {
							handler.OnAccept(key, (ServerSocketChannel) key
									.channel());
						}
					}
                }
            }
        }
    }

    public void finalize(){
        try{
            selector.close();
            regrequests.clear();
        }catch(Exception e){
        }
    }
}
