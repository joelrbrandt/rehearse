package test;

import edu.stanford.hci.helpmeout.HelpMeOut;
import edu.stanford.hci.helpmeout.HelpMeOutExceptionTracker;
import junit.framework.TestCase;



public class LineOffsetTest extends TestCase {

	public void testLineStartHelpMeOut() {
		String s1 = "0123\n5678\nA";
		int l0 = HelpMeOut.getInstance().getLineStartOffet(s1, 0);
		int l1 = HelpMeOut.getInstance().getLineStartOffet(s1, 1);
		int l2 = HelpMeOut.getInstance().getLineStartOffet(s1, 2);
	
		assertTrue(l0==0);
		assertTrue(l1==5);
		assertTrue(l2==10);

	}
	public void testLineStartExceptionTracker() {
		String s1 = "0123\n5678\nA";
		
		int l0 = HelpMeOutExceptionTracker.getInstance().getCharIndexFromLine(s1,0);
		int l1 = HelpMeOutExceptionTracker.getInstance().getCharIndexFromLine(s1,1);
		int l2 = HelpMeOutExceptionTracker.getInstance().getCharIndexFromLine(s1,2);

		assertTrue(l0==0);
		assertTrue(Integer.toString(l1), l1==5);
		assertTrue(l2==10);

	}
	
	public void testLineOfOffsetHelpMeOut() {
		
		String s1 = "0123\n5678\nA";
		
		assertTrue(0==HelpMeOut.getInstance().getLineOfOffset(s1, 0));
		assertTrue(0==HelpMeOut.getInstance().getLineOfOffset(s1, 3));
		assertTrue(0==HelpMeOut.getInstance().getLineOfOffset(s1, 4)); //"\n" is interpreted as being part of previous line
		assertTrue(1==HelpMeOut.getInstance().getLineOfOffset(s1, 5));
		assertTrue(1==HelpMeOut.getInstance().getLineOfOffset(s1, 8));
		assertTrue(1==HelpMeOut.getInstance().getLineOfOffset(s1, 9));
		assertTrue(2==HelpMeOut.getInstance().getLineOfOffset(s1, 10));
	}
	
	public void testGetLineFromCharIndex() {
		String s1 = "0123\n5678\nA";
		assertTrue(0==HelpMeOutExceptionTracker.getInstance().getLineFromCharIndex(s1,0));
		assertTrue(0==HelpMeOutExceptionTracker.getInstance().getLineFromCharIndex(s1,3));
		assertTrue(0==HelpMeOutExceptionTracker.getInstance().getLineFromCharIndex(s1,4));
		
		assertTrue(1==HelpMeOutExceptionTracker.getInstance().getLineFromCharIndex(s1,5));
		assertTrue(1==HelpMeOutExceptionTracker.getInstance().getLineFromCharIndex(s1,8));
		assertTrue(1==HelpMeOutExceptionTracker.getInstance().getLineFromCharIndex(s1,9));
		
		assertTrue(2==HelpMeOutExceptionTracker.getInstance().getLineFromCharIndex(s1,10));
		
	}
	
	public void testGetLineToWatchAux() {
		String s1 = "123\n456\n789";
		String s2 = "123\n4B6\n789";
		int absLineNum = 1;
		int returnedLine = HelpMeOutExceptionTracker.getInstance().getLineToWatchAux(s1, s2, absLineNum);
		assertTrue(Integer.toString(returnedLine), returnedLine==1);
		
		s1 = "\n123\n456\n789";
		s2 = "\n123\n4B6\n789";
		absLineNum = 2;
		returnedLine = HelpMeOutExceptionTracker.getInstance().getLineToWatchAux(s1, s2, absLineNum);
		assertTrue(Integer.toString(returnedLine), returnedLine==2);
	}
}
