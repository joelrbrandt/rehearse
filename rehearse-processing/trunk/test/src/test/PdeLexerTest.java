/**
 * 
 */
package test;

import java.io.StringReader;

import junit.framework.TestCase;
import processing.app.preproc.PdeLexer;
import antlr.Token;
import edu.stanford.hci.helpmeout.PdeLexingFilter;


/**
 * @author bjoern
 * Let's see how the ANTLR PdeLexer works 
 */
public class PdeLexerTest extends TestCase {
	
	

	protected void setUp() throws Exception {
		
	}
	
	public void testRun() throws Exception {
		
		String program = "\nvoid setup(){\nString str  = \"goo\";\ninnt x=2;\nx++;\nfoo();\n}\n";
		String filteredProgram = "";
		// construct the lexer
		PdeLexer lex = new PdeLexer(new StringReader(program));
		// and our custom filter
		PdeLexingFilter filter = new PdeLexingFilter(lex);
		
		
		//now read tokens one-by-one
		while(true) {
			Token tok = filter.nextToken();
			filteredProgram += tok.getText();
			
			if(tok.getType()==Token.EOF_TYPE) {
				break;
			}
		}
		System.out.println(filteredProgram);
		System.out.println("done.");
	}
}
