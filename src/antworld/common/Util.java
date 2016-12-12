package antworld.common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class Util
{
  /**
   * Loads a image file with the given path into a new bufferedImage. Blocks
   * until the image has finished loading. widit is the component on which the
   * images will eventually be drawn.
   * 
   * @return A buffered image containing the loaded image.
   */
  public static BufferedImage loadImage(String imagePath, Container widgit)
  {
    if (imagePath == null) return null;

    if (widgit == null)
    {
      widgit = new Container();
    }

    // Create a MediaTracker instance, to montior loading of images
    MediaTracker tracker = new MediaTracker(widgit);

    BufferedImage loadedImage = null;
    URL fileURL = null;

    try
    { // System.out.println("imagePath="+imagePath);

      imagePath = "resources/" + imagePath;
      fileURL = new URL("file:" + imagePath);

      loadedImage = ImageIO.read(fileURL);

      // Register it with media tracker
      tracker.addImage(loadedImage, 1);
      tracker.waitForAll();
    }
    catch (Exception e)
    {
      System.out.println("Cannot Open image: " + imagePath);
      e.printStackTrace();
      System.exit(0);
    }
    return loadedImage;
  }
  
  
  
  public static int manhattanDistance(int x1, int y1, int x2, int y2)
  {
    int dx = Math.abs(x2 - x1);
    int dy = Math.abs(y2 - y1);
    return dx + dy;
  }
  
  
  public static String arrayToString(int[] array)
  {
	String str = "[";
	for (int x : array)
	{
	  str += x + ", ";	
	}
	return str + "]";
  
  }
}
