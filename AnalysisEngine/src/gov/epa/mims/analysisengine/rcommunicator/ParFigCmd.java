package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.DisplaySizeType;
import java.util.List;


/**
 * generate a ParFigCmd R command
 * <br>
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class ParFigCmd extends Cmd
{
   public ParFigCmd(double[] size)
   {
      super();
      super.setName("par");

      if(size == null)
      {
         super.setIssueCommand(false);
      }
      else if(size.length == 4)
      {
         variableAdd("fig",Util.buildArrayCommand("c",size));
      }
      else if(size.length == 2)
      {
         variableAdd("fin",Util.buildArrayCommand("c",size));
      }
      else
      {
         throw new IllegalArgumentException(getClass().getName()
              + "size has the wrong number of elements. size = " + size);
      }
   }


   public static void main(String[] args)
   {
      double[] size = null;
      Cmd c = null;
      List l = null;

      //test 1
      size = new double[]{.1,.9,.2,.8};
      c = new ParFigCmd(size);
      l = c.getCommands();
      System.out.println("x11()");
//      System.out.println("par(fig=c(0.1,0.9,0.2,0.8))");
      for(int i=0;i<l.size();i++)
      {
         System.out.println(l.get(i));
      }
      System.out.println("matplot(x=c(0,100),y=c(0,100),type=\"n\")");
      System.out.println("box(which=\"plot\",col=\"blue\")");
      System.out.println("box(which=\"figure\",col=\"red\")");
      System.out.println("box(which=\"inner\",col=\"yellow\")");
      System.out.println("box(which=\"outer\",col=\"green\")");

      //test 2
      size = new double[]{3,4};
      c = new ParFigCmd(size);
      l = c.getCommands();
      System.out.println("x11()");
//      System.out.println("par(fig=c(0.1,0.9,0.2,0.8))");
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
