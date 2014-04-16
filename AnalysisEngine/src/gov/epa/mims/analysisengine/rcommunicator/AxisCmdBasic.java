package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class AxisCmdBasic extends AxisCmd implements 
gov.epa.mims.analysisengine.tree.FontConstantsIfc
{
   /** DOCUMENT_ME */
   private String atRvariable = null;

   /** DOCUMENT_ME */
   private String labelsRvariable = null;

   /**
    * Creates a new AxisCmdBasic object.
    *
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdBasic(int side, Axis options)
   {
      super(side);

      if (options != null)
      {
         processOptions(options);
      }

//      if (options instanceof AxisLog)
//      {
//         String coordinate = ((side % 2) == 0)
//                             ? "\"y\""
//                             : "\"x\"";
//         variableAdd("log", coordinate);
//      }

      if((options instanceof AxisNumeric)
      &&(((AxisNumeric)options).getLogScale()))
      {
         String coordinate = ((side % 2) == 0)
                             ? "\"y\""
                             : "\"x\"";
         variableAdd("log", coordinate);
      }
   }

   /**
    * Creates a new AxisCmdBasic object.
    *
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    * @param atRvariable DOCUMENT_ME
    * @param labelsRvariable DOCUMENT_ME
    ********************************************************/
   public AxisCmdBasic(int side, Axis options, String atRvariable, 
      String labelsRvariable)
   {
      super(side);

      this.atRvariable = atRvariable;
      this.labelsRvariable = labelsRvariable;

      if (options != null)
      {
         processOptions(options);
      }

//      if (options instanceof AxisLog)
//      {
//         String coordinate = ((side % 2) == 0)
//                             ? "\"y\""
//                             : "\"x\"";
//         variableAdd("log", coordinate);
//      }

      if((options instanceof AxisNumeric)
      &&(((AxisNumeric)options).getLogScale()))
      {
         String coordinate = ((side % 2) == 0)
                             ? "\"y\""
                             : "\"x\"";
         variableAdd("log", coordinate);
      }

   }

   /**
    * DOCUMENT_ME
    *
    * @param opts DOCUMENT_ME
    ********************************************************/
   private void processOptions(Axis opts)
   {
      // how to handle getDrawOpposingAxis() ????
      //enable the command
//      setIssueCommand(opts.getDrawAxis());
      setIssueCommand(opts.getEnableAxis());

      //enable drawing of tick marks
      if (!opts.getDrawTickMarks())
      {
         variableAdd("tick", "FALSE");
      }

      //tick mark labels
      if (opts.getDrawTickMarkLabels())
      {
         processTickMarkLabels(opts);
      }
      else
      {
         variableAdd("labels", "FALSE");
      }

//      //position of axis
//      //      if (opts.getPositionSetByUser())
//      if (!Double.isNaN(opts.getPosition()))
//      {
//         variableAdd("pos", Double.toString(opts.getPosition()));
//      }
//      else
//      {
//         variableAdd("line", Double.toString(opts.getLinesIntoMargin()));
//      }

      int algor = opts.getPositioningAlgorithm();
      if(algor == Axis.DEFAULT_POSITIONING)
      {
      }
      else if(algor == Axis.USER_COORDINATES)
      {
         variableAdd("pos", Double.toString(opts.getPosition()));
      }
      else if(algor == Axis.LINES_INTO_MARGIN)
      {
         variableAdd("line", Double.toString(opts.getPosition()));
      }
      else
      {
         throw new IllegalArgumentException(getClass().getName()
         + " unknown positioning algorithm: " + algor);
      }

      //axis color
      if (opts.getAxisColor() != null)
      {
         rCommandsPreAdd("originalFG <- par(\"fg\")");
         rCommandsPreAdd("par(\"fg\"=" + Util.parseColor(opts.getAxisColor())
                         + ")");
         rCommandsPostAdd("par(\"fg\" = originalFG )");
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param opts DOCUMENT_ME
    ********************************************************/
   private void processTickMarkLabels(Axis opts)
   {
      //get user specified labels
      processLabels(opts.getUserTickMarkPositions(), 
                    opts.getUserTickMarkLabels());

      //orientation of axis labels
      if (opts.getDrawTickMarkLabelsPerpendicularToAxis())
      {
         variableAdd("las", "2");
      }
      else
      {
         variableAdd("las", "0");
      }

      //tick mark color
      if (opts.getTickMarkLabelColor() != null)
      {
         variableAdd("col.axis", Util.parseColor(opts.getTickMarkLabelColor()));
      }

      //tick mark font
      if (opts.getTickMarkFont() != null)
      {
         String f = opts.getTickMarkFont();

         if (f.equals(PLAIN_TEXT))
         {
            variableAdd("font", "1");
         }
         else if (f.equals(BOLD_TEXT))
         {
            variableAdd("font", "2");
         }
         else if (f.equals(ITALIC_TEXT))
         {
            variableAdd("font", "3");
         }
         else if (f.equals(BOLD_ITALIC_TEXT))
         {
            variableAdd("font", "4");
         }
         else
         {
            throw new IllegalArgumentException(getClass().getName()
                                               + " unknown font=" + f);
         }
      }

      //tick mark expansion
      double cexAxis = opts.getTickMarkLabelExpansion();

      if (cexAxis > 0.0)
      {
         variableAdd("cex.axis", Double.toString(cexAxis));
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param at DOCUMENT_ME
    * @param labels DOCUMENT_ME
    * @pre (((atRvariable==null)&&(labelsRvariable==null))||
    * ((atRvariable==null)&&(labelsRvariable!=null)))
    ********************************************************/
   private void processLabels(double[] at, String[] labels)
   {
      if (atRvariable == null)
      {
         if ((at != null) && (labels != null))
         {
            if ((((at == null) && (labels != null))
                      || ((at != null) && (labels == null)))
                   && (at.length != labels.length))
            {
               String msg = getClass().getName()
                            + " setUserTickMarkPositions(double[])";
               msg += " and setUserTickMarkLabels(String[]) must both be ";
               msg += "of the same length or not set at all";
               throw new IllegalArgumentException(msg);
            }
            else
            {
               variableAdd("labels", 
                           Util.buildArrayCommand("c", 
                                                  Util.escapeQuote(labels)));
               variableAdd("at", Util.buildArrayCommand("c", at));
            }
         }
      }
      else
      {
         variableAdd("labels", labelsRvariable);
         variableAdd("at", atRvariable);
      }
   }
}
