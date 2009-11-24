/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package difflib.myers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import difflib.*;

/**
 * A clean-room implementation of <a href="http://www.cs.arizona.edu/people/gene/">
 * Eugene Myers</a> differencing algorithm.
 * 
 * <p> See the paper at <a href="http://www.cs.arizona.edu/people/gene/PAPERS/diff.ps">
 * http://www.cs.arizona.edu/people/gene/PAPERS/diff.ps</a></p>
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 */
public class MyersDiff implements DiffAlgorithm
{
    /**
     * Constructs an instance of the Myers differencing algorithm.
     */
    public MyersDiff()
    {
    }

    /**
     * {@inheritDoc}
     * 
     * Return empty diff if get the error while procession the difference.
     */
    public Patch diff(List<?> original, List<?> revised) {
		return diff(original.toArray(), revised.toArray());
	}
    
    public Patch diff(String filename1, String filename2) {
    	List<String> lines1 = stringArrayFromFile(filename1);
    	List<String> lines2 = stringArrayFromFile(filename2);
    	return diff(lines1, lines2);
    }
    
    private List<String> stringArrayFromFile(String filename) {
    	try {
    		BufferedReader in = new BufferedReader(new FileReader(filename));
    		List<String> result = new LinkedList<String>();
    		
    		String line = "";
    		while( (line = in.readLine()) != null ) {
    			result.add(line);
    		}

    		return result;
    		
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			return null;
		} catch (IOException e) {
			System.out.println("IOException!");
			return null;
		}
    }
    
    /**
     * {@inheritDoc}
     * 
     * Return empty diff if get the error while procession the difference.
     */
	public Patch diff(Object[] orig, Object[] rev)
    {
        PathNode path;
		try {
			path = buildPath(orig, rev);
			return buildRevision(path, orig, rev);
		} catch (DifferentiationFailedException e) {
			e.printStackTrace();
		}
        return new Patch();
    }

    /**
     * Computes the minimum diffpath that expresses de differences
     * between the original and revised sequences, according
     * to Gene Myers differencing algorithm.
     *
     * @param orig The original sequence.
     * @param rev The revised sequence.
     * @return A minimum {@link PathNode Path} accross the differences graph.
     * @throws DifferentiationFailedException if a diff path could not be found.
     */
    public static PathNode buildPath(Object[] orig, Object[] rev)
        throws DifferentiationFailedException
    {
        if (orig == null)
            throw new IllegalArgumentException("original sequence is null");
        if (rev == null)
            throw new IllegalArgumentException("revised sequence is null");

        // these are local constants
        final int N = orig.length;
        final int M = rev.length;

        final int MAX = N + M + 1;
        final int size = 1 + 2 * MAX;
        final int middle = (size + 1) / 2;
        final PathNode diagonal[] = new PathNode[size];

        diagonal[middle + 1] = new Snake(0, -1, null);
        for (int d = 0; d < MAX; d++)
        {
            for (int k = -d; k <= d; k += 2)
            {
                final int kmiddle = middle + k;
                final int kplus = kmiddle + 1;
                final int kminus = kmiddle - 1;
                PathNode prev = null;

                int i;
                if ( (k == -d) ||
                    (k != d && diagonal[kminus].i < diagonal[kplus].i))
                {
                    i = diagonal[kplus].i;
                    prev = diagonal[kplus];
                }
                else
                {
                    i = diagonal[kminus].i + 1;
                    prev = diagonal[kminus];
                }

                diagonal[kminus] = null; // no longer used

                int j = i - k;

                PathNode node = new DiffNode(i, j, prev);

                // orig and rev are zero-based
                // but the algorithm is one-based
                // that's why there's no +1 when indexing the sequences
                while (i < N && j < M && orig[i].equals(rev[j]))
                {
                    i++;
                    j++;
                }
                if (i > node.i)
                    node = new Snake(i, j, node);

                diagonal[kmiddle] = node;

                if (i >= N && j >= M)
                {
                    return diagonal[kmiddle];
                }
            }
            diagonal[middle+d-1] = null;

        }
        // According to Myers, this cannot happen
        throw new DifferentiationFailedException("could not find a diff path");
    }

    /**
     * Constructs a {@link Revision} from a difference path.
     *
     * @param path The path.
     * @param orig The original sequence.
     * @param rev The revised sequence.
     * @return A {@link Revision} script corresponding to the path.
     * @throws DifferentiationFailedException if a {@link Revision} could
     *         not be built from the given path.
     */
    public static Patch buildRevision(PathNode path, Object[] orig, Object[] rev)
    {
        if (path == null)
            throw new IllegalArgumentException("path is null");
        if (orig == null)
            throw new IllegalArgumentException("original sequence is null");
        if (rev == null)
            throw new IllegalArgumentException("revised sequence is null");

        Patch patch = new Patch();
        if (path.isSnake())
            path = path.prev;
        while (path != null && path.prev != null && path.prev.j >= 0)
        {
            if(path.isSnake()) 
               throw new IllegalStateException("bad diffpath: found snake when looking for diff");
            int i = path.i;
            int j = path.j;

            path = path.prev;
            int ianchor = path.i;
            int janchor = path.j;
            
            int numElems1 = i - ianchor;
            int numElems2 = j - janchor;
            
            Object[] copy1 = new Object[numElems1];
            Object[] copy2 = new Object[numElems2];
            
            for (int ii=0; ii<numElems1; ii++) {
            	copy1[ii] = orig[ianchor+ii];
            }
            for (int ii=0; ii<numElems2; ii++) {
            	copy2[ii] = rev[janchor+ii];
            }
            
            Chunk original = new Chunk(ianchor, i - ianchor, copy1);
            Chunk revised = new Chunk(janchor, j - janchor, copy2);
            
            //Chunk original = new Chunk(ianchor, i - ianchor, Arrays.copyOfRange(orig, ianchor, i));
            //Chunk revised = new Chunk(janchor, j - janchor, Arrays.copyOfRange(rev, janchor, j));
            
            Delta delta = null;
            if (original.getSize() == 0 && revised.getSize() != 0) {
            	delta = new InsertDelta(original, revised);
            } else if (original.getSize() > 0 && revised.getSize() == 0) {
            	delta = new DeleteDelta(original, revised);
            } else {
            	delta = new ChangeDelta(original, revised);
            }
            
                                         
            patch.addDelta(delta);
            if (path.isSnake())
                path = path.prev;
        }
        return patch;
    }

}
