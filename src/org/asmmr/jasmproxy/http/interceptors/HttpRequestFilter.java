/**
 * 
 */
package org.asmmr.jasmproxy.http.interceptors;

import java.nio.channels.SocketChannel;

import org.asmmr.http.HttpRequestMessage;
import org.asmmr.http.HttpResponseMessage;
import org.asmmr.jasmproxy.IHttpRequestHandler;
import org.asmmr.jasmproxy.interceptor.AbstractHttpRequestInterceptor;


/**
 * @author asim.ali
 *
 */
public class HttpRequestFilter extends AbstractHttpRequestInterceptor {

	/* (non-Javadoc)
	 * @see org.asmmr.jasmproxy.interceptor.AbstractHttpRequestInterceptor#requestArrived(src.IHttpRequestHandler, org.asmmr.http.HttpRequestMessage, java.nio.channels.Channel)
	 */
	@Override
	protected int requestArrived(IHttpRequestHandler handler,
			HttpRequestMessage message, SocketChannel client) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Http Request Filter - Request Arrived From - " + client.socket().getRemoteSocketAddress().toString());
		
		
		if(message.getHeader("host").getValue(0).toString().matches("[a-zA-Z0-9.]{0,}sex[a-zA-Z0-9.]{1,}"))
		{
			handler.sendResponseToClient("Access Denied by Ghost HTTP Request Filter");
			return DISCONTINUE;
		}
		
		
		return CONTINUE;
	}

	/* (non-Javadoc)
	 * @see org.asmmr.jasmproxy.interceptor.AbstractHttpRequestInterceptor#responseArrived(src.IHttpRequestHandler, org.asmmr.http.HttpResponseMessage, org.asmmr.http.HttpRequestMessage, java.nio.channels.Channel, java.nio.channels.Channel)
	 */
	@Override
	protected int responseArrived(IHttpRequestHandler handler,
			HttpResponseMessage message, HttpRequestMessage requestMessage,
			SocketChannel client, SocketChannel server) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Http Request Filter - Response Arrived From - " + server.socket().getRemoteSocketAddress().toString());
		return CONTINUE;
	}

}
