package edu.stanford.hci.processing;

import java.awt.Dimension;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import processing.video.Movie;
//import edu.stanford.hci.processing.VersionHistoryFrameiMovie.VersionHistoryPanel;

public class FishEyeView extends PApplet {

  VersionHistoryFrameFishEye frame;
  BigMovieView bigMovie;

  public FishEyeView() {
    
  }
  
  public void addVersion(VersionHistory vh) {
    // Check if particle is already there
    for (int i=0; i<parts.size(); i++) {
      particle p = parts.get(i);
      if (p.n == vh.getVersion()) {
        p.setVideoFilename(vh.getVideoFilename());
        return;
      }
    }
    
    particle p = new particle(parts.size(), vh.getVideoFilename());
    
    histories.add(vh);
    parts.add(p);
  }
  
  public VersionHistory getVersion(int n) {
    // TODO (Abel): stub
    for (int i=0; i<histories.size(); i++) {
      if (histories.get(i).getVersion() == n) {
        return histories.get(i);
      }
    }
    return null;
  }
  
  public VersionHistory getVersion(String filename) {
    // TODO (Abel): stub
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
    
    float ox;
    float oy;
    
    float sx;
    float sy;
    
    int c;
    int n;
    
    String filename;
    Movie recording;
    
    public final int DEFAULT_SIZE = 15;
    float psize;
    
    public particle(int n, String filename) {
      psize = DEFAULT_SIZE;
      
      this.filename = filename;
      this.n = n;
      
      try {
        recording = new Movie(FishEyeView.this, filename);
        recording.jump((int)(recording.duration()/2.0));
        recording.read();
      } catch (NullPointerException e) {
        recording = null;
      }
      
      c = color(random(0,255), random(0,255), random(0,255));
      
      bigMovie.addRecording(filename);
    }
   
    public void setVideoFilename(String filename) {
      if (filename != null) {
        try {
          recording = new Movie(FishEyeView.this, filename);
          recording.jump((int)(recording.duration()/2.0));
          recording.read();
        } catch (NullPointerException e) {
          recording = null;
        }
      }
    }
    
    public void draw() {
      
      background(0);
      fill(c);
      noStroke();
      
      if (mouseX > (x-(sx*psize)/2.0) && mouseX < (x-(sx*psize)/2.0) + (sx*psize)) {
      
        if (recording != null) {
          float vid_position = (float)(
            (mouseX - (x-(sx*psize)/2.0)) / (sx*psize) * 
            recording.duration()
          );
          recording.jump(vid_position);
          recording.read();
          
          
          bigMovie.setRecordingJump(this.filename, vid_position);
        }
        
        frame.updateCodeArea(this.filename);
      }
      
      double scale = 1;
      
      int recWidth;
      int recHeight;
      
      if (recording.width > recording.height) {
        
        //scale = ((double)recording.width) / psize;
        recWidth = (int)psize;
        recHeight = (int)(psize * (recording.height / recording.width));
      } else if (recording.width < recording.height) {
        
        //scale = ((double)recording.height) / psize;
        recWidth = (int)(psize * (recording.width / recording.height));
        recHeight = (int)psize;
      } else {
        
        recWidth = (int)psize;
        recHeight = (int)psize;
      }
      
      if (recording != null) {
        image(recording,
              (float)(x-(sx*psize)/2.0), 
              (float)(y-(sy*psize)/2.0), 
              (float)(sx*recWidth), 
              (float)(sy*recHeight));
      }
      
      if (mouseX > (x-(sx*psize)/2.0) && mouseX < (x-(sx*psize)/2.0) + (sx*psize)) {
        noFill();
        stroke(255);
        rect((float)(x-(sx*psize)/2.0), 
             (float)(y-(sy*psize)/2.0), 
             (float)(sx*recWidth), 
             (float)(sy*recHeight)
        );
      }
      
      if (this.sx > 1.05) {
        //fill(255);
        //text("r: "+this.n, 
        //    (float)(x-(sx*psize)/2.0), 
        //    (float)(y+(sy*psize)/2.0), 60, 30);
      }
    } 
  }

  final float MAX_MOUSE_DIST = 125;
  final int N_PARTICLES = 200;
  
  ArrayList<VersionHistory> histories = new ArrayList<VersionHistory>();
  ArrayList<particle> parts = new ArrayList<particle>();
  //particle[] parts = new particle[N_PARTICLES];
  
  Object lock = new Object();
  
  PFont pfont;
  
  @Override
  public void init() {
    super.init();
    
    //System.out.println("Fisheye init");
  }
  
  @Override
  public void setup() {
   
    //println("hiya");
    
    size(700, 200);
    
    noLoop();  
  }
  
  
  @Override
  public void draw() {
    
    background(0);
    
    fill(255,0,0);
    //ellipse(mouseX, mouseY, MAX_MOUSE_DIST*2, MAX_MOUSE_DIST*2);
    for (int i=0; i<parts.size(); i++) {
       parts.get(i).draw();
    }
    
    //System.out.println("fisheye draw");
  }
  
  float nextX = 0;
  boolean layingOut = false;
  void layoutParticles() {
    
    if (parts.size() != 0) {
      int mx = mouseX;
      int my = mouseY;
      
      //println("" + mx + " " + my);
      
      nextX = (float)50.0;
      float totalX = (float)0.0;
      
      float dim = (width - 100) / parts.size();
      float pscale = (float)(1.0 / ( dim / 200.0 ) / 2.0);
      
      for (int i=0; i<parts.size(); i++) {
        
        parts.get(i).psize = dim;
        
        float xdist = (float)Math.sqrt(
          Math.pow(Math.abs(parts.get(i).x - mx),2) 
          + Math.pow(Math.abs(parts.get(i).y - my),2)
        );
        
        float capped_dist = xdist > MAX_MOUSE_DIST ? MAX_MOUSE_DIST : xdist;
        float norm_dist = (float)(1.0 - (capped_dist / MAX_MOUSE_DIST));
        
        float scale_amt = (float)(1.0 + 
          (3.0*Math.pow(norm_dist,2) - 2.0*Math.pow(norm_dist,3)) * pscale);
          //(float)1.0 + (float)Math.min(5.0, (1.0 / Math.log((1.0+10*xdist)))));
        
        //xdist = (xdist > 2.0 ? 2.0 : xdist);
        
        //if (i == 5) { println(scale_amt); }
        
        parts.get(i).sx = scale_amt;
        parts.get(i).sy = scale_amt;
        //parts.get(i).sx = 1.0f;
        //parts.get(i).sy = 1.0f;
        parts.get(i).x = nextX;
        parts.get(i).y = height/2;
        
        totalX += parts.get(i).sx * parts.get(i).psize + 10;
        
      }
      totalX -= 10; // to account for last bit added
      
      nextX = ((float)mx / (float)width) * (width - totalX);
      
      //println("total: " + totalX + " nextX: " + nextX);
      
      for (int i=0; i<parts.size(); i++) {
        parts.get(i).x = (float)(nextX + (parts.get(i).sx * parts.get(i).psize) / 2.0);
        nextX = nextX + (parts.get(i).sx * parts.get(i).psize) + 10; 
      }
      
      redraw();
    }
  }
  
  @Override
  public void mouseMoved() {
     layoutParticles();
     
     
  }

}
