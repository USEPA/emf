package gov.epa.mims.analysisengine.rcommunicator;

/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class NewsPositionCmd extends Cmd
{
   /**
    * Creates a new PositionCmd object.
    * @param side DOCUMENT_ME
    ********************************************************/
   public NewsPositionCmd(String rtrnVar, int side, String p, double xJust, 
      double yJust)
   {
      if ((side < 1) || (side > 4))
      {
         throw new IllegalArgumentException(getClass().getName() + " side= "
                                            + side);
      }

      if (!(p.equals("NW") || p.equals("N") || p.equals("NE") || p.equals("W")
                || p.equals("C") || p.equals("E") || p.equals("SE")
                || p.equals("S") || p.equals("SE")))
      {
         throw new IllegalArgumentException(getClass().getName() + " p= " + p);
      }

      if (!((xJust >= 0.0) && (xJust <= 1.0)))
      {
         throw new IllegalArgumentException(getClass().getName() + " xJust= "
                                            + xJust);
      }

      if (!((yJust >= 0.0) && (yJust <= 1.0)))
      {
         throw new IllegalArgumentException(getClass().getName() + " yJust= "
                                            + yJust);
      }

      setName("newsPosition");
      setReturnVariable(rtrnVar);

      switch (side)
      {
      case 1:
         variableAdd(Util.escapeQuote("maBot"), null);

         break;

      case 2:
         variableAdd(Util.escapeQuote("maLeft"), null);

         break;

      case 3:
         variableAdd(Util.escapeQuote("maTop"), null);

         break;

      case 4:
         variableAdd(Util.escapeQuote("maRight"), null);

         break;
      }

      variableAdd(Util.escapeQuote(p), null);
      variableAdd(Double.toString(xJust), null);
      variableAdd(Double.toString(yJust), null);
   }

   /**
    * DOCUMENT_ME
    *
    * @param args DOCUMENT_ME
    ********************************************************/
   public static void main(String[] args)
   {
      NewsPositionCmd news;
      java.util.ArrayList cmds;

      news = new NewsPositionCmd("xy", 1, "N", 0.5, 0.5);
      cmds = (java.util.ArrayList) news.getCommands();

      for (int i = 0; i < cmds.size(); ++i)
      {
         System.out.println(cmds.get(i));
      }

      news = new NewsPositionCmd(null, 3, "NE", 0.6, 0.2);
      cmds = (java.util.ArrayList) news.getCommands();

      for (int i = 0; i < cmds.size(); ++i)
      {
         System.out.println(cmds.get(i));
      }
   }
}
