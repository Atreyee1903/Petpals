package org.petpals.ui.components;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class ImageLabel extends JLabel {

  public ImageLabel() {
    setHorizontalAlignment(CENTER);
    setVerticalAlignment(CENTER);
  }

  public void loadImage(String basePath, String imageName, int width, int height) {
    if (imageName == null || imageName.trim().isEmpty()) {
      loadDefaultImage(basePath + "default_placeholder.png", width, height);
      return;
    }

    String resourcePath = "/" + basePath.replaceFirst("^resources/", "") + imageName;
    URL imgUrl = getClass().getResource(resourcePath);

    if (imgUrl != null) {
      try {
        ImageIcon icon = new ImageIcon(imgUrl);
        Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        setIcon(new ImageIcon(image));
        setText("");
      } catch (Exception e) {
        System.err.println("Error loading image resource: " + resourcePath + " - " + e.getMessage());
        setText("Img Err");
        setIcon(null);
      }
    } else {
      System.err.println("Image resource not found: " + resourcePath);
      setText("No Img");
      setIcon(null);
    }
  }

  private void loadDefaultImage(String defaultImagePath, int width, int height) {
    String resourcePath = "/" + defaultImagePath.replaceFirst("^resources/", "");
    URL imgUrl = getClass().getResource(resourcePath);
    if (imgUrl != null) {
      try {
        ImageIcon icon = new ImageIcon(imgUrl);
        Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        setIcon(new ImageIcon(image));
      } catch (Exception e) {
        setText("No Def Img");
        setIcon(null);
      }
    } else {
      setText("No Def Img");
      setIcon(null);
    }
  }
}