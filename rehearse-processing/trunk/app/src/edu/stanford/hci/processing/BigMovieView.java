package edu.stanford.hci.processing;

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
    } else {Movie recording = new Movie(this, recordingFilename);
      recording.jump((float)(recording.duration() / 2.0));
      recording.read();
      recordings.add(recording);
    }
  }
  
  public void setRecordingAt(int index, String recordingFileName) {
    recordingFileNames.set(index, recordingFileName);
    Movie recording = new Movie(this, recordingFileName);
    recording.jump((float)(recording.duration() / 2.0));
    recording.read();
    recordings.set(index, recording);
    System.out.println("index: " + index + " fileName: " + recordingFileName);
  }
  
  public void setRecordingJump(String fileName, float time) {
    currIndex = recordingFileNames.indexOf(fileName);
    
    if (currIndex == -1) return;
    
    Movie recording = recordings.get(currIndex);
    
    recording.jump(time);
    recording.read();
    
    redraw();
  }
  
  @Override
  public void setup() {
    size(300,300);
    textFont(createFont("Arial", 12));
    background(0);
    
    noLoop();
  }
  
  @Override
  public void draw() {
    
    if (currIndex != -1) {
      image(recordings.get(currIndex), 0, 0, width, height);
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
