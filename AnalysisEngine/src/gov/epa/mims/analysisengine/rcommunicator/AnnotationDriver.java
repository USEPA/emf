package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.TextBoxesType;


/**
 * insures that Objects are called to generate all plot commands
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class AnnotationDriver extends Driver
implements
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /**
    * Creates a new Driver object.
    * @param p AnnotationDriver
    ********************************************************/
   public AnnotationDriver(Plot p)
   {
      super(p);

      Cmd cmd = null;

      //
      //Title
      //
      Text t = (Text) p.getOption(PLOT_TITLE);

      if (t != null)
      {
         cmd = new TitleCmd(t);
         rCommandsPostAdd(cmd.getCommands());
      }


      //
      //Sub Title
      //
      t = (Text) p.getOption(PLOT_SUBTITLE);

      if (t != null)
      {
         cmd = new SubTitleCmd(t);
         rCommandsPostAdd(cmd.getCommands());
      }


      //
      //Footer
      //
      t = (Text) p.getOption(PLOT_FOOTER);

      if (t != null)
      {
         cmd = new FooterCmd(t);
         rCommandsPostAdd(cmd.getCommands());
      }

      //
      //Text Boxes
      //
      TextBoxesType textBoxes = (TextBoxesType)p.getOption(TEXT_BOXES);
      if( textBoxes != null )
      {
         for(int i=0;i<textBoxes.getNumTextBoxes();i++)
         {
            cmd = new TextBoxCmd(textBoxes.getTextBox(i));
            rCommandsPostAdd(cmd.getCommands());
         }
      }

      //
      //outline
      //
      Cmd boxCmd = new BoxCmd(p);
      rCommandsPostAdd(boxCmd.getCommands());
   }
}
