package edu.stanford.hci.processing.editor;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class SnapshotModel {

	// TODO: needs thread protection
	static int imageCounter = 0;
	static boolean imageDirectoryMade = false;
	public static final String TEMP_DIRECTORY = "temp/";
	//public static final int MAX_IMAGES = 500;
	
	private BufferedImage image;
	
	// we first write the image out to disk, and only bring it into memory when needed.
	public static final String IMAGE_EXTENSION = "png";
	private File imageLocation;
	private Map<String, String> variableMap;
	private int lineNum;
	
	public SnapshotModel() {
		variableMap = new HashMap<String, String>();
	}
	
	public BufferedImage getImage() {
		if (image == null) {
			try {
				image = ImageIO.read(imageLocation);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return image;
	}
	public void setImage(BufferedImage image) {
		this.image = image;
		
		// We should get another thread that does this
		// this.image will be null after writeImageToDisk() call
		writeImageToDisk();
	}
	public void writeImageToDisk() {
		if (!imageDirectoryMade) {
			File dir = new File("temp/");
			dir.mkdir();
			dir.deleteOnExit();
			imageDirectoryMade = true;
		}
		
		// TODO: we should probably only make X number of these.
		imageCounter++;
		
		imageLocation = new File("temp/" + imageCounter + "." + IMAGE_EXTENSION);
		imageLocation.deleteOnExit();
		try {
			imageLocation.createNewFile();
			ImageIO.write(image, IMAGE_EXTENSION, imageLocation);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		image = null;
	}
	public Map<String, String> getVariableMap() {
		return variableMap;
	}
	
	public void setVariableMap(Map<String, String> variableMap) {
		this.variableMap = variableMap;
	}
	public int getLineNum() {
		return lineNum;
	}
	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}
	
	
}
