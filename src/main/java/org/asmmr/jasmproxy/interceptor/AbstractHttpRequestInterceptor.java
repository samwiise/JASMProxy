/**
 * 
 */
package org.asmmr.jasmproxy.interceptor;

import java.nio.channels.SocketChannel;

import org.asmmr.http.HttpRequestMessage;
import org.asmmr.http.HttpResponseMessage;
import org.asmmr.jasmproxy.IHttpRequestHandler;


/**
 * @author asim.ali
 *
 */
public abstract class AbstractHttpRequestInterceptor implements IHttpRequestInterceptor {

	/* (non-Javadoc)
	 * @see org.asmmr.jasmproxy.interceptor.IHttpRequestInterceptor#internalRequestArrived(src.IHttpRequestHandler, org.asmmr.http.HttpRequestMessage, java.nio.channels.Channel)
	 */
	public int internalRequestArrived(IHttpRequestHandler handler,
			HttpRequestMessage message, SocketChannel client)throws Exception {
		// TODO Auto-generated method stub
		return requestArrived(handler, message, client);
	}

	/* (non-Javadoc)
	 * @see org.asmmr.jasmproxy.interceptor.IHttpRequestInterceptor#internalResponseArrived(src.IHttpRequestHandler, org.asmmr.http.HttpResponseMessage, org.asmmr.http.HttpRequestMessage, java.nio.channels.Channel, java.nio.channels.Channel)
	 */
	public int internalResponseArrived(IHttpRequestHandler handler,
			HttpResponseMessage message, HttpRequestMessage requestMessage,
			SocketChannel client, SocketChannel server) throws Exception{
		// TODO Auto-generated method stub
		return responseArrived(handler, message, requestMessage, client, server);
	}
	
	protected abstract int requestArrived(IHttpRequestHandler handler,
			HttpRequestMessage message, SocketChannel client)throws Exception;
	
	protected abstract int responseArrived(IHttpRequestHandler handler,
			HttpResponseMessage message, HttpRequestMessage requestMessage,
			SocketChannel client, SocketChannel server)throws Exception;
	
	

}
