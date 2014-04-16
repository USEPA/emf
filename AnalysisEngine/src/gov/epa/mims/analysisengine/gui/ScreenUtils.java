package gov.epa.mims.analysisengine.gui;

/**
 * <p>Title: ScreenUtils </p>
 * <p>Description: Utilities for positioning windows on the screen</p>
 * @author Alison Eyth
 * @version $Id: ScreenUtils.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 *
 */
import java.awt.*;

public class ScreenUtils
{
  /**
  *
  *  To center a dialog:
         pack();
         setLocation(ScreenUtils.getPointToCenter(this));
  * @param frame wanted to center
  * @return Point that will put frame at center

 */
  public static Point getPointToCenter(Component comp)
  {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
    if (comp == null)
    {
       return new Point((int)screenSize.getWidth()/2,(int)screenSize.getHeight()/2);
       //return new Point(400,300);
   }
    Dimension frameSize = comp.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
//System.out.println("frameSize.width=" + frameSize.width + " frameSize.height=" + frameSize.height);    
//System.out.println("screenSize.width=" + screenSize.width + " screenSize.height=" + screenSize.height);
    return new Point( (screenSize.width - frameSize.width) / 2,
                      (screenSize.height - frameSize.height) / 2);
  }



  /**
   * Assign a cascaded location for a window</p>
   *
   * To cascade windows (e.g. open different DAVE databases):</p>
   *
   * pack();
   * lastLocation = ScreenUtils.getCascadedLocation(this, lastLocation, deltaX, deltaY);
   * setLocation(lastLocation);
   *
   * where lastLocation is a static variable for the class.
   * @param comp Component to get size for
   * @param lastLocation  Point for previous location
   * @param deltaX int amount to adjust X
   * @param deltaY int amount to adjust Y
   * @return Point for new location
   */
  public static Point getCascadedLocation(
     Component comp, Point lastLocation, int deltaX, int deltaY)
  {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    if (comp == null)
    {
       lastLocation.translate(deltaX,deltaY);
       return lastLocation;
    }
    Dimension compSize = comp.getSize();
    double compWidth = compSize.getWidth();
    double compHeight = compSize.getHeight();
    int newX = lastLocation.x + deltaX;
    int newY = lastLocation.y + deltaY;
    /* if we've gone off the edge of the screen, start back at 0 */
    if ((newX + compWidth) > screenSize.getWidth())
    {
      newX = deltaX;
      newY = deltaY;
    }
    if ((newY + compHeight) > screenSize.getHeight())
    {
      newX = deltaX;
      newY = deltaY;
    }
    return new Point(newX, newY);
  }
}