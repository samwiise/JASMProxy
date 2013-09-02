/**
 * 
 */
package org.asmmr.jasmproxy.proxy.requesthandler;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.asmmr.jasmproxy.IRequestHandler;
import org.asmmr.jasmproxy.IRequestHandlerContainer;
import org.asmmr.jasmproxy.RequestHandlerFactory;
import org.asmmr.nio.SocketEventEngine;


/**
 * @author asim.ali
 *
 */
public class SOCKS4RequestHandlerFactory extends RequestHandlerFactory {

	/* (non-Javadoc)
	 * @see src.RequestHandlerFactory#create(java.nio.channels.SocketChannel, org.asmmr.nio.SocketEventEngine, src.IRequestHandlerContainer)
	 */
	@Override
	public IRequestHandler create(SocketChannel client,
			SocketEventEngine eventengine, IRequestHandlerContainer container)
			throws IOException {
		// TODO Auto-generated method stub
		return new SOCKS4RequestHandler(client,eventengine,container);
	}
	
	@Override
	public IRequestHandler createOnDelegationRequest(SocketChannel client,
			SocketEventEngine eventengine, IRequestHandlerContainer container,
			Object args) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
