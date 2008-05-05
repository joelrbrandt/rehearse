package edu.stanford.rehearse;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

public class RehearseClient extends TimerTask implements WindowListener {
	
	private static final int DELAY = 1000;
	private static final int NO_STOPPED_WINDOWS = -1;
	private static final String REHEARSE_CHECK_BP = 
		"http://localhost:6670/rehearse/check_for_breakpoint.sjs";
	
	private static Timer timer;
	
	public static void main(String[] args) {
		timer = new Timer();
		TimerTask pollingTask = new RehearseClient();
		timer.scheduleAtFixedRate(pollingTask, 0, DELAY);
	}

	@Override
	public void run() {
		
		ArrayList<String> result = new ArrayList<String>();
		
		try {
			URL myURL = new URL(REHEARSE_CHECK_BP);
			URLConnection myUC = myURL.openConnection();
			myUC.setDoOutput(true);
			myUC.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			myUC.connect();

			BufferedReader in = new BufferedReader(new InputStreamReader(myUC.getInputStream()));
			String s;
			while ((s = in.readLine()) != null) {
				result.add(s);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(result.size() == 0)
			return;
		
		int uid = Integer.parseInt(result.get(0));
		System.out.println(uid);
		
		if(uid != NO_STOPPED_WINDOWS) {
			this.cancel();
			
			String functionName = result.get(1);
			String parameters = result.get(2);
			System.out.println("functionname: " + functionName);
			System.out.println("parameters: " + parameters);
			
			Rehearse rehearseFrame = new Rehearse(uid, functionName, parameters);
			rehearseFrame.addWindowListener(this);
			rehearseFrame.requestFocusInWindow();
			rehearseFrame.toFront();
		}
	}
	public void windowClosed(WindowEvent arg0) {
		TimerTask pollingTask = new RehearseClient();
		timer.scheduleAtFixedRate(pollingTask, 0, DELAY);
	}
	
	public void windowActivated(WindowEvent arg0) {}
	public void windowClosing(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

}
