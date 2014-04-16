/*
 * ImageSelection.java
 *
 * Created on November 21, 2003, 4:41 PM
 */

package gov.epa.mims.analysisengine.gui;

import javax.swing.ImageIcon;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * A class to encapsulte javax.swing.ImageIcon for copying and pasting.
 * 
 * @author Daniel Gatti
 *
 * @see gov.epa.mims.analysisengine.gui.EditableTablePanel
 * @see gov.epa.mims.analysisengine.gui.ImageEditableTablePanel
 */

public class ImageSelection implements Transferable, ClipboardOwner
{
   
  /** The data flavor for a javax.swing.ImageIcon. */
   public static DataFlavor imageFlavor = null;
   
   /** All of the data flavors that this class supports. */
   protected DataFlavor[] dataFlavors = {imageFlavor};
   
   /** The ImageIcon that is stored for transfer. */
   protected ImageIcon image = null; 
   
   // Set the data flavor.
   static
   {
     try
     {
       imageFlavor = new DataFlavor(Class.forName("javax.swing.ImageIcon"), "Swing ImageIcon");
     }
     catch (ClassNotFoundException e)
     {
        // The user can't do anyting about this, so just print out an error for 
        // the developer.
        e.printStackTrace();
     }
   } // static


   /**
    * Constructor
    * 
    * @author Daniel Gatti 
    * @param image ImageIcon that is stored in this TransferHandler.
    */
   public ImageSelection(ImageIcon image) 
   {
      this.image = image;  
   }
   

   /**
    * Return the data flavors supported by this class. (javax.swing.ImageIcon)
    * 
    * @author Daniel Gatti
    * @return DataFlavor[] that lists all of the flavors suppoted gy this object.  
    * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
    */
   public DataFlavor[] getTransferDataFlavors() 
   {
      return dataFlavors;
   }


   /**
    * Return true if the given flavor is for javax.swing.ImageIcon.
    * 
    * @author Daniel Gatti
    * @param queryFlavor DataFlavor to compare to this objects flavors.
    * @return boolean that is true if the passed in flavor is for a javax.swing.ImageIcon 
    * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
    */
   public boolean isDataFlavorSupported(DataFlavor queryFlavor) 
   {
      return (queryFlavor == imageFlavor);
   }


   /**
    * Return the transfer data encapsulted in this object. 
    * 
    * @author Daniel Gatti
    * @param flavor DataFlavor to request from this object.
    * @return Object that is the data contained in this object.   
    * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
    */
   public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException 
   {
      if (flavor == imageFlavor)
         return image;
      else
         throw new UnsupportedFlavorException(flavor);
   }
   

   /**
    * Do nothing.
    * 
    * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
    */
   public void lostOwnership(Clipboard arg0, Transferable arg1) 
   {
      /* Nothing */
   }
}

