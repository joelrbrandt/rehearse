package edu.stanford.hci.processing.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import processing.app.EditorToolbar;
import processing.app.Sketch;

import edu.stanford.hci.processing.RehearseLogger;

public class RehearseSketchUploader {

  private static String UPLOAD_URL = "http://hci.stanford.edu/research/opportunistic/gcafe/upload_file.php";
  
  synchronized public static boolean uploadSketchToServer(final RehearseEditor e) {
    Thread t = new Thread(new Runnable() {
      public void run() {
        boolean exportSuccess = false;
        boolean uploadSuccess = false;
        try {
          System.out.println("\n\n---------\nBeginning export...");
          Sketch sketch = e.getSketch();
          String sketchName = sketch.getName();
          exportSuccess = sketch.exportApplet();
          if (exportSuccess) {
            System.out.println("...export succeeded, beginning upload...");
            File appletFolder = new File(sketch.getFolder(), "applet");
            uploadSuccess = uploadDirectory(appletFolder, UPLOAD_URL, sketchName);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (!exportSuccess) {
          JOptionPane.showMessageDialog(null, "Export failed!", "Error", JOptionPane.WARNING_MESSAGE);
          System.out.println("...export failed, stopping\n---------");
        } else if (!uploadSuccess) {
          JOptionPane.showMessageDialog(null, "Upload failed!", "Error", JOptionPane.WARNING_MESSAGE);
          System.out.println("...upload failed, stopping\n---------");
        } else { // success!
          System.out.println("...done!\n---------");
          JOptionPane.showMessageDialog(null, "Upload complete!", "Upload complete", JOptionPane.INFORMATION_MESSAGE);
        }
        
      }});
    t.start();
    return true;

  }

  private static boolean uploadDirectory(File dir, String url, String name) {
    boolean result = false;
    try {      
      ClientHttpRequest chr = new ClientHttpRequest(url);
      if (dir.isDirectory()) {
        File[] files = dir.listFiles();
        int index = 1;
        for (File f : files) {
          if (f.canRead()) {
            chr.setParameter("file" + index, f);
            index++;
          }
        }
      }
      else if (dir.canRead()) {
        chr.setParameter("file", dir);
      }
      
      // also send along the log
      File logFile = new File(RehearseLogger.getLogFilePath());
      if (logFile.exists() && logFile.canRead()) {
        chr.setParameter("log", logFile);
      }
      
      chr.setParameter("dir", name);
      InputStream is = chr.post();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println(line);
      }
      result = true;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }    
    return result;
  }

}
