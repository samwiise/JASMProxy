package org.asmmr.nio;

import java.io.IOException;


/**
 * @author asim.ali
 *
 */
public class SEEDriver implements Runnable {

	SocketEventEngine eventengine;
	private Thread thread;
	
	public SEEDriver() 
	{
		eventengine = new SocketEventEngine();
	}
	public void start() 
	{
		thread = new Thread(this);
		thread.start();
	}
	public void stop() {
		eventengine.stop();
	}
	public void join()throws InterruptedException 
	{
		thread.join();
	}
	public void run() 
	{
		// TODO Auto-generated method stub
		try {
			eventengine.Run();
		}catch(IOException e) {
			eventengine.stop();
			e.printStackTrace();
		}
	}

}
