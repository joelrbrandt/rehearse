package edu.stanford.rehearse;

import java.util.*;

import edu.stanford.rehearse.UndidLinesList.UndidLinesListModel;

public class CodeTree {
	
	private CodeElement root;
	private CodeElement curr;
	private CodeElement lastUndo;
	
	public CodeTree(int initialSnapshot) {
		root = new CodeElement(-1, "", null);
		root.setSnapshotId(initialSnapshot);
		curr = root;
	}
	
	public void addNewCode(int lineNum, String line) {
		CodeElement elem = new CodeElement(lineNum, line, curr);
		curr.addChild(elem);
		curr = elem;
	}
	
	public int undo() {
		if(curr == root)
			return -1;
			
		curr.setActive(false);
		lastUndo = curr;
		curr = curr.getParent();
		return curr.getSnapshotId();
	}
	
	public int stepsToLine(int lineNum) {
		CodeElement temp = curr;
		int numSteps = 0;
		while(temp != root) {
			numSteps++;
			if(temp.getLineNum() == lineNum)
				break;
			temp = temp.getParent();
		}
		if(temp == root) return -1;
		else return numSteps;
	}
	
	public int undo(int numSteps, UndidLinesListModel model) {
		if(curr == root) return -1;
		while(numSteps != 0 && curr != null) {
			if(model != null) model.addCodeElement(curr);
			curr.setActive(false);
			lastUndo = curr;
			curr = curr.getParent();
			numSteps--;
		}
		if(curr == null)
			return -1;
		return curr.getSnapshotId();
	}
	
	public int undo(int numSteps, InteractiveTextAreaPainter painter) {
		if(curr == root) return -1;
		while(numSteps != 0 && curr != null) {
			if(painter != null) painter.mark(curr.getLineNum(), true);
			curr.setActive(false);
			lastUndo = curr;
			curr = curr.getParent();
			numSteps--;
		}
		if(curr == null) return -1;
		return curr.getSnapshotId();
	}
	
	/*
	public int undo(int lineNum, UndidLinesListModel model) {
		CodeElement temp = curr;
		while(temp != root) {
			if(temp.getLineNum() == lineNum)
				break;
			temp = temp.getParent();
		}
		if(temp == root)
			return -1;
		while(curr != temp) {
			if(model != null) model.addCodeElement(curr);
			curr.setActive(false);
			curr = curr.getParent();
		}
		if(model != null) model.addCodeElement(curr);
		curr.setActive(false);
		lastUndo = curr;
		curr = curr.getParent();
		return curr.getSnapshotId();
	}
	
	public int undo(int lineNum, InteractiveTextAreaPainter painter) {
		CodeElement temp = curr;
		while(temp != root) {
			if(temp.getLineNum() == lineNum)
				break;
			temp = temp.getParent();
		}
		if(temp == root)
			return -1;
		while(curr != temp) {
			if(painter != null) painter.mark(curr.getLineNum(), true);
			curr.setActive(false);
			curr = curr.getParent();
		}
		if(painter != null) painter.mark(curr.getLineNum(), true);
		curr.setActive(false);
		lastUndo = curr;
		curr = curr.getParent();
		return curr.getSnapshotId();
	}
	
	*/
	
	public CodeElement getLastUndid() {
		return lastUndo;
	}
	
	public String getCode() {
		String code = "";
		for(CodeElement c = root.getActiveChild(); c != null; c = c.getActiveChild()) {
			if(!c.isError())
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
	
	public CodeElement getChildByLineNum(int lineNum) {
		for(int i = 0; i < curr.getNumChildren(); i++) {
			if(lineNum == curr.getChild(i).getLineNum()) {
				return curr.getChild(i);
			}
		}
		return null;
	}
	
	/*
	public int redo(int lineNum) {
		for(int i = 0; i < curr.getNumChildren(); i++) {
			if(lineNum == curr.getChild(i).getLineNum()) {
				curr = curr.getChild(i);
				curr.setActive(true);
				return curr.getSnapshotId();
			}
		}
		return -1;
	}*/
	
	public int redo(CodeElement codeElem) {
		for(int i = 0; i < curr.getNumChildren(); i++) {
			if(codeElem.getCode().equals(curr.getChild(i).getCode())) {
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
	
	
	public CodeElement getCurr() {
		return curr;
	}
	
}
