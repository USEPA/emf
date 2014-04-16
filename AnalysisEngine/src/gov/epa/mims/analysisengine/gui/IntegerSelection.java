package gov.epa.mims.analysisengine.gui;

import java.awt.datatransfer.*;
import java.io.IOException;

/**
 * A class to encapsulte java.awt.Integers for copying and pasting.
 * 
 * @author Daniel Gatti
 *
 * @see gov.epa.mims.analysisengine.gui.EditableTablePanel
 * @see gov.epa.mims.analysisengine.gui.IntegerEditableTablePanel
 */
public class IntegerSelection 
   implements ClipboardOwner, Transferable 
{
   /** The data flavor for a java.lang.Integer. */
   public static DataFlavor integerFlavor = null;
   
   /** All of the data flavors that this class supports. */
   protected DataFlavor[] dataFlavors = {integerFlavor};
   
   /** The Integer that is stored for transfer. */
   protected Integer integer = null; 
   
   // Set the data flavor.
   static
   {
        try
        {
         integerFlavor = new DataFlavor(Class.forName("java.lang.Integer"), "Java Integer");
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
    * @param integer Integer that is stored in this TransferHandler. 
    */
   public IntegerSelection(Integer integer) 
   {
      this.integer = integer;
   }
   

   /**
    * Return the data flavors supported by this class. (java.lang.Integer)
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
    * Return true if the given flavor is for java.lang.Integer.
    * 
    * @author Daniel Gatti
    * @param queryFlavor DataFlavor to compare to this objects flavors.
    * @return boolean that is true if the passed in flavor is for a java.lang.Integer. 
    * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
    */
   public boolean isDataFlavorSupported(DataFlavor queryFlavor) 
   {
      return (queryFlavor == integerFlavor);
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
        if (flavor == integerFlavor)
           return integer;
        else
           throw new UnsupportedFlavorException(flavor);
     }
     
     
   /**
    * Do nothing.
    * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
    */
   public void lostOwnership(Clipboard arg0, Transferable arg1) 
   {
      /* Nothing */
   }
} // class IntegerSelection

