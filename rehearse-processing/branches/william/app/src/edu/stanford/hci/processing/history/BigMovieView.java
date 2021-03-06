package edu.stanford.hci.processing.history;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.video.Movie;

public class BigMovieView extends PApplet {
  
  private ArrayList<String> recordingFileNames;
  private ArrayList<Movie> recordings;
  
  private int currIndex = -1;
  
  private boolean setup_done = false;
  private int initialFrameCount = 0;
  static final private int INITIAL_FRAME_COUNT_MAX = 100;
  
  public BigMovieView() {
    recordingFileNames = new ArrayList<String>();
    recordings = new ArrayList<Movie>();
  }
  
  public void addRecording(String recordingFilename) {
    recordingFileNames.add(recordingFilename);
    if (recordingFilename == null) {
      recordings.add(null);
    } else {
      try {
        Movie recording = new Movie(this, recordingFilename);
        recording.jump((float)(recording.duration() / 2.0));
        recording.read();
        recordings.add(recording);
      } catch (NullPointerException e) {
        recordings.add(null);
      }
    }
  }
  
  public void setRecordingAt(int index, String recordingFileName) {
    recordingFileNames.set(index, recordingFileName);
    try {
      Movie recording = new Movie(this, recordingFileName);
      if (recording != null) {
        recording.jump((float)(recording.duration() / 2.0));
        recording.read();
        recordings.set(index, recording);
        System.out.println("index: " + index + " fileName: " + recordingFileName);
      }
    } catch (NullPointerException e) {
      System.out.println("Movie not found");
    }
    
  }
  
  public void setRecordingJump(String fileName, float time) {
    
    int tmpIndex = recordingFileNames.indexOf(fileName);
    //currIndex = recordingFileNames.indexOf(fileName);
    
    if (tmpIndex == -1) return;
    
    Movie recording = recordings.get(tmpIndex);
    
    if (recording != null) {
      recording.jump(time);
      recording.read();
      currIndex = tmpIndex;
    }
    
    redraw();
  }
  
  @Override
  public void setup() {
    size(300,300);
    imageMode(CENTER);
    textFont(createFont("Arial", 12));
    background(0);
    
    noLoop();
  }
  
  @Override
  public void draw() {
    
    if (currIndex != -1) {
      background(100);
      Movie recording = recordings.get(currIndex);
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
    } else {
      background(0);
    }
  }
  
  @Override
  public void mouseMoved() {
    if (currIndex == -1) return;
    Movie recording = recordings.get(currIndex);
    
    if (recording != null) {
      float pos = (float)mouseX / (float)width;
      float jumpTime = pos * recording.duration();
      
      recording.jump(jumpTime);
      recording.read();
      redraw();
    }
  }
  
  

}
