package edu.stanford.hci.processing;

import java.awt.event.MouseEvent;

import processing.core.PApplet;
import processing.video.Movie;

public class RecordingView extends PApplet {
  
// highly suspect coding practices, 
// in the name of prototyping
  BigMovieView bigMovie;
  VersionHistoryFrameiMovie frame;
  
  
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
    
    if (this.recordingFilename == null) return;
    
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
    // This is from a suggestion by Fry to stop stalling
    /*
    System.out.print("Opening QuickTime Session...");
    try {
      quicktime.QTSession.open();
    } catch (quicktime.QTException qte) {
      qte.printStackTrace();
    }
    System.out.println("DONE!");
    */
    
    size(VersionHistoryFrame.ROW_HEIGHT, VersionHistoryFrame.ROW_HEIGHT, P2D);
    imageMode(CENTER);
    textFont(createFont("Arial", 12));
   
    println("setup");
    
    if (recordingFilename == null) {
      background(50);
      textAlign(CENTER);
      text("Currently running...", width/2, height/2);
      return;
    }
  
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
    	background(100);
      double scale = 1;
      if (recording.width > recording.height) {
    	  scale = ((double)recording.width) / width;
          image(recording, width/2, height/2, width, (int)(recording.height / scale));
      } else if (recording.width < recording.height){
    	  scale = ((double)recording.height) / height;
          image(recording, width/2, height/2, (int)(recording.width / scale), height);
      } else {
    	  image(recording, width/2, height/2, width, height);
      }

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
      bigMovie.setRecordingJump(recordingFilename, jumpTime);
      frame.updateCodeArea(recordingFilename);
      
      recording.read();
      redraw();
    }
  } 
  
}
