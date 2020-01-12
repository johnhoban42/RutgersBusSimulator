package apps;

import javax.swing.*; 
import java.io.*; 
import java.awt.*; 
import java.awt.image.*; 
import javax.imageio.*; 

// Modified JPanel to display the map image
public class ImagePanel extends JPanel {

	public BufferedImage image; 
	  
	public ImagePanel(String src) { 
		super(); 
		try {                
			image = ImageIO.read(new File(src)); 
		}catch(IOException e) { 
			e.printStackTrace(); 
		} 
	}

	public void paintComponent(Graphics g) { 
		g.drawImage(image, 0, 0, null); 
	} 
	
}
