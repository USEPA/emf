package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.GridType;
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
public class GridCmd implements Serializable, Cloneable,
gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /** DOCUMENT_ME */
   private static ArrayList rCommands = new ArrayList();

   /** DOCUMENT_ME */
   private static ArrayList variables = new ArrayList();

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static List getCommands(Plot p)
   {
      rCommands.clear();
      variables.clear();

      String gridTypeConst = GRID_TYPE;
      GridType gridType = (GridType) p.getOption(gridTypeConst);

      if ((gridType != null) && (gridType.getDraw()))
      {
         rCommands.add("lwd=" + Double.toString(gridType.getLineWidth()));

         variables.add("lty=" + Util.parseLineTypes(gridType.getLineStyle()));
         variables.add("col=" + Util.parseColor(gridType.getColor()));

         String nx = (gridType.getNumberXcells() == -1)
                     ? "NULL"
                     : Integer.toString(gridType.getNumberXcells());
         variables.add("nx=" + nx);

         String ny = (gridType.getNumberYcells() == -1)
                     ? "NULL"
                     : Integer.toString(gridType.getNumberYcells());
         variables.add("ny=" + ny);

         String[] tmp = new String[variables.size()];
         variables.toArray(tmp);

         rCommands.add(Util.buildArrayCommand("grid", tmp));
      }

      if (rCommands.isEmpty())
      {
         rCommands.add("print(\"========No grid command generated========\")");
      }

      return rCommands;
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
