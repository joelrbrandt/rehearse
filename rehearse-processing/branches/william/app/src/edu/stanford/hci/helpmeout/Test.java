package edu.stanford.hci.helpmeout;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;



/**
 * Test class to try echoing strings, storing into remote db and loading from remote db.
 * for HelpMeOut project
 * @author bjoern
 *
 */
public class Test {

	static ServiceProxy proxy;
	/**
	 * @param args
	 */
	private static final String SERVICE_URL = "http://rehearse.stanford.edu/helpmeout/server.py";
	
	public static void main(String[] args) {
		proxy = new ServiceProxy(SERVICE_URL);
			
		echo();
		String file1 = readFile("file1.txt");
		String file2 = readFile("file2.txt");
		store("java-error4",file1,file2);
		query("java-error4","foo");


	}

	private static String readFile(String fname) {
		String wholeFile = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(fname));
			String str;
			while ((str = in.readLine()) != null) {
				wholeFile = wholeFile.concat(str+'\n');
			}
			in.close();
			return wholeFile;
		} catch (IOException e) {
			System.err.println("couldn't open file "+fname);
			return null;
		}

	}

	/**
	 * Simple test: call an echo function that takes a string and returns that same string
	 */
	private static void echo() {
		String result = (String)proxy.call("echo", "hello you!");
		System.out.println(result);
	}

	/**
	 * Store a compile error fix in the remote database via JSON-RPC
	 * 
	 * @param error The error string
	 * @param s0 The "old" file contents (i.e., the one with the compile error)
	 * @param s1 The "new" file contents (without compile error)
	 */
	private static void store(String error, String s0, String s1) {
		if((error!=null)&&(s0!=null)&&(s1!=null)) {
			try {

				String result = (String)proxy.call("store2",error, s0, s1);

			}catch (Exception e) {
				System.err.println("couldn't store.");
				e.printStackTrace();
			}
		} else {
			System.err.println("store called with at least one null argument. tsk tsk.");
		}
	}

		/**
		 * Query the remote HelpMeOut database for relevant example fixes.
		 * Via JSON-RPC
		 * 
		 * @param error The compile error string of the current error
		 * @param code The line of code referenced by the compile error
		 */
		private static void query(String error, String code) {
			try {
				ArrayList<HashMap<String,ArrayList<String>>> result = 
					(ArrayList<HashMap<String,ArrayList<String>>>) proxy.call("query", error, code);
				for(HashMap<String,ArrayList<String>> m:result) {
					if(m.containsKey("old")) {
						System.out.println("=== BEFORE ===");
						Object o = m.get("old");
						System.out.println(o);
					}
					if(m.containsKey("new")) {
						System.out.println("=== AFTER ===");
						Object o = m.get("new");
						System.out.println(o);
					}
				}
			} catch (Exception e) {
				System.err.println("couldn't query or wrong type returned.");
				
				e.printStackTrace();
			}
		}
	}