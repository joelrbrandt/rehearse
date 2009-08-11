package edu.stanford.hci.processing;

import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JFrame;

import processing.core.PApplet;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.UtilEvalError;
import edu.stanford.hci.helpmeout.ExceptionInfo;
import edu.stanford.hci.helpmeout.HelpMeOutExceptionTracker;

public class RehearsePApplet extends PApplet {
	
	public enum MethodType { draw, setup, mouseClicked, 
		mouseDragged, mouseMoved, mousePressed, mouseReleased,
		keyPressed, keyReleased, keyTyped
	} 
	
	Interpreter i;
	
	public void setInterpreter(Interpreter i) {
		this.i = i;
	}
	
	@Override
	public void size(final int iwidth, final int iheight,
            String irenderer, String ipath) {
		super.size(iwidth, iheight, irenderer, ipath);
		((JFrame)frame).getContentPane().setPreferredSize(new Dimension(width, height));
		((JFrame)frame).pack();
	}
	
	@Override
	public void draw() {
		invoke(MethodType.draw);
	}
	
	@Override
	public void setup() {
		invoke(MethodType.setup);
	}
	
	@Override
	public void mouseClicked() {
		invoke(MethodType.mouseClicked);
	}
	
	@Override
	public void mouseDragged() {
		invoke(MethodType.mouseDragged);
	}
	
	@Override
	public void mouseMoved() {
		invoke(MethodType.mouseMoved);
	}
	
	@Override
	public void mousePressed() {
		invoke(MethodType.mousePressed);
	}
	
	@Override
	public void mouseReleased() {
		invoke(MethodType.mouseReleased);
	}
	
	@Override
	public void keyPressed() {
		invoke(MethodType.keyPressed);
	}
	
	@Override
	public void keyReleased() {
		invoke(MethodType.keyReleased);
	}
	
	@Override
	public void keyTyped() {
		invoke(MethodType.keyTyped);
	}
	
	@Override
	protected void exit2() {
		// do nothing instead of System.exit
	}
	
	public Image snapshot() {
		return g.getImage();
	}
	
	// Some methods are not supposed to be called directly.
	// This method is mainly used by the Interpreter to skip
	// registering these methods.
	public static boolean isCallableMethodName(String name) {
		for (MethodType m : MethodType.values()) {
			if (name.equals(m.name()))
				return false;
		}
		return true;
	}
	
	
	// If user defined, invoke it. Otherwise, the call does nothing.
	// This only works because the default behavior of every user-overrideable
	// method call is to do nothing.
	public void invoke(MethodType m) {
		try {
			if (i.getNameSpace().getMethod(m.toString(), new Class[0]) != null)	{
				i.eval(m.toString() + "()");
			} else {
			  if (m.toString().equals("draw")) {
			    // We do this to match the default implementation of the draw() method in PApplet.java
			    finished=true;
			  }
			}
		} catch (UtilEvalError e) {
			throw new RuntimeException(e);
		} catch (EvalError e) {
		  	HelpMeOutExceptionTracker.getInstance().processRuntimeException(e, i); //notify editor in here.
        
		  	throw new RuntimeException(e);
		}
	}

  /* (non-Javadoc)
   * @see processing.core.PApplet#stop()
   */
  @Override
  public void stop() {
    System.err.println("RehearsePApplet.stop()");
    // check if waitForNextLine is true
    
    if (i.getWatchForNextLine()) {
    // if so, resolve since we executed the error-causing line and finished without a problem
    HelpMeOutExceptionTracker.getInstance().resolveRuntimeException();
    i.setLineToWatch(-1);
    i.setWatchForNextLine(false);
    
    }
    super.stop();
  }
	
	
}
