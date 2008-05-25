package edu.stanford.rehearse;

import java.util.*;

public class CodeList {
	
	private CodeElement head;
	private CodeElement curr;
	
	public CodeList(int initialSnapshot) {
		head = new CodeElement(-1, "", null);
		head.setSnapshotId(initialSnapshot);
		curr = head;
	}
	
	public void addNewCode(int lineNum, String line) {
		CodeElement elem = new CodeElement(lineNum, line, curr);
		curr.addChild(elem);
		curr = elem;
	}
	
	public int undo() {
		if(curr == head)
			return -1;
			
		curr.setActive(false);
		curr = curr.getParent();
		return curr.getSnapshotId();
	}
	
	public String getCode() {
		String code = "";
		for(CodeElement c = head.getActiveChild(); c != null; c = c.getActiveChild()) {
			code += c.getCode() + "\n";
		}
		return code;
	}
	
	public void setCurrentSnapshotId(int snapshotId) {
		curr.setSnapshotId(snapshotId);
	}
	
	public Set<Integer> getRedoLineNums() {
		Set<Integer> lineNums = new HashSet<Integer>();
		for(int i = 0; i < curr.getNumChildren(); i++) {
			lineNums.add(curr.getChild(i).getLineNum());
		}
		return lineNums;
	}
	
	public int redo(int lineNum) {
		for(int i = 0; i < curr.getNumChildren(); i++) {
			if(lineNum == curr.getChild(i).getLineNum()) {
				curr = curr.getChild(i);
				curr.setActive(true);
				return curr.getSnapshotId();
			}
		}
		return -1;
	}
	
	public int getCurrentCommandLine() {
		return curr.getLineNum();
	}
	
}
