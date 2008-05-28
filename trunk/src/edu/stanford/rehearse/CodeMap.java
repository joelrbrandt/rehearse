package edu.stanford.rehearse;

import java.util.HashMap;
import java.util.Map;

public class CodeMap {
	

	private Map<Integer, CodeElement> codeLineMap;
	
	public CodeMap() {
		codeLineMap = new HashMap<Integer, CodeElement>();
	}
	
	public void add(CodeElement elem) {
		codeLineMap.put(elem.getLineNum(), elem);
	}
	
	public void undo(int lineNum) {
		CodeElement elem = codeLineMap.get(lineNum);
		if(elem != null) {
			elem.setActive(false);
		}
	}
	
	public String getCodeAtLine(int lineNum) {
		CodeElement elem = codeLineMap.get(lineNum);
		if(elem != null) {
			return elem.getCode();
		}
		return "";
	}
	
	public boolean isLineActive(int lineNum) {
		CodeElement elem = codeLineMap.get(lineNum);
		if(elem != null) {
			return elem.isActive();
		}
		return false;
	}

}
