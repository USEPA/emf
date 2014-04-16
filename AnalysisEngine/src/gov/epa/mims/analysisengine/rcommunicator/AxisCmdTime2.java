package gov.epa.mims.analysisengine.rcommunicator;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.AxisTime;

import java.util.Date;
import java.util.TimeZone;


/**
 * DOCUMENT_ME
 *
 * @version $Revision: 1.2 $
 * @author $author$
 ********************************************************/
public class AxisCmdTime2 extends AxisCmdAnnotated
{
   /** DOCUMENT_ME */
   private static final int MIN_NUM_TICKS = AnalysisEngineConstants.TIME_SERIES_DEF_MIN_TICKS;

   /**
    * Creates a new AxisCmdTime2 object.
    *
    * @param side DOCUMENT_ME
    * @param options DOCUMENT_ME
    ********************************************************/
   public AxisCmdTime2(int side, AxisTime options)
   {
      super(side, options);

      if ((options != null) && (options.getTimeZone() != null)
             && (options.getFirstTickMark() != null)
             && (options.getTickLabelFormat() != null)
             && (options.getConstantTimeLabelFormat() != null)
             && (options.getTickIncrement() > 0)
             && (options.getTimeZone() != null))
      {
         processTimeSeriesAxisOptions(side, options);
      }
      else
      {
         processDefaultTimeSeriesAxis(side);
      }
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    ********************************************************/
   private void processDefaultTimeSeriesAxis(int side)
   {

      if ((side != 1) && (side != 2))
      {
         throw new IllegalArgumentException(getClass().getName() + " side="
                                            + side);
      }

      double[] atD = getTickLocations(null, 0);
      String at = Util.buildArrayCommand("c", atD);
      String[] labelsArray = getLabels(atD, null, TimeZone.getDefault());
      String labels = Util.buildArrayCommand("c", 
                                             Util.escapeQuote(labelsArray));

      variableAdd("at", at);
      variableAdd("labels", labels);

      printConstantTime(side, TimeZone.getDefault());
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    * @param opts DOCUMENT_ME
    ********************************************************/
   private void processTimeSeriesAxisOptions(int side, AxisTime opts)
   {
      if ((side != 1) && (side != 2))
      {
         throw new IllegalArgumentException(getClass().getName() + " side="
                                            + side);
      }

      Date firstTickMark = opts.getFirstTickMark();
      long tickIncrement = opts.getTickIncrement();
      double[] atD = getTickLocations(firstTickMark, tickIncrement);
      String at = Util.buildArrayCommand("c", atD);

      TimeZone timeZone = opts.getTimeZone();
      timeZone = (timeZone == null)
                 ? (TimeZone.getDefault())
                 : timeZone;

      String tickLabelFormat = opts.getTickLabelFormat();
      Text constantTimeLabelFormat = opts.getConstantTimeLabelFormat();
      String[] labelsArray = getLabels(atD, tickLabelFormat, timeZone);
      String labels = Util.buildArrayCommand("c", 
                                             Util.escapeQuote(labelsArray));

      variableAdd("at", at);
      variableAdd("labels", labels);

      printConstantTime(side, constantTimeLabelFormat, timeZone);
   }

   /**
    * DOCUMENT_ME
    *
    * @param at DOCUMENT_ME
    * @param tickLabelFormat DOCUMENT_ME
    * @param timeZone DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private String[] getLabels(double[] at, String tickLabelFormat, 
      TimeZone timeZone)
   {
      return TimeSeriesAxisConverter.getLabels(at, tickLabelFormat, timeZone);
   }

   /**
    * DOCUMENT_ME
    *
    * @param firstTickMark DOCUMENT_ME
    * @param tickIncrement DOCUMENT_ME
    *
    * @return DOCUMENT_ME
    ********************************************************/
   private double[] getTickLocations(Date firstTickMark, long tickIncrement)
   {
      double[] at = null;

      if (firstTickMark != null)
      {
         if (tickIncrement > 0)
         {
            at = TimeSeriesAxisConverter.getTicks(firstTickMark, tickIncrement);
         }
         else
         {
            at = TimeSeriesAxisConverter.getRoundedTicks(firstTickMark, 
                                                         MIN_NUM_TICKS);
         }
      }
      else
      {
         if (tickIncrement > 0)
         {
            at = TimeSeriesAxisConverter.getTicks(tickIncrement);
         }
         else
         {
            at = TimeSeriesAxisConverter.getRoundedTicks(MIN_NUM_TICKS);
         }
      }

      return at;
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    * @param tz DOCUMENT_ME
    ********************************************************/
   private void printConstantTime(int side, TimeZone tz)
   {
      String constTime;
      constTime = TimeSeriesAxisConverter.getConstantTimeString(null, tz);

      gov.epa.mims.analysisengine.tree.Text text = new gov.epa.mims.analysisengine.tree.Text();
      text.setColor(java.awt.Color.blue);
      text.setPosition("C", 0.1, 0.5);
      text.setTextExpansion(0.0);
      text.setTextDegreesRotation(0.0);


      //      text.setTypeface(java.lang.String);
      //      text.setStyle(java.lang.String);
      text.setTextString(constTime);
      text.setTimeFormat(null);

      //      Text2CmdMargin textCmdMargin = new Text2CmdMargin(text,side);
      //      String region = null;
      //      region = (side == 1)?("maBot"):(region);
      //      region = (side == 2)?("maLeft"):(region);
      //      region = (side == 3)?("maTop"):(region);
      //      region = (side == 4)?("maRight"):(region);
      //      Text2CmdMargin textCmdMargin = new Text2CmdMargin(text,region,"C");
      Text textClone = (Text) text.clone();
      String pos = textClone.getPosition();
      String reg = textClone.getRegion();
      double xjust = textClone.getXJustification();
      double yjust = textClone.getYJustification();
      pos = (pos == null)
            ? (Text.CENTER)
            : pos;

      if (reg == null)
      {
         reg = (side == 1)
               ? (Text.BOTTOM_HAND_MARGIN)
               : reg;
         reg = (side == 2)
               ? (Text.LEFT_HAND_MARGIN)
               : reg;
         reg = (side == 3)
               ? (Text.TOP_HAND_MARGIN)
               : reg;
         reg = (side == 4)
               ? (Text.RIGHT_HAND_MARGIN)
               : reg;
      }

      reg = (reg == null)
            ? (Text.LEFT_HAND_MARGIN)
            : reg;
      textClone.setPosition(reg, pos, xjust, yjust);

      Text2Cmd textCmd = new Text2Cmd(textClone);

      rCommandsPostAdd(textCmd.getCommands());
   }

   /**
    * DOCUMENT_ME
    *
    * @param side DOCUMENT_ME
    * @param text DOCUMENT_ME
    * @param tz DOCUMENT_ME
    ********************************************************/
   private void printConstantTime(int side, Text text, TimeZone tz)
   {
      if (text == null)
      {
         throw new IllegalArgumentException(getClass().getName()
                                            + " text=null");
      }

      String fmt = text.getTimeFormat();

      String constTime;
      constTime = TimeSeriesAxisConverter.getConstantTimeString(fmt, tz);

      String s = text.getTextString();
      s += constTime;
      text.setTextString(s);

      //      Text2CmdMargin textCmdMargin = new Text2CmdMargin(text,side);
      //      String region = null;
      //      region = (side == 1)?("maBot"):(region);
      //      region = (side == 2)?("maLeft"):(region);
      //      region = (side == 3)?("maTop"):(region);
      //      region = (side == 4)?("maRight"):(region);
      //      Text2CmdMargin textCmdMargin = new Text2CmdMargin(text,region,"C");
      Text textClone = (Text) text.clone();
      String pos = textClone.getPosition();
      String reg = textClone.getRegion();
      double xjust = textClone.getXJustification();
      double yjust = textClone.getYJustification();
      pos = (pos == null)
            ? (Text.CENTER)
            : pos;

      if (reg == null)
      {
         reg = (side == 1)
               ? (Text.BOTTOM_HAND_MARGIN)
               : reg;
         reg = (side == 2)
               ? (Text.LEFT_HAND_MARGIN)
               : reg;
         reg = (side == 3)
               ? (Text.TOP_HAND_MARGIN)
               : reg;
         reg = (side == 4)
               ? (Text.RIGHT_HAND_MARGIN)
               : reg;
      }

      reg = (reg == null)
            ? (Text.LEFT_HAND_MARGIN)
            : reg;
      textClone.setPosition(reg, pos, xjust, yjust);

      Text2Cmd textCmd = new Text2Cmd(textClone);

      rCommandsPostAdd(textCmd.getCommands());
   }
}
