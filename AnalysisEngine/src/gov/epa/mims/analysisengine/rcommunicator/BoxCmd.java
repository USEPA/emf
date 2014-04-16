package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.OutlineType;
import gov.epa.mims.analysisengine.tree.Plot;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class BoxCmd extends Cmd implements Serializable, Cloneable,
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /** DOCUMENT_ME */
   private static ArrayList rCommands = new ArrayList();

   /** DOCUMENT_ME */
   private static ArrayList boxVariables = new ArrayList();

   public BoxCmd(Plot p)
   {
      super();
      super.setName("box");

      OutlineType outlineType = (OutlineType) p.getOption(
                                      OUTLINE_TYPE);

      if (outlineType != null)
      {
         for(int i=0;i<OutlineType.TYPES.length;++i)
         {
            int type = OutlineType.TYPES[i];

            if(outlineType.getDraw(type))
            {
               String col = Util.parseColor(outlineType.getColor(type));
               variableAdd("col", col);

               String lty = Util.parseLineTypes(outlineType.getLineStyle(type));
               variableAdd("lty", lty);

               String lwd = Double.toString(outlineType.getLineWidth(type));
               variableAdd("lwd", lwd);

               String which = null;
               if(type == outlineType.FIGURE)
               {
                  which = "figure";
               }
               else if(type == outlineType.PLOT)
               {
                  which = "plot";
               }
               else if(type == outlineType.INNER)
               {
                  which = "inner";
               }
               else if(type == outlineType.OUTER)
               {
                  which = "outer";
               }
               else
               {
                  throw new IllegalArgumentException(getClass().getName() + " unknown type");
               }
               variableAdd("which="+Util.escapeQuote(which));

               super.generateAsPreCommand();
               super.clearAllVariables();

            }
         }
      }
   }

   /**
    * describe object in a String
    *
    * @return String describing object
    ******************************************************/
   public String toString()
   {
      return Util.toString(this);
   }
}
