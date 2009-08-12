package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class RegExpTest extends TestCase {

	public void testExceptionNameGrabber() {
		String exception = "java.lang.ArrayOutOfBoundsException";
		String error = "Blah blah: "+exception;
		
		String patternStr = "(.*?)([A-Za-z\\.]+\\.[A-Za-z]+)$";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(error);
        boolean matchFound = matcher.find();
        String match = matcher.group(2);
        
        assertTrue(match, match.equals(exception));
        
        
        exception = " not an array";
		error = "Blah blah: "+exception;
		
		patternStr = "(.*?)([A-Za-z\\.]+\\.[A-Za-z]+)$";
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(error);
        matchFound = matcher.find();
        //match = matcher.group(2);
        
        assertFalse(matchFound);
	}
	
}
