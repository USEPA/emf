package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.tree.AxisNumeric;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class AxisCmdNumeric extends AxisCmdGrids
{
   /**
    * Creates a new AxisCmdNumeric object.
    *
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdNumeric(int side, AxisNumeric options)
   {
      super(side, options);
   }
   public AxisCmdNumeric(int side, AxisNumeric options,boolean juxtaposed)
   {
      super(side, options,juxtaposed);
   }
}
