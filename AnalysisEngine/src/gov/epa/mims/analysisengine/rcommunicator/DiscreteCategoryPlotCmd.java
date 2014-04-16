package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.DiscreteCategoryPlot;
import gov.epa.mims.analysisengine.tree.LineType;


/**
 * generate a DiscreteCategoryPlot R command
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class DiscreteCategoryPlotCmd extends MatplotCmd
{
   /**
    * Creates a new DiscreteCategoryPlotCmd object.
    * @param p a DiscreteCategoryPlot 
    ********************************************************/
   public DiscreteCategoryPlotCmd(DiscreteCategoryPlot p)
   {
      super(p);
      processDataInfo(p);
   }

   /**
    * process data sets in order to create x and y vectors for
    * the R matplot command
    *
    * @param p DiscreteCategoryPlot
    ********************************************************/
   private void processDataInfo(DiscreteCategoryPlot p)
   {
      //retrieve the xKeys and yKeys
      //
//      String[] xKeys = p.getKeys(0);
      String[] xKeys = null;
//      String[] yKeys = Rvariable.getName(p.getKeys(1));
      String[] yKeys = Rvariable.getName(p.getKeys(0));

      if ((yKeys == null) || (yKeys.length == 0))
      {
         throw new IllegalArgumentException("no yKeys found");
      }

      //allocate space to hold R variables; x and y must be the same length
      //
      String[] x = new String[yKeys.length];
      String[] y = new String[yKeys.length];

      if (xKeys == null)
      {
         //there is no X data set; R commands must be written in order
         //to generate an X data set for use in the matplot command
         String[] xTmp = new String[y.length];

         //generate x & y variables for R matplot command; also set up
         //xTmp[] to prepare for generating pre-commands
         for (int i = 0; i < y.length; i++)
         {
            xTmp[i] = "as.character(d" + (i + 1) + "Label)";
            x[i] = "tmpfX[match(d" + (i + 1) + "Label,tmpfL)]";
            y[i] = "d" + (i + 1);
         }


         //
         //Below is a example of how the X data set is generated in R
         //
         //
         //> plotData
         //         d1 d1Label         d3 d3Label         d2 d2Label
         //1 0.2116827   Water 0.42275520   Water 0.22942489   Water
         //2 0.6825683    Soil 0.69413894   Water 0.07428316   Water
         //3 0.4378744     Air 0.08172102   Water 0.86908953    Soil
         //4        NA    <NA> 0.76830554    Soil 0.44727469    Soil
         //5        NA    <NA> 0.52954581    Soil 0.49667391     Air
         //6        NA    <NA> 0.77412404    Soil 0.69246887     Air
         //7        NA    <NA> 0.68776324     Air         NA    <NA>
         //8        NA    <NA> 0.93085035     Air         NA    <NA>
         //9        NA    <NA> 0.79778775     Air         NA    <NA>
         //> tmpfL<-levels(factor(c(as.character(d1Label),
         //            as.character(d2Label),as.character(d3Label))))
         //> tmpfL
         //[1] "Air"   "Soil"  "Water"
         //> x<-c(1:length(tmpfL))
         //> x
         //[1] 1 2 3
         //> x<-c(x,rep(NA,length.out=length(d1)-length(x)))
         //> x
         //[1]  1  2  3 NA NA NA NA NA NA
         //> tmpfX<-c(1:length(tmpfL))
         //> tmpfX
         //[1] 1 2 3
         //> tmpfX[match(d1Label,tmpfL)]
         //[1]  3  2  1 NA NA NA NA NA NA
         //> tmpfX[match(d2Label,tmpfL)]
         //[1]  3  3  2  2  1  1 NA NA NA
         //> tmpfX[match(d3Label,tmpfL)]
         //[1] 3 3 3 2 2 2 1 1 1
         //
         //example matplot commands:
         //matplot(add=FALSE,ann=FALSE,axes=FALSE,type="n",
         //     x=c(tmpfX[match(d1Label,tmpfL)],tmpfX[match(d2Label,tmpfL)],
         //     tmpfX[match(d3Label,tmpfL)]),y=c(d1,d2,d3))
         //matplot(add=TRUE,ann=FALSE,axes=FALSE,type="p",
         //     x=tmpfX[match(d1Label,tmpfL)],y=d1,col="#ff0000",lty=1,
         //     lwd=1.0,pch=1,cex=1.0)
         //matplot(add=TRUE,ann=FALSE,axes=FALSE,type="p",
         //     x=tmpfX[match(d2Label,tmpfL)],y=d2,col="#0000ff",lty=2,
         //     lwd=1.0,pch=2,cex=1.0)
         //matplot(add=TRUE,ann=FALSE,axes=FALSE,type="p",
         //     x=tmpfX[match(d3Label,tmpfL)],y=d3,col="#ff0000",lty=3,
         //     lwd=1.0,pch=3,cex=1.0)
         //
         rCommandsPreAdd("tmpfL<-levels(factor("
                         + Util.buildArrayCommand("c", xTmp) + "))");
         rCommandsPreAdd("x<-c(1:length(tmpfL))");
         rCommandsPreAdd("x<-c(x,rep(NA,length.out=length(d1)-length(x)))");
         rCommandsPreAdd("tmpfX<-c(1:length(tmpfL))");
      }
      else
      {
         //there is an X data set, "d1"; Everything must be plotted according
         //to "d1" used as X; illustrative example is below:
         //
         //> plotData
         //         d2 d2Label        d4 d4Label d1 d1Label          d3 d3Label
         //1 0.7404081   Water 0.4393493   Water  0   Water 0.638183889   Water
         //2 0.7262432    Soil 0.7654535   Water  1    Soil 0.177519607   Water
         //3 0.7257218     Air 0.4935395   Water  2     Air 0.824553282    Soil
         //4        NA    <NA> 0.1049500    Soil NA    <NA> 0.252672297    Soil
         //5        NA    <NA> 0.1859632    Soil NA    <NA> 0.002516817     Air
         //6        NA    <NA> 0.4781371    Soil NA    <NA> 0.140163503     Air
         //7        NA    <NA> 0.8222344     Air NA    <NA>          NA    <NA>
         //8        NA    <NA> 0.5724883     Air NA    <NA>          NA    <NA>
         //9        NA    <NA> 0.5802000     Air NA    <NA>          NA    <NA>
         //> d1[match(d2Label,d1Label)]
         //[1]  0  1  2 NA NA NA NA NA NA
         //> d1[match(d3Label,d1Label)]
         //[1]  0  0  1  1  2  2 NA NA NA
         //> d1[match(d4Label,d1Label)]
         //[1] 0 0 0 1 1 1 2 2 2
         //> 
         //example matplot commands are:
         //matplot(add=FALSE,ann=FALSE,axes=FALSE,type="n",
         //     x=c(d1[match(d2Label,d1Label)],d1[match(d3Label,d1Label)],
         //     d1[match(d4Label,d1Label)]),y=c(d2,d3,d4))
         //matplot(add=TRUE,ann=FALSE,axes=FALSE,type="p",
         //     x=d1[match(d2Label,d1Label)],y=d2,col="#ff0000",lty=1,
         //     lwd=1.0,pch=1,cex=1.0)
         //matplot(add=TRUE,ann=FALSE,axes=FALSE,type="p",
         //     x=d1[match(d3Label,d1Label)],y=d3,col="#0000ff",lty=2,
         //     lwd=1.0,pch=2,cex=1.0)
         //matplot(add=TRUE,ann=FALSE,axes=FALSE,type="p",
         //     x=d1[match(d4Label,d1Label)],y=d4,col="#ff0000",lty=3,
         //     lwd=1.0,pch=3,cex=1.0)
         //
         for (int i = 0; i < y.length; i++)
         {
            x[i] = "d1[match(d" + (i + 2) + "Label,d1Label)]";
            y[i] = "d" + (i + 2);
         }
      }

      super.setX(x);
      super.setY(y);
   }
}
