package edu.stanford.rehearse;

import java.util.ArrayList;

public class CodeElement {
	
	private int lineNum;
	private String code;
	private CodeElement parent;
	private ArrayList<CodeElement> children;
	private boolean isActive;
	private int snapshotId;

	public CodeElement(int lineNum, String code, CodeElement parent) {
		this.lineNum = lineNum;
		this.code = code;
		this.parent = parent;
		this.isActive = true;
		this.children = new ArrayList<CodeElement>();
		this.snapshotId = -1;
	}

	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public CodeElement getParent() {
		return parent;
	}
	
	public void setParent(CodeElement parent) {
		this.parent = parent;
	}
	
	public void addChild(CodeElement child) {
		children.add(child);
	}
	
	public void removeChild(CodeElement child) {
		children.remove(child);
	}
	
	public int getNumChildren() {
		return children.size();
	}
	
	public CodeElement getChild(int i) {
		return children.get(i);
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}
	

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	
	public int getSnapshotId() {
		return snapshotId;
	}

	public void setSnapshotId(int snapshotId) {
		this.snapshotId = snapshotId;
	}

	public CodeElement getActiveChild() {
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).isActive()) {
				return children.get(i);
			}
		}
		return null;
	}
	
}
