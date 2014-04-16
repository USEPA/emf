
package gov.epa.mims.analysisengine.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
/**
 * Intends to display a image in a panel with a close button to dispose it
 *
 * @author  parthee
 */
public class ImageViewer extends JDialog
{
   /** the image object displayed on the viewer */
   private ImageIcon image;

   /** Creates a new instance of ImageViewer */
   public ImageViewer(JDialog owner, ImageIcon image)
   {
      super(owner);
      this.image = image;
      setTitle("ImageViewer");
      initialize();
      pack();
      setLocation(ScreenUtils.getPointToCenter(this));
   }

   /** Creates a new instance of ImageViewer */
   public ImageViewer(JDialog owner,String title, ImageIcon image)
   {
      super(owner);
      this.image = image;
      setTitle(title);
      initialize();
      pack();
      setLocation(ScreenUtils.getPointToCenter(this));
   }

   private void initialize()
   {
      JLabel iconLabel = new JLabel(image);
      JPanel imagePanel = new JPanel(new BorderLayout());
      imagePanel.setBorder(BorderFactory.createLoweredBevelBorder());
      imagePanel.add(iconLabel);

      JButton closeButton = new JButton("Close");
      closeButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            ImageViewer.this.dispose();
         }
      });

      JPanel buttonPanel = new JPanel();
      buttonPanel.add(closeButton);

      Container container =  getContentPane();
      container.setLayout(new BorderLayout());
      container.add(imagePanel);
      container.add(buttonPanel, BorderLayout.SOUTH);
   }


   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      String path = "D:/TRIM/src/gov/epa/mims/analysisengine/gui/icons/displaySizeHelp.jpeg";
      ImageIcon image = new ImageIcon(path);
      JDialog dialog = new JDialog();

      ImageViewer viewer = new ImageViewer(dialog, image);
      viewer.setVisible(true);
   }

}
