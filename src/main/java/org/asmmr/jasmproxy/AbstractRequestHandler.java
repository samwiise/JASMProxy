package org.asmmr.jasmproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.log4j.Logger;
import org.asmmr.common.MessageParts;
import org.asmmr.common.MessageParts.HeaderField;
import org.asmmr.http.HttpRequestMessage;
import org.asmmr.http.HttpResponseMessage;
import org.asmmr.jasmproxy.base.GenericRequestHandler;
import org.asmmr.nio.SocketEventEngine;

/**
 * @author asim.ali
 *
 */
public abstract class AbstractRequestHandler extends GenericRequestHandler implements IHttpRequestHandler
{

	static Logger logger = Logger.getLogger(AbstractRequestHandler.class);
	
//	STATES
	protected static final int GETREQUESTHEADERS=1;
	protected static final int GETREQUESTBODY=2;
	protected static final int GETRESPONSEHEADERS=3;
	protected static final int GETRESPONSEBODY=4;
	protected static final int GETRESPONSEBODY2=10;

		
	protected static final int SSLSTARTED=11;
	
//	write operation
	protected static final int SENDHEADERS=0;
    protected static final int SENDBODY=1;
    protected static final int BODYSENT=2;
		
	protected ByteBuffer headerdata,data;
	
	protected HttpRequestMessage request;
    protected HttpResponseMessage response;
	
	protected short tempvar;
		
	
	protected static Charset chardecoder;
    
    static {
    	chardecoder = Charset.forName("US-ASCII");
    }
	
    private AbstractRequestHandler() {
    	currentstate = GETREQUESTHEADERS;
        tempvar=0;
    }
    
	public AbstractRequestHandler(SocketChannel client, SocketEventEngine eventengine,IRequestHandlerContainer container)throws IOException
    {		
		this();
        initHandler(client, eventengine, container);
    }
	
	public AbstractRequestHandler(SocketChannel client, SocketEventEngine eventengine,IRequestHandlerContainer container,Object args)throws IOException
	{
		this();
		initHandlerOnDelegationRequest(client, eventengine, container);
		
		Object[] objArr = (Object[])args;
		
		this.headerdata =(ByteBuffer)objArr[0];
		this.data = (ByteBuffer)objArr[1];
	}
	
	protected SocketAddress getServerSocketAddress() 
	{
		String host = request.getHeader("Host").getValue(0).Value;
		int i = host.indexOf(':');
		int port;
		
		if(i<0)
			port=getDefaultPort();
		else {
			port = Integer.parseInt(host.substring(i+1));
			host= host.substring(0,i);
		}
		return new InetSocketAddress(host,port);	
	}
	
	protected void sendError(String message) 
    {
    	
    	//************Error Content*******************
    	String body = "<html><head><title>Bad Response</title></head>" +
    			"<body><h1>Bad Server Response</h1><p>Error generated " +
    			"on server due to some reason.</p><p>" + message + "</p>" +
    			"<hr><p><i>JASMProxy Server 1.0</i></p></body></html>";
    	
    	int contentlength = body.length();
    	
    	//********************************************
    	
    	response = new HttpResponseMessage(500,"HTTP/1.1","Error Response");
    	//response.
    	HeaderField header = new MessageParts.HeaderField("Server");
    	header.addValue(new MessageParts.HeaderFieldValue("JASMProxy Server 1.0"));
    	response.addHeader(header);
    	
    	header = new MessageParts.HeaderField("Connection");
    	header.addValue(new MessageParts.HeaderFieldValue("Close"));
    	response.addHeader(header);
    	
    	    	
    	header = new MessageParts.HeaderField("Date");
    	header.addValue(new MessageParts.HeaderFieldValue((new Date()).toString()));
    	response.addHeader(header);
    	
    	header = new MessageParts.HeaderField("Content-Type");
    	header.addValue(new MessageParts.HeaderFieldValue("text/html"));
    	response.addHeader(header);
    	
    	header = new MessageParts.HeaderField("Content-Length");
    	header.addValue(new MessageParts.HeaderFieldValue(String.valueOf(contentlength)));
    	response.addHeader(header);
    	    	
    
    	container.echo("Sending Error Response to " + client.socket().getRemoteSocketAddress().toString(),this);
    	
    	response.Body = body;
    	internal_SendResponseToClient(response);
    }
	
	protected abstract int getDefaultPort();
	
	protected void shutDownHandler(boolean delegated) 
    {
		super.shutDownHandler(delegated);
		headerdata=null;
		data=null;
    }
		
	public void sendErrorToClient(String message) {
		if(client!=null && client.isConnected()){
			sendError(message);
		}
	}

	public void sendResponseToClient(HttpResponseMessage response)
	{
		container.echo("Sending Response to " + client.socket().getRemoteSocketAddress().toString(),this);
		internal_SendResponseToClient(response);
	}
	public void internal_SendResponseToClient(HttpResponseMessage response) 
	{
		
		String headers = response.toStringHeaders();
    	
    	try {
    		headerdata = ByteBuffer.wrap(headers.getBytes("US-ASCII"));
     		data = ByteBuffer.wrap(response.Body.getBytes("US-ASCII"));
    		//container.echo("Sending Response to " + client.socket().getRemoteSocketAddress().toString(),this);
    	
    		client.write(headerdata);
    		
    		while(data.hasRemaining()) 
    			client.write(data);
    		
    	}catch(Exception ee) {
    		ee.printStackTrace();
    	}
    	   	
    	headerdata=null;
    	data=null;
	}
	
	public void sendResponseToClient(String message){
		//************Error Content*******************
    	String body = "<html><head><title>Server Response</title></head>" +
    			"<body><h1>Server Response</h1><p>" + message + "</p>" +
    			"<hr><p><i>JASMProxy Server 1.0</i></p></body></html>";
    	
    	int contentlength = body.length();
    	
    	//********************************************
    	
    	response = new HttpResponseMessage(500,"HTTP/1.1","Server Response");
    	//response.
    	HeaderField header = new MessageParts.HeaderField("Server");
    	header.addValue(new MessageParts.HeaderFieldValue("JASMProxy Server 1.0"));
    	response.addHeader(header);
    	
    	header = new MessageParts.HeaderField("Connection");
    	header.addValue(new MessageParts.HeaderFieldValue("Close"));
    	response.addHeader(header);
    	
    	    	
    	header = new MessageParts.HeaderField("Date");
    	header.addValue(new MessageParts.HeaderFieldValue((new Date()).toString()));
    	response.addHeader(header);
    	
    	header = new MessageParts.HeaderField("Content-Type");
    	header.addValue(new MessageParts.HeaderFieldValue("text/html"));
    	response.addHeader(header);
    	
    	header = new MessageParts.HeaderField("Content-Length");
    	header.addValue(new MessageParts.HeaderFieldValue(String.valueOf(contentlength)));
    	response.addHeader(header);
    	    	
    
    	//container.echo("Sending Error Response to " + client.socket().getRemoteSocketAddress().toString(),this);
    	
    	response.Body = body;
    	sendResponseToClient(response);	
	}
	
	protected void delegateRequest(String delegationTopic) 
	{
		Object[] arrObj = new Object[] {headerdata,data};
		
		if(container.delegateRequest(this, delegationTopic, arrObj)) 
		{
			shutDownHandler(true);
		}else {
			sendError("Request not supported");
			shutDownHandler();
		}
	}
	public Logger getLogger() {
		return logger;
	}

	
}
