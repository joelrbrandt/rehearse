package edu.stanford.hci.processing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;

import edu.stanford.hci.processing.editor.RehearseEditor;
import edu.stanford.hci.processing.editor.RehearseImageViewer;
import edu.stanford.hci.processing.editor.SnapshotModel;

public class RehearseCanvasFrame extends JFrame {
  
  private final RehearseEditor editor;
  private final RehearsePApplet applet;
  private ArrayList<SnapshotModel> snapshots = new ArrayList<SnapshotModel>();
  
  public RehearseCanvasFrame(final RehearseEditor editor, final RehearsePApplet applet) {
    super();
    this.editor = editor;
    this.applet = applet;
    setLayout(new BorderLayout());
    setSize(100, 100);
    setResizable(false);
    
    JComponent glassPane = new JComponent() {
      public void paint(Graphics g) {
        g.setColor(new Color(0, 0, 0, 125));
        Rectangle bounds = g.getClipBounds();
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
      }
    };
    glassPane.setOpaque(false);
    setGlassPane(glassPane);
    
    add(applet, BorderLayout.CENTER);
    setVisible(true);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        applet.stop();
        editor.setIsInInteractiveRun(false);
        if (snapshots.size() > 0) {
          RehearseImageViewer viewer = new RehearseImageViewer(
              snapshots);
          viewer.setVisible(true);
        }
        editor.logRunFeedback(true);
      }
    });
    
    addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        if (!applet.isVisible()) {
          editor.resumeWithDrawUpdate();
        }
      }
    });
  }
  
  public void addSnapshot(SnapshotModel snapshot) {
    snapshots.add(snapshot);
  }
  
  public void toggleRunSuspendedScreen(boolean runSuspended) {
    this.getGlassPane().setVisible(runSuspended);
    this.applet.setVisible(!runSuspended);
  }
}
