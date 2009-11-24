package edu.stanford.hci.processing;

import java.awt.Dimension;

import processing.core.PApplet;
import processing.video.Movie;
import edu.stanford.hci.processing.VersionHistoryFrameiMovie.VersionHistoryPanel;

public class RecordingView extends PApplet {
  
// highly suspect coding practices, 
// in the name of prototyping
  private static final int SEC_PER_SEGMENT = 1;
  
  BigMovieView bigMovie;
  VersionHistoryFrameiMovie frame;
  
  private String recordingFilename;
  private Movie recording;
  
  private boolean setup_done = false;
  private int initialFrameCount = 0;
  static final private int INITIAL_FRAME_COUNT_MAX = 100;
  

  private static final int PLAY_BUTTON_SIZE = 15;
  private static final int PLAY_BUTTON_X_OFFSET = 25;
  private static final int PLAY_BUTTON_Y_OFFSET = 25;
  
  private VersionHistoryPanel vhp;
  private float jumpTime;
  private int numSegments;
  private float jumpTimes[];
  
  public RecordingView(String recordingFilename, VersionHistoryPanel vhp) {
	  this(recordingFilename);
	  this.vhp = vhp;
  }
  
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
      setSegments();
      
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

      setSegments();
      
    } catch(NullPointerException e) {
      recording = null;
      System.out.println("Could not find recording for: " + this.recordingFilename);
    }
  }
  
  private void setSegments() {
	  numSegments = (int)(recording.duration() / SEC_PER_SEGMENT);
      if (recording.duration() % SEC_PER_SEGMENT != 0) {
    	  numSegments++;
      }   
      jumpTimes = new float[numSegments];
      for (int i = 0; i < numSegments; i++) {
    	  jumpTimes[i] = i * SEC_PER_SEGMENT;
      }
    
      size(VersionHistoryFrame.ROW_HEIGHT * numSegments, VersionHistoryFrame.ROW_HEIGHT, P2D);
      if (vhp != null) {
    	  vhp.setPreferredSize(new Dimension(VersionHistoryFrame.ROW_HEIGHT * numSegments, VersionHistoryFrame.ROW_HEIGHT));
    	  vhp.setMaximumSize(new Dimension(VersionHistoryFrame.ROW_HEIGHT * numSegments, VersionHistoryFrame.ROW_HEIGHT));
    	  vhp.setMinimumSize(new Dimension(VersionHistoryFrame.ROW_HEIGHT * numSegments, VersionHistoryFrame.ROW_HEIGHT));
      }
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
    	background(240);
    	double scale = 1;
    	
    	int recWidth;
    	int recHeight;
    	
    	if (recording.width > recording.height) {
    		scale = ((double)recording.width) / VersionHistoryFrame.ROW_HEIGHT;
    		recWidth = VersionHistoryFrame.ROW_HEIGHT;
    		recHeight = (int)(recording.height / scale);
    	} else if (recording.width < recording.height) {
    		scale = ((double)recording.height) / VersionHistoryFrame.ROW_HEIGHT;
    		recWidth = (int)(recording.width / scale);
    		recHeight = VersionHistoryFrame.ROW_HEIGHT;
    	} else {
    		recWidth = VersionHistoryFrame.ROW_HEIGHT;
    		recHeight = VersionHistoryFrame.ROW_HEIGHT;
    	}
    	
    	for (int i = 0; i < numSegments; i++) {
    		int x = (i * VersionHistoryFrame.ROW_HEIGHT) + (VersionHistoryFrame.ROW_HEIGHT/2);
    		recording.jump(jumpTimes[i]);
    		recording.read();
    		image(recording, x, height/2, recWidth, recHeight);
    	}
    	
    	if (mouseOverPlayButton()) {
        fill(0,255,0);
      } else {
        fill(230,230,230);
      }
      triangle(width - PLAY_BUTTON_X_OFFSET, height - PLAY_BUTTON_Y_OFFSET, 
           width - PLAY_BUTTON_X_OFFSET, height - PLAY_BUTTON_Y_OFFSET + PLAY_BUTTON_SIZE,
           width - PLAY_BUTTON_X_OFFSET + PLAY_BUTTON_SIZE, height - PLAY_BUTTON_Y_OFFSET + PLAY_BUTTON_SIZE / 2);
      }
    	
    	flush();
  }
  
  private boolean mouseOverPlayButton() {
	  return (mouseX > width - PLAY_BUTTON_X_OFFSET 
			  && mouseX < width - PLAY_BUTTON_X_OFFSET + PLAY_BUTTON_SIZE
			  && mouseY > height - PLAY_BUTTON_Y_OFFSET 
			  && mouseY < height - PLAY_BUTTON_Y_OFFSET + PLAY_BUTTON_SIZE);
  }
  
  @Override
  public void mouseMoved() {
     if (recording != null) {
      float pos = (float)mouseX / (float)width;
      jumpTime = pos * recording.duration();
      int whichSegment = (int)(jumpTime / SEC_PER_SEGMENT);
      jumpTimes[whichSegment] = jumpTime;

      bigMovie.setRecordingJump(recordingFilename, jumpTime);
      frame.updateCodeArea(recordingFilename);

      redraw();

    }
  } 
  
  @Override
  public void mouseClicked() {
    if (mouseOverPlayButton()) {
      frame.getController().runHistoryCode(vhp.getModel().getCode());
    }
  }
}
