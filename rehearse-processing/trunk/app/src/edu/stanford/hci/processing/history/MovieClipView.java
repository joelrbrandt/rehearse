package edu.stanford.hci.processing.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;


import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.video.Movie;

public class MovieClipView extends VersionsView {

  private static final int SEC_PER_SEGMENT = 2;
  
  particle selected = null; 
  particle hovered = null;
  boolean viewLocked = false;

  ArrayList<particle> allParts = new ArrayList<particle>();
  
  public MovieClipView() {
    
  }
  
  public void addVersion(VersionHistory vh) {
    // Check if particle is already there
    for (int i=0; i<allParts.size(); i++) {
      particle p = (particle)allParts.get(i);
      if (p.version == vh.getVersion()) {
        println("Updating video");
        p.setVideoFilename(vh.getVideoFilename());
        layoutParticles();
        return;
      }
    }
    
    println("Creating new particle");
    particle p = new particle(allParts.size(), vh.getVideoFilename());
    
    histories.add(vh);
    allParts.add(p);

    clearVersionFilter();
  }
  
  public VersionHistory getVersion(int n) {

    for (int i=0; i<histories.size(); i++) {
      if (histories.get(i).getVersion() == n) {
        return histories.get(i);
      }
    }
    return null;
  }
  
  public VersionHistory getVersion(String filename) {
    
    for (int i=0; i<histories.size(); i++) {
      if (histories.get(i).getVideoFilename().equals(filename)) {
        return histories.get(i);
      }
    }
    return null;
  }

  class particle {
    float x;
    float y;
    
    private float sx;
    float sy;
    
    boolean minimized;
    int version;
    boolean marked = false;
    
    float jumpTime;
    int numSegments;
    float jumpTimes[];
    PImage clipImages[];
    
    String filename;
    Movie recording;
    
    private static final int PLAY_BUTTON_SIZE = 10;
    private static final int PLAY_BUTTON_X_OFFSET = 15;
    private static final int PLAY_BUTTON_Y_OFFSET = 15;
    
    private static final int STOP_BUTTON_SIZE = 10;
    private static final int STOP_BUTTON_X_OFFSET = 15;
    private static final int STOP_BUTTON_Y_OFFSET = 55;
    
    public particle(int n, String filename) {
      this.filename = filename;
      this.version = n;
      this.sx = VersionHistoryFrame.ROW_HEIGHT;
      setVideoFilename(filename);
      
      bigMovie.addRecording(filename);
      
    }
    
    public void setVideoFilename(String filename) {
      if (filename != null) {
        try {
          recording = new Movie(MovieClipView.this, filename);
          recording.jump((int)(recording.duration()/2.0));
          recording.read();
          this.filename = filename;
          
          setSegments();
        } catch (NullPointerException e) {
          recording = null;
        }
      }
    }
    
    private void setSegments() {
      numSegments = (int)(recording.duration() / SEC_PER_SEGMENT);
      if (recording.duration() % SEC_PER_SEGMENT != 0) {
        numSegments++;
      }   
      jumpTimes = new float[numSegments];
      clipImages = new PImage[numSegments];
      for (int i = 0; i < numSegments; i++) {
        jumpTimes[i] = i * SEC_PER_SEGMENT;
        recording.jump(jumpTimes[i] + .05f);
        recording.read();
        clipImages[i] = recording.get();
      }
      
      sx = VersionHistoryFrame.ROW_HEIGHT * numSegments;
      sy = VersionHistoryFrame.ROW_HEIGHT;
    }
    
    public float getEffectiveWidth() {
      return (minimized ? 10.0f : sx);
    }
    
    public boolean isMouseOver() {
      return !scrolling & mouseX > x &&
        mouseX < x + getEffectiveWidth() && mouseY > y && mouseY < y + sy;
    }
    
    public void draw() {
      if (minimized) {
        noStroke();
        fill(255, 0, 0, 120);
        rect(x, y, 10, sy);
        return;
      }
      
      if (marked) {
        fill(255, 255, 0);
        rect(x - 5, y - 5, sx + 10, sy + 10);
      }
      
      if (isMouseOver()) {
        if (recording != null) {
          float vid_position = (float)((mouseX - x) / sx * recording.duration());
          int whichSegment = (int)(vid_position / SEC_PER_SEGMENT);
          jumpTimes[whichSegment] = vid_position;
          recording.jump(vid_position);
          recording.read();
          clipImages[whichSegment] = recording.get();
          
          bigMovie.setRecordingJump(this.filename, vid_position);
          frame.setVersionNumber(this.version);
        
        }
      }
        
      for (int i = 0; i < numSegments; i++) {
        float xPos = x + (i * VersionHistoryFrame.ROW_HEIGHT);
        if (clipImages[i] != null)
          image(clipImages[i], xPos, y, VersionHistoryFrame.ROW_HEIGHT, VersionHistoryFrame.ROW_HEIGHT);
      }

      float width = x + sx;
      float height = y + sy;
      
      if (mouseOverPlayButton()) {
        fill(0,255,0);
      } else {
        fill(255, 255, 50, 150);
      }
      noStroke();
      triangle(width - PLAY_BUTTON_X_OFFSET, height - PLAY_BUTTON_Y_OFFSET, 
          width - PLAY_BUTTON_X_OFFSET, height - PLAY_BUTTON_Y_OFFSET + PLAY_BUTTON_SIZE,
          width - PLAY_BUTTON_X_OFFSET + PLAY_BUTTON_SIZE, height - PLAY_BUTTON_Y_OFFSET + PLAY_BUTTON_SIZE / 2);

      if (mouseOverCloseButton()) {
        stroke(255, 0, 0);
      } else {
        stroke(255, 255, 50, 150);
      }
      strokeWeight(2);
      line(width - STOP_BUTTON_X_OFFSET, height - STOP_BUTTON_Y_OFFSET,
          width - STOP_BUTTON_X_OFFSET + STOP_BUTTON_SIZE,
          height - STOP_BUTTON_Y_OFFSET + STOP_BUTTON_SIZE);
      line(width - STOP_BUTTON_X_OFFSET, height - STOP_BUTTON_Y_OFFSET + STOP_BUTTON_SIZE,
          width - STOP_BUTTON_X_OFFSET + STOP_BUTTON_SIZE,
          height - STOP_BUTTON_Y_OFFSET);
      noStroke();
      
      // Draw outline
      noFill();
      stroke(255);
      strokeWeight(1);
      rect(x, y, sx, sy);
    }
    
    private boolean mouseOverCloseButton() {
      float width = x + sx;
      float height = y + sy;
      
      return ((mouseX > (width - STOP_BUTTON_X_OFFSET)) 
          && (mouseX < (width - STOP_BUTTON_X_OFFSET + STOP_BUTTON_SIZE))
          && (mouseY > (height - STOP_BUTTON_Y_OFFSET))
          && (mouseY < (height - STOP_BUTTON_Y_OFFSET + STOP_BUTTON_SIZE)));
    }
    
    private boolean mouseOverPlayButton() {
      float width = x + sx;
      float height = y + sy;
      
      return (mouseX > width - PLAY_BUTTON_X_OFFSET 
          && mouseX < width - PLAY_BUTTON_X_OFFSET + PLAY_BUTTON_SIZE
          && mouseY > height - PLAY_BUTTON_Y_OFFSET 
          && mouseY < height - PLAY_BUTTON_Y_OFFSET + PLAY_BUTTON_SIZE);
    }
    
    public void setMinimized(boolean minimized) {
      this.minimized = minimized;
      layoutParticles();
      redraw();
    }
    
    public boolean handleMouseClick() {
      if (!isMouseOver()) return false;
      
      if (mouseOverCloseButton()) {
        setMinimized(true);
      } else if (mouseOverPlayButton()) {
        frame.getController().runHistoryCode(histories.get(version).getCode());
      } else if (minimized) {
        setMinimized(false);
      } else {
        marked = !marked;
        this.draw();
      }
      
      return true;
    }
  }

  final float MAX_MOUSE_DIST = 125;
  final int N_PARTICLES = 200;
  
  ArrayList<particle> parts = new ArrayList<particle>();
  Set<Integer> filterVersions;
  //particle[] parts = new particle[N_PARTICLES];
  
  Object lock = new Object();
  
  PFont pfont;
  
  float scrollPos = 0.0f;
 
  boolean scrolling = false;
  
  public void clearVersionFilter() {
    parts.clear();
    parts.addAll(allParts);

    scrollPos = 0.0f;
    layoutParticles();
    redraw();
  }
  
  public void filterMarkedVersions() {
    parts.clear();
    for (int i = 0; i < allParts.size(); i++) {
      particle p = allParts.get(i);
      if (p.marked) parts.add(p);
    }
    
    scrollPos = 0.0f;
    layoutParticles();
    redraw();
  }
  
  @Override
  public void init() {
    super.init();
  }
  
  @Override
  public void setup() {
    size(700, 318);
    noLoop();  
  }
  
  
  @Override
  public void draw() {
    background(0);
    
    for (int i=0; i<parts.size(); i++) {
       parts.get(i).draw();
    }
    
    drawScrollArea();
    scrolling = false;
    
    if (parts.size() > 0) {
      float offset = 0.0f;
      if (mouseY < SCROLL_BAR_HEIGHT) {
        if (parts.get(0).y < SCROLL_BAR_HEIGHT) {
          offset = 5.0f;
          scrolling = true;
        }
      } else if (mouseY > height - SCROLL_BAR_HEIGHT) {
        if (parts.get(parts.size() - 1).y + VersionHistoryFrame.ROW_HEIGHT > height - SCROLL_BAR_HEIGHT) {
          offset = -5.0f;
          scrolling = true;
        }
      }
      for (int i = 0; i < parts.size(); i++) {
        parts.get(i).y += offset;
      }
      scrollPos += offset;
      
      loop();
    } else {
      noLoop();
    }
  }
  
  static final int SCROLL_BAR_HEIGHT = 10;
  
  private void drawScrollArea() {
    noStroke();
    fill(150,150,150,100);
    if (mouseY < SCROLL_BAR_HEIGHT) {
      fill(100,100,0,180);
    }
    rect(0, 0, width, SCROLL_BAR_HEIGHT);
    
    fill(150,150,150,100);
    if (mouseY > height - SCROLL_BAR_HEIGHT) {
      fill(100,100,0,180);
    }
    rect(0, height - SCROLL_BAR_HEIGHT, width, height);
  }
  
  float nextX = X_START_POS;
  float nextY = 0;
  
  static final float X_START_POS = 10.0f;
  
  void layoutParticles() {
    if (parts.size() != 0) {
      nextX = X_START_POS;
      nextY = scrollPos + 10.0f;
      
      for (int i = 0; i < parts.size(); i++) {
        particle p = parts.get(i);
        if (nextX != X_START_POS && 
            nextX + p.getEffectiveWidth() > width) {
          nextX = X_START_POS;
          nextY += VersionHistoryFrame.ROW_HEIGHT + 10;
        }
        
        p.x = nextX;
        p.y = nextY;
        
        nextX += p.getEffectiveWidth() + 10;
      }
    } // End if
  }
  
  @Override
  public void mouseMoved() {
    redraw();
  }
  
  @Override
  public void mouseClicked() {
    for (int i = 0; i < parts.size(); i++) {
      particle p = parts.get(i);
      if (p.handleMouseClick()) break;
    }
  }
}
