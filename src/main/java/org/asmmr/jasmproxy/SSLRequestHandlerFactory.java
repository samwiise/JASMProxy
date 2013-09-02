package org.asmmr.jasmproxy;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.asmmr.nio.SocketEventEngine;

/**
 * @author asim.ali
 *
 */
public class SSLRequestHandlerFactory extends RequestHandlerFactory {

	@Override
	public IRequestHandler create(SocketChannel client,
			SocketEventEngine eventengine, IRequestHandlerContainer container)
			throws IOException{
		// TODO Auto-generated method stub
		return new SSLRequestHandler(client,eventengine,container);
	}
	
	@Override
	public IRequestHandler createOnDelegationRequest(SocketChannel client,
			SocketEventEngine eventengine, IRequestHandlerContainer container,
			Object args) throws IOException {
		return new SSLRequestHandler(client,eventengine,container,args);
	}

}
