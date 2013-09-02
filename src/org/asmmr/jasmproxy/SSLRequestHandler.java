package org.asmmr.jasmproxy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import org.apache.log4j.Logger;
import org.asmmr.common.MessageParts;
import org.asmmr.common.MessageParts.HeaderField;
import org.asmmr.http.HttpMessage;
import org.asmmr.http.HttpRequestMessage;
import org.asmmr.http.HttpResponseMessage;
import org.asmmr.nio.SocketEventEngine;

/**
 * @author asim.ali
 *
 */
public class SSLRequestHandler extends  AbstractRequestHandler
{
	static Logger logger = Logger.getLogger(SSLRequestHandler.class);
	
	private int writeoperation;
	
	public SSLRequestHandler(SocketChannel client, SocketEventEngine eventengine,IRequestHandlerContainer container)throws IOException
    {
		super(client,eventengine,container);
    	writeoperation = SENDHEADERS;
        //contentlength=0;
    }
	
	public SSLRequestHandler(SocketChannel client, SocketEventEngine eventengine,IRequestHandlerContainer container,Object args)throws IOException
	{
		super(client,eventengine,container,args);
		writeoperation = SENDHEADERS;
		
	}
			
	@Override
	public void OnConnect(SelectionKey key, SocketChannel channel) {
		// TODO Auto-generated method stub
		try {
    		if(channel.finishConnect()) {
    			serverstate=CONNECTED;
    			container.echo("Connected to " + channel.socket().getRemoteSocketAddress().toString(), this);
    			if(container.isForwardToUpStreamProxyServer())
    			{
    				String headers = request.toStringHeaders();
        			headerdata = ByteBuffer.wrap(headers.getBytes("US-ASCII"));
        			container.echo("Sending request headers data to server " + channel.socket().getRemoteSocketAddress().toString(), this);
        			channel.write(headerdata);
        			if(headerdata.hasRemaining()){
        				writeoperation = SENDHEADERS;
        				key.interestOps(SelectionKey.OP_WRITE );
        			}else {
        				headerdata=null;
        				key.interestOps(SelectionKey.OP_READ );
        			}
        			//}else
        				//key.interestOps(SelectionKey.OP_READ);
        			if(currentstate==NOSTATE)currentstate=GETRESPONSEHEADERS;
    			}else{
	    			response = new HttpResponseMessage(200,"HTTP/1.1","Connection Established");
	    			HeaderField header = new MessageParts.HeaderField("Via");
	    			header.addValue(new MessageParts.HeaderFieldValue("1.0 JASMProxy Server"));
	    			response.addHeader(header);
	    			
	    			String headers =  response.toStringHeaders();
	    			headerdata = ByteBuffer.wrap(headers.getBytes("US-ASCII"));
	    			container.echo("Sending OK response to client. " + client.socket().getRemoteSocketAddress().toString(), this);
	
	    			while(headerdata.hasRemaining())
	    				client.write(headerdata);
	
	    			headerdata=null;
	    			
	    			container.echo("SSL tunnel established. ", this);
	    			
	    			key.interestOps(SelectionKey.OP_READ );
	    			
	    				//key.interestOps(SelectionKey.OP_READ);
	    			currentstate=SSLSTARTED;
    			}
    			
    		}
    	}catch(IOException e) {
    		e.printStackTrace();
    		serverstate=ERROR;
    		sendError("Could not connect to the requested host\r\nError :" + e.getMessage());
    		shutDownHandler();
    	}
	}
	
	 public void OnWrite(SelectionKey key, SocketChannel channel) {
	        //To change body of implemented methods use File | Settings | File Templates.
	    	if(channel==client)
	    		container.echo("OnWrite called for client " + channel.socket().getRemoteSocketAddress().toString() , this);
	    	else
	    		container.echo("OnWrite called for server " + channel.socket().getRemoteSocketAddress().toString() , this);
	    	
	    	try {
	    		switch(writeoperation) {
	    			
	    			case SENDHEADERS:
	    				 int tempvar2 = channel.write(headerdata);
	    				 container.echo(tempvar2 + " header bytes in OnWrite sent to " + channel.socket().getRemoteSocketAddress().toString() , this);
	    				if(!headerdata.hasRemaining()){ 
	    					key.interestOps(SelectionKey.OP_READ);
	    					//if(currentstate==GETRESPONSEHEADERS || currentstate==GETREQUESTHEADERS) 
	    					headerdata=null;
	    					//if(currentstate==GETRESPONSEHEADERS)
	    				}
	    				break;
	    			/*case SENDBODY:
	    				contentlength -= channel.write(data);
	    				if(!data.hasRemaining()){
	    					key.interestOps(SelectionKey.OP_READ);
	    					data.clear();
	    					writeoperation=BODYSENT;
	    					if(contentlength<=0) {
	    						headerdata=null;
	    						data =null;
	    						if(currentstate==GETREQUESTBODY)
	    							currentstate=GETRESPONSEHEADERS;
	    						else if(currentstate==GETRESPONSEBODY)
	    							currentstate=GETREQUESTHEADERS;
	    					}
	    				}
	    				break;*/
	    		}
	    	}catch(IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	
	@Override
	public void OnRead(SelectionKey key, SocketChannel channel) {
		// TODO Auto-generated method stub
		int res;
		try{
	        switch(currentstate){
	        	
	        	case GETRESPONSEHEADERS:
	        	case GETREQUESTHEADERS:
	        		if(headerdata==null)headerdata = ByteBuffer.allocate(1024);
	        		if(data==null)data = ByteBuffer.allocate(channel.socket().getReceiveBufferSize()/2);
	        		res = channel.read(data);
	        		if(res<0) {
	        			shutDownHandler();
	        			break;
	        		}
	        		data.flip();
	        		
	        		while(data.hasRemaining()){
	        			byte b = data.get();
	        			try {
	        				headerdata.put(b);
	        			}catch(BufferOverflowException e) {
	        				//headerdata.flip();
	        				
	        				ByteBuffer tempbuffer = ByteBuffer.allocate(headerdata.capacity()+1024);
	        				headerdata.flip();
	        				String tmpstr = chardecoder.decode(headerdata).toString();
	        				headerdata.flip();
	        				tempbuffer.put(headerdata);
	        				tempbuffer.put(b);
	        				headerdata=tempbuffer;
	        			}
	        			if(b==13 || b==10)
	        				tempvar++;
	        			else 
	        				tempvar=0;
	        			if(tempvar>=4)
	        			{
	        				data.compact();
	        				//tempvar=0;
	        				process();
	        				break;
	        			}
	        		}
	        		if(tempvar<4) 
	        			data.clear();
	        		else
	        			tempvar=0;
	        		break;
	        		
	        	case SSLSTARTED:
	        		if(headerdata==null){
		        		res=channel.read(data);
		        		data.flip();
		        		String tmpstr = chardecoder.decode(data).toString();
		        		if(res<0) {
	    	        		shutDownHandler();
	    	        		break;
	    	        	}
		        		while(data.position()>0) {
		        			int tempvar2;
		        	    	data.flip();
		        	    	if(channel==client) {
		        				tempvar2 = server.write(data);
		        				container.echo(tempvar2 + " bytes sent to server " + 
		        						server.socket().getRemoteSocketAddress().toString(),this);
		        	    	}else{
		        				tempvar2 = client.write(data);
		        				container.echo(tempvar2 + " bytes sent to client " + 
		        						client.socket().getRemoteSocketAddress().toString(),this);
		        	    	}
		        	    	data.compact();
		        		}
	        		}else{
	        			if(channel==client) {
	        				container.echo("Cannot read more data from client, still have header data to send - " + 
        						client.socket().getRemoteSocketAddress().toString(),this);
	        			}else{
	        				container.echo("Cannot read more data from server, still have header data to send - " + 
	        						server.socket().getRemoteSocketAddress().toString(),this);
	        			}
	        		}
		        	break;
	        		        	
	        }
    	}catch(IOException e){
    		e.printStackTrace();
    		shutDownHandler();
    	}
	}
	private void process() {
		currentstate=NOSTATE;
		headerdata.flip();
		String headers = chardecoder.decode(headerdata).toString();
		try{
			HttpMessage tmpmess = HttpMessage.parse(headers);
	
			if(tmpmess.getMessageType() == HttpMessage.REQUESTMESSAGE){
				request = (HttpRequestMessage)tmpmess;
				if (request.StartLine.Method.compareToIgnoreCase("CONNECT") == 0) {
					container.echo("======= Got HTTPS Request from "
							+ client.socket().getRemoteSocketAddress().toString()
							+ "======\n" + request.toStringHeaders()
							+ "\n=====================================", this);
		
					connectToServer();
				}else{
					sendError("Bad HTTPS Request");
					shutDownHandler();
				}
			} else {
				response = (HttpResponseMessage)tmpmess;
				
				container.echo("========Got Response from  " + 
						server.socket().getRemoteSocketAddress().toString() +
						"=========\n" + response.toStringHeaders() + 
						"\n=============================", this);
				
				headers = response.toStringHeaders();
				headerdata = ByteBuffer.wrap(headers.getBytes("US-ASCII"));
				container.echo("Sending response headers data to client " + client.socket().getRemoteSocketAddress().toString(), this);
				client.write(headerdata);
				if(headerdata.hasRemaining()){
					writeoperation = SENDHEADERS;
					eventengine.getKey(client).
					interestOps(SelectionKey.OP_WRITE);
				}else
					headerdata=null;
				
				currentstate = SSLSTARTED;
			}
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
			shutDownHandler();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendError("IO Error occured while processing your request.\r\nError :" + e.getMessage());
			shutDownHandler();
		}
	}
	
	@Override
	protected int getDefaultPort() {
		// TODO Auto-generated method stub
		return 443;
	}
	
	public String getClientAddress() {
    	// TODO Auto-generated method stub
    	if(client!=null)
    		return client.socket().getRemoteSocketAddress().toString();
    	return "Client Disconnected";
    }
    public String getServerAddress() {
    	// TODO Auto-generated method stub
    	if(server!=null)
    		return server.socket().getRemoteSocketAddress().toString();
    	return "Server Disconnected";
    }
	
	@Override
	public void Registered(SelectionKey key, AbstractSelectableChannel channel) {
		// TODO Auto-generated method stub
		if(channel == client)
    		container.echo("New HTTPS Request Handler created.", this);
	}
	
	@Override
    protected void finalize() throws Throwable {
    	container.echo("HTTPS Hanlder Finalized method called.",this);
    	super.finalize();
    }

	public void handleDelegatedRequest() 
	{
		process();
	}
	
	public Logger getLogger() {
		return logger;
	}
}
