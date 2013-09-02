package org.asmmr.jasmproxy.gui;

import org.asmmr.nio.LoadBalancer;

public class TestWindowDriver implements Runnable {

	public TestWindowDriver() {
		Thread thread=new Thread(this);
		thread.start();
	}
	
	public void run() {
		// TODO Auto-generated method stub
		
		
		
		
		LoadBalancer.getInstance();
		
		//TestWindowDriver wdriver = new TestWindowDriver();
		TestWindow window = new TestWindow("My Window");
		while(true) {
			System.out.println("Press E to stop Server.");
			//try {
				/*int i = System.in.read();
				if(i>0) {
					char c = (char)i;
					if(c=='e' || c=='E')
						break;
				}else
					break;
			}catch(IOException e) {
				e.printStackTrace();
				break;
			}*/
			try {
				Thread.sleep(5000);
			}catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		

	}
}
