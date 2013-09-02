/**
 * 
 */
package org.asmmr.jasmproxy.proxy.requesthandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import org.apache.log4j.Logger;
import org.asmmr.jasmproxy.IRequestHandlerContainer;
import org.asmmr.jasmproxy.SSLRequestHandler;
import org.asmmr.jasmproxy.base.GenericRequestHandler;
import org.asmmr.nio.SocketEventEngine;

/**
 * @author asim.ali
 *
 */
public class SOCKS4RequestHandler extends GenericRequestHandler {

	static Logger logger = Logger.getLogger(SOCKS4RequestHandler.class);
	
	protected static final int VERSION=4;
	protected static final int CD_CONNECT=1;
	protected static final int CD_BIND=2;
	protected static final int REPLYVERSION=0;
	protected static final int CD_REQUESTGRANTED=90;
	protected static final int CD_REQUESTFAILED=91;
	protected static final int CD_REQUESTREJECTED1=92;
	protected static final int CD_REQUESTREJECTED2=93;
	
	protected ByteBuffer data;
	
	protected InetAddress destIP;
	protected short destPort;
	protected String userId;
	
	public SOCKS4RequestHandler(SocketChannel client, SocketEventEngine eventengine,IRequestHandlerContainer container)throws IOException
    {
        initHandler(client, eventengine, container);
    }
	
	/* (non-Javadoc)
	 * @see org.asmmr.jasmproxy.base.GenericRequestHandler#getServerSocketAddress()
	 */
	@Override
	protected SocketAddress getServerSocketAddress() {
		// TODO Auto-generated method stub
		return new InetSocketAddress(destIP,destPort);
	}

	/* (non-Javadoc)
	 * @see org.asmmr.jasmproxy.base.GenericRequestHandler#sendError(java.lang.String)
	 */
	@Override
	protected void sendError(String message) {
		// TODO Auto-generated method stub

	}

	protected void shutDownHandler() 
    {
		super.shutDownHandler();
		data=null;
    }
	
	@Override
    public void Registered(SelectionKey key, AbstractSelectableChannel channel) {
    	// TODO Auto-generated method stub
    	if(channel == client)
    		container.echo("New SOCKS 4 Request Handler created.", this);
    }
	
	 public void OnConnect(SelectionKey key, SocketChannel channel) {
    	try {
    		if(channel.finishConnect()) {
    			serverstate=CONNECTED;
    			container.echo("Connected to " + channel.socket().getRemoteSocketAddress().toString(), this);
    			    			
    			key.interestOps(SelectionKey.OP_READ);
    			
    			currentstate=CONNECTED;
    			data.clear();
   			
    			container.echo("Notifying Client : Connection Established. ", this);
    			replyClient(CD_REQUESTGRANTED);
    		}
    	}catch(IOException e) {
    		e.printStackTrace();
    		serverstate=ERROR;
    		sendError("Could not connect to the requested host\r\nError :" + e.getMessage());
    		shutDownHandler();
    	}
    }
	 
	protected void replyClient(int replyCD) 
	{
		try {
			if(client!=null) 
			{
				byte[] rpBytes = new byte[8];
				
				rpBytes[0] = REPLYVERSION;
				rpBytes[1] = (byte)replyCD;
				
				ByteBuffer buffer =	ByteBuffer.wrap(rpBytes);
				
				client.write(buffer);
				
				if(replyCD!=CD_REQUESTGRANTED)
					shutDownHandler();
			}
		}catch(IOException e) {
			e.printStackTrace();
			currentstate=ERROR;
			sendError("Could not notify to the client \r\nError :" + e.getMessage());
			shutDownHandler();
		}
	}
	
	public void OnRead(SelectionKey key, SocketChannel channel) {
        //To change body of implemented methods use File | Settings | File Templates.
    	if(channel==server)
    		container.echo("OnRead Called from server.", this);
    	else
    		container.echo("OnRead Called from client.", this);
    	
    	int res=0;
    	
    	try{
	        switch(currentstate){
	        	
	        	case GETREQUEST:
	        		if(data==null)data = ByteBuffer.allocate(channel.socket().getReceiveBufferSize()/2);
	        		res = channel.read(data);
	        		if(res<0) {
	        			shutDownHandler();
	        			break;
	        		}
	        		process();
	        		break;
	        		
	        	case CONNECTED:
	        		if(data==null)data = ByteBuffer.allocate(channel.socket().getReceiveBufferSize()/2);
	        		res = channel.read(data);
	        		
	        		if(res<0) {
	        			shutDownHandler();
	        			break;
	        		}
	        		processData(channel);
	        		
	        		break;
	        }
    	}catch(IOException e){
    		e.printStackTrace();
    		shutDownHandler();
    	}
    }

	private void processData(SocketChannel channel)throws IOException 
    {
    	int tempvar2;
    	while(data.position()>0) {
	    	data.flip();
	    	if(channel==client) {
				tempvar2 = server.write(data);
				container.echo(tempvar2 + " content bytes sent to server " + 
						server.socket().getRemoteSocketAddress().toString(),this);
	    	}else{
				tempvar2 = client.write(data);
				container.echo(tempvar2 + " content bytes sent to client " + 
						client.socket().getRemoteSocketAddress().toString(),this);
	    	}
			data.compact();
    	}
    }
	
	 private void process(){
	    	currentstate=NOSTATE;
	    	data.flip();
			try{
				
				byte version = data.get();
				
				if(version!=VERSION) {
					sendError("Invalid SOCKS Protocol Version.");
					shutDownHandler();						
				}
				
				byte command = data.get(); 
				
				switch(command) 
				{
					case CD_CONNECT:
						destPort = data.getShort();
						byte[] addrBytes = new byte[4];
						data.get(addrBytes);
												
						destIP = InetAddress.getByAddress(addrBytes);
						userId="";
						while(data.hasRemaining()){
							byte b = data.get();
							if(b==0)
								break;
							
							userId += (char)b; 
						}
						if(userId=="")
							userId="Unknown";
						container.echo("SOCKs CONNECT Command Received for " + destIP.getHostAddress() + ":" + destPort + ", User - " + userId, this);
						connectToServer();

						break;
					
					case CD_BIND:
						container.echo("BIND Request is not supported.", this);
						shutDownHandler();
						break;
					
				}
				
			}catch(IOException e) {
				e.printStackTrace();
				sendError("IO Error occured while processing your request.\r\nError :" + e.getMessage());
				shutDownHandler();
			} catch (Exception e) {
				e.printStackTrace();
				sendError("Error occured while processing your request.\r\nError :" + e.getMessage());
				shutDownHandler();
			}
					
	    }
	 
		public Logger getLogger() {
			return logger;
		}
}
