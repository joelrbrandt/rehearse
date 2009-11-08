package edu.stanford.hci.processing;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;

public class VersionHistoryIO {
  
  private static final String HISTORY_TXT_FILE_NAME = "history.txt";
  private static final String HISTORY_IMAGE_FORMAT = "png";
  private static final String VERSION_DELIMITER = "#_#_#";
  private static final String FIELD_DELIMITER = "@_@_@";
  
  private File historyFolder;
  private BufferedWriter txtWriter;
  private int nextIndex;
  
  public VersionHistoryIO(File sketchFolder) {
    historyFolder = new File(sketchFolder, "history");
    if (!historyFolder.exists()) {
      historyFolder.mkdir();
    }
    
    try {
      File outputFile = new File(historyFolder, HISTORY_TXT_FILE_NAME);
      if(!outputFile.exists()){
        outputFile.createNewFile();
        nextIndex = 0;
      }
      txtWriter = new BufferedWriter(new FileWriter(outputFile, true));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public ArrayList<VersionHistory> loadHistory() {
    ArrayList<VersionHistory> history = new ArrayList<VersionHistory>();
    File file = new File(historyFolder, HISTORY_TXT_FILE_NAME);
    if (!file.exists()) return history;
    
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      StringBuilder fileData = new StringBuilder(1000);
      char[] buf = new char[1024];
      int numRead=0;
      while((numRead=reader.read(buf)) != -1){
          fileData.append(buf, 0, numRead);
      }
      reader.close();
      
      String[] versions = fileData.toString().split(VERSION_DELIMITER);
      for (String version : versions) {
        if (version.length() > 0) {
          String[] fields = version.split(FIELD_DELIMITER);
        
          String imageFileName = fields[0] + "." + HISTORY_IMAGE_FORMAT;
          Image image = ImageIO.read(new File(historyFolder, imageFileName));
          Date date = new Date(Long.parseLong(fields[2]));
        
          VersionHistory vh = new VersionHistory(image, fields[1], date);
          history.add(vh);
        }
      }
      
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    nextIndex = history.size();
    return history;
  }

  public void appendHistory(VersionHistory history) {
    try {
      String imageFileName = nextIndex + "." + HISTORY_IMAGE_FORMAT;
      saveImage(new File(historyFolder, imageFileName), history.getScreenshot());
      
      txtWriter.write(nextIndex + "");
      txtWriter.write(FIELD_DELIMITER);
      txtWriter.write(history.getCode());
      txtWriter.write(FIELD_DELIMITER);
      txtWriter.write(history.getTime().getTime() + "");
      txtWriter.write(VERSION_DELIMITER);
      txtWriter.flush();
      
      nextIndex++;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void updateImage(int index, Image image) {
    String imageFileName = index + "." + HISTORY_IMAGE_FORMAT;
    saveImage(new File(historyFolder, imageFileName), image);
  }
  
  private void saveImage(File file, Image image) {
    try {
      BufferedImage bi = (BufferedImage)image;
      ImageIO.write(bi, HISTORY_IMAGE_FORMAT, file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void finalize() {
    try {
      txtWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
