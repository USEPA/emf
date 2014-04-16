package gov.epa.mims.analysisengine.rcommunicator;

/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class AxisCmd extends Cmd
{
   /** DOCUMENT_ME */
   public static final int BELOW = 1;

   /** DOCUMENT_ME */
   public static final int LEFT = 2;

   /** DOCUMENT_ME */
   public static final int ABOVE = 3;

   /** DOCUMENT_ME */
   public static final int RIGHT = 4;

   /**
    * Creates a new AxisCmd object.
    * @param side DOCUMENT_ME
    ********************************************************/
   public AxisCmd(int side)
   {
      if ((side != BELOW) && (side != LEFT) && (side != ABOVE)
             && (side != RIGHT))
      {
         throw new IllegalArgumentException("side=" + side + " is invalid");
      }

      if((side == 1) || (side == 3))
         rCommandsPreAdd("par(xaxt=\"s\")");

      if((side == 2) || (side == 4))
         rCommandsPreAdd("par(yaxt=\"s\")");

      setName("axis");
      setBannerOn(true);
      variableAdd("side", Integer.toString(side));
   }
}
