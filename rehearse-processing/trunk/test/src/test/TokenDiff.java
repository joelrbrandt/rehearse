package test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;
import antlr.Token;
import edu.stanford.hci.helpmeout.Diff;
import edu.stanford.hci.helpmeout.Difference;
import edu.stanford.hci.helpmeout.PdeMatchProcessor;

/**
 * Test diffing two lists of Tokens using the incava.org Diff algorithm
 * @author bjoern
 *
 */
public class TokenDiff extends TestCase {

	private Comparator<Token> ct = new Comparator<Token>() {
		public int compare(Token o1, Token o2) {
			return o2.getType()-o1.getType();
		}
	};

	public void testRun() throws Exception {
		//take two source code strings
		String line1 = "x int f;\nq";    // user pgm
		String line2 = "int x = 5;\nq";  // suggested fix

		//tokenize each line
		PdeMatchProcessor proc = new PdeMatchProcessor();
		List<Token> tokens1 = proc.getUnfilteredTokenArray(line1); //want unchanged tokens!
		List<Token> tokens2 = proc.getUnfilteredTokenArray(line2);
		for(Token t1:tokens1) {
			System.out.print(t1.getType()+" ");
		}
		System.out.println();
		for(Token t2:tokens2) {
			System.out.print(t2.getType()+" ");
		}
		System.out.println();

		//do a diff on the token level
		Diff<Token> diff = new Diff<Token>(tokens1, tokens2,ct);
		List<Difference> differences = diff.diff();
		for(Difference d : differences) {
			System.out.println(d.toString());
		}
		List<Token> tokensOut = new ArrayList<Token>();
		//now transform tokens1 into tokens2 by stepping through diffs
		//set through token1 tokens
		int diffIndex =0;
		for(int i=0; i<tokens1.size(); i++) {
			System.out.println(i);
			Difference d = differences.get(diffIndex);
			//copy everything that's unchanged until next difference
			if(i<d.getDeletedStart()) {
				tokensOut.add(tokens1.get(i));
			} else {
				//now were at the difference
				//handle deletion - skip forward in ptr
				if(d.getDeletedEnd()!=Difference.NONE) {
					i+=d.getDeletedEnd()-d.getDeletedStart();
				}
				//handle addition - insert into output
				if(d.getAddedEnd()!=Difference.NONE) {
					tokensOut.addAll(tokens2.subList(d.getAddedStart(), d.getAddedEnd()+1));
				}
				diffIndex++;
				if(diffIndex>=differences.size()) {
					//copy remaining
					tokensOut.addAll(tokens1.subList(i, tokens1.size()-1));
					break;
				}
			}
			//print what we have so far
			for(Token t: tokensOut) {
				System.out.print(t.getText());
			}System.out.println();

			

		}
		//print what we have so far
		for(Token t: tokensOut) {
			System.out.print(t.getText());
		}System.out.println();

		
	}
}
