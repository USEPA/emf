package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.AxisCategory;
import gov.epa.mims.analysisengine.tree.BoxPlot;
import gov.epa.mims.analysisengine.tree.BoxType;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.Legend;

import java.util.List;


/**
 * insures that Objects are called to generate all plot commands
 *
 * @version $Revision: 1.2 $
 * @author Tommy E. Cathey
 ********************************************************/
public class BoxPlotDriver extends AnnotationDriver
implements gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc
{
   /**
    * Creates a new Driver object.
    * @param p BoxPlotDriver
    ********************************************************/
   public BoxPlotDriver(BoxPlot p)
   {
      super(p);
      rCommandsPreAdd(ReadTableFacade.getCommands(p));

      //pass the BoxPlot argument to BoxPlotCmd(p) to create
      //the commands for plotting the Boxes; retrieve the 
      //generated commands and put them on our command list
      //by passing them to the rCommandsPreAdd method
      //of our super class Cmd;
      //
      Cmd cmd = new BoxPlotCmd(p);
      rCommandsPreAdd(cmd.getCommands());

      //get the Data set names for labeling the Box Plots
      //along the Category axis; the names of the data sets
      //are escaped quoted to protect the quote marks; then
      //they are stored in dataSetName[i]; Once all the names
      //are collected dataSetName is passed to Util.buildArrayCommand
      //to build a vector in R which is assigned to the
      //R variable atXlabels by calling rCommandsPreAdd method
      //of our super class Cmd; atXlabels can be used along
      //with atX which is the variable containing the returned
      //results of bxp() (i.e.   atX <- bxp(...)   )
      //
      List keyList = ((BoxPlot) p).getDataKeyList();
      String[] dataSetName = new String[keyList.size()];
      for(int i =0 ; i < keyList.size(); i++)
      {
         Object key = keyList.get(i);
         DataSetIfc ds = p.getDataSet(key);
         dataSetName[i] = Util.escapeQuote(ds.getName());
      }
      rCommandsPreAdd("atXlabels <- " + Util.buildArrayCommand("c",dataSetName));

      BoxType boxType = Option.getBoxType(p);
      boolean horiz = boxType.getHorizontal();

      AxisCategory axisCategory = (AxisCategory)Option.getXAxis(p);
      AxisNumeric axisNumeric = (AxisNumeric)Option.getYAxis(p);
      if(horiz)
      {
         //in the horizontal case axis 1 is the numeric axis and
         //axis 2 is the category axis
         //
         boolean juxtaposed = true;
         cmd = new AxisCmdBoxPlot(2, axisCategory,juxtaposed);
         rCommandsPostAdd(cmd.getCommands());
         cmd = new AxisCmdNumeric(1, axisNumeric,juxtaposed);
         rCommandsPostAdd(cmd.getCommands());
      }
      else
      {
         //in the non-horizontal case axis 2 is the numeric axis and
         //axis 1 is the category axis
         //
         boolean juxtaposed = false;
         cmd = new AxisCmdBoxPlot(1, axisCategory,juxtaposed);
         rCommandsPostAdd(cmd.getCommands());
         cmd = new AxisCmdNumeric(2, axisNumeric,juxtaposed);
         rCommandsPostAdd(cmd.getCommands());
      }

      if (p.getOption(LEGEND) != null)
      {
         LegendCmdGenericPlot legCmd = new LegendCmdGenericPlot(
            (Legend) p.getOption(LEGEND), "bxpCol","atXlabels");
         rCommandsPostAdd(legCmd.getCommands());
      }
   }
}
