/*
 * BooleanSelection.java
 *
 * Created on December 8, 2003, 2:22 PM
 */

package gov.epa.mims.analysisengine.gui;
import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * A class to encapsulte javax.lang.Boolean for copying and pasting.
 * 
 * @author Daniel Gatti
 *
 * @see gov.epa.mims.analysisengine.gui.BooleanTableColumn
 * @see gov.epa.mims.analysisengine.gui.SpecialTableColumn
 * @see gov.epa.mims.analysisengine.gui.MultiEditableTablePanel
 */
public class BooleanSelection implements Transferable, ClipboardOwner
{
   
   /** The data flavor for a java.lang.Boolean. */
   public static DataFlavor booleanFlavor = null;
   
   /** All of the data flavors that this class supports. */
   protected DataFlavor[] dataFlavors = {booleanFlavor};
   
   /** The Boolean that is stored for transfer. */
   protected Boolean booleanValue = null; 
   
   // Set the data flavor.
   static
   {
      try
      {
         booleanFlavor = new DataFlavor(Class.forName("java.lang.Boolean"), "Java Boolean");
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
    * @param value Boolean that is stored in this TransferHandler.
    */
   public BooleanSelection(Boolean value) 
   {
      this.booleanValue = value;  
   }
   

   /**
    * Return the data flavors supported by this class. (java.lang.Boolean)
    * 
    * @author Daniel Gatti
    * @return DataFlavor[] that lists all of the flavors suppoted by this object.  
    * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
    */
   public DataFlavor[] getTransferDataFlavors() 
   {
      return dataFlavors;
   }


   /**
    * Return true if the given flavor is for java.lang.Boolean.
    * 
    * @author Daniel Gatti
    * @param queryFlavor DataFlavor to compare to this objects flavors.
    * @return boolean that is true if the passed in flavor is for a java.lang.Boolean 
    * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
    */
   public boolean isDataFlavorSupported(DataFlavor queryFlavor) 
   {
      return (queryFlavor == booleanFlavor);
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
      if (flavor == booleanFlavor)
         return booleanValue;
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

