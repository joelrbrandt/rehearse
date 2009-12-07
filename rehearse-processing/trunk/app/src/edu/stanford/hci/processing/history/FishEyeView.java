package edu.stanford.hci.processing.history;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Set;


import processing.core.PApplet;
import processing.core.PFont;
import processing.video.Movie;
//import edu.stanford.hci.processing.VersionHistoryFrameiMovie.VersionHistoryPanel;

public class FishEyeView extends PApplet {

  VersionHistoryFrame frame;
  BigMovieView bigMovie;
  particle selected = null; 
  particle hovered = null;
  boolean viewLocked = false;
  
  public FishEyeView() {
    
  }
  
  public void addVersion(VersionHistory vh) {
    // Check if particle is already there
    for (int i=0; i<parts.size(); i++) {
      particle p = parts.get(i);
      if (p.version == vh.getVersion()) {
        println("Updating video");
        p.setVideoFilename(vh.getVideoFilename());
        return;
      }
    }
    
    //println("Creating new particle");
    particle p = new particle(parts.size(), vh.getVideoFilename());
    
    histories.add(vh);
    parts.add(p);
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
    
    float ox;
    float oy;
    
    float sx;
    float sy;
    
    int c;
    int version;
    
    boolean isSelected;
    boolean isHovered;
    
    String filename;
    Movie recording;
    
    public final int DEFAULT_SIZE = 15;
    float psize;
    
    public particle(int n, String filename) {
      psize = DEFAULT_SIZE;
      
      this.filename = filename;
      this.version = n;
      this.isSelected = false;
      
      try {
        recording = new Movie(FishEyeView.this, filename);
        recording.jump((int)(recording.duration()/2.0));
        recording.read();
      } catch (NullPointerException e) {
        println("Video not found.  Hopefully it's the current version");
        recording = null;
      }
      
      c = color(random(0,255), random(0,255), random(0,255));
      
      bigMovie.addRecording(filename);
    }
   
    public void setSelected(boolean selected) {
      this.isSelected = selected;
    }
    
    public void setHover(boolean hovered) {
      this.isHovered = hovered;
    }
    
    public void setVideoFilename(String filename) {
      if (filename != null) {
        try {
          recording = new Movie(FishEyeView.this, filename);
          recording.jump((int)(recording.duration()/2.0));
          recording.read();
          this.filename = filename;
        } catch (NullPointerException e) {
          recording = null;
        }
      }
    }
    
    public void draw() {
      
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
          
          if (!viewLocked) {
            bigMovie.setRecordingJump(this.filename, vid_position);
          }
        }
        
        // TODO (Abel): change this to call the proper method
        if (!viewLocked) {
          frame.setVersionNumber(this.version);
        }
        //frame.updateCodeArea(this.filename);
      }
      
      double scale = 1;
      
      int recWidth;
      int recHeight;
      
      if (recording != null && recording.width > recording.height) {
        
        //scale = ((double)recording.width) / psize;
        recWidth = (int)psize;
        recHeight = (int)(psize * (recording.height / recording.width));
      } else if (recording != null && recording.width < recording.height) {
        
        //scale = ((double)recording.height) / psize;
        recWidth = (int)(psize * (recording.width / recording.height));
        recHeight = (int)psize;
      } else {
        
        recWidth = (int)psize;
        recHeight = (int)psize;
      }
      
      if (filterVersions != null && filterVersions.contains(version)) {
        fill(255, 255, 0, 50);
        rect((float)(x - (sx*psize)/2.0), 0, sx*recWidth, height);
      }
      
      if (recording != null) {
        image(recording,
              (float)(x-(sx*psize)/2.0), 
              (float)(y-(sy*psize)/2.0), 
              (float)(sx*recWidth), 
              (float)(sy*recHeight));
        
      }
      
      //if (mouseX > (x-(sx*psize)/2.0) && mouseX < (x-(sx*psize)/2.0) + (sx*psize)) {
      if (isSelected) {
        stroke(0, 255, 0);
        noFill();
        rect((float)(x-(sx*psize)/2.0), 
             (float)(y-(sy*psize)/2.0), 
             (float)(sx*recWidth), 
             (float)(sy*recHeight)
        );
      } else if (isHovered) {
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
  Set<Integer> filterVersions;
  //particle[] parts = new particle[N_PARTICLES];
  
  Object lock = new Object();
  
  PFont pfont;
  
  public void setVersionFilter(Set<Integer> versions) {
    filterVersions = versions;
    redraw();
  }
  
  public void clearVersionFilter() {
    filterVersions = null;
    redraw();
  }
  
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
      
      // Dimension of non-magnified version
      float dim = Math.min( (width - 100) / parts.size(), 100);
      
      // Max magnification scale amount
      float pscale = (float)(1.0 / ( dim / 200.0 ) / 2.0);
      
      if (dim > 99.0) {
        float totalWidth = (parts.size() * dim) + ((parts.size()-1) * 10);
        nextX = (float)((width/2.0) - (totalWidth/2.0) + (dim/2.0));
        for (int i=0; i<parts.size(); i++) {
          particle p = parts.get(i);
          p.psize = dim;
          p.sx = 1.0f;
          p.sy = 1.0f;
          p.x = nextX;
          p.y = height/2;
          nextX+=(p.sx * p.psize) + 10; 
          
        }
      } else {
      
        for (int i=0; i<parts.size(); i++) {
          
          parts.get(i).psize = dim;
          
          float xdist = (float)Math.sqrt(
            Math.pow(Math.abs(parts.get(i).x - mx),2) 
            //+ Math.pow(Math.abs(parts.get(i).y - my),2)
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
      
      } // End if
      
      redraw();
    }
  }
  
  @Override
  public void mouseMoved() {
    if (!viewLocked) {
      layoutParticles();
    }
    
    if (hovered != null) {
      hovered.setHover(false);
    }
    
    for (int i=0; i<parts.size(); i++) {
      particle p = parts.get(i);
      if (mouseX > (p.x-(p.sx*p.psize)/2.0) && 
          mouseX < (p.x-(p.sx*p.psize)/2.0) + (p.sx*p.psize)) {
        if (p.isSelected) {
          
        } else {
          
          p.setHover(true);
          hovered = p;
          
        }
      }
        
    }
    redraw();
  }

  @Override
  public void mouseClicked() {
    for (int i=0; i<parts.size(); i++) {
      particle p = parts.get(i);
      if (mouseX > (p.x-(p.sx*p.psize)/2.0) && 
          mouseX < (p.x-(p.sx*p.psize)/2.0) + (p.sx*p.psize)) {
        
        if (viewLocked) {
          selected.setSelected(false);
          selected = null;
          viewLocked = false;
        } else {
          p.setSelected(true);
          selected = p;
          viewLocked = true;
        }
        layoutParticles();
        /*
        if (p.isSelected) {
          p.setSelected(false);
          viewLocked = false;
          selected = null;
        } else {
          if (selected != null) {
            selected.setSelected(false);
          }
          
          p.setSelected(true);
          
          selected = p;
          viewLocked = true;
          
          layoutParticles();
        }
        */
      }
        
    }
  }
  
}
