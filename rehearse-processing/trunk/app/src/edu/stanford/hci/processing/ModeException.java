package edu.stanford.hci.processing;

public class ModeException extends Exception {
	
	boolean javaMode = false;

	public boolean isJavaMode() {
		return javaMode;
	}

	public void setJavaMode(boolean javaMode) {
		this.javaMode = javaMode;
	}
	
	
}
