package org.asmmr.jasmproxy.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.asmmr.jasmproxy.ServerControl;
import org.asmmr.jasmproxy.cui.CommandControl;

/**
 * @author asim.ali
 *
 */
public class WindowControl extends JFrame implements ActionListener, Observer,ChangeListener 
{
	private static WindowControl controlwindow;
	
	JButton switchcommand;
	JProgressBar httpreqpg, httpsreqpg;

	static Logger logger = Logger.getLogger(WindowControl.class);
	
	private WindowControl() {
		super("JASMProxy Server Control & Monitoring Window");
		
		setSize(640, 480);
		
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		
		JPanel northpanel = new JPanel();
		
				
		switchcommand = new JButton("Command");
		
		switchcommand.addActionListener(this);
		northpanel.add(switchcommand);
		
		
		
		Box centerbox = Box.createVerticalBox();
		Box toppanelbox = Box.createHorizontalBox();
		
		Box httpreqbox = Box.createHorizontalBox();
		Box httpsreqbox = Box.createHorizontalBox();
		
		JLabel httpreqlabel = new JLabel("HTTP Requests");
		httpreqpg = new JProgressBar();
		httpreqpg.setMinimum(0);
		httpreqpg.setMaximum(100);
		httpreqpg.setValue(0);
		httpreqpg.setStringPainted(true);
		httpreqpg.setString(String.valueOf(httpreqpg.getValue()));
		//httpreqpg.setIndeterminate(true);
		
		JLabel httpsreqlabel = new JLabel("HTTPS Requests");
		httpsreqpg = new JProgressBar();
		httpsreqpg.setMinimum(0);
		httpsreqpg.setMaximum(100);
		httpsreqpg.setValue(0);
		httpsreqpg.setStringPainted(true);
		httpsreqpg.setString(String.valueOf(httpsreqpg.getValue()));
		
		httpreqpg.addChangeListener(this);
		httpsreqpg.addChangeListener(this);
		//httpsreqpg.setIndeterminate(true);
		
		httpreqbox.add(httpreqlabel);
		httpreqbox.add(Box.createHorizontalStrut(12));
		httpreqbox.add(httpreqpg);
		httpsreqbox.add(httpsreqlabel);
		httpsreqbox.add(Box.createHorizontalStrut(4));
		httpsreqbox.add(httpsreqpg);
		
		Box requestsbox = Box.createVerticalBox();
		requestsbox.add(httpreqbox);
		requestsbox.add(Box.createVerticalStrut(4));
		requestsbox.add(httpsreqbox);
		
		toppanelbox.add(requestsbox);
		centerbox.add(toppanelbox);
		
		JPanel centerpanel = new JPanel();
		centerpanel.add(centerbox);
		
		container.add(northpanel,BorderLayout.NORTH);
		container.add(centerpanel,BorderLayout.CENTER);
		
		
		setVisible(true);
		
		ServerControl.addObserver(this);
	}	
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() ==switchcommand) {
			ServerControl.deleteObserver(this);
			dispose();
			controlwindow=null;
			CommandControl.get().start();
		}
		
	}	
	public static void start() {
		if(controlwindow==null)controlwindow=new WindowControl();
	}
	
	@Override
	protected void processWindowEvent(WindowEvent e) {
		// TODO Auto-generated method stub
		if(e.getID()==WindowEvent.WINDOW_CLOSING) 
		{
			ServerControl.deleteObserver(this);
			ServerControl.stop();
			dispose();
			controlwindow=null;
		}
		super.processWindowEvent(e);
	}
	public void update(Observable o, Object arg) {
	/*	if(o==ServerControl.httplistener) {
			httpreqpg.setValue(ServerControl.httplistener.getCount());
		}else if(o==ServerControl.ssllistener) {
			httpsreqpg.setValue(ServerControl.ssllistener.getCount());
		}*/
	}
	
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		JProgressBar bar =  (JProgressBar)e.getSource();
		bar.setString(String.valueOf(bar.getValue()));
	}
}
