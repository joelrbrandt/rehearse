package edu.stanford.rehearse;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.stanford.rehearse.undo1.Rehearse;
import edu.stanford.rehearse.undo2.Rehearse2;
import edu.stanford.rehearse.undo3.Rehearse3;
import edu.stanford.rehearse.undo4.Rehearse4;

public class RehearseClient extends TimerTask {

	private static final int DELAY = 1000;
	private static final int NO_STOPPED_WINDOWS = -1;

	private static final int REHEARSE_OPTION = 2; // 1: treeundo or 2: flatundo

	private static int numFasterTimerIters = 0;
	private static TimerTask pollingTask;
	private static Timer timer;

	private static Rehearse[] rehearseWindows = new Rehearse[100];
	//private static Set<String> definedFunctionNames = new HashSet<String>();

	public static Lock lock = new ReentrantLock();


	public static void main(String[] args) {
		timer = new Timer();
		pollingTask = new RehearseClient();
		timer.scheduleAtFixedRate(pollingTask, 0, DELAY);
	}

	public static void reschedule(int uid) {
		System.out.println("RESCHEDULED");
		resumeExecution(uid);
		
		//timer.cancel();
		timer = new Timer();
		timer.scheduleAtFixedRate(new RehearseClient(), 0, DELAY / 10);
		numFasterTimerIters = 5;
	}

	@Override
	public void run() {

		System.out.println("run thread acquire");
		lock.lock();
		try {

			if(numFasterTimerIters > 0) {
				numFasterTimerIters--;
				if(numFasterTimerIters <= 0) {
					timer.cancel();
					timer = new Timer();
					timer.scheduleAtFixedRate(new RehearseClient(), 0, DELAY);
				}
			}

			ArrayList<String> result = 
				POWUtils.callPOWScript(POWUtils.REHEARSE_CHECK_BP, "");

			if(result.size() == 0) {
				lock.unlock();
				System.out.println("run release early");
				return;
			}

			int uid = Integer.parseInt(result.get(0));
			System.out.println(uid);

			if(uid != NO_STOPPED_WINDOWS) {

				int functionNum = Integer.parseInt(result.get(1));
				System.out.print("function num: " + functionNum + "\t");

				String functionName = result.get(2);
				System.out.println("name: " + functionName);
				String parameters = "";
				if(result.size() >= 4)
					parameters = result.get(3);

				int initialSnapshot = Integer.parseInt(result.get(4));
				if(rehearseWindows[functionNum] == null) {
					if(true) { //if(!definedFunctionNames.contains(functionName)) {
						rehearseWindows[functionNum] = 
							getRehearseWindow(uid, functionNum, functionName, parameters, initialSnapshot);
						rehearseWindows[functionNum].requestFocusInWindow();
						rehearseWindows[functionNum].toFront();
					}
				} else {
					if(result.size() >= 6)
						processResponses(rehearseWindows[functionNum], result.get(5));
//					if(rehearseWindows[functionNum].isDone()) {
//					markDone(rehearseWindows[functionNum]);
//					definedFunctionNames.add(rehearseWindows[functionNum].getFunctionName());
//					rehearseWindows[functionNum] = null;
//					}
				}

				timer.cancel();
				//resumeExecution(uid);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
			System.out.println("run thread release in finally");
		}
	}


	private Rehearse getRehearseWindow(int uid, int functionNum,
			String functionName, String parameters, int initialSnapshot) {
		switch(REHEARSE_OPTION) {
		case 2:
			return new RehearseFlatUndo(uid, functionNum, functionName, parameters, initialSnapshot);
			/*
		  case 2:
			return new Rehearse2(uid, functionNum, functionName, parameters, initialSnapshot);
		  case 3:
			return new Rehearse3(uid, functionNum, functionName, parameters, initialSnapshot);
		  case 4:
			return new Rehearse4(uid, functionNum, functionName, parameters, initialSnapshot);
			 */
		default:
			return new RehearseTreeUndo(uid, functionNum, functionName, parameters, initialSnapshot);

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


	public static void markDone(Rehearse rehearse, int functionNum) {

		if(rehearseWindows[functionNum].isDone()) {

			String params = "rehearse_uid=" + rehearse.getUid() + "&function_num="
			+ rehearse.getFunctionNum();
			ArrayList<String> result;
			do {
				result = POWUtils.callPOWScript(POWUtils.MARK_DONE_URL, params);
				System.out.println("mark done trying again");
			} while(result.get(0).contains("Error"));
			//definedFunctionNames.add(rehearseWindows[functionNum].getFunctionName());
			rehearseWindows[functionNum] = null;
		}

	}

	private static void resumeExecution(int uid) {
		String params = "rehearse_uid=" + uid;
		ArrayList<String> result;
		do {
			result = POWUtils.callPOWScript(POWUtils.RESUME_EXECUTION_URL, params);
			System.out.println("resume trying again");
		} while(result.get(0).contains("Error"));
		
	}

}
