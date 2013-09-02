package org.asmmr.jasmproxy;

import org.apache.log4j.Logger;
import org.asmmr.jasmproxy.cui.CommandControl;
/**
 * @author asim.ali
 *
 */
public class JASMProxy {

	/**
	 * @param args
	 */
	
	protected static Logger logger = Logger.getLogger(JASMProxy.class);
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		logger.debug("Starting ServerControl");
		ServerControl.start();
		
		//TestWindowDriver driver = new TestWindowDriver();
		logger.debug("Starting CommandControl");
		CommandControl.get().start();
		//WindowControl.start();
		
	}

}
