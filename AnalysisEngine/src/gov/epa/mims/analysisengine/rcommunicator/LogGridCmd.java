package gov.epa.mims.analysisengine.rcommunicator;

import java.awt.Color;

import java.util.List;


/**
 * class for drawing logrithmic grid lines in R
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class LogGridCmd extends Cmd
{
   /** the name given to the R function this class generates */
//   private static final String R_FUNCTION_NAME = "logTicks";
   private static final String R_FUNCTION_NAME = "logGrid";

   /**
    * Creates a new LogGridCmd object.
    *
    * @param side 1-vertical or 2-horizontal grid lines
    * @param c color of grid lines
    * @param lty line style for grid lines
    * @param lwd line width of grid lines
    ********************************************************/
   public LogGridCmd(int side, Color c, String lty, double lwd,
    double min, double max, double incr)
   {
      String n = getClass().getName();

      if ((side != 1) && (side != 2))
      {
         throw new IllegalArgumentException(n + " side=" + side);
      }

      if (c == null)
      {
         throw new IllegalArgumentException(n + " c= " + c);
      }

      if (lty == null)
      {
         throw new IllegalArgumentException(n + " lty= " + lty);
      }


      //super methods to setup for this command
      setName(R_FUNCTION_NAME);
      setBannerOn(true);


      //Pre-command to save current parameters
      rCommandsPreAdd("savedlty<-par(lty=" + lty + ")");
      rCommandsPreAdd("savedlwd<-par(lwd=" + lwd + ")");
      rCommandsPreAdd("tmpSavedColor<-par(\"fg\")");
      rCommandsPreAdd("par(fg=" + Util.parseColor(c) + ")");


      //Post-command to restore original parameters
      rCommandsPostAdd("par(lty=savedlty)");
      rCommandsPostAdd("par(lwd=savedlwd)");
      rCommandsPostAdd("par(fg=tmpSavedColor)");


      //generate the R function; add to Pre-command list
//      rCommandsPreAdd(createRfunction());


      //set the side variable
      variableAdd("side", Integer.toString(side));

      //determine the indices into par("usr") which
      //determines the range of the grid lines
      int p1 = 1;
      int p2 = 2;

      if (side == 2)
      {
         p1 = 3;
         p2 = 4;
      }


      String minStr = (Double.isNaN(min))?("par(\"usr\")[" + p1 + "]"):("" +min);
      String maxStr = (Double.isNaN(max))?("par(\"usr\")[" + p2 + "]"):("" +max);
      String incrStr = (Double.isNaN(incr))?("1"):("" +incr);

      variableAdd("min", minStr);
      variableAdd("max", maxStr);
      variableAdd("interval", incrStr);

//      String min = "par(\"usr\")[" + p1 + "]";
//      String max = "par(\"usr\")[" + p2 + "]";


      //set the grid line ranges
//      variableAdd("min", min);
//      variableAdd("max", max);
//      variableAdd("interval", "1");
   }

//   /**
//    * generate the R function to draw the grid lines
//    *
//    * @return the R function to draw the grid lines as a String
//    ********************************************************/
//   private String createRfunction()
//   {
//      StringBuffer buf = new StringBuffer(1000);
//      buf.append(R_FUNCTION_NAME + "<-function(side,min,max)\n");
//      buf.append("{\n");
//      buf.append("  f1<-10^floor(min)\n");
//      buf.append("  c1<-10^ceiling(max)\n");
//      buf.append("  while(f1 <= c1)\n");
//      buf.append("  {\n");
//      buf.append("    for(j in c(1:10))\n");
//      buf.append("    {\n");
//      buf.append("      tick<-j*f1\n");
//      buf.append("      if((log10(tick)>=min)&&(log10(tick)<=max))\n");
//      buf.append("      {\n");
//      buf.append("        if(side == 1)\n");
//      buf.append("        {\n");
//      buf.append("          abline(v=tick);\n");
//      buf.append("        }\n");
//      buf.append("        if(side == 2)\n");
//      buf.append("        {\n");
//      buf.append("          abline(h=tick);\n");
//      buf.append("        }\n");
//      buf.append("      }\n");
//      buf.append("    }\n");
//      buf.append("    f1<-f1*10\n");
//      buf.append("  }\n");
//      buf.append("}\n");
//
//      return buf.toString();
//   }
//
//   /**
//    * Unit test
//    *
//    * @param args command line args not used
//    ********************************************************/
//   public static void main(String[] args)
//   {
//      LogGridCmd cmd;
//      List cmds;
//
//      cmd = new LogGridCmd(1, Color.blue, "1", 2.2);
//
//      cmds = cmd.getCommands();
//
//      for (int i = 0; i < cmds.size(); ++i)
//      {
//         System.out.println(cmds.get(i));
//      }
//
//      cmd = new LogGridCmd(2, Color.red, "2", 1.2);
//
//      cmds = cmd.getCommands();
//
//      for (int i = 0; i < cmds.size(); ++i)
//      {
//         System.out.println(cmds.get(i));
//      }
//   }
}
