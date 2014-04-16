package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.Plot;


/**
 * command options common to generating all Plots
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class Driver extends Cmd
{
   /**
    * Creates a new Driver object.
    * @param p Plot
    ********************************************************/
   public Driver(Plot p)
   {
      setBannerOn(false);
      Rvariable.reset();
//      rCommandsPreAdd(ReadTableCmd.getCommands(p));

      String[] parCommands = Par.getPltCmd(p);

      for (int i = 0; i < parCommands.length; i++)
      {
         rCommandsPreAdd(parCommands[i]);
      }


      //      titleCmd(p);
      //      subtitleCmd(p);
      //      footerCmd(p);
      Cmd boxCmd = new BoxCmd(p);
      rCommandsPostAdd(boxCmd.getCommands());
   }
}
