package edu.stanford.hci.processing.editor;

import java.util.HashSet;
import java.util.Set;

/**
 * A class that encapsulates all data associated with one line in the
 * editor. This means that when lines are added, removed, etc. the right
 * data stays with each line.
 * 
 * @author William Choi
 *
 */
public class RehearseLineModel {

	public boolean isPrintPoint;
	public boolean executedInLastRun;
	public boolean isMostRecentlyExecuted;
	
	public int countAtLastExec;
	
	public Set<Integer> relevantVersions;
	
	public RehearseLineModel() {
		isPrintPoint = false;
		executedInLastRun = false;
		isMostRecentlyExecuted = false;
		
		relevantVersions = new HashSet<Integer>();
	}
	
}
