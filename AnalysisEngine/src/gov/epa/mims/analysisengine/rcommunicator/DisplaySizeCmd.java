package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.DisplaySizeType;


/**
 * generate a DisplaySizeDrv R command
 * <br>
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class DisplaySizeCmd extends Cmd
{
   public DisplaySizeCmd(DisplaySizeType o)
   {
      super();

      if((o != null) && (o.getEnable()))
      {
         Cmd c = null;

         c = new ParPltCmd(o.getPlot(),o.getPlotUnits());
         rCommandsPreAdd(c.getCommands());

         c = new ParFigCmd(o.getFigure());
         rCommandsPreAdd(c.getCommands());

         c = new ParOmiCmd(o.getMarginOuter(),o.getMarginOuterUnits());
         rCommandsPreAdd(c.getCommands());

      }
   }

   public static void main(String[] args)
   {
      java.util.List l = null;

      DisplaySizeType o = new DisplaySizeType();
      o.setEnable(true);
      o.setFigure(0.1, 0.9, 0.1, 0.9);
//      o.setFigure(double, double);
//      o.setFigure([D);
      o.setPlot(.05, .95, .05, .95, DisplaySizeType.FOF);
//      o.setPlot(double, double);
//      o.setPlot([D, int);
      o.setMarginOuter(0.05, 0.05, 0.05, 0.05, DisplaySizeType.NDC);
//      o.setMarginOuter([D, int);

      DisplaySizeCmd cmd = new DisplaySizeCmd(o);

      l = cmd.getCommands();
      System.out.println("x11()");
      for(int i=0;i<l.size();i++)
      {
         System.out.println(l.get(i));
      }
      System.out.println("matplot(x=c(0,100),y=c(0,100),type=\"n\")");
      System.out.println("box(which=\"plot\",col=\"blue\")");
      System.out.println("box(which=\"figure\",col=\"red\")");
      System.out.println("box(which=\"inner\",col=\"yellow\")");
      System.out.println("box(which=\"outer\",col=\"green\")");


   }
}
