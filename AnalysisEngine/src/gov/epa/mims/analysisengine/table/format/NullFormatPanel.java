package gov.epa.mims.analysisengine.table.format;


import java.text.Format;
import javax.swing.JLabel;

/**
 * <p>Description: A panel to display when there is no format to dispaly
 * for a column. This occurs when we don't have a date or a number. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: UNC - CEP</p>
 * @author Daniel Gatti
 * @version $Id: NullFormatPanel.java,v 1.1 2006/11/01 15:33:38 parthee Exp $
 */
public class NullFormatPanel extends FormatChooserPanel
{
   public NullFormatPanel()
   {
      super(FormattedCellRenderer.nullFormatter, null, null, false);
      removeAll();
      add(new JLabel("No Common Format to Diaplay"));
   } // NullFormatPanel()


   /**
    * Create a new formatter with the given format String and add it to the
    * hashtable of existing formatters. Then return it.
    * @return Format that has just been created.
    */
   protected Format createNewFormat(String formatString)
   {
      return FormattedCellRenderer.nullFormatter;
   } // createNewFormat()


   /**
    * If the requested formatter has been created before, then return it.
    * Otherwise make a new one, add it to the hashtable and return it.
    * This has the effect of only creating each formatter once. Although it
    * means that we keep unused ones around once we're done with them, too.
    */
   public Format getSelectedFormat()
   {
      return null;
   } // getSelectedFormat()


   /**
    * Set the current format.
    */
   public void setFormat(Format newFormat)
   {
      /* Nothing */
   }

   /**
    * blank implementation
    * @param
    * @return null
    */
   public String splitToGetSecondString(StringBuffer formatString)
   {
      return null;
   }

} // class NullFormatPanel

