package edu.mit.yingyin.tabletop.browser;

// Source code of demo application SimpleBrowser.
import java.awt.*;

import javax.swing.*;

import java.net.URL;
import java.net.MalformedURLException;
import org.jdesktop.jdic.browser.*;

public class BrowserFrame extends JFrame {
	private static final long serialVersionUID = 1558117851587166147L;
	private static final int XPOS = -70;
	private static final int YPOS = 0;
	
	WebBrowser webBrowser; 
	boolean simulate;
	
	public BrowserFrame(boolean simulate) {
		super("JDIC - SimpleBrowser");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		webBrowser = new WebBrowser();
		this.simulate = simulate;
	
	 //Use below code to check the status of the navigation process,
		//or register a listener for the notification events.
		webBrowser.addWebBrowserListener(
				new WebBrowserListener() {                       
					public void downloadStarted(WebBrowserEvent event) {
					}
					public void downloadCompleted(WebBrowserEvent event) {
						System.out.println("download completed");
					}
					public void downloadProgress(WebBrowserEvent event) {;}
					public void downloadError(WebBrowserEvent event) {;}
					public void documentCompleted(WebBrowserEvent event) {
						System.out.println("document completed");
					}
					public void titleChange(WebBrowserEvent event) {;}  
					public void statusTextChange(WebBrowserEvent event) {;}

					@Override
					public void windowClose(WebBrowserEvent arg0) {
						// TODO Auto-generated method stub
					}        
				});

    try {
        webBrowser.setURL(new URL("http://people.csail.mit.edu/yingyin/earth/earth.html"));
    } catch (MalformedURLException e) {
        System.out.println(e.getMessage());
        return;
    }
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    
    panel.setPreferredSize(getScreenSize());
    panel.add(webBrowser, BorderLayout.CENTER);

    getContentPane().add(panel, BorderLayout.CENTER);
    setLocation(XPOS, YPOS);
	}
	
	private Dimension getScreenSize() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		int width = 0;
		int height = 0;
		 
		// Get size of each screen
		if(simulate) {
			DisplayMode dm = gs[0].getDisplayMode();
			width = dm.getWidth();
			height = dm.getHeight();
		}
		else {
			DisplayMode dm = gs[0].getDisplayMode();
			width = dm.getWidth() * 2;
			height = dm.getHeight() * 2;
		}
		return new Dimension(width, height);
	}
	
	public WebBrowser getWebBrowser() {
		return webBrowser;
	}
	
}