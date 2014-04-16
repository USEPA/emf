package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.LineType;
import gov.epa.mims.analysisengine.tree.DisplaySizeType;
import java.util.List;


/**
 * generate a ParOmiCmd R command
 * <br>
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class ParOmiCmd extends Cmd
{

   public ParOmiCmd(double[] size, int marginUnits)
   {
      super();
      super.setName("par");

      if(size == null)
      {
         super.setIssueCommand(false);
      }
      else if(size.length != 4)
      {
         throw new IllegalArgumentException(getClass().getName()
              + "size has the wrong number of elements. size = " + size);
      }
      else if(marginUnits == DisplaySizeType.NDC) 
      {
         variableAdd("omd",Util.buildArrayCommand("c",size));
      }
      else if(marginUnits == DisplaySizeType.INCHES) 
      {
         variableAdd("omi",Util.buildArrayCommand("c",size));
      }
      else if(marginUnits == DisplaySizeType.LINES_OF_TEXT) 
      {
         variableAdd("oma",Util.buildArrayCommand("c",size));
      }
      else
      {
         throw new IllegalArgumentException(getClass().getName()
              + "unknown marginUnits");
      }
   }

   public static void main(String[] args)
   {
      double[] size = null;
      Cmd c = null;
      List l = null;

      //test 1
      size = new double[]{.1,.9,.2,.8};
      c = new ParOmiCmd(size,DisplaySizeType.NDC);
      l = c.getCommands();
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

      //test 2
      size = new double[]{1,1,2,2};
      c = new ParOmiCmd(size,DisplaySizeType.INCHES);
      l = c.getCommands();
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

      //test 3
      size = new double[]{.1,.9,.2,.8};
      c = new ParOmiCmd(size,DisplaySizeType.LINES_OF_TEXT);
      l = c.getCommands();
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
