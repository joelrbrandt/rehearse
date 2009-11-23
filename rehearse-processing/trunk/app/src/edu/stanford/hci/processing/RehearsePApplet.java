package edu.stanford.hci.processing;

import java.awt.Dimension;
import java.awt.Image;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import processing.core.PApplet;
import processing.core.PApplet.RegisteredMethods;
import processing.video.MovieMaker;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.UtilEvalError;
import edu.stanford.hci.helpmeout.HelpMeOutExceptionTracker;

/**
 * Overrides PApplet & lets the methods called on it (e.g. setup, draw) 
 * to pass through the interpreter BeanShell instead.
 */
public class RehearsePApplet extends PApplet {

  private static Object mediaLock = new Object();
  private int version = -1;
  private MovieMaker mm;
  
  public enum MethodType { draw, setup, mouseClicked, 
    mouseDragged, mouseMoved, mousePressed, mouseReleased,
    keyPressed, keyReleased, keyTyped
  } 

  Interpreter i;
  
  public boolean resolveException = true;

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
  public void handleDraw() {
    super.handleDraw();
    
    // Then add frame to video recording
    if (frameCount != 0 && mm != null) {
      synchronized(mm) {
        // null check again in case was nulled while waiting
        if (mm != null) {
          this.mm.addFrame();
        }
      }
    }
  }
  
  @Override
  public void setup() {
    invoke(MethodType.setup);
    
    // Create video recorder
    // TODO (Abel): Make history folder based on constant
    System.out.println("Creating MovieMaker to record execution");
    
    synchronized(mediaLock) {
      mm = new MovieMaker(this, this.width, this.height, "history/" + this.version + ".mov", 
                          30, MovieMaker.VIDEO, MovieMaker.LOW);
    }
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

  public MovieMaker getVideoRecording() { return mm; }
  
  public void setVersion(int version) { this.version = version; }
  public int getVersion() { return version; }
  
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
      RehearseLogger.getInstance().log(
          RehearseLogger.EventType.RUNTIME_ERROR, i.getEditor().getSketch(), e.toString());
      throw new RuntimeException(e);
    }
  }

  public void finishVideoRecording() {
    // Stop video recording
    // stop is called multiple times for whatever reason.  Only dispose of mm once
    // TODO: check if mm is actually recording?
    if (mm != null) {
      System.out.println("Finishing video recording");
      
      synchronized(mm) {
        mm.finish();
        mm = null;
      }
    }
  }
  
  public void startNewMovie() {
    synchronized(mediaLock) {
      this.version++;
      mm = new MovieMaker(this, this.width, this.height, "history/" + this.version + ".mov", 
                          30, MovieMaker.VIDEO, MovieMaker.LOW);
    }
  }
  
  /* (non-Javadoc)
   * @see processing.core.PApplet#stop()
   */
  @Override
  public void stop() {
    //System.err.println("RehearsePApplet.stop()");
    // check if waitForNextLine is true

    finishVideoRecording();
    
    if (i.getWatchForNextLine() && resolveException) {
      System.out.println("\tRehearsePApplet.stop() inner");
      // if so, resolve since we executed the error-causing line and finished without a problem
      HelpMeOutExceptionTracker.getInstance().resolveRuntimeException();
      i.setLineToWatch(-1);
      i.setWatchForNextLine(false);

    }
    super.stop();
  }

  @Override
  protected void registerNoArgs(RegisteredMethods meth,
      String name, Object o) {
    Class<?> c = o.getClass();
    try {
      Method method = c.getDeclaredMethod(name, new Class[] {});
      method.setAccessible(true);
      meth.add(o, method);

    } catch (NoSuchMethodException nsme) {
      die("There is no " + name + "() method in the class " +
          o.getClass().getName());

    } catch (Exception e) {
      die("Could not register " + name + " + () for " + o, e);
    }
  }
  
  @Override
  protected void registerWithArgs(RegisteredMethods meth,
      String name, Object o, Class<?> cargs[]) {
    Class<?> c = o.getClass();
    try {
      Method method = c.getDeclaredMethod(name, cargs);
      method.setAccessible(true);
      meth.add(o, method);

    } catch (NoSuchMethodException nsme) {
      die("There is no " + name + "() method in the class " +
          o.getClass().getName());

    } catch (Exception e) {
      die("Could not register " + name + " + () for " + o, e);
    }
  }

}
