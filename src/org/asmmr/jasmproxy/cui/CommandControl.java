package org.asmmr.jasmproxy.cui;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.asmmr.jasmproxy.RequestListener;
import org.asmmr.jasmproxy.ServerControl;
import org.asmmr.jasmproxy.gui.WindowControl;

/**
 * @author asim.ali
 *
 */
public class CommandControl implements Runnable {

	
	private static CommandControl commandcontrol;
	
	private Thread thread;
	private boolean isrunning;
	
	static Logger logger = Logger.getLogger(CommandControl.class);
		
	private CommandControl() {
		isrunning=false;
	}
	public void start() {
		if(!isrunning){
			thread=new Thread(this);
			thread.start();
		}
	}
	public void run() {
		// TODO Auto-generated method stub
		boolean stopall=true;
		isrunning=true;
		while(true) {
			logger.info("Press E to stop Server.");
			try {
				int i = System.in.read();
				if(i>0) {
					char c = (char)i;
					if(c=='e' || c=='E')
						break;
					else if(c=='w' || c=='W') {
						stopall=false;
						break;
					}
				}else
					break;
			}catch(IOException e) {
				logger.error(e.getMessage(),e);
				break;
			}
		}
		
		if(stopall) {
			logger.debug("Stopping ServerControl.");
			ServerControl.stop();
		}else {
			logger.debug("Starting WindowControl.");
			WindowControl.start();
		}
		isrunning=false;
	}

	public synchronized static CommandControl get(){
		if(commandcontrol==null)
			commandcontrol = new CommandControl();
		
		return commandcontrol;
	} 
}
