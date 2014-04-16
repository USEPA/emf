package gov.epa.mims.analysisengine.rcommunicator;

import java.awt.Color;

import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class LogTickCmd extends Cmd
{
   /** DOCUMENT_ME */
   private static final String rFuncName = "logTicks";

   /**
    * Creates a new LogTickCmd object.
    * @param side DOCUMENT_ME
    ********************************************************/
   public LogTickCmd(int side, Color c, double tcl, double lwd,
     double min, double max, double incr)
   {
      setName(rFuncName);
      setBannerOn(true);
      rCommandsPreAdd("par(tcl=" + tcl + ")");
      rCommandsPreAdd("par(lwd=" + lwd + ")");
      rCommandsPreAdd("tmpSavedColor<-par(\"fg\")");
      rCommandsPreAdd("par(fg=" + Util.parseColor(c) + ")");
//      rCommandsPreAdd(createRfunction());
      rCommandsPostAdd("par(fg=tmpSavedColor)");
      variableAdd("side", Integer.toString(side));

      int p1 = (side % 2 == 0)
               ? 3
               : 1;
      int p2 = (side % 2 == 0)
               ? 4
               : 2;
//      String min = "par(\"usr\")[" + p1 + "]";
//      String max = "par(\"usr\")[" + p2 + "]";
//      variableAdd("min", min);
//      variableAdd("max", max);
//      variableAdd("interval", "1");

      String minStr = (Double.isNaN(min))?("par(\"usr\")[" + p1 + "]"):("" +min);
      String maxStr = (Double.isNaN(max))?("par(\"usr\")[" + p2 + "]"):("" +max);
      String incrStr = (Double.isNaN(incr))?("1"):("" +incr);

      variableAdd("min", minStr);
      variableAdd("max", maxStr);
      variableAdd("interval", incrStr);
   }

//   /**
//    * DOCUMENT_ME
//    *
//    * @return DOCUMENT_ME
//    ********************************************************/
//   private String createRfunction()
//   {
//      StringBuffer buf = new StringBuffer(1000);
//      buf.append(rFuncName + "<-function(side,min,max)\n");
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
//      buf.append("        axis(side,labels=FALSE,at=c(tick))\n");
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
//    * DOCUMENT_ME
//    *
//    * @param args DOCUMENT_ME
//    ********************************************************/
//   public static void main(String[] args)
//   {
//      LogTickCmd cmd;
//      List cmds;
//
//      cmd = new LogTickCmd(1, Color.blue, 0.3, 2.2);
//
//      cmds = cmd.getCommands();
//
//      for (int i = 0; i < cmds.size(); ++i)
//      {
//         System.out.println(cmds.get(i));
//      }
//
//      cmd = new LogTickCmd(2, Color.red, 0.3, 1.2);
//
//      cmds = cmd.getCommands();
//
//      for (int i = 0; i < cmds.size(); ++i)
//      {
//         System.out.println(cmds.get(i));
//      }
//
//      cmd = new LogTickCmd(3, Color.green, 0.3, 0.2);
//
//      cmds = cmd.getCommands();
//
//      for (int i = 0; i < cmds.size(); ++i)
//      {
//         System.out.println(cmds.get(i));
//      }
//
//      cmd = new LogTickCmd(4, Color.blue, 0.3, 2.2);
//
//      cmds = cmd.getCommands();
//
//      for (int i = 0; i < cmds.size(); ++i)
//      {
//         System.out.println(cmds.get(i));
//      }
//   }
}
