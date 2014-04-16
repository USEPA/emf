package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Text;

import java.awt.Color;

import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class TitleCmd extends TextCmd
{
   /**
    * Creates a new TitleCmd object.
    * @param side DOCUMENT_ME
    ********************************************************/
   public TitleCmd(Text t)
   {
      super(t);
   }

   /**
    * return list of commands
    ********************************************************/
   public List getCommands()
   {
      Text t = super.getText();
      t = Util.textOverRide(t, Text.TOP_HAND_MARGIN, Text.NORTH, 0.5, 0.5);
      super.setText(t);

      List l = super.getCommands();

      return l;
   }

   /**
    * DOCUMENT_ME
    *
    * @param args DOCUMENT_ME
    ********************************************************/
   public static void main(String[] args)
   {
      TitleCmd titleCmd;
      Text text;

      text = new Text();
      text.setColor(java.awt.Color.green);
      text.setPosition(Text.LEFT_HAND_MARGIN, text.NORTH, 1.0, 3.0);
      text.setPosition(91.4, 35.06);
      text.setTextExpansion(0.1);
      text.setTextDegreesRotation(0);
      text.setTypeface("bold");
      text.setStyle("times");
      text.setTextString("my text string");

      titleCmd = new TitleCmd(text);

      java.util.ArrayList l;
      l = (java.util.ArrayList) titleCmd.getCommands();

      for (int i = 0; i < l.size(); ++i)
      {
         System.out.println(l.get(i));
      }
   }
}
