package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.ReferenceLine;
import gov.epa.mims.analysisengine.tree.Text;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class AxisCmdAnnotated extends AxisCmdBasic
{
   private boolean juxtaposed = false;

   /**
    * Creates a new AxisCmdBasic object.
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdAnnotated(int side, Axis options)
   {
      super(side, options);

      processDriver(side, options);
   }

   /**
    * Creates a new AxisCmdBasic object.
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdAnnotated(int side, Axis options, boolean juxtaposed)
   {
      super(side, options);

      this.juxtaposed = juxtaposed;
      processDriver(side, options);
   }

   /**
    * Creates a new AxisCmdBasic object.
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    * @param atRvariable DOCUMENT_ME
    * @param labelsRvariable DOCUMENT_ME
    ********************************************************/
   public AxisCmdAnnotated(int side, Axis options, String atRvariable, 
      String labelsRvariable)
   {
      super(side, options, atRvariable, labelsRvariable);

      processDriver(side, options);
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    * @param axis DOCUMENT_ME
    ********************************************************/
   private void processDriver(int side, Axis axis)
   {
      if (axis != null)
      {
         if (axis.getAxisLabelText() != null)
         {
            processTextOptions(side, axis.getAxisLabelText());
         }

         if( axis instanceof AxisNumeric)
         {
            AxisNumeric a = (AxisNumeric)axis;
            for (int i = 0; i < a.getNumReferenceLines(); i++)
            {
               processRefLine(a.getReferenceLine(i));
            }
         }
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param ref DOCUMENT_ME
    ********************************************************/
   private void processRefLine(ReferenceLine ref)
   {
      ReferenceLineCmd refCmd = new ReferenceLineCmd(ref);
      rCommandsPostAdd(refCmd.getCommands());
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    * @param text DOCUMENT_ME
    ********************************************************/
   private void processTextOptions(int side, Text text)
   {
      if ((side != 1) && (side != 2))
      {
         throw new IllegalArgumentException(getClass().getName() + " side="
                                            + side);
      }

      double xJust = text.getXJustification();
      double yJust = text.getYJustification();

      String xposCmd = "NULL";
      String yposCmd = "NULL";
      Text2Cmd textCmd = null;

      if (side == 1)
      {
         Text t = Util.textOverRide(text, Text.BOTTOM_HAND_MARGIN, Text.CENTER, 
                                    0.5, 0.5);
         if((Double.isNaN(t.getTextDegreesRotation())) || juxtaposed)
         {
            t.setTextDegreesRotation(0.0);
         }
         textCmd = new Text2Cmd(t);
      }
      else if (side == 2)
      {
         Text t = Util.textOverRide(text, Text.LEFT_HAND_MARGIN, Text.CENTER, 
                                    0.5, 0.5);
         if(Double.isNaN(t.getTextDegreesRotation()) || juxtaposed)
         {
            t.setTextDegreesRotation(-90.0);
         }
         textCmd = new Text2Cmd(t);
      }

      rCommandsPreAdd(xposCmd);
      rCommandsPreAdd(yposCmd);

      rCommandsPostAdd(textCmd.getCommands());
   }
}
