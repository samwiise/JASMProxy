import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Vector;

import org.asmmr.jasmproxy.HttpRequestHandler;
import org.asmmr.jasmproxy.IRequestHandler;
import org.asmmr.jasmproxy.IRequestHandlerContainer;
import org.asmmr.jasmproxy.SSLRequestHandler;
import org.asmmr.nio.AbstractSocketEventHandler;
import org.asmmr.nio.SCRegistrationRequest;
import org.asmmr.nio.SocketEventEngine;



public class TestClass3 extends AbstractSocketEventHandler
	implements IRequestHandlerContainer
{

	/**
	 * @param args
	 */
	SocketEventEngine engine;
	long counter;
	Vector<IRequestHandler> requests;
	
	ServerSocketChannel server,sslserver;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestClass3 obj = new TestClass3();
		obj.Listen();
		
		
	}
	private void Listen() {
		counter=0;
		engine = new SocketEventEngine();
		requests = new Vector<IRequestHandler>();
		try{
			server = ServerSocketChannel.open();
			engine.queueForRegister(
					new SCRegistrationRequest(server,SelectionKey.OP_ACCEPT,this));
			System.out.println("Binding Server on port 8080.");
			server.socket().bind(new InetSocketAddress("localhost",8080));
			
			sslserver = ServerSocketChannel.open();
			engine.queueForRegister(
					new SCRegistrationRequest(sslserver,SelectionKey.OP_ACCEPT,this));
			System.out.println("Binding SSL Server on port 8081.");
			sslserver.socket().bind(new InetSocketAddress("localhost",8081));
			
			System.out.println("Starting Socket Engine.");
			engine.Run();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void OnAccept(SelectionKey key, ServerSocketChannel channel) {
		// TODO Auto-generated method stub
		SocketChannel client=null;
		try {
			client = channel.accept();
			System.out.println("New Connection request arrived from " + client.socket().getRemoteSocketAddress().toString());
			IRequestHandler handler=null;
			
			if(channel==server)
				handler = new HttpRequestHandler(client,engine,this);
			else if(channel==sslserver)
				handler = new SSLRequestHandler(client,engine,this);
			
			handler.setId(counter++);
			
			requests.add(handler);
			
		}catch(IOException e) {
			try { 
				if(client!=null)client.close();
			}catch(IOException ee) {
				ee.printStackTrace();
			}
			e.printStackTrace();
		}
		
	}
	public void close(IRequestHandler handler) {
		// TODO Auto-generated method stub
		
		requests.remove(handler);
	}
	public void echo(String message, IRequestHandler handler) {
		// TODO Auto-generated method stub
		System.out.println("id " + handler.getId() + " : " + message); 
	}
	public SocketAddress getUpStreamProxyAddress() {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean isForwardToUpStreamProxyServer() {
		// TODO Auto-generated method stub
		return false;
	}
	public void setForwardToUpStreamProxyServer(boolean forwardToUpStreamProxyServer) {
		// TODO Auto-generated method stub
		
	}
	public void setUpStreamProxyAddress(SocketAddress upStreamProxyAddress) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean delegateRequest(IRequestHandler request,
			String delegationTopic, Object args) {
		// TODO Auto-generated method stub
		return false;
	}
}
