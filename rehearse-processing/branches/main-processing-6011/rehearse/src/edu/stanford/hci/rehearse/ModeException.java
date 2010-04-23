package edu.stanford.hci.rehearse;

public class ModeException extends Exception {

    boolean javaMode = false;

    public boolean isJavaMode() {
            return javaMode;
    }

    public void setJavaMode(boolean javaMode) {
            this.javaMode = javaMode;
    }


}