/**
 * 
 */
package org.asmmr.jasmproxy.base;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.spi.AbstractSelectableChannel;

import org.apache.log4j.Logger;
import org.asmmr.jasmproxy.IRequestHandler;
import org.asmmr.jasmproxy.IRequestHandlerContainer;
import org.asmmr.nio.AbstractSocketEventHandler;
import org.asmmr.nio.SCRegistrationRequest;
import org.asmmr.nio.SocketEventEngine;


/**
 * @author asim.ali
 *
 */
public abstract class GenericRequestHandler  extends AbstractSocketEventHandler 
					implements IRequestHandler {
	
	
	static Logger logger = Logger.getLogger(GenericRequestHandler.class);
	
	protected static final int CONNECTING=5;
	protected static final int NOTCONNECTED=6;
	protected static final int CONNECTED=7;
	protected static final int ERROR=9;
	protected static final int GETREQUEST=1;	
    protected static final int NOSTATE=8;
    
	protected long id;
	protected SocketChannel client,server;
	protected IRequestHandlerContainer container;
	protected SocketEventEngine eventengine;
	
	protected int currentstate,serverstate;

	/**
	 * 
	 */
	public GenericRequestHandler()
    {
		currentstate = GETREQUEST;
        serverstate = NOTCONNECTED;
    }
	
	protected void initHandler(SocketChannel client, SocketEventEngine eventengine,IRequestHandlerContainer container)throws IOException 
	{
    	this.container = container;
        this.client = client;
        this.eventengine = eventengine;
        eventengine.queueForRegister(new SCRegistrationRequest(client,
        		SelectionKey.OP_READ,this));
        eventengine.wakeup();				
	}
	
	protected void initHandlerOnDelegationRequest(SocketChannel client, SocketEventEngine eventengine,IRequestHandlerContainer container)throws IOException 
	{
    	this.container = container;
        this.client = client;
        this.eventengine = eventengine;
        SelectionKey key = eventengine.getKey(client);

        key.attach(this);
        
	}
	
	protected void connectToServer() 
    {
		try {
    		if(server!=null)server.close();
    		
        	server = SocketChannel.open();
    		
    		eventengine.queueForRegister(
    				new SCRegistrationRequest(server,SelectionKey.OP_CONNECT,this));
    		
    		SocketAddress address =null;
    		
    		if(container.isForwardToUpStreamProxyServer())
    		{
    			address = container.getUpStreamProxyAddress();
    		}else{
    			address= getServerSocketAddress();
    		}

    		container.echo("Connecting to " + address.toString(), this);
    		server.connect(address);
    		serverstate = CONNECTING;
    	}catch(IOException e) {
    		e.printStackTrace();
    		shutDownHandler();
    	}catch(UnresolvedAddressException e) {
    		e.printStackTrace();
    		sendError("Requested host could not be resolved.");
    		shutDownHandler();
    	}
    
    }
	protected abstract SocketAddress getServerSocketAddress();
	protected abstract void sendError(String message); 
	
	protected void shutDownHandler() {
		shutDownHandler(false);
	}
	protected void shutDownHandler(boolean delegated) 
    {
		if(!delegated)
			container.echo("Shutting down handler",this);
		else
			container.echo("Shutting down handler, request has been delegated.",this);
		
    	try{
    		if(client!=null && !delegated)client.close();
    		if(server!=null)server.close();
    	}catch(IOException e) {
    		e.printStackTrace();
    	}finally {
    		client=null;
    		server=null;
    		eventengine=null;
    		container.close(this);
    	}
    }
	
	public String getClientAddress() {
    	if(client!=null)
    		return client.socket().getRemoteSocketAddress().toString();
    	return "Client Disconnected";
    }
    public String getServerAddress() {
    	if(server!=null)
    		return server.socket().getRemoteSocketAddress().toString();
    	return "Server Disconnected";
    }

	/* (non-Javadoc)
	 * @see src.IRequestHandler#getId()
	 */
	public long getId() {
		return this.id;
	}
	
	/* (non-Javadoc)
	 * @see src.IRequestHandler#setId(long)
	 */
	public void setId(long id) {
		this.id=id;
	}
	
	public void Registered(SelectionKey key, AbstractSelectableChannel channel) {
		if(channel == client)
			container.echo("New Request Handler created.", this);
	}
	
	public SocketChannel getClient() {
		return client;
	}
	public SocketEventEngine getSocketEventEngine() {
		return eventengine;
	}
	
	public void handleDelegatedRequest() {
		// TODO Auto-generated method stub
		shutDownHandler();
	}
	public Logger getLogger() {
		return logger;
	}
}
