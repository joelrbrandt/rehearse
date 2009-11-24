package edu.stanford.hci.processing;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;

import processing.core.PApplet;
import processing.video.Movie;
import edu.stanford.hci.processing.VersionHistoryFrameiMovie.VersionHistoryPanel;

public class RecordingView extends PApplet {

	private static final int SEC_PER_SEGMENT = 1;
	private static final int PLAY_BUTTON_SIZE = 15;
	private static final int PLAY_BUTTON_X_OFFSET = 25;
	private static final int PLAY_BUTTON_Y_OFFSET = 25;
	
	private static final int STOP_BUTTON_SIZE = 15;
	private static final int STOP_BUTTON_X_OFFSET = PLAY_BUTTON_X_OFFSET * 2;
	private static final int STOP_BUTTON_Y_OFFSET = PLAY_BUTTON_Y_OFFSET;

	// highly suspect coding practices, 
	// in the name of prototyping
	BigMovieView bigMovie;
	VersionHistoryFrameiMovie frame;

	private String recordingFilename;
	private Movie recording;

	private boolean setup_done = false;
	private int initialFrameCount = 0;
	static final private int INITIAL_FRAME_COUNT_MAX = 100;

	private VersionHistoryPanel vhp;
	private float jumpTime;
	private int numSegments;
	private float jumpTimes[];
	
	private boolean isMarked;

	public RecordingView(String recordingFilename, VersionHistoryPanel vhp) {
		this(recordingFilename);
		this.vhp = vhp;
	}

	public RecordingView(String recordingFilename) {
		System.out.println("Creating Recording view for: " + recordingFilename);
		this.recordingFilename = recordingFilename;
		isMarked = false;
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

		setAllSizes(VersionHistoryFrame.ROW_HEIGHT * numSegments, VersionHistoryFrame.ROW_HEIGHT);
	}

	private void setAllSizes(int newWidth, int newHeight) {
		size(newWidth, newHeight, P2D);
		setVHPSizes(newWidth, newHeight);
	}
	
	private void setVHPSizes(int newWidth, int newHeight) {
		if (vhp != null) {
			vhp.setPreferredSize(new Dimension(newWidth, newHeight));
			vhp.setMaximumSize(new Dimension(newWidth, newHeight));
			vhp.setMinimumSize(new Dimension(newWidth, newHeight));
			vhp.revalidate();
		}
	}
	
	@Override
	public void draw() {
		if (vhp.getPreferredSize().width == CLOSED_WIDTH) return;
		
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

			if (mouseOverCloseButton()) {
				fill(255, 0, 0);
			} else {
				fill(230, 230, 230);
			}
			rect(width - STOP_BUTTON_X_OFFSET, height - STOP_BUTTON_Y_OFFSET, STOP_BUTTON_SIZE, STOP_BUTTON_SIZE);
		}

		flush();
	}

	public boolean isMarked() {
		return isMarked;
	}
	
	private boolean mouseOverCloseButton() {
		return ((mouseX > (width - STOP_BUTTON_X_OFFSET)) 
				&& (mouseX < (width - STOP_BUTTON_X_OFFSET + STOP_BUTTON_SIZE))
				&& (mouseY > (height - STOP_BUTTON_Y_OFFSET))
				&& (mouseY < (height - STOP_BUTTON_Y_OFFSET + STOP_BUTTON_SIZE)));
	}
	
	private boolean mouseOverPlayButton() {
		return (mouseX > width - PLAY_BUTTON_X_OFFSET 
				&& mouseX < width - PLAY_BUTTON_X_OFFSET + PLAY_BUTTON_SIZE
				&& mouseY > height - PLAY_BUTTON_Y_OFFSET 
				&& mouseY < height - PLAY_BUTTON_Y_OFFSET + PLAY_BUTTON_SIZE);
	}

	@Override
	public void mouseMoved() {
		if (vhp.getPreferredSize().width == CLOSED_WIDTH) return;
		
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

	private static final int CLOSED_WIDTH = 10;
	@Override
	public void mouseClicked() {
		if (vhp.getPreferredSize().width == CLOSED_WIDTH) {
			setVHPSizes(VersionHistoryFrame.ROW_HEIGHT * numSegments, VersionHistoryFrameiMovie.ROW_HEIGHT);
			redraw();
		} else {
			if (mouseOverPlayButton()) {
				frame.getController().runHistoryCode(vhp.getModel().getCode());
			} else if (mouseOverCloseButton()){
				setVHPSizes(CLOSED_WIDTH, VersionHistoryFrameiMovie.ROW_HEIGHT);
			} else {
				isMarked = !isMarked;
				if (isMarked) {
					vhp.setBorder(BorderFactory.createLineBorder(Color.yellow, 3));
				} else {
					vhp.setBorder(BorderFactory.createLineBorder(Color.black, 3));
				}
			}
		}
	}
}
