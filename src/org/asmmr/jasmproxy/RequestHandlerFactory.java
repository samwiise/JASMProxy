package org.asmmr.jasmproxy;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.asmmr.nio.SocketEventEngine;

/**
 * @author asim.ali
 *
 */
public abstract class RequestHandlerFactory 
{
	
	protected String[] handlerKeys;
	
	public abstract IRequestHandler create(SocketChannel client,SocketEventEngine eventengine,IRequestHandlerContainer container)throws IOException;
	
	public abstract IRequestHandler createOnDelegationRequest(SocketChannel client,SocketEventEngine eventengine,IRequestHandlerContainer container,
						Object args)throws IOException;

	public String[] getHandlerKeys() {
		return handlerKeys;
	}

	public void setHandlerKeys(String handlerKeys) {
		this.handlerKeys = handlerKeys.split(",");
	}
	
	public boolean canHandle(String key) 
	{
		if(handlerKeys!=null) {
			for(int i=0;i<handlerKeys.length;i++) 
			{
				if(key.equalsIgnoreCase(handlerKeys[i].trim()))
					return true;
			}
		}
		return false;
	}
	 
}
