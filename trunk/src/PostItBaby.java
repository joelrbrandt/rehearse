import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class PostItBaby {

	private static final String REHEARSE_URL = "http://localhost:6670/rehearse.sjs";

	public static List<String> doPost(String command) throws Exception {
		URL myURL = new URL(REHEARSE_URL);
		URLConnection myUC = myURL.openConnection();
		myUC.setDoOutput(true);
		myUC.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		myUC.connect();

		PrintWriter out = new PrintWriter(myUC.getOutputStream());
		String cmdEnc = "command=" + URLEncoder.encode(command, "UTF-8");
		out.print(cmdEnc);
		out.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(myUC.getInputStream()));
		ArrayList<String> result = new ArrayList<String>();
		String s;
		while ((s = in.readLine()) != null) {
			result.add(s);
		}
		in.close();

		return result;
	}

	private static void executeAndPrintCommand(String command) throws Exception {
		System.out.println("command: " + command);
		System.out.print("result: ");
		List<String> a = doPost(command);
		Iterator<String> iter = a.iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
		System.out.println("-----");
	}
	
	public static void main(String[] args) throws Exception {
		executeAndPrintCommand("$('#p1').css('border','thin solid rgb(255,0,0)')");
		executeAndPrintCommand("$('#p2').css('font-family','monospace')");
		executeAndPrintCommand("$.ajax({ type:'GET', url:'example_more.html', success:function(msg) { $('#empty').html(msg) } } )");
	}
}
