package gov.epa.mims.analysisengine.rcommunicator;

import java.util.ArrayList;
import java.util.List;


/**
 * generates R functions needed for drawing bordered text
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 */
public class TextBorderRfunctions implements java.io.Serializable
{
   /**
    * DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   public static List getFunctions()
   {
      ArrayList cmdList = new ArrayList();

      cmdList.add(
            "###########################################################");
      cmdList.add("#       translationMatrix");
      cmdList.add(
            "###########################################################");
      cmdList.add("#");
      cmdList.add("#    | 1  0  x |");
      cmdList.add("#    | 0  1  y |");
      cmdList.add("#    | 0  0  1 |");
      cmdList.add("#");
      cmdList.add(
            "###########################################################");
      cmdList.add("translationMatrix <- function(x,y)");
      cmdList.add("{");
      cmdList.add("   a = c(1,0,0,0,1,0,x,y,1)");
      cmdList.add("   return (array(a,dim=c(3,3)))");
      cmdList.add("}");
      cmdList.add("");
      cmdList.add(
            "###########################################################");
      cmdList.add("#      rotationMatrix");
      cmdList.add(
            "###########################################################");
      cmdList.add("#generate the Matrix");
      cmdList.add("#");
      cmdList.add("# | cos()  -sin()     0 |");
      cmdList.add("# | sin()   cos()     0 |");
      cmdList.add("# |   0       0       1 |");
      cmdList.add("#");
      cmdList.add("# argument must be in radians");
      cmdList.add(
            "###########################################################");
      cmdList.add("rotationMatrix <- function(radians)");
      cmdList.add("{");
      cmdList.add("   a = c(cos(radians),sin(radians),0,");
      cmdList.add("        -sin(radians),cos(radians),0,");
      cmdList.add("              0,          0,       1)");
      cmdList.add("   return (array(a,dim=c(3,3)))");
      cmdList.add("}");
      cmdList.add("");
      cmdList.add(
            "###########################################################");
      cmdList.add("#       deg2rad");
      cmdList.add(
            "###########################################################");
      cmdList.add("#convert degrees into radians");
      cmdList.add(
            "###########################################################");
      cmdList.add("deg2rad <- function(deg)");
      cmdList.add("{");
      cmdList.add("   return (pi*deg/180)");
      cmdList.add("}");
      cmdList.add("");
      cmdList.add(
            "###########################################################");
      cmdList.add("#              findRotation");
      cmdList.add(
            "###########################################################");
      cmdList.add("# (x1,y1) first end pt of line");
      cmdList.add("# (x2,y2) second end pt of line");
      cmdList.add("findRotation <- function(x1,y1,x2,y2)");
      cmdList.add("{");
      cmdList.add("   v1 <- c(x1,y1,1)");
      cmdList.add("   v2 <- c(x2,y2,1)");
      cmdList.add("   q1 <- Nu2q() %*% v1");
      cmdList.add("   q2 <- Nu2q() %*% v2");
      cmdList.add("   return( rad2deg( atan((q2[2]-q1[2])/(q2[1]-q1[1])) ))");
      cmdList.add("}");
      cmdList.add("");
      cmdList.add(
            "###########################################################");
      cmdList.add("#       rad2deg");
      cmdList.add(
            "###########################################################");
      cmdList.add("#convert radians into degrees ");
      cmdList.add(
            "###########################################################");
      cmdList.add("rad2deg <- function(rad)");
      cmdList.add("{");
      cmdList.add("   return (rad * 180 / pi)");
      cmdList.add("}");
      cmdList.add("");
      cmdList.add(
            "###########################################################");
      cmdList.add("#      borderHeight");
      cmdList.add(
            "###########################################################");
      cmdList.add("#generate the y coordinates of a correctly sized border");
      cmdList.add("#about (0,0) in \"usr\" coordinates");
      cmdList.add(
            "###########################################################");
      cmdList.add("borderHeight <-function(strHeight,percentTop,percentBot)");
      cmdList.add("{");
      cmdList.add("   yb <- strHeight*percentBot + strHeight/2");
      cmdList.add("   yt <- strHeight*percentTop + strHeight/2");
      cmdList.add("   return(c(-yb,-yb,yt,yt,-yb))");
      cmdList.add("}");
      cmdList.add("");
      cmdList.add(
            "###########################################################");
      cmdList.add("#      borderWidth");
      cmdList.add(
            "###########################################################");
      cmdList.add("#generate the x coordinates of a correctly sized border");
      cmdList.add("#about (0,0) in \"usr\" coordinates");
      cmdList.add(
            "###########################################################");
      cmdList.add("borderWidth <-function(strW,chrW,percentLeft,percentRight)");
      cmdList.add("{");
      cmdList.add("   xl <- chrW*percentLeft + strW/2");
      cmdList.add("   xr <- chrW*percentRight + strW/2");
      cmdList.add("   return(c(-xl,xr,xr,-xl,-xl))");
      cmdList.add("}");
      cmdList.add("");
      cmdList.add(
            "###########################################################");
      cmdList.add("#             Nu2q");
      cmdList.add(
            "###########################################################");
      cmdList.add("#Normalization matrix from \"usr\" to \"pin\" coordinates");
      cmdList.add(
            "###########################################################");
      cmdList.add("Nu2q <- function()");
      cmdList.add("{  ");
      cmdList.add("   u1 <- par(\"usr\")[2]-par(\"usr\")[1]");
      cmdList.add("   u2 <- par(\"usr\")[4]-par(\"usr\")[3]");
      cmdList.add("   q1 <- par(\"pin\")[1]");
      cmdList.add("   q2 <- par(\"pin\")[2]");
      cmdList.add("   sx <- q1/u1");
      cmdList.add("   sy <- q2/u2");
      cmdList.add(
            "   a = c(sx,0,0,0,sy,0,-sx*par(\"usr\")[1],-sy*par(\"usr\")[3],1)");
      cmdList.add("   return (array(a,dim=c(3,3)))");
      cmdList.add("}  ");
      cmdList.add("   ");
      cmdList.add(
            "###########################################################");
      cmdList.add("#             Nu2q ");
      cmdList.add(
            "###########################################################");
      cmdList.add("#Normalization matrix from \"pin\" to \"usr\" coordinates");
      cmdList.add(
            "###########################################################");
      cmdList.add("Nq2u <- function()");
      cmdList.add("{");
      cmdList.add("   u1 <- par(\"usr\")[2]-par(\"usr\")[1]");
      cmdList.add("   u2 <- par(\"usr\")[4]-par(\"usr\")[3]");
      cmdList.add("   q1 <- par(\"pin\")[1]");
      cmdList.add("   q2 <- par(\"pin\")[2]");
      cmdList.add("   sx <- q1/u1");
      cmdList.add("   sy <- q2/u2");
      cmdList.add(
            "   a = c(1/sx,0,0,0,1/sy,0,par(\"usr\")[1],par(\"usr\")[3],1)");
      cmdList.add("   return (array(a,dim=c(3,3)))");
      cmdList.add("}");
      cmdList.add("");
      cmdList.add(
            "###########################################################");
      cmdList.add("#                     borderedText");
      cmdList.add(
            "###########################################################");
      cmdList.add("# txt - text string to be bordered");
      cmdList.add("# (x,y) - the center of the text string");
      cmdList.add("# srt - rotation in degrees");
      cmdList.add("# bcol - border color NULL=use par(\"fg\") NA=omit border");
      cmdList.add("# fcol - background color NA=do not fill");
      cmdList.add("# rP - pad % of char width on right");
      cmdList.add("# lP - pad % of char width on left");
      cmdList.add("# bP - pad % of char height on bottom");
      cmdList.add("# tP - pad % of char height on top");
      cmdList.add(
            "borderedText <- function(txt,x,y,srt,bcol,fcol,rP,lP,bP,tP)");
      cmdList.add("{");
      cmdList.add("   #size the text border around (0,0)");
      cmdList.add("   w <- strwidth(txt)");
      cmdList.add("   wchar <- par(\"cex\")*strwidth(\"X\")");
      cmdList.add("   h <- strheight(txt)");
      cmdList.add("   xRecOld <- borderWidth(w,wchar,rP,lP)");
      cmdList.add("   yRecOld <- borderHeight(h,bP,tP)");
      cmdList.add("");
      cmdList.add("   #translate the border corner points to be centered");
      cmdList.add("   #around the center of the text string");
      cmdList.add("   xRec <- NULL");
      cmdList.add("   yRec <- NULL");
      cmdList.add("   tM <- translationMatrix(x,y)");
      cmdList.add("   for(i in 1:5)");
      cmdList.add("   {");
      cmdList.add("      vOld <- c(xRecOld[i],yRecOld[i],1)");
      cmdList.add("      vNew <-  tM %*% vOld");
      cmdList.add("      xRec[i] = vNew[1]");
      cmdList.add("      yRec[i] = vNew[2]");
      cmdList.add("   }");
      cmdList.add("");
      cmdList.add("   #convert the (x,y) center of the text into par(\"pin\")");
      cmdList.add("   #coordinates q is used to represent the par(\"pin\")");
      cmdList.add("   #coordinates");
      cmdList.add("   q <- Nu2q() %*% c(x,y,1)");
      cmdList.add("");
      cmdList.add("   #build matrix to translate the center of text in");
      cmdList.add("   #par(\"pin\") coordinates to the origin");
      cmdList.add("   T1 <- translationMatrix(q[1],q[2])");
      cmdList.add("");
      cmdList.add("   #the inverse translation of T1 above");
      cmdList.add("   T2 <- translationMatrix(-q[1],-q[2])");
      cmdList.add("");
      cmdList.add("   #build rotation matrix");
      cmdList.add("   R <- rotationMatrix(deg2rad(srt))");
      cmdList.add("");
      cmdList.add("   #for each corner point on the border");
      cmdList.add("   #*convert from \"usr\" to \"pin\" coordinates");
      cmdList.add("   #*translate to origin of \"pin\" coordinates");
      cmdList.add("   #*rotate");
      cmdList.add("   #*translate from origin back to center of text location");
      cmdList.add("   #*convert from \"pin\" to \"usr\" coordinates");
      cmdList.add("   xRot <- NULL");
      cmdList.add("   yRot <- NULL");
      cmdList.add("   for(i in 1:5)");
      cmdList.add("   {");
      cmdList.add("      v <- c(xRec[i],yRec[i],1)");
      cmdList.add("      vNew <- Nq2u() %*% T1 %*% R %*% T2 %*% Nu2q() %*% v");
      cmdList.add("      xRot[i] = vNew[1]");
      cmdList.add("      yRot[i] = vNew[2]");
      cmdList.add("   }");
      cmdList.add("");
      cmdList.add("   #finally draw the text border and fill");
      cmdList.add("   polygon(xRot,yRot,border = bcol,col=fcol)");
      cmdList.add("}");
      cmdList.add(
            "###########################################################");
      cmdList.add("#              positionText");
      cmdList.add(
            "###########################################################");
      cmdList.add("# (x1,y1) first end pt of line");
      cmdList.add("# (x2,y2) second end pt of line");
      cmdList.add("# xj adjustment along the line");
      cmdList.add("#    0 = (x1,y1)");
      cmdList.add("#    1 = (x2,y2)");
      cmdList.add("#  0.5 = ((x2+x1)/2,(y2+y1)/2)");
      cmdList.add("# yj = adjustment above the line by \"h\" units");
      cmdList.add("# h = usually the height of the text");
      cmdList.add("positionText <- function(x1,y1,x2,y2,xj,yj,h)");
      cmdList.add("{");
      cmdList.add("");
      cmdList.add("   #create a line of the proper length along the X-axis");
      cmdList.add("   l <- sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1))");
      cmdList.add("   xo1 <- -l/2");
      cmdList.add("   xo2 <-  l/2");
      cmdList.add("   yo1 <- 0");
      cmdList.add("   yo2 <- 0");
      cmdList.add("");
      cmdList.add("   #position the text relative to the line on the X-axis");
      cmdList.add("   pt <- c(xj*xo2 + xo1*(1 - xj),yj*h,1)");
      cmdList.add("");
      cmdList.add("   #build rotation matrix");
      cmdList.add("   R <- rotationMatrix(atan((y2-y1)/(x2-x1)))");
      cmdList.add("");
      cmdList.add("   #build translation matrix to the center of the line");
      cmdList.add("   xm <- (x2+x1)/2");
      cmdList.add("   ym <- (y2+y1)/2");
      cmdList.add("   T1 <- translationMatrix(xm,ym)");
      cmdList.add("");
      cmdList.add("   #rotate and translate");
      cmdList.add("   pt2 <- T1 %*% R %*% pt");
      cmdList.add("");
      cmdList.add("   return(c(pt2[1],pt2[2]))");
      cmdList.add("}");
      cmdList.add(
            "###########################################################");
      cmdList.add("#         lineCategory");
      cmdList.add(
            "###########################################################");
      cmdList.add("lineCategory <- function(x1,y1,x2,y2){");
      cmdList.add("   p1bits<-pbits(x1,y1)");
      cmdList.add("   p2bits<-pbits(x2,y2)");
      cmdList.add(
            "   if(identical(p1bits,c(FALSE,FALSE,FALSE,FALSE)) && identical(p2bits,c(FALSE,FALSE,FALSE,FALSE)))");
      cmdList.add("   {");
      cmdList.add("#      print (\"lineCategory returning: VISIBLE\")");
      cmdList.add("      return (\"VISIBLE\")");
      cmdList.add("   }");
      cmdList.add(
            "   if( ! identical((p1bits & p2bits),c(FALSE,FALSE,FALSE,FALSE)) )");
      cmdList.add("   {");
      cmdList.add("#      print (\"lineCategory returning: NOT VISIBLE\")");
      cmdList.add("      return (\"NOT VISIBLE\")");
      cmdList.add("   }");
      cmdList.add(
            "   if(identical((p1bits & p2bits),c(FALSE,FALSE,FALSE,FALSE)))");
      cmdList.add("   {");
      cmdList.add(
            "#      print (\"lineCategory returning: CANDIDATE FOR CLIPPING\")");
      cmdList.add("      return (\"CANDIDATE FOR CLIPPING\")");
      cmdList.add("   }");
      cmdList.add("   else");
      cmdList.add("   {");
      cmdList.add("      return (\"ERROR\")");
      cmdList.add("   }");
      cmdList.add("}");
      cmdList.add(
            "###########################################################");
      cmdList.add("#             refLine");
      cmdList.add(
            "###########################################################");
      cmdList.add("#");
      cmdList.add(
            "###########################################################");
      cmdList.add("refLine <- function(x1,y1,x2,y2,m=NULL)");
      cmdList.add("{");
      cmdList.add("   if(!is.null(m))");
      cmdList.add("   {");
      cmdList.add("      if((m==\"Inf\")||(m==\"-Inf\"))");
      cmdList.add("      {");
      cmdList.add("         b <- \"Inf\"");
      cmdList.add("      }  ");
      cmdList.add("      else");
      cmdList.add("      {");
      cmdList.add("         b <- y1 - m*x1");
      cmdList.add("      }  ");
      cmdList.add("      if((b == \"Inf\") || (b == \"-Inf\"))");
      cmdList.add("      {");
      cmdList.add("         abline(v=x1)");
      cmdList.add("         return(c(x1,par(\"usr\")[3],x1,par(\"usr\")[4]))");
      cmdList.add("      }");
      cmdList.add("      else if( m == 0)");
      cmdList.add("      {");
      cmdList.add("         abline(h=y1)");
      cmdList.add("         #intersection with LHS of plot region");
      cmdList.add("         xLHS <- par(\"usr\")[1]");
      cmdList.add("         yLHS <- y1 + m*(xLHS - x1)");
      cmdList.add("         #intersection with RHS of plot region");
      cmdList.add("         xRHS <- par(\"usr\")[2]");
      cmdList.add("         yRHS <- y1 + m*(xRHS - x1)");
      cmdList.add("         return(c(xLHS,yLHS,xRHS,yRHS))");
      cmdList.add("      }");
      cmdList.add("      else");
      cmdList.add("      {");
      cmdList.add("         abline(b,m)");
      cmdList.add("         #intersection with LHS of plot region");
      cmdList.add("         xLHS <- par(\"usr\")[1]");
      cmdList.add("         yLHS <- y1 + m*(xLHS - x1)");
      cmdList.add("         #intersection with RHS of plot region");
      cmdList.add("         xRHS <- par(\"usr\")[2]");
      cmdList.add("         yRHS <- y1 + m*(xRHS - x1)");
      cmdList.add("         x1 <- xLHS");
      cmdList.add("         y1 <- yLHS");
      cmdList.add("         x2 <- xRHS");
      cmdList.add("         y2 <- yRHS");
      cmdList.add("         for(i in 1:4)");
      cmdList.add("         {");
      cmdList.add("            l <- clip1(x1,y1,x2,y2)");
      cmdList.add("            x1 = l[1]");
      cmdList.add("            y1 = l[2]");
      cmdList.add("            x2 = l[3]");
      cmdList.add("            y2 = l[4]");
      cmdList.add(
            "            if(identical(lineCategory(x1,y1,x2,y2),\"VISIBLE\"))");
      cmdList.add("            {");
      cmdList.add("               return(c(x1,y1,x2,y2))");
      cmdList.add("            }");
      cmdList.add(
            "            else if(identical(lineCategory(x1,y1,x2,y2),\"NOT VISIBLE\"))");
      cmdList.add("            {");
      cmdList.add("               return(NULL)");
      cmdList.add("            }");
      cmdList.add("         }");
      cmdList.add("      }");
      cmdList.add("   }");
      cmdList.add("   else");
      cmdList.add("   {");
      cmdList.add("      lines(c(x1,x2),c(y1,y2))");
      cmdList.add("      return(c(x1,y1,x2,y2))");
      cmdList.add("   }");
      cmdList.add("}");
      cmdList.add(
            "###########################################################");
      cmdList.add(
            "###########################################################");
      cmdList.add("clip1 <- function(x1,y1,x2,y2){");
      cmdList.add("");
      cmdList.add("#   print (\"clip1\")");
      cmdList.add("#   print (c(x1,y1,x2,y2))");
      cmdList.add("   p1bits<-pbits(x1,y1)");
      cmdList.add("   p2bits<-pbits(x2,y2)");
      cmdList.add("   x <- NULL");
      cmdList.add("   y <- NULL");
      cmdList.add("   m <- (y2-y1)/(x2-x1)");
      cmdList.add("   if(p1bits[1])");
      cmdList.add("   { #clip pt1 to Ymax");
      cmdList.add("      y = par(\"usr\")[4]");
      cmdList.add("      x = x1+(1/m)*(y-y1)");
      cmdList.add("#   print (c(x,y,x2,y2))");
      cmdList.add("      return(c(x,y,x2,y2))");
      cmdList.add("   }");
      cmdList.add("   else if(p1bits[2])");
      cmdList.add("   { #clip pt1 to Ymin");
      cmdList.add("      y = par(\"usr\")[3]");
      cmdList.add("      x = x1+(1/m)*(y-y1)");
      cmdList.add("#   print (c(x,y,x2,y2))");
      cmdList.add("      return(c(x,y,x2,y2))");
      cmdList.add("   }");
      cmdList.add("   else if(p1bits[3])");
      cmdList.add("   { #clip pt1 to Xmax");
      cmdList.add("      x = par(\"usr\")[2]");
      cmdList.add("      y = y1+m*(x-x1)");
      cmdList.add("#   print (c(x,y,x2,y2))");
      cmdList.add("      return(c(x,y,x2,y2))");
      cmdList.add("   }");
      cmdList.add("   else if(p1bits[4])");
      cmdList.add("   { #clip pt1 to Xmin");
      cmdList.add("      x = par(\"usr\")[1]");
      cmdList.add("      y = y1+m*(x-x1)");
      cmdList.add("#   print (c(x,y,x2,y2))");
      cmdList.add("      return(c(x,y,x2,y2))");
      cmdList.add("   }");
      cmdList.add("   else if(p2bits[1])");
      cmdList.add("   { #clip pt2 to Ymax");
      cmdList.add("      y = par(\"usr\")[4]");
      cmdList.add("      x = x1+(1/m)*(y-y1)");
      cmdList.add("#   print (c(x1,y1,x,y))");
      cmdList.add("      return(c(x1,y1,x,y))");
      cmdList.add("   }");
      cmdList.add("   else if(p2bits[2])");
      cmdList.add("   { #clip pt2 to Ymin");
      cmdList.add("      y = par(\"usr\")[3]");
      cmdList.add("      x = x1+(1/m)*(y-y1)");
      cmdList.add("#   print (c(x1,y1,x,y))");
      cmdList.add("      return(c(x1,y1,x,y))");
      cmdList.add("   }");
      cmdList.add("   else if(p2bits[3])");
      cmdList.add("   { #clip pt2 to Xmax");
      cmdList.add("      x = par(\"usr\")[2]");
      cmdList.add("      y = y1+m*(x-x1)");
      cmdList.add("#   print (c(x1,y1,x,y))");
      cmdList.add("      return(c(x1,y1,x,y))");
      cmdList.add("   }");
      cmdList.add("   else if(p2bits[4])");
      cmdList.add("   { #clip pt2 to Xmin");
      cmdList.add("      x = par(\"usr\")[1]");
      cmdList.add("      y = y1+m*(x-x1)");
      cmdList.add("#   print (c(x1,y1,x,y))");
      cmdList.add("      return(c(x1,y1,x,y))");
      cmdList.add("   }");
      cmdList.add("#   print (c(x1,y1,x2,y2))");
      cmdList.add("   return(c(x1,y1,x2,y2))");
      cmdList.add("}");
      cmdList.add(
            "###########################################################");
      cmdList.add(
            "###########################################################");
      cmdList.add("pbits <- function(x,y){");
      cmdList.add(
            "c(y>par(\"usr\")[4],y<par(\"usr\")[3],x>par(\"usr\")[2],x<par(\"usr\")[1])");
      cmdList.add("}");

      return cmdList;
   }
}
