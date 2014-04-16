package gov.epa.mims.analysisengine.tree;

import gov.epa.mims.analysisengine.AnalysisException;
import gov.epa.mims.analysisengine.rcommunicator.LabeledDoubleSeries;
import gov.epa.mims.analysisengine.rcommunicator.RCommandGenerator;
import gov.epa.mims.analysisengine.rcommunicator.RCommunicator;
import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.Axis;
import gov.epa.mims.analysisengine.tree.AxisCategory;
import gov.epa.mims.analysisengine.tree.AxisNumeric;
import gov.epa.mims.analysisengine.tree.BarPlot;
import gov.epa.mims.analysisengine.tree.BarType;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.DisplaySizeType;
import gov.epa.mims.analysisengine.tree.Legend;
import gov.epa.mims.analysisengine.tree.OutlineType;
import gov.epa.mims.analysisengine.tree.Page;
import gov.epa.mims.analysisengine.tree.PageType;
import gov.epa.mims.analysisengine.tree.Text;
import gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc;
import gov.epa.mims.analysisengine.tree.PageConstantsIfc;
import gov.epa.mims.analysisengine.tree.SymbolsConstantsIfc;
import gov.epa.mims.analysisengine.tree.MarginConstantsIfc;
import gov.epa.mims.analysisengine.tree.CompassConstantsIfc;
import gov.epa.mims.analysisengine.tree.FontConstantsIfc;
import gov.epa.mims.analysisengine.tree.LineTypeConstantsIfc;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class FIXME_DisplaySizeJPEGAndPngTest_ extends TestCase implements AnalysisOptionConstantsIfc, PageConstantsIfc, SymbolsConstantsIfc,
        MarginConstantsIfc, CompassConstantsIfc, LineTypeConstantsIfc, FontConstantsIfc {

    private DataSets dataSets;

    private List commands = null;

    private String form;

    private String file;

    /**
     * delete data file flag; passed to
     * pageType.setDeleteTemporaryFileOnExit(flag)
     */
    private boolean flag;
    
    public FIXME_DisplaySizeJPEGAndPngTest_(){
        //
    }

    public FIXME_DisplaySizeJPEGAndPngTest_(String form, String file, boolean flag) {
        this.form = form;
        this.file = file;
        this.flag = flag;

        setUp();
        commands = generateCommands();
    }

    public List getCommands() {
        return commands;
    }

    public void testDisplaySizeAndPng() {
        String oForm = System.getProperty("setForm", Page.JPEG);
        String fileName = "test/data/barplot.jpeg";
        String oFile = System.getProperty("oFile", fileName);
        boolean oDelete = true;
        String oClosePlotsStr = null;
        String oTerminateRStr = null;

        if ((oForm.equals(Page.SCREEN)) || (oForm.equals(Page.X11))) {
            oClosePlotsStr = "false";
            oTerminateRStr = "false";
        } else {
            oClosePlotsStr = "true";
            oTerminateRStr = "true";
        }

        // allow user to override settings
        oClosePlotsStr = System.getProperty("closePlots", oClosePlotsStr);
        oTerminateRStr = System.getProperty("terminateR", oTerminateRStr);

        boolean oClosePlots = (Boolean.valueOf(oClosePlotsStr)).booleanValue();
        boolean oTerminateR = (Boolean.valueOf(oTerminateRStr)).booleanValue();

        FIXME_DisplaySizeJPEGAndPngTest_ p = new FIXME_DisplaySizeJPEGAndPngTest_(oForm, oFile, oDelete);
        List cmds = p.getCommands();

        if (Boolean.getBoolean("E")) {
            for (int i = 0; i < cmds.size(); i++) {
                System.out.println(cmds.get(i));
            }
        }

        try {
            RCommunicator rCommunicator = RCommunicator.getInstance();
            String rExec = System.getProperty("os.name").startsWith("Windows") ? "RTerm.exe" : "R";
            rCommunicator.setPathToR(rExec);
            rCommunicator.setLog(new File("mylogfile.txt"));
            rCommunicator.issueCommands(cmds, 5000);
        } catch (IOException e) {
            assertTrue("Should not reach here-"+e.getMessage(),false);
            e.printStackTrace();
        } catch (AnalysisException e) {
            e.printStackTrace();
            assertTrue("Should not reach here-"+e.getMessage(),false);
            File file = new File(fileName);
            if(file.exists()){
                file.delete();
            }
        }
        

        if (oClosePlots) {
            RCommunicator.getInstance().closePlotWindows(2000);
        }

        if (oTerminateR) {
            RCommunicator.getInstance().closePlotWindows(2000);
            RCommunicator.getInstance().terminate();
        }
    }

    /***************************************************************************
     * set up for JUnit test
     **************************************************************************/
    protected void setUp() {
        AnalysisOptions optionsGlobal = initAnalysisOptions();
        Page page = new Page();
        BarPlot barPlot = new BarPlot();
        dataSets = initDataSets(barPlot);
        // ====================================================================
        // build tree
        // ====================================================================
        // dataSets
        // |
        // optionsGlobal
        // |
        // page
        // |
        // barPlot
        dataSets.add(optionsGlobal);
        optionsGlobal.add(page);
        page.add(barPlot);
    }

    /***************************************************************************
     * initialize DataSets
     * 
     * @param p
     *            BarPlot to associate data keys with
     * 
     * @return initialized DataSet
     **************************************************************************/
    private DataSets initDataSets(BarPlot p) {
        DataSets dataSets = new DataSets();
        // store data; use data sets unique ID as an unique key name
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        dataSets.add(initData("My data set 1", "lds1", 3), key1);
        dataSets.add(initData("My data set 2", "lds1", 3), key2);
        dataSets.add(initData("My data set 3", "lds1", 3), key3);

        p.setDataSetKeys(new Object[] { new String[] { key1, key2, key3 } });

        return dataSets;
    }

    private LabeledDoubleSeries initData(String seriesName, String labelPrefix, int count) {
        LabeledDoubleSeries lds = new LabeledDoubleSeries();
        lds.setName(seriesName);

        for (int i = 0; i < count; ++i) {
            String labelName = labelPrefix + i;
            double value = Math.random() * 10.0;
            lds.addData(value, labelName);
        }

        return lds;
    }

    private AnalysisOptions initAnalysisOptions() {
        String aOUTLINETYPE = OUTLINE_TYPE;
        String aPAGETYPE = PAGE_TYPE;
        String aBARTYPE = BAR_TYPE;
        String aXAXIS = CATEGORY_AXIS;
        String aYAXIS = NUMERIC_AXIS;
        String aPLOTTITLE = PLOT_TITLE;
        String aPLOTFOOTER = PLOT_FOOTER;
        String aLEGEND = LEGEND;

        AnalysisOptions options = new AnalysisOptions();
        options.addOption(aPAGETYPE, initPageType());
        options.addOption(aBARTYPE, initBarType());
        options.addOption(aOUTLINETYPE, initOutlineType());
        options.addOption(aXAXIS, initXAxis());
        options.addOption(aYAXIS, initYAxis());
        options.addOption(aPLOTTITLE, initPlotTitle());
        options.addOption(aPLOTFOOTER, initPlotFooter());
        options.addOption(aLEGEND, initLegend());
        options.addOption(DISPLAY_SIZE_TYPE, initDisplaySizeType());
        return options;
    }

    private Object initDisplaySizeType() {
        DisplaySizeType defaultDisplaySizeType = new DisplaySizeType();
        defaultDisplaySizeType.setEnable(false);
        /*
         * defaultDisplaySizeType.setFigure(0.0, 1.0, 0.0, 1.0);
         * defaultDisplaySizeType.setPlot(0.15, 0.70, 0.15, 0.85,
         * DisplaySizeType.FOF); defaultDisplaySizeType.setMarginOuter(0.05,
         * 0.05, 0.05, 0.05, DisplaySizeType.NDC);
         */
        return defaultDisplaySizeType;
    }

    private PageType initPageType() {
        PageType pageType = new PageType();
        pageType.setForm(form);
        pageType.setFilename(file);
        pageType.setDeleteTemporaryFileOnExit(flag);
        return pageType;
    }

    /***************************************************************************
     * create and initialize a BarType Object
     * 
     * @return initialized BarType Object
     **************************************************************************/
    private BarType initBarType() {
        BarType barType = new BarType();
        barType.setColor(new Color[] { Color.blue, Color.red, Color.green });
        barType.setBorderColor(new Color[] { Color.green });
        barType.setHorizontal(true);
        barType.setStacked(true);
        barType.setCategoriesSpanDataSets(false);
        barType.setSpaceBetweenBars(1.0);
        barType.setSpaceBetweenCategories(1.0);
        barType.setWidth(new double[] { 0.5 });

        return barType;
    }

    private OutlineType initOutlineType() {
        OutlineType outlineType = new OutlineType();
        return outlineType;
    }

    private Text initXAxisLabel() {
        Text text = new Text();
        text.setTextString("The X Axis Label");
        text.setColor(java.awt.Color.green);
        text.setPosition(Text.CENTER, 0.5, 0.5);
        text.setEnable(true);
        text.setTextExpansion(2.25);
        text.setTextDegreesRotation(0.0);
        text.setTypeface("sans serif");
        text.setStyle("italic");
        return text;
    }

    private AxisCategory initXAxis() {
        AxisCategory axis = new AxisCategory();
        axis.setAxisLabelText(initXAxisLabel());
        axis.setEnableAxis(true);
        axis.setDrawTickMarks(true);
        axis.setDrawTickMarkLabels(true);
        axis.setDrawTickMarkLabelsPerpendicularToAxis(false);
        axis.setAxisColor(java.awt.Color.red);
        axis.setTickMarkLabelColor(java.awt.Color.blue);
        axis.setTickMarkFont(ITALIC_TEXT);
        axis.setTickMarkLabelExpansion(3.0);
        return axis;
    }

    private Text initYAxisLabel() {
        Text text = new Text();
        text.setTextString("The Y Axis Label");
        text.setColor(java.awt.Color.red);
        text.setPosition(Text.CENTER, 0.5, 0.5);
        text.setEnable(true);
        text.setTextExpansion(1.0);
        text.setTextDegreesRotation(-90.0);
        text.setTypeface("sans serif");
        text.setStyle("italic");

        return text;
    }

    /***************************************************************************
     * create and initialize an Y AxisNumeric Object
     * 
     * @return initialized Y AxisNumeric Object
     **************************************************************************/
    private AxisNumeric initYAxis() {
        AxisNumeric axis = new AxisNumeric();
        axis.setAxisLabelText(initYAxisLabel());
        axis.setEnableAxis(true);
        axis.setDrawTickMarks(true);
        axis.setDrawTickMarkLabels(true);
        axis.setDrawTickMarkLabelsPerpendicularToAxis(false);
        axis.setAxisColor(java.awt.Color.red);
        axis.setTickMarkLabelColor(java.awt.Color.blue);
        axis.setTickMarkFont(ITALIC_TEXT);
        axis.setTickMarkLabelExpansion(1.0);
        axis.setGridlineStyle(Axis.DOTTED);
        axis.setGridTickmarkLength(2.0);
        axis.setGridlineWidth(1.0);
        axis.setGridTickmarkEnable(true);
        axis.setGridEnable(true);
        axis.setGridColor(java.awt.Color.yellow);
        axis.setAxisRange(new Double(0), new Double(25));
        axis.setGrid();
        axis.setLogScale(false);

        return axis;
    }

    private Text initPlotTitle() {
        Text text = new Text();
        text.setTextString("Default Title Text");

        return text;
    }

    private Text initPlotFooter() {
        Text text = new Text();
        text.setTextString("My Foot");

        return text;
    }

    private Legend initLegend() {
        Legend legend = new Legend();
        legend.setPosition("R", 1.75, 0.5, 0.5);
        legend.setCharacterExpansion(0.9);
        legend.setNumberColumns(1);
        legend.setHorizontal(false);
        legend.setXInterspacing(1.2);
        legend.setYInterspacing(1.2);

        return legend;
    }

    private List generateCommands() {
        ArrayList commands = new ArrayList();
        ArrayList allPages = new ArrayList();
        dataSets.getPages(allPages);

        for (int i = 0; i < allPages.size(); i++) {
            RCommandGenerator nv = new RCommandGenerator();
            Page currentPage = (Page) allPages.get(i);
            currentPage.accept(nv);
            commands.addAll(nv.getCommands());
        }

        return commands;
    }
}
