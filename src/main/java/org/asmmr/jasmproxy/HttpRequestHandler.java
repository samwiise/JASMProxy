package org.asmmr.jasmproxy;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import org.apache.log4j.Logger;
import org.asmmr.common.MessageParts.HeaderField;
import org.asmmr.http.HttpMessage;
import org.asmmr.http.HttpRequestMessage;
import org.asmmr.http.HttpResponseMessage;
import org.asmmr.jasmproxy.interceptor.IHttpRequestInterceptor;
import org.asmmr.nio.SocketEventEngine;

/**
 * @author asim.ali
 *
 */

public class HttpRequestHandler extends AbstractRequestHandler
{
    
    
    private int writeoperation;
   
    int contentlength;

    static Logger logger = Logger.getLogger(HttpRequestHandler.class);
            
    public HttpRequestHandler(SocketChannel client, SocketEventEngine eventengine,IRequestHandlerContainer container)throws IOException
    {
    	super(client,eventengine,container);
    	        
        writeoperation = SENDHEADERS;
        contentlength=0;
    }
    
    
    public void OnConnect(SelectionKey key, SocketChannel channel) {
        //To change body of implemented methods use File | Settings | File Templates.
    	try {
    		if(channel.finishConnect()) {
    			serverstate=CONNECTED;
    			container.echo("Connected to " + channel.socket().getRemoteSocketAddress().toString(), this);
    			
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
    				if(currentstate==GETREQUESTBODY)
    					if(data.position()>0)processData();
    			}
    			//}else
    				//key.interestOps(SelectionKey.OP_READ);
    			if(currentstate==NOSTATE)currentstate=GETRESPONSEHEADERS;
    				
    			
    		}
    	}catch(IOException e) {
    		e.printStackTrace();
    		serverstate=ERROR;
    		sendError("Could not connect to the requested host\r\nError :" + e.getMessage());
    		shutDownHandler();
    	}
    }
    
    public void OnRead(SelectionKey key, SocketChannel channel) {
        //To change body of implemented methods use File | Settings | File Templates.
    	if(channel==server)
    		container.echo("OnRead Called from server.", this);
    	else
    		container.echo("OnRead Called from client.", this);
    	
    	try{
	        switch(currentstate){
	        	
	        	case GETRESPONSEHEADERS:
	        	case GETREQUESTHEADERS:
	        		if(headerdata==null)headerdata = ByteBuffer.allocate(1024);
	        		if(data==null)data = ByteBuffer.allocate(channel.socket().getReceiveBufferSize()/2);
	        		int res = channel.read(data);
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
	        		
	        	case GETRESPONSEBODY:
	        	case GETREQUESTBODY:
	        		if(serverstate==CONNECTED) {
	        			if(headerdata==null){
	        				res=channel.read(data);
	        				if(res<0) {
	    	        			shutDownHandler();
	    	        			break;
	    	        		}
	        				//writeoperation=SENDBODY;
	        				//eventengine.getKey(server).
	        					//interestOps(SelectionKey.OP_WRITE);
	        				processData();
	        			}
	        		}
	        		
	        		break;
	        	case GETRESPONSEBODY2:
	        		if(headerdata==null && channel==server){
        				res=channel.read(data);
        				if(res<0) {
    	        			shutDownHandler();
    	        			break;
    	        		}
        				//writeoperation=SENDBODY;
        				//eventengine.getKey(server).
        					//interestOps(SelectionKey.OP_WRITE);
        				processData2();
        			}else if(channel==client){
        				currentstate = GETREQUESTHEADERS;
        			}
	        		break;
	        	/*	if(!headerdata.hasRemaining()){
        				if(writeoperation!=SENDBODY){
        					channel.read(data);
        					writeoperation=SENDBODY;
        					eventengine.getKey(client).
        						interestOps(SelectionKey.OP_WRITE);
        					data.flip();
        				}
        			}
	        		break;*/
	        }
    	}catch(IOException e){
    		//e.printStackTrace();
    		logger.error(e.getMessage(), e);
    		//logger.error(e);
    		shutDownHandler();
    	}
    }
    
    private void processData()throws IOException 
    {
    	int tempvar2;
    	while(data.position()>0) {
	    	data.flip();
	    	if(currentstate==GETREQUESTBODY) {
				tempvar2 = server.write(data);
				container.echo(tempvar2 + " content bytes sent to server " + 
						server.socket().getRemoteSocketAddress().toString(),this);
	    	}else{
				tempvar2 = client.write(data);
				container.echo(tempvar2 + " content bytes sent to client " + 
						client.socket().getRemoteSocketAddress().toString(),this);
	    	}
	    	contentlength-=tempvar2;
			if(contentlength<=0) {
				data =null;
				//data.clear();
				if(currentstate==GETREQUESTBODY)
					currentstate=GETRESPONSEHEADERS;
				else if(currentstate==GETRESPONSEBODY){
					currentstate=GETREQUESTHEADERS;
					server.close();
					server=null;
					serverstate = NOTCONNECTED;
				}
				break;
			}else
				data.compact();
    	}
    }
    private void processData2()throws IOException 
    {
    	int tempvar2;
    	while(data.position()>0) {
	    	data.flip();
	    	tempvar2 = client.write(data);
			container.echo(tempvar2 + " content bytes sent to client " + 
				client.socket().getRemoteSocketAddress().toString(),this);
			data.compact();
    	}
    }
    private void process(){
    	currentstate=NOSTATE;
    	headerdata.flip();
    	String headers = chardecoder.decode(headerdata).toString();
		try{
			//container.echo("HHHEEEDDD = \r\n" + headers,this); 
			HttpMessage tmpmess =HttpMessage.parse(headers);
			if(tmpmess==null)
				tmpmess =HttpMessage.parse(headers);
			HeaderField clength = tmpmess.getHeader("Content-Length");
			if(clength!=null) 
				contentlength =Integer.parseInt(clength.getValue(0).Value);
			else
				contentlength=0;
			if(tmpmess.getMessageType()==HttpMessage.REQUESTMESSAGE) 
			{
				
				request =(HttpRequestMessage)tmpmess;
				
				if (request.StartLine.Method.compareToIgnoreCase("CONNECT") == 0) 
				{
					container.echo("======= Got HTTPS Request from " +
							client.socket().getRemoteSocketAddress().toString()+
							"======\n" + "Delegating Request to appropriate handler." + 
							"\n=====================================",this);
					delegateRequest("SSL");
					return;
				}
				
				container.echo("======= Got HTTP Request from " +
						client.socket().getRemoteSocketAddress().toString()+
						"======\n" + request.toStringHeaders() + 
						"\n=====================================",this);
				
				
				int result = ServerControl.HttpRequestArrived(this,request, client);
				
				if(result==IHttpRequestInterceptor.DISCONTINUE){
					shutDownHandler();
					return;
				}
				
				connectToServer();
				if(contentlength > 0) 
					currentstate=GETREQUESTBODY;
				
			}else{
				response = (HttpResponseMessage)tmpmess;
				
				container.echo("========Got Response from  " + 
						server.socket().getRemoteSocketAddress().toString() +
						"=========\n" + response.toStringHeaders() + 
						"\n=============================", this);
				
				
				int result = ServerControl.HttpResponseArrived(this, response, request, client, server);
				
				if(result==IHttpRequestInterceptor.DISCONTINUE){
					shutDownHandler();
					return;
				}
				
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
    			//data.flip();
    			//String testd = chardecoder.decode(data).toString();
    			
    			//data.compact();
    			
    			if(clength==null) 
    				if(response.getHeader("Content-Type")!=null){
    					currentstate=GETRESPONSEBODY2;
    					if(data.position()>0 && headerdata==null)processData2();
    					return;
    				}
    			
    			if(contentlength>0){
					currentstate=GETRESPONSEBODY;
					if(data.position()>0 && headerdata==null)processData();
    			}else {
    				currentstate=GETREQUESTHEADERS;
    				server.close();
    				server=null;
    				serverstate = NOTCONNECTED;
    				if(response.StartLine.StatusCode==302) 
    					shutDownHandler();
    				else{
    					HeaderField conn =	response.getHeader("Connection");
    					if(conn!=null)
    						if(conn.getValue(0).Value.compareToIgnoreCase("Close")==0)
    							shutDownHandler();
    				}
    				
    			}
			}
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
			shutDownHandler();
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
    					if(currentstate==GETREQUESTBODY || currentstate==GETRESPONSEBODY) {
        					if(data.position()>0)processData();
    					}else if(currentstate==GETRESPONSEBODY2 && data.position()>0)
    						processData2();
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
    protected int getDefaultPort() {
    	// TODO Auto-generated method stub
    	return 80;
    }
    
    @Override
    public void Registered(SelectionKey key, AbstractSelectableChannel channel) {
    	// TODO Auto-generated method stub
    	if(channel == client)
    		container.echo("New Http Request Handler created.", this);
    }

        
    @Override
    protected void finalize() throws Throwable {
    	container.echo("HTTP Hanlder Finalized method called.",this);
    	super.finalize();
    }

	public Logger getLogger() {
		return logger;
	}
}
