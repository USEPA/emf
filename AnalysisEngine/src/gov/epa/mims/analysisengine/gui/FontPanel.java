package gov.epa.mims.analysisengine.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * <p>Description: A panel that displays and allows editing of font
 *    characteristics. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: UNC - CEP</p>
 * @author Daniel Gatti
 * @version $Id: FontPanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class FontPanel extends JPanel
{
   /** The Font that is being edited.*/
   protected Font font = null;

   /** A list of all font in the system. */
   protected static final String[] allFonts =
         GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

   /** The list of Font names in the GUI. */
   protected JList fontList = null;

   /** The list of font styles. */
   protected JList styleList = null;

   /** The list of font sizes. */
   protected JList sizeList = null;

   /** The converter for font styles to numbers. */
   protected static final Hashtable styleConverter = new Hashtable();

   public static final String PLAIN      = "Plain";
   public static final String BOLD       = "Bold";
   public static final String ITALIC     = "Italic";
   public static final String BOLDITALIC = "Bold & Italic";

   static
   {
      styleConverter.put(PLAIN,      Integer.valueOf(Font.PLAIN));
      styleConverter.put(BOLD,       Integer.valueOf(Font.BOLD));
      styleConverter.put(ITALIC,     Integer.valueOf(Font.ITALIC));
      styleConverter.put(BOLDITALIC, Integer.valueOf(Font.BOLD|Font.ITALIC));
   } // static

   /**
    * Constructor.
    * @param font Font to be displayed and edited.
    */
   public FontPanel(Font font)
   {
      fontList = new JList(allFonts);
      JScrollPane fontPanel = new JScrollPane(fontList,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      fontPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Font", TitledBorder.LEFT,
            TitledBorder.TOP));

      styleList = new JList(new String[] {PLAIN, BOLD, ITALIC, BOLDITALIC});
      styleList.setFixedCellWidth(100);
      JScrollPane stylePanel = new JScrollPane(styleList,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      stylePanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(), "Style", TitledBorder.LEFT,
            TitledBorder.TOP));

      Integer[] sizes = new Integer[36];
      for (int i = 1; i <= 36; i++)
      {
         sizes[i-1] = Integer.valueOf(i);
      }

      sizeList = new JList(sizes);
      sizeList.setFixedCellWidth(100);
      JScrollPane sizePanel = new JScrollPane(sizeList,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      sizePanel.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(), "Size", TitledBorder.LEFT,
            TitledBorder.TOP));

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      add(fontPanel);
      add(stylePanel);
      add(sizePanel);

      initGUIFromModel();
   } // FontPanel()


   /**
    * Populate the GUI.
    */
   protected void initGUIFromModel()
   {
      if (font != null)
      {
         fontList.setSelectedValue(font.getName(), true);
         switch (font.getStyle())
         {
            case Font.BOLD:
               styleList.setSelectedValue(BOLD, true);
               break;
            case Font.ITALIC:
               styleList.setSelectedValue(ITALIC, true);
               break;
            case (Font.BOLD | Font.ITALIC):
               styleList.setSelectedValue(BOLDITALIC, true);
               break;
            default:
               styleList.setSelectedValue(PLAIN, true);
         } // switch (font.getStyle())

         sizeList.setSelectedValue(Integer.valueOf(font.getSize()), true);
      }
      else
      {
         fontList.setSelectedIndex(0);
         styleList.setSelectedIndex(0);
         sizeList.setSelectedValue(Integer.valueOf(12), true);
      }
   } // initGUIFromModel()


   /**
    * Return the Font from this panel.
    */
   public Font getFontValue()
   {
      // Font Name
      String fontName = (String)fontList.getSelectedValue();
      if (fontName == null)
      {
         fontName = "Serif";
      }

      // Style
      Object obj = styleList.getSelectedValue();
      int style = Font.PLAIN;
      if(obj != null)
      {
         Integer I = (Integer)styleConverter.get(obj);
         if (I != null)
         {
            style = I.intValue();
         }
      }
      // Size
      Integer I = (Integer)sizeList.getSelectedValue();
      int size = 12;
      if (I != null)
      {
         size = I.intValue();
      }

      font = new Font(fontName, style, size);
      return font;
   } // getFontValue()



   /**
    * Set the font value for this panel and display it in the GUI.
    */
   public void setFontValue(Font font)
   {
      this.font = font;
      initGUIFromModel();
   } // setFontValue()
} // class FontPanel
