package UI;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ScalingBufferedImage{
	
	
	public static BufferedImage scaleBufferedImage(BufferedImage srcImage, double scale){
		
		int w = (int)(srcImage.getWidth()*scale);
		int h = (int)(srcImage.getHeight()*scale);
		BufferedImage resizedImage = new BufferedImage(w,h , BufferedImage.TRANSLUCENT);
		Graphics2D g2 = resizedImage.createGraphics();
		
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//The Picture is scaled by that method if necessary
	    g2.drawImage(srcImage, 0, 0, w, h, null);
	    g2.dispose();
	
		return resizedImage;
	}
	

}
