package gov.epa.mims.analysisengine.gui;

import java.awt.Color;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * A class to encapsulte java.awt.Colors for copying and pasting.
 * 
 * @author Daniel Gatti
 *
 * @see gov.epa.mims.analysisengine.gui.EditableTablePanel
 * @see gov.epa.mims.analysisengine.gui.ColorEditableTablePanel
 */
public class ColorSelection implements Transferable, ClipboardOwner 
{
   /** The data flavor for a java.awt.Color. */
   public static DataFlavor colorFlavor = null;
   
   /** All of the data flavors that this class supports. */
   protected DataFlavor[] dataFlavors = {colorFlavor};
   
   /** The Color that is stored for transfer. */
   protected Color color = null; 
   
   // Set the data flavor.
   static
   {
     try
     {
        colorFlavor = new DataFlavor(Class.forName("java.awt.Color"), "AWT Color");
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
    * @param color Color that is stored in this TransferHandler.
    */
   public ColorSelection(Color color) 
   {
      this.color = color;  
   }
   

   /**
    * Return the data flavors supported by this class. (java.awt.Color)
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
    * Return true if the given flavor is for java.awt.Color.
    * 
    * @author Daniel Gatti
    * @param queryFlavor DataFlavor to compare to this objects flavors.
    * @return boolean that is true if the passed in flavor is for a java.awt.Color. 
    * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
    */
   public boolean isDataFlavorSupported(DataFlavor queryFlavor) 
   {
      return (queryFlavor == colorFlavor);
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
      if (flavor == colorFlavor)
         return color;
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
} // class ColorSelection

