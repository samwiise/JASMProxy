package org.asmmr.jasmproxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Observable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.asmmr.nio.LoadBalancer;
/**
 * @author asim.ali
 *
 */

public class RequestListener extends Observable implements Runnable,IRequestHandlerContainer {

	private RequestHandlerFactory factory;
	private int port;
	
	protected static Vector<RequestHandlerFactory> handlerFactories  = new Vector();
	
	private static long counter=0;
	
	private Vector<IRequestHandler> requests;
	private Thread thread;
	
	
	protected SocketAddress upStreamProxyAddress = null;
	protected boolean forwardToUpStreamProxyServer=false;
	
	protected static Logger logger = Logger.getLogger(RequestListener.class);
	
	public RequestListener(RequestHandlerFactory factory,int port) {
		this.factory=factory;
		this.port=port;
		requests = new Vector<IRequestHandler>();
		
		handlerFactories.add(factory);
	}
	
	public RequestListener(RequestHandlerFactory factory,int port,SocketAddress upStreamProxyAddress) {
		this(factory,port);
		if(upStreamProxyAddress!=null){
			setUpStreamProxyAddress(upStreamProxyAddress);
			setForwardToUpStreamProxyServer(true);
		}
	}
	
	public void start() 
	{
		thread= new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}
	public void run() {
		// TODO Auto-generated method stub
		ServerSocketChannel server=null;
		try {
			server = ServerSocketChannel.open();
			logger.debug("Trying to bind on port - " + port );
			server.socket().bind(new InetSocketAddress(port));
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
		while (true) {
			SocketChannel client = null;
			try {
				client = server.accept();
				
				IRequestHandler handler = factory.create(client, LoadBalancer
						.getInstance().get(), this);

				handler.setId(counter++);
				requests.add(handler);
				
				setChanged();
				notifyObservers(handler);

			} catch (IOException e) {
				try {
					if (client != null)
						client.close();
				} catch (IOException ee) {
					logger.error(ee.getMessage(),ee);
				}
				logger.error(e.getMessage(),e);
			}
		}
	}
	public void close(IRequestHandler handler) {
		// TODO Auto-generated method stub
		requests.remove(handler);
		setChanged();
		notifyObservers(handler);
	}
	public void echo(String message, IRequestHandler handler) {
		// TODO Auto-generated method stub
		//System.out.println("id " + handler.getId() + " : " + message);
		if(handler.getLogger().isInfoEnabled()) {
			handler.getLogger().info("id " + handler.getId() + " : " + message);
		}
	}
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		requests.clear();
		super.finalize();
	}
	public int getCount() {
		return requests.size();
	}
	public boolean isForwardToUpStreamProxyServer() {
		return forwardToUpStreamProxyServer;
	}
	public void setForwardToUpStreamProxyServer(boolean forwardToUpStreamProxyServer) {
		this.forwardToUpStreamProxyServer = forwardToUpStreamProxyServer;
	}
	public SocketAddress getUpStreamProxyAddress() {
		return upStreamProxyAddress;
	}
	public void setUpStreamProxyAddress(SocketAddress upStreamProxyAddress) {
		this.upStreamProxyAddress = upStreamProxyAddress;
	}
	
	public boolean delegateRequest(IRequestHandler request,
			String delegationTopic, Object args) {

		try {
			Iterator<RequestHandlerFactory> itr =  handlerFactories.iterator();
			
			while(itr.hasNext()) 
			{
				RequestHandlerFactory hFactory = itr.next();
				
				if(hFactory.canHandle(delegationTopic)) {
					IRequestHandler handler = hFactory.createOnDelegationRequest(request.getClient(), request.getSocketEventEngine(), this, args);
					
					handler.setId(counter++);
					requests.add(handler);
					
					this.echo("New HTTPS Request Handler created for a delegated Request.", handler);
					
					setChanged();
					notifyObservers(handler);
					
					
					handler.handleDelegatedRequest();
					
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
