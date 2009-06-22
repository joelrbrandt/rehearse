package edu.stanford.rehearse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class POWUtils {
	
	public static final String BASE_URL = "http://localhost:6670/rehearse/";
	
	public static final String REHEARSE_CHECK_BP = BASE_URL + "check_for_breakpoint.sjs";
	
	public static final String RESUME_EXECUTION_URL = BASE_URL + "resume_execution.sjs";
	
	public static final String INSERT_CODE_URL = BASE_URL + "insert_code.sjs";
	
	//public static final String REHEARSE_URL = BASE_URL + "rehearse.sjs";
	
	//public static final String UNDO_URL = BASE_URL + "undo.sjs";
	
	public static final String MARK_DONE_URL = BASE_URL + "mark_done.sjs";
	
	public static final String UPDATE_CODE_URL = BASE_URL + "update_code_queue.sjs";
	
	public static ArrayList<String> callPOWScript(String url, String params) {
		try {
			URL myURL = new URL(url);
			URLConnection myUC = myURL.openConnection();
			myUC.setDoOutput(true);
			myUC.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			myUC.connect();
			
			PrintWriter out = new PrintWriter(myUC.getOutputStream());
			out.print(params);
			out.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(myUC.getInputStream()));
			ArrayList<String> result = new ArrayList<String>();
			String s;
			while ((s = in.readLine()) != null) {
				result.add(s);
			}
			in.close();
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
