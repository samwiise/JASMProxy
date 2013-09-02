package org.asmmr.nio;

import org.apache.log4j.Logger;

/**
 * @author asim.ali
 *
 */
public class LoadBalancer {
	
	private SEEDriver[] servers; 
	
	private static LoadBalancer loadbalancer;
	private static int seed=2;
	
	static Logger logger = Logger.getLogger(LoadBalancer.class);
	
	private LoadBalancer(int totalservers)
	{
		servers = new SEEDriver[totalservers];
		
		for(int i=0;i<totalservers;i++) {
			servers[i] = new SEEDriver();
			logger.info("Starting Socket EventEngine " + (i+1));
			servers[i].start();
		}
	}
	
	public SocketEventEngine get() {
		
		SEEDriver server=servers[0]; 
				
		for(int i=1;i<servers.length;i++) {
			if(servers[i].eventengine.getLoad()<server.eventengine.getLoad())
				server=servers[i];
		}
		
		return server.eventengine;
			
	}
	
	public synchronized static LoadBalancer getInstance() 
	{
		if(loadbalancer==null)
			loadbalancer = new LoadBalancer(seed);
		return loadbalancer;
	}
	
	public static boolean isRunning() {
		return loadbalancer!=null;
	}
	public static void stop() {
		if(loadbalancer!=null) {
			loadbalancer.stopServers();
		}
	}
	private void stopServers() 
	{
		int count=1;
		for(SEEDriver server: servers) {
			logger.info("Stopping Socket EventEngine " + count++);
			server.stop();
			try {
				server.join();
			}catch(InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static int getSeed() {
		return seed;
	}

	public static void setSeed(int seed) {
		LoadBalancer.seed = seed;
	}
	
}
