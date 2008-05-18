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
	
	private static Timer timer;
	
	private static Rehearse[] rehearseWindows = new Rehearse[100];
	
	public static void main(String[] args) {
		timer = new Timer();
		TimerTask pollingTask = new RehearseClient();
		timer.scheduleAtFixedRate(pollingTask, 0, DELAY);
	}

	@Override
	public void run() {
		
		ArrayList<String> result = 
			POWUtils.callPOWScript(POWUtils.REHEARSE_CHECK_BP, "");
		
		if(result.size() == 0)
			return;
		
		int uid = Integer.parseInt(result.get(0));
		System.out.println(uid);
		
		if(uid != NO_STOPPED_WINDOWS) {
			
			int functionNum = Integer.parseInt(result.get(1));
			System.out.println("function num: " + functionNum);
			
			String functionName = result.get(2);
			System.out.println("functionname: " + functionName);
			String parameters = "";
			if(result.size() >= 4)
				parameters = result.get(3);
			
			if(rehearseWindows[functionNum] == null) {
				rehearseWindows[functionNum] = new Rehearse(uid, functionNum, functionName, parameters);
				rehearseWindows[functionNum].requestFocusInWindow();
				rehearseWindows[functionNum].toFront();
			} else {
				if(result.size() >= 5)
					processResponses(rehearseWindows[functionNum], result.get(4));
				addCodeToQueue(rehearseWindows[functionNum]);
				if(rehearseWindows[functionNum].isDone()) {
					markDone(rehearseWindows[functionNum]);
					rehearseWindows[functionNum] = null;
				}
			}
			resumeExecution(uid);
		}
	}
	
	private void processResponses(Rehearse rehearse, String responseObj) {
		System.out.println("RESPONSE : " + responseObj);
		String[] response = responseObj.split(",");
		int sid = Integer.parseInt(response[0]);
		int errorCode = Integer.parseInt(response[1]);
		String responseText = "";
		for(int i = 2; i < response.length; i++)
			responseText += response[i];
		rehearse.appendResponse(sid, errorCode, responseText);
	}
	
	private void addCodeToQueue(Rehearse rehearse) {
		
	}
	
	private void markDone(Rehearse rehearse) {
		String params = "rehearse_uid=" + rehearse.getUid() + "&function_num="
				+ rehearse.getFunctionNum();
		POWUtils.callPOWScript(POWUtils.MARK_DONE_URL, params);
	}
	
	private void resumeExecution(int uid) {
		String params = "rehearse_uid=" + uid;
		POWUtils.callPOWScript(POWUtils.RESUME_EXECUTION_URL, params);
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
