package edu.mit.yingyin.tabletop.handtracking;

import handtracking.camera.WebcamType;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;


import rywang.image.FilterImage.ImageComponent;
import rywang.util.DirectBufferUtils;
import webcam.PointGreyDragonflyDriver;
import yingyin.webcam.IWebcamDriver;
import yingyin.webcam.WebcamDriverFirei;
import yingyin.webcam.WebcamDriverJmf;

import edu.mit.yingyin.tabletop.handtracking.LowLatencyPCUtility.CameraInterface;;

public class Webcam implements CameraInterface {

  private IWebcamDriver driver;
  
  private IntBuffer imageBuf;
  
  private BufferedImage image;
  
  private ImageComponent imageComponent;
  
  private int cameraIndex = 0;
  
  public Webcam(WebcamType camType, int previewWidth, int previewHeight) {
    switch(camType)
    {
      case FIREI:
        driver = new WebcamDriverFirei();
        break;
      case POINT_GREY:
        driver = new PointGreyDragonflyDriver();
        break;
      case JMF:
      	driver = new WebcamDriverJmf();
      	break;
    }
    int width = driver.getWidth();
    int height = driver.getHeight();
    
    imageBuf = DirectBufferUtils.allocateIntBuffer(width * height);

    image = new BufferedImage(previewWidth, previewHeight, BufferedImage.TYPE_INT_ARGB);
    imageComponent = new ImageComponent(image);
  }
  
  public void setCameraIndex(int cameraIndex) {
    this.cameraIndex = cameraIndex;
  }
  
  @Override
  public void captureImage(BufferedImage currentImage) {
    int width = currentImage.getWidth();
    int height = currentImage.getHeight();
    driver.captureNow(imageBuf, width, height);
    imageBuf.rewind();
    int[] rgbArray = ((DataBufferInt) currentImage.getRaster().getDataBuffer()).getData();
    for (int i=0; i<imageBuf.capacity(); i++) {
      rgbArray[i] = imageBuf.get(i) | 0xff000000;
    }
    
    Graphics2D g2d = (Graphics2D) image.getGraphics();
    AffineTransform scale = new AffineTransform();
    scale.scale(image.getWidth() / (float) currentImage.getWidth(), image.getHeight() / (float) currentImage.getHeight());
    g2d.drawImage(currentImage, scale, null);
    g2d.dispose();
    
    imageComponent.repaint();
  }
  
  public IWebcamDriver getDriver() { return driver; }
  
  public ImageComponent getImageComponent() { return imageComponent; }

  @Override
  public void initialize() {
    driver.initialize(cameraIndex);
  }

  @Override
  public int getHeight() {
    return driver.getHeight();
  }

  @Override
  public int getWidth() {
    return driver.getWidth();
  }
}
