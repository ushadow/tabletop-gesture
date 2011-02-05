package edu.mit.yingyin.tabletop.handtracking;

import handtracking.camera.FPSCounter;
import handtracking.processpipeline.ProcessPacket;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import edu.mit.yingyin.tabletop.environment.EnvConstants;

import rywang.util.Timer;

/**
 * Producer consumer framework for camera image processing designed to have low
 * latency
 * 
 * @author rywang
 * 
 */
public class LowLatencyPCUtility {
  
  private boolean done;
  
  public interface CameraInterface {
    public void initialize();
    
    public void captureImage(BufferedImage image);
    
    public int getWidth();
    
    public int getHeight();
  }
  
  public interface ProcessingInterface {
    public void initialize();
    
    public ProcessPacket process(BufferedImage image, int index);
  }
  
  public interface DriverInterface {
    public void initialize();
    
    public void handleDebugPacket(ProcessPacket state, int frame);
  }
  
  public static class NumberedElement<T> implements Comparable<NumberedElement<T>> { 
    
    
    private T object;
    private int index;
    private Timer timer;
    private StringBuffer timerBuffer;
    
    public T getObject() { return object; }

    public Timer getTimer() { return timer; }
    
    public int getIndex() { return index; }
    
    public NumberedElement(T object, int index, Timer timer, StringBuffer timerBuffer) {
      this.index = index;
      this.object = object;
      this.timer = timer;
      this.timerBuffer = timerBuffer;
    }
    
    public void tocln(String what) {
      timerBuffer.append(timer.getTocln(what));
      timerBuffer.append("\r\n");
    }
    
    public StringBuffer getTimerBuffer() {
      return timerBuffer;
    }
    
    @Override
    public int compareTo(NumberedElement<T> o) {
      if (index < o.index) return -1;
      if (index > o.index) return 1;
      return 0;
    } 
  }
  
  public class SynchronizedCamera {
    private CameraInterface cameraInterface;
    
    private int index;
    
    public SynchronizedCamera(CameraInterface cameraInterface) {
      this.cameraInterface = cameraInterface;
      cameraInterface.initialize();
      index = 0;
    }
    
    public synchronized NumberedElement<BufferedImage> fireCamera(BufferedImage image) {
      Timer timer = new Timer();
      cameraInterface.captureImage(image);
      NumberedElement<BufferedImage> ne = new NumberedElement<BufferedImage>(
          image, index++, timer, new StringBuffer());
      //timer.tocln("Received image from camera " + ne.getIndex() + " " + Thread.currentThread().getName());
      return ne;
    }
  }
  
  public class ImageConsumerDebugPacketProducer implements Runnable {

    private BlockingQueue<NumberedElement<ProcessPacket>> skeletonStateQueue;
    
    private ProcessingInterface processor;
    
    private BufferedImage image;
    
    private SynchronizedCamera camera;
    
    public ImageConsumerDebugPacketProducer(SynchronizedCamera camera, ProcessingInterface processor, 
        BlockingQueue<NumberedElement<ProcessPacket>> skeletonStateQueue) {
      this.camera = camera;
      this.processor = processor;
      this.skeletonStateQueue = skeletonStateQueue;
      this.image = new BufferedImage(camera.cameraInterface.getWidth(),
          camera.cameraInterface.getHeight(), BufferedImage.TYPE_INT_ARGB);
      
      processor.initialize();
    }
    
    @Override
    public void run() {
      try {
        while (true) {
          NumberedElement<BufferedImage> imageElement = camera.fireCamera(image);
          NumberedElement<ProcessPacket> ne = new NumberedElement<ProcessPacket>(
              processor.process(
              imageElement.getObject(), imageElement.getIndex()), imageElement
              .getIndex(), imageElement.getTimer(), imageElement.getTimerBuffer());
          skeletonStateQueue.put(ne);
          //ne.tocln("Put on the driver queue " + imageElement.getIndex() + " " + Thread.currentThread().getName());
          if (done) {
            return;
          }
        }
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
  }
  
  public static class DebugPacketConsumer implements Runnable {

  	private static final boolean DEBUG = EnvConstants.BROWSER_DEBUG;
  	private int lastIndex;

  	private BlockingQueue<NumberedElement<ProcessPacket>> skeletonStateQueue;
    private List<NumberedElement<ProcessPacket>> outOfOrderStates;
    private FPSCounter fps;
    private DriverInterface driver;
    
    public DebugPacketConsumer(DriverInterface driver,
        BlockingQueue<NumberedElement<ProcessPacket>> skeletonStateQueue) {
      this.skeletonStateQueue = skeletonStateQueue;
      this.driver = driver;
      outOfOrderStates = new ArrayList<NumberedElement<ProcessPacket>>();
      lastIndex = -1;
      fps = new FPSCounter();
      
      driver.initialize();
    }
    
    @Override
    public void run() {
      try {
        while (true) {
          NumberedElement<ProcessPacket> ne = skeletonStateQueue.take();
          if (ne.getIndex() == lastIndex+1) {
            driver.handleDebugPacket(ne.getObject(), ne.getIndex());
            if (DEBUG)
            	fps.computeFPS();
            lastIndex++;
            for (NumberedElement<ProcessPacket> o : outOfOrderStates) {
              skeletonStateQueue.put(o);
            }
            outOfOrderStates.clear();
          } else {
            outOfOrderStates.add(ne);
          }
        }
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
    }
  }
  
  public <T extends ProcessingInterface> void start(CameraInterface camera,
      List<T> processors, DriverInterface driver) {
    BlockingQueue<NumberedElement<ProcessPacket>> skeletonStateQueue = new PriorityBlockingQueue<NumberedElement<ProcessPacket>>();

    SynchronizedCamera synchronizedCamera = new SynchronizedCamera(camera);
    
    for (ProcessingInterface process : processors) {
      Thread processorThread = new Thread(new ImageConsumerDebugPacketProducer(
          synchronizedCamera, process, skeletonStateQueue));
      //processorThread.setPriority(Thread.MAX_PRIORITY);
      System.out.println("Processing thread priority: " + processorThread.getPriority());
      processorThread.start();
    }
    
    Thread driverThread = new Thread(new DebugPacketConsumer(driver, skeletonStateQueue));
    //driverThread.setPriority(Thread.MAX_PRIORITY);
    System.out.println("Driver thread priority: " + driverThread.getPriority());
    driverThread.start();
  }
  
  public void setDone() { done = true; }
}
