package org.asmmr.jasmproxy;

import java.net.SocketAddress;
/**
 * @author asim.ali
 *
 */
public interface IRequestHandlerContainer {

	public void close(IRequestHandler handler);
	public void echo(String message,IRequestHandler handler);
	
	public boolean isForwardToUpStreamProxyServer();
	public void setForwardToUpStreamProxyServer(boolean forwardToUpStreamProxyServer);
	public SocketAddress getUpStreamProxyAddress();
	public void setUpStreamProxyAddress(SocketAddress upStreamProxyAddress);
	
	public boolean delegateRequest(IRequestHandler request,String delegationTopic,Object args);
	
	
}
