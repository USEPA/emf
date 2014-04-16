package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.BarType;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;
import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.ScatterPlot;

import java.util.ArrayList;
import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 */
public class LegendCmdBarPlot extends LegendCmd implements java.io.Serializable
{
   private boolean NEW_CODE = true;
   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public LegendCmdBarPlot(Legend l, BarType bt, String[] keys, 
      LabeledDataSetIfc[] ds)
   {
      super(l);

      String legend = "NULL";

      if (bt == null)
      {
         legend = useDataSetNamesAsLabels(keys, ds);
      }

      /*
      *      if (!bt.getStacked())
      *      {
      *         legend = useLabeledDataSetIfc(keys);
      *      }
      *      else if (bt.getCategoriesSpanDataSets())
      *      {
      *         legend = useDataSetNamesAsLabels(keys, ds);
      *      }
      *      else
      *      {
      *         legend = useLabeledDataSetIfc(keys);
      *      }
      */
      else if (bt.getCategoriesSpanDataSets())
      {
if(Boolean.getBoolean("NEW_CODE") != NEW_CODE)
{
   NEW_CODE = !NEW_CODE;
}
//if(NEW_CODE)
//{
//         legend = "BarPlotColumnNames()";
//}
//else
//{
         legend = useDataSetNamesAsLabels(keys, ds);
//}
      }
      else
      {
if(NEW_CODE)
{
         legend = "BarPlotDataLabels()";
}
else
{
         legend = useLabeledDataSetIfc(keys);
}
      }

      variableAdd("legend", legend);
      variableAdd("fill", fill(bt));
   }

   /**
    * Creates a new LegendCmdBarPlot object.
    *
    * @param l DOCUMENT_ME
    * @param bt DOCUMENT_ME
    * @param keys DOCUMENT_ME
    * @param ds DOCUMENT_ME
    ********************************************************/
   public LegendCmdBarPlot(Legend l, BarType bt, String[] keys, 
      DataSetIfc[] ds)
   {
      super(l);

      String legend = "NULL";

      if (!bt.getCategoriesSpanDataSets())
      {
         legend = useElementIndexAsLabels();
      }
      else
      {
         legend = useDataSetNamesAsLabels(keys, ds);
      }

      variableAdd("legend", legend);
      variableAdd("fill", fill(bt));
   }

   /**
    * DOCUMENT_ME
    *
    * @param bt DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String fill(BarType bt)
   {
      return Util.buildArrayCommand("c", Util.parseColors(bt.getColor()));
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String useElementIndexAsLabels()
   {
      int nElements = BarPlotAxisHelper.getMaxNumElements();
      String[] stringElements = new String[nElements];

      for (int i = 0; i < nElements; i++)
      {
         stringElements[i] = "" + i;
      }

      return Util.buildArrayCommand("c", stringElements);
   }

   /**
    * DOCUMENT_ME
    *
    * @param keys DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String useLabeledDataSetIfc(String[] keys)
   {
      String[] y = Rvariable.getName(keys);

      return "c(as.character(" + y[0] + "Label" + "))";
   }

   /**
    * DOCUMENT_ME
    *
    * @param keys DOCUMENT_ME
    * @param ds DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String useDataSetNamesAsLabels(String[] keys, DataSetIfc[] ds)
   {
      String[] labels = new String[keys.length];

      for (int i = 0; i < labels.length; ++i)
      {
         labels[i] = Util.escapeQuote(ds[i].getName());
      }

      return Util.buildArrayCommand("c", labels);
   }
}
