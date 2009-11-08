package edu.stanford.hci.processing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

public class VersionHistoryFrame extends JFrame {
  
  private static final int ROW_HEIGHT = 120;
  private static final Color selectedColor = new Color(150,255,150);
  
  private static final Border hoverBorder = BorderFactory.createLineBorder(Color.green);
  private static final Border normalBorder = BorderFactory.createLineBorder(Color.black);
  
  private final VersionHistoryController controller;
  
  private JPanel rootPanel;
  private ArrayList<VersionHistoryPanel> versionPanels;
  
  
  public VersionHistoryFrame(final VersionHistoryController controller) {
    super("Version History");
    this.controller = controller;
    this.versionPanels = new ArrayList<VersionHistoryPanel>();
    
    rootPanel = new JPanel();
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.PAGE_AXIS));
    JScrollPane scrollPane = new JScrollPane(rootPanel);
    add(scrollPane, BorderLayout.CENTER);
    
    setPreferredSize(new Dimension(700, 500));
  }
  
  public void addVersionHistory(VersionHistory vh) {
    VersionHistoryPanel panel = new VersionHistoryPanel(vh);
    rootPanel.add(panel);
    rootPanel.add(Box.createRigidArea(new Dimension(10, 5)));
    versionPanels.add(panel);
    validate();
  }
  
  public void lastRunningVersionChanged(int oldIndex, int newIndex) {
    if (oldIndex != -1) {
      versionPanels.get(oldIndex).setBackground(Color.white);
    }
    if (newIndex != -1) {
      versionPanels.get(newIndex).setBackground(selectedColor);
    }
  }
  
  public void updateScreenshot(int index, Image screenshot) {
    VersionHistoryPanel panel = versionPanels.get(index);
    VersionHistory model = panel.getModel();
    model.setScreenshot(screenshot);
    panel.setModel(model);
    repaint();
  }
  
  public class VersionHistoryPanel extends JPanel {
    private VersionHistory model;
    private JLabel screenshot;
    private JTextArea codeTextArea;
    private JLabel timeLabel;
    
    
    public VersionHistoryPanel(VersionHistory model) {
      super();
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      setMaximumSize(new Dimension(700, ROW_HEIGHT + 20));
      
      setBackground(Color.white);
      setBorder(BorderFactory.createLineBorder(Color.black));
      
      screenshot = new JLabel();
      add(screenshot);
      
      codeTextArea = new JTextArea();
      JScrollPane scrollPane = new JScrollPane(codeTextArea);
      scrollPane.setPreferredSize(new Dimension(400, ROW_HEIGHT));
      add(scrollPane);
      
      timeLabel = new JLabel();
      add(timeLabel);
      
      add(Box.createHorizontalGlue());
      
      setModel(model);
      
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          setBorder(hoverBorder);
        }
        
        @Override
        public void mouseExited(MouseEvent e) {
          setBorder(normalBorder);
        }
        
        @Override
        public void mouseClicked(MouseEvent e) { 
          controller.swapRunningCode(versionPanels.indexOf(e.getSource()));
        }
      });
    }
    
    public void setModel(VersionHistory model) {
      this.model = model;
      screenshot.setIcon(makeScaledImageIcon(model.getScreenshot()));
      codeTextArea.setText(model.getCode());
      int caretPos = Math.min(codeTextArea.getText().indexOf("void draw")+40, 
          codeTextArea.getText().length() - 1);
      codeTextArea.setCaretPosition(caretPos);
      timeLabel.setText(DateFormat.getTimeInstance().format(model.getTime()));
      validate();
    }
    
    private ImageIcon makeScaledImageIcon(Image image) {
      int width = 
        (int)(ROW_HEIGHT * (image.getWidth(null) / (double)image.getHeight(null)));
      return new ImageIcon(image.getScaledInstance(width, ROW_HEIGHT, Image.SCALE_DEFAULT));
    }
    
    public VersionHistory getModel() {
      return model;
    }
  }
}
