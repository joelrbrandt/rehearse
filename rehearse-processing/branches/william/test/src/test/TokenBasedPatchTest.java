package test;

import edu.stanford.hci.helpmeout.HelpMeOut;
import junit.framework.TestCase;

public class TokenBasedPatchTest extends TestCase {

	public void testRun() {
		HelpMeOut hmo = HelpMeOut.getInstance();
		String s;
		
		s = hmo.tokenBasedAutoPatch("int x;", "int y=5;");
		assertTrue(s,s.equals("int x=5;"));
		
		s = hmo.tokenBasedAutoPatch("int x;\n", "public int zoo=500;\n");
		assertTrue(s,s.equals("public int x=500;\n"));
		
		s = hmo.tokenBasedAutoPatch("public int x=500;\n", "int y;\n");
		assertTrue(s,s.equals("int x;\n"));
		
		s = hmo.tokenBasedAutoPatch("float q = 17;","int x = 5;");
		assertTrue(s,s.equals("int q = 17;"));
		
		s = hmo.tokenBasedAutoPatch("public float q = 17;","private int x = 5;");
		assertTrue(s,s.equals("private int q = 17;"));
		
		s = hmo.tokenBasedAutoPatch("int x", "int y=5;");
		assertTrue(s,s.equals("int x=5;"));
	}
}
