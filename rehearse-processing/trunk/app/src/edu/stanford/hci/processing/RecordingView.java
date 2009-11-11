package edu.stanford.hci.processing;

import processing.core.PApplet;
import processing.video.Movie;

public class RecordingView extends PApplet {
  
  private String recordingFilename;
  private Movie recording;
  
  private boolean setup_done = false;
  private int initialFrameCount = 0;
  static final private int INITIAL_FRAME_COUNT_MAX = 100;
  
  public RecordingView(String recordingFilename) {
    // TODO Auto-generated constructor stub
    System.out.println("Creating Recording view for: " + recordingFilename);
    this.recordingFilename = recordingFilename;
    //this.init();
  }
  
  public void setRecording(String recordingFilename) {
    this.recordingFilename = recordingFilename;
    try {
      recording = new Movie(this, this.recordingFilename);
      recording.jump((float)(recording.duration() / 2.0));
      recording.read();
      
    } catch(NullPointerException e) {
      recording = null;
      System.out.println("Could not find recording for: " + this.recordingFilename);
    }
    
    redraw();
  }
  
  @Override
  public void setup() {
    size(100, 100, P2D);
    println("setup");
    
    try {
      print("loading: " + this.recordingFilename + "...");
      recording = new Movie(this, this.recordingFilename);
      println("DONE!");
      recording.jump((float)(recording.duration() / 2.0));
      recording.read();
    } catch(NullPointerException e) {
      recording = null;
      System.out.println("Could not find recording for: " + this.recordingFilename);
    }
    //background(0);
    
    //noLoop();
    //setup_done = true;
    //redraw();
  }
  
  @Override
  public void draw() {
    
    if (!setup_done && initialFrameCount < INITIAL_FRAME_COUNT_MAX) {
      initialFrameCount++;
    } else if (initialFrameCount >= INITIAL_FRAME_COUNT_MAX) {
      noLoop();
      setup_done = true;
    }
    
    if (recording!=null) {
      //println("recordingview: draw!");
      image(recording, 0, 0, width, height);
      flush();
    } 
  }
  
  @Override
  public void mouseMoved() {
    
    if (recording != null) {
      float pos = (float)mouseX / (float)width;
      float jumpTime = pos * recording.duration();
      //println(jumpTime);
      
      recording.jump(jumpTime);
      recording.read();
      redraw();
    }
  }
  
  

}
