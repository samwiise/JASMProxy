package org.asmmr.jasmproxy;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.asmmr.http.HttpRequestMessage;
import org.asmmr.http.HttpResponseMessage;
import org.asmmr.jasmproxy.interceptor.IHttpRequestInterceptor;
import org.asmmr.nio.LoadBalancer;
import org.asmmr.util.ASMTreeNode;
import org.w3c.dom.Document;

/**
 * @author asim.ali
 *
 */
public class ServerControl 
{
	private ServerControl() {
	}

	//public static RequestListener httplistener,ssllistener;
	
	public static List<RequestListener> reqListeners = new ArrayList<RequestListener>();
	public static List<IHttpRequestInterceptor> httpInterceptors = new ArrayList<IHttpRequestInterceptor>();
	
	static Logger logger = Logger.getLogger(ServerControl.class);
	
	public static void start() {
		
		
		readConfig("config.xml");
		
		//SocketAddress proxyAddr = new InetSocketAddress("61.91.190.251",8080);
	/*	SocketAddress proxyAddr = new InetSocketAddress("192.168.128.8",8080);
		
		httplistener = new RequestListener(new HttpRequestHandlerFactory(),8082,proxyAddr);
		ssllistener = new RequestListener(new SSLRequestHandlerFactory(),8081,proxyAddr);
		
		httplistener.setForwardToUpStreamProxyServer(false);
		ssllistener.setForwardToUpStreamProxyServer(false);
		
		System.out.println("Starting Http Request Listener on port 8082");
		httplistener.start();
		System.out.println("Starting Https Request Listener on port 8081");
		ssllistener.start();
		
		LoadBalancer.getInstance();*/
		//stop();
	}
	public static void stop() {
		logger.info("Stopping Load Balancer.");
		LoadBalancer.stop();
	}
	public static void addObserver(Observer obj) {
		//httplistener.addObserver(obj);
		//ssllistener.addObserver(obj);
		
		for(RequestListener listener:reqListeners)
			listener.addObserver(obj);
		
	}
	public static void deleteObserver(Observer obj) {
		
		for(RequestListener listener:reqListeners)
			listener.deleteObserver(obj);
	}
	
	protected static void readConfig(String file)
	{
		try {
			
			logger.debug("Reading configurations from " + file);
			
			DocumentBuilder documentBuilder = 
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			
			Document document = documentBuilder.parse(new File(file));
			
			
			/*NodeList nodeList = document.getElementsByTagName("*");
			
			
			
			
 			for(int i=0;i<nodeList.getLength();i++)
			{
				Node node = nodeList.item(i);
				
				System.out.println("Node - " + node.getNodeName());
			}*/
			logger.debug("Creating configuration tree from configuration file.");
			ASMTreeNode configTree = ASMTreeNode.MakeTree(null, document.getDocumentElement());
			
			
			List<ASMTreeNode> listeners = configTree.getNodes("JASMProxy/Server/Listener");
			
			for(ASMTreeNode node:listeners)
			{
				String name = node.getAttributes().get("name");
				
				if(name==null)
					name = "Unknown Listener";
				
				logger.debug("Found Request listener - " + name);
				
				ASMTreeNode factoryNode = node.getSingleNode("JASMProxy/Server/Listener/Factory");
				ASMTreeNode portNode  = node.getSingleNode("JASMProxy/Server/Listener/Port");
				
				if(factoryNode!=null && portNode!=null){
					
										
					ASMTreeNode forwardProxyNode  = node.getSingleNode("JASMProxy/Server/Listener/ForwardProxy");
					
					SocketAddress proxyAddr=null;
					
					if(forwardProxyNode !=null &&  forwardProxyNode.getAttributes().get("host")!=null && 
							forwardProxyNode.getAttributes().get("port")!=null){
						
						logger.debug("Configuring forward proxy - " + forwardProxyNode.getAttributes().get("host") + ":" + forwardProxyNode.getAttributes().get("port"));
						
						proxyAddr = new InetSocketAddress(forwardProxyNode.getAttributes().get("host"),
								Integer.parseInt(forwardProxyNode.getAttributes().get("port")));
					}
					
					RequestHandlerFactory factory =	(RequestHandlerFactory) Class.forName(factoryNode.getValue()).newInstance();
										
					if(factoryNode.getAttributes().get("handler-keys")!=null) {
						logger.debug("Setting handler keys - " + factoryNode.getAttributes().get("handler-keys"));
						factory.setHandlerKeys(factoryNode.getAttributes().get("handler-keys"));
					}
					
					
					RequestListener listener=new RequestListener(factory,Integer.parseInt(portNode.getValue()),proxyAddr);
										
						
					logger.info("Starting " + name  + " on port " + portNode.getValue());
					listener.start();
					
					reqListeners.add(listener);
					
									
				}else {
					logger.debug("Factory or Port is not specified for a Request Listener - " + name);					
				}
				
			}
			
			
			List<ASMTreeNode> httpInterceptorNodes = configTree.getNodes("JASMProxy/Server/HttpRequestInterceptor");
					
			for(ASMTreeNode node:httpInterceptorNodes){
				
				if(node.getAttributes().get("class")!=null)
				{
					IHttpRequestInterceptor interceptor =	(IHttpRequestInterceptor) Class.forName(node.getAttributes().get("class")).newInstance();
					logger.info("Configuring Http Request Interceptor - " + node.getAttributes().get("class") );
					httpInterceptors.add(interceptor);
				}
				
			}
			
			
			ASMTreeNode loadBalNode = configTree.getSingleNode("JASMProxy/Server/LoadBalancer");
			
			if(loadBalNode!=null && loadBalNode.getAttributes().get("seed")!=null){
				LoadBalancer.setSeed(Integer.parseInt(loadBalNode.getAttributes().get("seed")));
			}
			
			logger.debug("Starting Load Balancer...");
			LoadBalancer.getInstance();
			//System.out.println(listeners.size());
			
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.fatal(e.getMessage(),e);
		}
	}
	
	public static int HttpRequestArrived(IHttpRequestHandler handler,
			HttpRequestMessage message,SocketChannel client)
	throws Exception{
		
		logger.trace("HttpRequestArrived called.");
		
		for(IHttpRequestInterceptor interceptor:httpInterceptors){
			int res = interceptor.internalRequestArrived(handler, message, client);
			if(res!=IHttpRequestInterceptor.CONTINUE)return res;
		}
		
		logger.trace("HttpRequestArrived end.");
		return IHttpRequestInterceptor.CONTINUE;
	}
	public static int HttpResponseArrived(IHttpRequestHandler handler, 
			HttpResponseMessage message,HttpRequestMessage requestMessage,SocketChannel client,SocketChannel server)
	throws Exception{
		logger.trace("HttpResponseArrived called.");
		for(IHttpRequestInterceptor interceptor:httpInterceptors){
			int res = interceptor.internalResponseArrived(handler, message, requestMessage, client, server);
			if(res!=IHttpRequestInterceptor.CONTINUE)return res;
		}
		logger.trace("HttpResponseArrived end.");
		return IHttpRequestInterceptor.CONTINUE;
	}
	
	public static void main(String[] args) 
	{
		
		readConfig("config.xml");	
		
	}
}
