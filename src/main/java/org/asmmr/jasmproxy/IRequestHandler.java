package org.asmmr.jasmproxy;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;
import org.asmmr.nio.SocketEventEngine;

/**
 * @author asim.ali
 *
 */
public interface IRequestHandler {

	public void setId(long id);
	public long getId();
	public String getClientAddress();
	public String getServerAddress();
	
	public SocketChannel getClient();
	public SocketEventEngine getSocketEventEngine();
	
	public void handleDelegatedRequest();
	
	public Logger getLogger();
	 
}
