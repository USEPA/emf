package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.BarPlot;
import gov.epa.mims.analysisengine.tree.BarType;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;

import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class BarPlotAxisHelper
{
   /** DOCUMENT_ME */
   private static int numCategories;

   /** DOCUMENT_ME */
   private static int maxNumElements;

   /** DOCUMENT_ME */
   private static boolean stacked;

   /** DOCUMENT_ME */
   private static boolean labeledDataSetIfcFlag;

   /** DOCUMENT_ME */
   private static boolean categoriesSpanDataSets = false;

   /** DOCUMENT_ME */
   private static String at;

   /** DOCUMENT_ME */
   private static String labels;

   /** DOCUMENT_ME */
   private static List keyList;

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    ********************************************************/
   public static void init(BarPlot p)
   {
      maxNumElements = 0;
      keyList = p.getDataKeyList();
      numCategories = keyList.size();
      findMaxNumElements(p);

      //      BarType barType = (BarType) p.getOption(BAR_TYPE);
      BarType barType = Option.getBarType(p);
      stacked = barType.getStacked();
      labeledDataSetIfcFlag = false;

      Object key = keyList.get(0);
      DataSetIfc ds = (DataSetIfc) p.getDataSet(key);

      if (ds instanceof LabeledDataSetIfc)
      {
         labeledDataSetIfcFlag = true;
      }

      categoriesSpanDataSets = false;

      if (barType.getCategoriesSpanDataSets())
      {
         categoriesSpanDataSets = true;
      }

      at = findAt();
      labels = findLabels(p);
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String getAt()
   {
      return at;
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static String getLabels()
   {
      return labels;
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static int getNumCategories()
   {
      return numCategories;
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static int getMaxNumElements()
   {
      return maxNumElements;
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    ********************************************************/
   private static void findMaxNumElements(BarPlot p)
   {
      try
      {
         for (int i = 0; i < keyList.size(); ++i)
         {
            Object key = keyList.get(i);
            DataSetIfc ds = (DataSetIfc) p.getDataSet(key);
            ds.open();

            int numEle = ds.getNumElements();
            maxNumElements = Math.max(maxNumElements, numEle);
            ds.close();
         }
      }
      catch (java.lang.Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static String findAtOLD()
   {
      String rtrn = null;

      if ((numCategories == 1) || stacked)
      {
         rtrn = "c(mx)";
      }
      else
      {
         String[] midPts = new String[numCategories];

         for (int i = 0; i < numCategories; i++)
         {
            //            midPts[i] = mxMidPt(i+1,1,i+1,maxNumElements);
            midPts[i] = mxMidPt(1, i + 1, maxNumElements, i + 1);
         }

         rtrn = Util.buildArrayCommand("c", midPts);

         /*
          *       rtrn = "c((mx[dim(mx)[1],1] + mx[1,1])/2";
          *       for (int i = 2; i <= numCategories; i++)
          *       {
          *          rtrn += (",(mx[dim(mx)[1]," + i + "] + mx[1," + i + "])/2");
          *       }
          *       rtrn += ")";
          */
      }

      return rtrn;
   }

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static String findAt()
   {
      String rtrn = null;

      String[] midPts = new String[1];

      if (categoriesSpanDataSets)
      {
         midPts = new String[maxNumElements];

         for (int i = 0; i < maxNumElements; i++)
         {
            if (stacked)
            {
               int loc = i + 1;
               midPts[i] = "mx[" + loc + "]";
            }
            else
            {
               midPts[i] = mxMidPt(1, i + 1, numCategories, i + 1);
            }
         }
      }
      else
      {
         if ((numCategories == 1) && stacked)
         {
            midPts[0] = "mx[1]";
         }
         else
         {
            midPts = new String[numCategories];

            for (int i = 0; i < numCategories; i++)
            {
               if (stacked)
               {
                  int loc = i + 1;
                  midPts[i] = "mx[" + loc + "]";
               }
               else
               {
                  midPts[i] = mxMidPt(1, i + 1, maxNumElements, i + 1);
               }
            }
         }
      }

      rtrn = Util.buildArrayCommand("c", midPts);

      return rtrn;
   }

   /**
    * DOCUMENT_ME
    *
    * @param x1 DOCUMENT_ME
    * @param y1 DOCUMENT_ME
    * @param x2 DOCUMENT_ME
    * @param y2 DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static String mxMidPt(int x1, int y1, int x2, int y2)
   {
      return "(mx[" + x1 + "," + y1 + "]+mx[" + x2 + "," + y2 + "])/2";
   }

   /**
    * DOCUMENT_ME
    *
    * @param p DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private static String findLabels(BarPlot p)
   {
      String rtrn = null;
      BarType barType = Option.getBarType(p);

      if (!barType.getCategoriesSpanDataSets())
      {
         if (labeledDataSetIfcFlag)
         {
            String[] labelsArray = new String[numCategories];

            for (int i = 0; i < numCategories; ++i)
            {
               Object key = keyList.get(i);
               DataSetIfc ds = (DataSetIfc) p.getDataSet(key);
               labelsArray[i] = Util.escapeQuote(ds.getName());
            }

            rtrn = Util.buildArrayCommand("c", labelsArray);
         }
         else
         {
            String[] labelsArray = new String[numCategories];

            for (int i = 0; i < numCategories; ++i)
            {
               Object key = keyList.get(i);
               DataSetIfc ds = (DataSetIfc) p.getDataSet(key);
               labelsArray[i] = Util.escapeQuote(ds.getName());
            }

            rtrn = Util.buildArrayCommand("c", labelsArray);
         }
      }
      else
      {
         if (labeledDataSetIfcFlag)
         {
            //
            // convert the data key into an R variable
            //
            String[] y = Rvariable.getName(p.getKeys(0));


            //
            // add the key word "Label" to the newly converted
            // R variable name to produce the R variable name holding
            // the desired data labels
            // NOTE: "Label" should probably be a java constant
            //
            //rtrn = "c(as.character(" + y[0] + "Label" + "))";
            rtrn = "c(as.character(lx[,1]))";
         }
         else
         {
            String[] stringElements = new String[getMaxNumElements()];

            for (int i = 0; i < getMaxNumElements(); i++)
            {
               stringElements[i] = "" + i;
            }

            rtrn = Util.buildArrayCommand("c", stringElements);
         }
      }

      return rtrn;
   }
}
