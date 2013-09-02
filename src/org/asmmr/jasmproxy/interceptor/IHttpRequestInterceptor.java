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
public interface IHttpRequestInterceptor 
{
	
	public static int CONTINUE = 1;  
	public static int DISCONTINUE = 2;
	
	
	public int internalRequestArrived(IHttpRequestHandler handler,HttpRequestMessage message,SocketChannel client)throws Exception;
	public int internalResponseArrived(IHttpRequestHandler handler, HttpResponseMessage message,HttpRequestMessage requestMessage,SocketChannel client,SocketChannel server)throws Exception;
	
}
