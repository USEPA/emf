package gov.epa.mims.analysisengine.rcommunicator;

import java.util.ArrayList;
import java.util.List;


/**
 * DOCUMENT_ME
 *
 * @version $Id: InitializeCmd.java,v 1.2 2005/09/19 14:50:10 rhavaldar Exp $
 * @author Tommy E. Cathey
 ********************************************************/
public class InitializeCmd implements java.io.Serializable
{
   /** DOCUMENT_ME */
   private static ArrayList rCommands = new ArrayList();

   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static List getCommands()
   {
      rCommands.clear();

//      newsPositionFunction();

      return rCommands;
   }
//
//   /**
//    * DOCUMENT_ME
//    ********************************************************/
//   private static void newsPositionFunction()
//   {
//      StringBuffer buf = new StringBuffer(1000);
//
//      buf.append("newsPosition<-function(region,position,xjust,yjust)\n");
//      buf.append("{\n");
//      buf.append("   catn<-function(...)do.call(\"cat\",");
//      buf.append("c(lapply(list(...),formatC),list(\"\\n\")))\n");
//      buf.append("   ptslope<-function(x1,y1,x2,y2,x)");
//      buf.append("{slp<-(y2-y1)/(x2-x1);y1+slp*(x-x1)}\n");
//      buf.append("   fig<-c(\n");
//      buf.append("          figure<-ptslope(par(\"plt\")[1],");
//      buf.append("par(\"usr\")[1],par(\"plt\")[2],par(\"usr\")[2],0),\n");
//      buf.append("          figure<-ptslope(par(\"plt\")[1],");
//      buf.append("par(\"usr\")[1],par(\"plt\")[2],par(\"usr\")[2],1),\n");
//      buf.append("          figure<-ptslope(par(\"plt\")[3],");
//      buf.append("par(\"usr\")[3],par(\"plt\")[4],par(\"usr\")[4],0),\n");
//      buf.append("          figure<-ptslope(par(\"plt\")[3],");
//      buf.append("par(\"usr\")[3],par(\"plt\")[4],par(\"usr\")[4],1))\n");
//      buf.append("   center<-function(x1,x2,y1,y2,R,xjust,yjust)\n");
//      buf.append("   {\n");
//      buf.append("      w1<-(x2-x1)/3\n");
//      buf.append("      w2<-(y2-y1)/3\n");
//      buf.append("      Bx<-c( x1 , (x1+w1) , (x2-w1) , x2 )\n");
//      buf.append("      By<-c( y1 , (y1+w2) , (y2-w2) , y2 )\n");
//      buf.append("      rtrn <- \"error\"\n");
//      buf.append("      if(R==\"NW\")\n");
//      buf.append("      {\n");
//      buf.append("         x<-Bx[1]+(Bx[2]-Bx[1])*xjust\n");
//      buf.append("         y<-By[3]+(By[4]-By[3])*yjust\n");
//      buf.append("         rtrn <- c(x,y)\n");
//      buf.append("      }\n");
//      buf.append("      else if(R==\"N\")\n");
//      buf.append("      {\n");
//      buf.append("         x<-Bx[2]+(Bx[3]-Bx[2])*xjust\n");
//      buf.append("         y<-By[3]+(By[4]-By[3])*yjust\n");
//      buf.append("         rtrn <- c(x,y)\n");
//      buf.append("      }\n");
//      buf.append("      else if(R==\"NE\")\n");
//      buf.append("      {\n");
//      buf.append("         x<-Bx[3]+(Bx[4]-Bx[3])*xjust\n");
//      buf.append("         y<-By[3]+(By[4]-By[3])*yjust\n");
//      buf.append("         rtrn <- c(x,y)\n");
//      buf.append("      }\n");
//      buf.append("      else if(R==\"W\")\n");
//      buf.append("      {\n");
//      buf.append("         x<-Bx[1]+(Bx[2]-Bx[1])*xjust\n");
//      buf.append("         y<-By[2]+(By[3]-By[2])*yjust\n");
//      buf.append("         rtrn <- c(x,y)\n");
//      buf.append("      }\n");
//      buf.append("      else if(R==\"C\")\n");
//      buf.append("      {\n");
//      buf.append("         x<-Bx[1]+(Bx[4]-Bx[1])*xjust\n");
//      buf.append("         y<-By[1]+(By[4]-By[1])*yjust\n");
//      buf.append("         rtrn <- c(x,y)\n");
//      buf.append("      }\n");
//      buf.append("      else if(R==\"E\")\n");
//      buf.append("      {\n");
//      buf.append("         x<-Bx[3]+(Bx[4]-Bx[3])*xjust\n");
//      buf.append("         y<-By[2]+(By[3]-By[2])*yjust\n");
//      buf.append("         rtrn <- c(x,y)\n");
//      buf.append("      }\n");
//      buf.append("      else if(R==\"SW\")\n");
//      buf.append("      {\n");
//      buf.append("         x<-Bx[1]+(Bx[2]-Bx[1])*xjust\n");
//      buf.append("         y<-By[1]+(By[2]-By[1])*yjust\n");
//      buf.append("         rtrn <- c(x,y)\n");
//      buf.append("      }\n");
//      buf.append("      else if(R==\"S\")\n");
//      buf.append("      {\n");
//      buf.append("         x<-Bx[2]+(Bx[3]-Bx[2])*xjust\n");
//      buf.append("         y<-By[1]+(By[2]-By[1])*yjust\n");
//      buf.append("         rtrn <- c(x,y)\n");
//      buf.append("      }\n");
//      buf.append("      else if(R==\"SE\")\n");
//      buf.append("      {\n");
//      buf.append("         x<-Bx[3]+(Bx[4]-Bx[3])*xjust\n");
//      buf.append("         y<-By[1]+(By[2]-By[1])*yjust\n");
//      buf.append("         rtrn <- c(x,y)\n");
//      buf.append("      }\n");
//      buf.append("   \n");
//      buf.append("      rtrn\n");
//      buf.append("   }\n");
//      buf.append("   if(region==\"plot\")\n");
//      buf.append("   {\n");
//      buf.append("      x1<-par(\"usr\")[1]\n");
//      buf.append("      x2<-par(\"usr\")[2]\n");
//      buf.append("      y1<-par(\"usr\")[3]\n");
//      buf.append("      y2<-par(\"usr\")[4]\n");
//      buf.append("\n");
//      buf.append("   }\n");
//      buf.append("   else if(region==\"maLeft\")\n");
//      buf.append("   {\n");
//      buf.append("      x1<-fig[1]\n");
//      buf.append("      x2<-par(\"usr\")[1]\n");
//      buf.append("      y1<-par(\"usr\")[3]\n");
//      buf.append("      y2<-par(\"usr\")[4]\n");
//      buf.append("   }\n");
//      buf.append("   else if(region==\"maBot\")\n");
//      buf.append("   {\n");
//      buf.append("      x1<-par(\"usr\")[1]\n");
//      buf.append("      x2<-par(\"usr\")[2]\n");
//      buf.append("      y1<-fig[3]\n");
//      buf.append("      y2<-par(\"usr\")[3]\n");
//      buf.append("   }\n");
//      buf.append("   else if(region==\"maTop\")\n");
//      buf.append("   {\n");
//      buf.append("      x1<-par(\"usr\")[1]\n");
//      buf.append("      x2<-par(\"usr\")[2]\n");
//      buf.append("      y1<-par(\"usr\")[4]\n");
//      buf.append("      y2<-fig[4]\n");
//      buf.append("   }\n");
//      buf.append("   else if(region==\"maRight\")\n");
//      buf.append("   {\n");
//      buf.append("      x1<-par(\"usr\")[2]\n");
//      buf.append("      x2<-fig[2]\n");
//      buf.append("      y1<-par(\"usr\")[3]\n");
//      buf.append("      y2<-par(\"usr\")[4]\n");
//      buf.append("   }\n");
//      buf.append("   xy<-center(x1,x2,y1,y2,position,xjust,yjust)\n");
//      buf.append("   if(par(\"xlog\")) { xy <- c(10^xy[1],xy[2]) }\n");
//      buf.append("   if(par(\"ylog\")) { xy <- c(xy[1],10^xy[2]) }\n");
//      buf.append("   xy\n");
//      buf.append("}\n");
//
//      rCommands.add(buf.toString());
//   }
//
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
