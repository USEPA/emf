package gov.epa.emissions.framework.services.cost.controlmeasure;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.FileDownload;
import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.AggregateEfficiencyRecordDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.ControlMeasureMonth;
import gov.epa.emissions.framework.services.cost.ControlMeasureProperty;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.cost.data.SumEffRec;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Jpeg;
import com.itextpdf.text.List;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

/*
 * 
 * This report will produce a report for one or more control measures, the older report just produces the
 * report for only one measure. 
 * 
 */
public class ControlMeasuresPDFReport implements Runnable {

    private File outputFile;
    
    private String outputFileName;

    private ControlMeasure controlMeasure;

//    private Map<String, ControlMeasureProperty> propertyMap;

    private User user;

    private Scc[] sccs;

    private EfficiencyRecord[] efficiencyRecords;

    private int[] controlMeasureIds;

    private ControlMeasureService service;

    private StatusDAO statusDao;

    private FileDownloadDAO fileDownloadDao;

    private int efficiencyRecordCount;

    private AggregateEfficiencyRecordDAO aggregateEfficiencyRecordDao;

    // private Map<String, String> nameToPrettyNameMap;

    public static final String N_A_STRING = "N/A";

    public static final Font HEADING_FONT = new Font(FontFamily.HELVETICA, 16, Font.BOLD);

    public static final Font SUB_HEADING_FONT = new Font(FontFamily.HELVETICA, 14, Font.BOLD);

    public static final Font TITLE_FONT = new Font(FontFamily.HELVETICA, 14, Font.BOLD);

    public static final Font BOLD_FONT = new Font(FontFamily.HELVETICA, 10, Font.BOLD);

    public static final Font REGULAR_FONT = new Font(FontFamily.HELVETICA, 10, Font.NORMAL);

    public static final Format COST_FORMAT = new DecimalFormat("$#,##0");

    public static final BaseColor COLORED_BORDER = BaseColor.BLACK;

    public static final BaseColor COLORLESS_BORDER = BaseColor.WHITE;

    private DbServerFactory dbServerFactory;

    private DbServer dbServer;

    private SumEffRec[] aggEfficiencyRecords;
    
    public ControlMeasuresPDFReport(User user, int[] controlMeasureIds, 
            ControlMeasureService service, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory) {
        this.user = user;
        this.controlMeasureIds = controlMeasureIds;
        this.service = service;
        this.statusDao = new StatusDAO(sessionFactory);
        this.fileDownloadDao = new FileDownloadDAO(sessionFactory);
        this.aggregateEfficiencyRecordDao = new AggregateEfficiencyRecordDAO();
        this.dbServerFactory = dbServerFactory;
        this.outputFile = new File(temporaryFile());
        
    }

    private String temporaryFile() {
        String separator = File.separator; 
  
        String exportDir = "";
        try {
            exportDir = fileDownloadDao.getDownloadExportFolder() + "/" + user.getUsername();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        File tempDirFile = new File(exportDir);

        if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
        {
            setStatus("Error: Import-export temporary folder does not exist or lacks write permissions: "
                    + exportDir);
        }
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        this.outputFileName = "cmdb_ataglance_" + timeStamp + ".pdf";
        return exportDir + separator + this.outputFileName; // this is how exported file name was
    }

//    public void setOutputFile(File outputFile) {
//        this.outputFile = outputFile;
//    }

    private Document initializeDocument() throws FileNotFoundException, DocumentException {

        String title = "Control Measure AT-A-GLANCE Report";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(this.outputFile));

        document.open();

        document.addTitle(title);
        document.addAuthor(this.user.getUsername());
        document.addCreator("CoST");
        document.addKeywords("CoST, Report");
        document.addCreationDate();

        return document;
    }

    private void createHeader(Document document) throws DocumentException, MalformedURLException, IOException {

        Image image = new Jpeg(this.getClass().getResource("/epa-logo.gif"));
        image.scalePercent(25);
        image.setAlignment(Element.ALIGN_CENTER);
        document.add(image);

        String headingString = "Control Measure AT-A-GLANCE Report";
        Font font = new Font(FontFamily.HELVETICA, 18, Font.BOLD);
        font.setColor(BaseColor.BLUE.darker().darker());
        Paragraph heading = new Paragraph(headingString, font);
        heading.setAlignment(Element.ALIGN_CENTER);
        document.add(heading);

        String sunHeadingString = "There are " + controlMeasureIds.length + " control measures included in this report";
        Paragraph sunHeading = new Paragraph(sunHeadingString, new Font(FontFamily.HELVETICA, 12, Font.NORMAL));
        sunHeading.setAlignment(Element.ALIGN_CENTER);
        document.add(sunHeading);
    }

    private void createSectionBreak(Document document) throws DocumentException {
        this.createSectionBreak(document, 10, 10, 1, 100);
    }

    private void createNewPageBreak(Document document) throws DocumentException {
        document.newPage();
    }

    private void createSectionBreak(Document document, float spacingBefore, float spacingAfter, float lineWidth,
            float percentage) throws DocumentException {

        Paragraph beforeSeparator = new Paragraph();
        beforeSeparator.setSpacingBefore(spacingBefore);
        document.add(beforeSeparator);

        LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setAlignment(Element.ALIGN_CENTER);
        lineSeparator.setLineWidth(lineWidth);
        lineSeparator.setPercentage(percentage);
        document.add(lineSeparator);

        Paragraph afterSeparator = new Paragraph();
        afterSeparator.setSpacingBefore(spacingAfter);
        document.add(afterSeparator);
    }

    private void createSectionTitle(Document document, String title) throws DocumentException {

        Paragraph paragraph = new Paragraph(title + ":", TITLE_FONT);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setSpacingBefore(-5);
        paragraph.setSpacingAfter(10);
        paragraph.setIndentationLeft(0);
        document.add(paragraph);
    }

    private void createEmptySectionContent(Document document) throws DocumentException {
        this.createEmptySectionContent(document, N_A_STRING);
    }

    private void createEmptySectionContent(Document document, String text) throws DocumentException {

        Paragraph paragraph = new Paragraph(text, REGULAR_FONT);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setIndentationLeft(20);
        document.add(paragraph);
    }

    private void createSummarySection(Document document) throws DocumentException {

        this.createSectionTitle(document, "Summary");

        BaseColor borderColor = COLORLESS_BORDER;

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1, 3f });

        int tablePaddingRight = 12;

        PdfPCell labelCell = new PdfPCell(new Phrase("Control Measure Name:", BOLD_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(tablePaddingRight);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(controlMeasure.getName(), REGULAR_FONT));
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);

        // labelCell = new PdfPCell(new Phrase(this.nameToPrettyNameMap.get("RULE") + ":", BOLD_FONT));
        // labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        // labelCell.setPaddingRight(tablePaddingRight);
        // labelCell.setBorderColor(borderColor);
        // table.addCell(labelCell);
        //
        // valueCell = new PdfPCell(new Phrase(this.propertyMap.get("RULE").getValue(), REGULAR_FONT));
        // valueCell.setBorderColor(borderColor);
        // table.addCell(valueCell);
        //
        // labelCell = new PdfPCell(new Phrase(this.nameToPrettyNameMap.get("APPLICATION") + ":", BOLD_FONT));
        // labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        // labelCell.setPaddingRight(tablePaddingRight);
        // labelCell.setBorderColor(borderColor);
        // table.addCell(labelCell);
        //
        // valueCell = new PdfPCell(new Phrase(this.propertyMap.get("APPLICATION").getValue(), REGULAR_FONT));
        // valueCell.setBorderColor(borderColor);
        // table.addCell(valueCell);

        labelCell = new PdfPCell(new Phrase("Abbreviation:", BOLD_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(tablePaddingRight);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(controlMeasure.getAbbreviation(), REGULAR_FONT));
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Phrase("Description:", BOLD_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(tablePaddingRight);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        String description = controlMeasure.getDescription();
        valueCell = new PdfPCell(new Phrase((description != null & description.trim().length() > 0) ? description
                : N_A_STRING, REGULAR_FONT));
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Phrase("Class:", BOLD_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(tablePaddingRight);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(controlMeasure.getCmClass().getName(), REGULAR_FONT));
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Phrase("Pollutant:", BOLD_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(tablePaddingRight);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(controlMeasure.getMajorPollutant().getName(), REGULAR_FONT));
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Phrase("Equipment Life:", BOLD_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(tablePaddingRight);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(this.getFloatAsString(controlMeasure.getEquipmentLife()) + " years",
                REGULAR_FONT));
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Phrase("Control Technology:", BOLD_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(tablePaddingRight);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(this.getControlTechnologyAsString(controlMeasure.getControlTechnology()),
                REGULAR_FONT));
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Phrase("Source Group:", BOLD_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(tablePaddingRight);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(this.getSourceGroupAsString(controlMeasure.getSourceGroup()), REGULAR_FONT));
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Phrase("Sectors:", BOLD_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(tablePaddingRight);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(this.getSectorsAsString(controlMeasure.getSectors()), REGULAR_FONT));
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Phrase("Months:", BOLD_FONT));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingRight(tablePaddingRight);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Phrase(this.getMonthsAsString(controlMeasure.getMonths()), REGULAR_FONT));
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);

        document.add(table);
    }

    private void createSCCsSection(Document document) throws DocumentException, EmfException {

        this.createSectionTitle(document, "Affected SCCs");
//        Scc[] sccs = this.getSCCs(session, controlMeasure.getId());
        if (sccs == null || sccs.length == 0) {
            this.createEmptySectionContent(document);
        } else {

            BaseColor borderColor = COLORED_BORDER;

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1, 6f });
            table.getDefaultCell().setBorderColor(borderColor);

            PdfPCell nameCell = new PdfPCell(new Phrase("Code", BOLD_FONT));
            nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            nameCell.setVerticalAlignment(Element.ALIGN_CENTER);
            nameCell.setPadding(5);
            nameCell.setBackgroundColor(BaseColor.GRAY.brighter());
            nameCell.setBorderColor(borderColor);
            table.addCell(nameCell);

            PdfPCell descCell = new PdfPCell(new Phrase("Description", BOLD_FONT));
            descCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            descCell.setVerticalAlignment(Element.ALIGN_CENTER);
            descCell.setPadding(5);
            descCell.setBackgroundColor(BaseColor.GRAY.brighter());
            descCell.setBorderColor(borderColor);
            table.addCell(descCell);

            for (Scc scc : sccs) {

                nameCell = new PdfPCell(new Phrase(scc.getCode(), REGULAR_FONT));
                nameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                nameCell.setVerticalAlignment(Element.ALIGN_CENTER);
                nameCell.setPadding(5);
                nameCell.setBorderColor(borderColor);
                table.addCell(nameCell);

                descCell = new PdfPCell(new Phrase(scc.getDescription(), REGULAR_FONT));
                descCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                descCell.setVerticalAlignment(Element.ALIGN_CENTER);
                descCell.setPadding(5);
                descCell.setBorderColor(borderColor);
                table.addCell(descCell);
            }

            document.add(table);
        }
    }

    private void createEquationsSection(Document document) throws DocumentException {

        this.createSectionTitle(document, "Cost Equations");
        ControlMeasureEquation[] equations = this.controlMeasure.getEquations();
        if (equations == null || equations.length == 0) {
            this.createEmptySectionContent(document);
        } else {

            for (ControlMeasureEquation equation : equations) {

                BaseColor borderColor = COLORLESS_BORDER;

                PdfPTable topTable = new PdfPTable(2);
                topTable.setWidthPercentage(100);
                topTable.setWidths(new float[] { 1, 4 });

                PdfPCell labelCell = new PdfPCell(new Phrase("Name:", BOLD_FONT));
                labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                labelCell.setVerticalAlignment(Element.ALIGN_CENTER);
                labelCell.setPadding(5);
                labelCell.setBorderColor(borderColor);
                topTable.addCell(labelCell);

                PdfPCell valueCell = new PdfPCell(new Phrase(equation.getEquationType().getName(), REGULAR_FONT));
                valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                valueCell.setVerticalAlignment(Element.ALIGN_CENTER);
                valueCell.setPadding(5);
                valueCell.setBorderColor(borderColor);
                topTable.addCell(valueCell);

                labelCell = new PdfPCell(new Phrase("Description:", BOLD_FONT));
                labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                labelCell.setVerticalAlignment(Element.ALIGN_CENTER);
                labelCell.setPadding(5);
                labelCell.setBorderColor(borderColor);
                topTable.addCell(labelCell);

                valueCell = new PdfPCell(new Phrase(equation.getEquationType().getDescription(), REGULAR_FONT));
                valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                valueCell.setVerticalAlignment(Element.ALIGN_CENTER);
                valueCell.setPadding(5);
                valueCell.setBorderColor(borderColor);
                topTable.addCell(valueCell);

                labelCell = new PdfPCell(new Phrase("Inventory Fields:", BOLD_FONT));
                labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                labelCell.setVerticalAlignment(Element.ALIGN_CENTER);
                labelCell.setPadding(5);
                labelCell.setBorderColor(borderColor);
                topTable.addCell(labelCell);

                valueCell = new PdfPCell(new Phrase(equation.getEquationType().getInventoryFields() + "", REGULAR_FONT));
                valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                valueCell.setVerticalAlignment(Element.ALIGN_CENTER);
                valueCell.setPadding(5);
                valueCell.setBorderColor(borderColor);
                topTable.addCell(valueCell);

                labelCell = new PdfPCell(new Phrase("Formula:", BOLD_FONT));
                labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                labelCell.setVerticalAlignment(Element.ALIGN_CENTER);
                labelCell.setPadding(5);
                labelCell.setBorderColor(borderColor);
                topTable.addCell(labelCell);

                valueCell = new PdfPCell(new Phrase(equation.getEquationType().getEquation() + "", REGULAR_FONT));
                valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                valueCell.setVerticalAlignment(Element.ALIGN_CENTER);
                valueCell.setPadding(5);
                valueCell.setBorderColor(borderColor);
                topTable.addCell(valueCell);

                document.add(topTable);

                borderColor = COLORED_BORDER;

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[] { 4, 2 });

                PdfPCell variableNameCell = new PdfPCell(new Phrase("Variable Name", BOLD_FONT));
                variableNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                variableNameCell.setVerticalAlignment(Element.ALIGN_CENTER);
                variableNameCell.setPadding(5);
                variableNameCell.setBackgroundColor(BaseColor.GRAY.brighter());
                variableNameCell.setBorderColor(borderColor);
                table.addCell(variableNameCell);

                valueCell = new PdfPCell(new Phrase("Value", BOLD_FONT));
                valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                valueCell.setVerticalAlignment(Element.ALIGN_CENTER);
                valueCell.setPadding(5);
                valueCell.setBackgroundColor(BaseColor.GRAY.brighter());
                valueCell.setBorderColor(borderColor);
                table.addCell(valueCell);

                /*
                 * add Pollutant
                 */
                variableNameCell = new PdfPCell(new Phrase("Pollutant", REGULAR_FONT));
                variableNameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                variableNameCell.setVerticalAlignment(Element.ALIGN_CENTER);
                variableNameCell.setPadding(5);
                variableNameCell.setBorderColor(borderColor);
                table.addCell(variableNameCell);

                valueCell = new PdfPCell(new Phrase(equation.getPollutant().getName(), REGULAR_FONT));
                valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                valueCell.setVerticalAlignment(Element.ALIGN_CENTER);
                valueCell.setPadding(5);
                valueCell.setBorderColor(borderColor);
                table.addCell(valueCell);

                /*
                 * add Cost Year
                 */
                variableNameCell = new PdfPCell(new Phrase("Cost Year", REGULAR_FONT));
                variableNameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                variableNameCell.setVerticalAlignment(Element.ALIGN_CENTER);
                variableNameCell.setPadding(5);
                variableNameCell.setBorderColor(borderColor);
                table.addCell(variableNameCell);

                valueCell = new PdfPCell(new Phrase(Integer.toString(equation.getCostYear()), REGULAR_FONT));
                valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                valueCell.setVerticalAlignment(Element.ALIGN_CENTER);
                valueCell.setPadding(5);
                valueCell.setBorderColor(borderColor);
                table.addCell(valueCell);

                EquationTypeVariable[] equationTypeVariables = equation.getEquationType().getEquationTypeVariables();
                for (EquationTypeVariable equationTypeVariable : equationTypeVariables) {

                    String variableName = equationTypeVariable.getName();
                    String value = "";
                    switch (equationTypeVariable.getFileColPosition()) {
                    case 1:
                        value = this.getDoubleAsString(equation.getValue1(), "");
                        break;
                    case 2:
                        value = this.getDoubleAsString(equation.getValue2(), "");
                        break;
                    case 3:
                        value = this.getDoubleAsString(equation.getValue3(), "");
                        break;
                    case 4:
                        value = this.getDoubleAsString(equation.getValue4(), "");
                        break;
                    case 5:
                        value = this.getDoubleAsString(equation.getValue5(), "");
                        break;
                    case 6:
                        value = this.getDoubleAsString(equation.getValue6(), "");
                        break;
                    case 7:
                        value = this.getDoubleAsString(equation.getValue7(), "");
                        break;
                    case 8:
                        value = this.getDoubleAsString(equation.getValue8(), "");
                        break;
                    case 9:
                        value = this.getDoubleAsString(equation.getValue9(), "");
                        break;
                    case 10:
                        value = this.getDoubleAsString(equation.getValue10(), "");
                        break;
                    case 11:
                        value = this.getDoubleAsString(equation.getValue11(), "");
                        break;

                    default:
                        break;
                    }

                    variableNameCell = new PdfPCell(new Phrase(variableName, REGULAR_FONT));
                    variableNameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    variableNameCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    variableNameCell.setPadding(5);
                    variableNameCell.setBorderColor(borderColor);
                    table.addCell(variableNameCell);

                    valueCell = new PdfPCell(new Phrase(value, REGULAR_FONT));
                    valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    valueCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    valueCell.setPadding(5);
                    valueCell.setBorderColor(borderColor);
                    table.addCell(valueCell);
                }

                document.add(table);
            }

        }
    }

    private void createAlternate2EfficiencyRecordsSection(Document document) throws DocumentException, EmfException {

        this.createSectionTitle(document, "Affected Pollutants, and their Control Efficiencies and Costs");

//        EfficiencyRecord[] efficiencyRecords = this.getEfficiencyRecords(session, controlMeasure.getId());
        if (efficiencyRecords == null || efficiencyRecords.length == 0) {
            this.createEmptySectionContent(document);
        } else {


            BaseColor borderColor = COLORED_BORDER;

            int labelHAlign = Element.ALIGN_RIGHT;
            int labelVAlign = Element.ALIGN_CENTER;
            int valueHAlign = Element.ALIGN_CENTER;
            int valueVAlign = Element.ALIGN_CENTER;
            int padding = 4;

            java.util.List<PdfPCell> pollutantList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> localeList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> effectiveList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> costYearList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> cptList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> refYrCPTList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> controlEfficiencyList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> minEmisList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> maxEmisList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> ruleEffectivenessList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> rulePenetrationList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> equationTypeList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> capRecFacList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> discountRateList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> capAnnRatList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> incCPTList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> detailsList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> existingMeasureList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> existingNEIDevList = new ArrayList<PdfPCell>();

            BaseColor backgroundColor = BaseColor.WHITE;// new BaseColor(0xAAAAAA);

            PdfPTable table = null;
            int maxRecords = 4;
            int efficiencyRecordCount = efficiencyRecords.length;

            //reset if necessary
            maxRecords = (maxRecords > efficiencyRecordCount ? efficiencyRecordCount : maxRecords);
            if (efficiencyRecordCount > 0) {
                for (int i = 0; i < efficiencyRecordCount; i++) {
    
                    EfficiencyRecord efficiencyRecord = efficiencyRecords[i];
    
                    if (i % maxRecords == 0) {
    
                        pollutantList.clear();
                        localeList.clear();
                        effectiveList.clear();
                        costYearList.clear();
                        cptList.clear();
                        refYrCPTList.clear();
                        controlEfficiencyList.clear();
                        minEmisList.clear();
                        maxEmisList.clear();
                        ruleEffectivenessList.clear();
                        rulePenetrationList.clear();
                        equationTypeList.clear();
                        capRecFacList.clear();
                        discountRateList.clear();
                        capAnnRatList.clear();
                        incCPTList.clear();
                        detailsList.clear();
                        existingMeasureList.clear();
                        existingNEIDevList.clear();
    
                        /*
                         * create table and header
                         */
                        table = new PdfPTable(maxRecords + 1);
                        table.setWidthPercentage(100);
    
                        float[] columnWidths = new float[maxRecords + 1];
                        for (int j = 0; j < columnWidths.length; j++) {
    
                            if (j == 0) {
                                columnWidths[j] = 1.2f;
                            } else {
                                columnWidths[j] = 1;
                            }
                        }
                        table.setTotalWidth(columnWidths);
                        table.getDefaultCell().setBorderColor(borderColor);
    
                        PdfPCell labelCell = new PdfPCell(new Phrase("Pollutant:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        pollutantList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Locale:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        localeList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Effective Date:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        effectiveList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Cost Year:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        costYearList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("CPT:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        cptList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Ref Yr CPT:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        refYrCPTList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Control Efficiency:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        controlEfficiencyList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Min Emis:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        minEmisList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Max Emis:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        maxEmisList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Rule Effectiveness:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        ruleEffectivenessList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Rule Penetration:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        rulePenetrationList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Equation Type:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        equationTypeList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Capital Rec Fac:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        capRecFacList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Discount Rate:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        discountRateList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Cap Ann Ratio:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        capAnnRatList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Incrememental CPT:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        incCPTList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Details:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        detailsList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Existing Measure:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        existingMeasureList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Existing NEI Dev:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        existingNEIDevList.add(labelCell);
                    }
    
                    PdfPCell valueCell = new PdfPCell(new Phrase(efficiencyRecord.getPollutant().getName(), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    pollutantList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(efficiencyRecord.getLocale(), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    localeList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getDateAsString(efficiencyRecord.getEffectiveDate()),
                            REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    effectiveList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getIntegerAsString(efficiencyRecord.getCostYear()),
                            REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    costYearList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getDoubleAsStringSpecial(efficiencyRecord.getCostPerTon(),
                            "", COST_FORMAT), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    cptList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getDoubleAsStringSpecial(
                            efficiencyRecord.getRefYrCostPerTon(), "", COST_FORMAT), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    refYrCPTList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getDoubleAsString(efficiencyRecord.getEfficiency()),
                            REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    controlEfficiencyList.add(valueCell);
    
                    valueCell = new PdfPCell(
                            new Phrase(this.getDoubleAsString(efficiencyRecord.getMinEmis()), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    minEmisList.add(valueCell);
    
                    valueCell = new PdfPCell(
                            new Phrase(this.getDoubleAsString(efficiencyRecord.getMaxEmis()), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    maxEmisList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getFloatAsString(efficiencyRecord.getRuleEffectiveness()),
                            REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    ruleEffectivenessList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getFloatAsString(efficiencyRecord.getRulePenetration()),
                            REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    rulePenetrationList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(efficiencyRecord.getEquationType(), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    equationTypeList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getDoubleAsString(efficiencyRecord.getCapRecFactor()),
                            REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    capRecFacList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getDoubleAsString(efficiencyRecord.getDiscountRate()),
                            REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    discountRateList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this
                            .getDoubleAsString(efficiencyRecord.getCapitalAnnualizedRatio()), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    capAnnRatList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(
                            this.getDoubleAsString(efficiencyRecord.getIncrementalCostPerTon()), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    incCPTList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(efficiencyRecord.getDetail(), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    detailsList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(efficiencyRecord.getExistingMeasureAbbr(), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    existingMeasureList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getIntegerAsString(efficiencyRecord.getExistingDevCode()),
                            REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    existingNEIDevList.add(valueCell);
    
                    if ((i + 1) % maxRecords == 0) {
    
                        /*
                         * build table
                         */
                        for (PdfPCell pdfPCell : pollutantList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : localeList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : effectiveList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : costYearList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : cptList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : refYrCPTList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : controlEfficiencyList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : minEmisList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : maxEmisList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : ruleEffectivenessList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : rulePenetrationList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : equationTypeList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : capRecFacList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : discountRateList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : capAnnRatList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : incCPTList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : detailsList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : existingMeasureList) {
                            table.addCell(pdfPCell);
                        }
    
                        for (PdfPCell pdfPCell : existingNEIDevList) {
                            table.addCell(pdfPCell);
                        }
    
                        if (i > maxRecords)
                            this.createEmptySectionContent(document);

                        document.add(table);
                    }
    
                    // if (efficiencyRecord == efficiencyRecords[efficiencyRecords.length - 1]) {
                    // this.createSectionBreak(document, 10, 10, .5f, 100);
                    // }
                }

                table = new PdfPTable((efficiencyRecordCount > maxRecords ? efficiencyRecordCount % maxRecords + 1 : maxRecords + 1));
                table.setWidthPercentage(100);

                float[] columnWidths = new float[(efficiencyRecordCount > maxRecords ? efficiencyRecordCount % maxRecords + 1 : maxRecords + 1)];
                for (int j = 0; j < columnWidths.length; j++) {

                    if (j == 0) {
                        columnWidths[j] = 1.2f;
                    } else {
                        columnWidths[j] = 1;
                    }
                }
                table.setTotalWidth(columnWidths);
                table.getDefaultCell().setBorderColor(borderColor);

                /*
                 * build table
                 */
                for (PdfPCell pdfPCell : pollutantList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : localeList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : effectiveList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : costYearList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : cptList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : refYrCPTList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : controlEfficiencyList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : minEmisList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : maxEmisList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : ruleEffectivenessList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : rulePenetrationList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : equationTypeList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : capRecFacList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : discountRateList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : capAnnRatList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : incCPTList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : detailsList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : existingMeasureList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : existingNEIDevList) {
                    table.addCell(pdfPCell);
                }
    
                document.add(table);
            }
        }
    }

    private void createSummaryEfficiencyRecordsSection(Document document) throws DocumentException, EmfException {

        this.createSectionTitle(document, "Affected Pollutants, and their Control Efficiencies and Costs");

//        EfficiencyRecord[] efficiencyRecords = this.getEfficiencyRecords(session, controlMeasure.getId());
        if (aggEfficiencyRecords == null || aggEfficiencyRecords.length == 0) {
            this.createEmptySectionContent(document);
        } else {

            BaseColor borderColor = COLORED_BORDER;

            int labelHAlign = Element.ALIGN_RIGHT;
            int labelVAlign = Element.ALIGN_CENTER;
            int valueHAlign = Element.ALIGN_CENTER;
            int valueVAlign = Element.ALIGN_CENTER;
            int padding = 4;

            java.util.List<PdfPCell> pollutantList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> maxCEList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> minCEList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> avgCEList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> maxCPTList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> minCPTList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> avgCPTList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> avgREList = new ArrayList<PdfPCell>();
            java.util.List<PdfPCell> avgRPList = new ArrayList<PdfPCell>();

            BaseColor backgroundColor = BaseColor.WHITE;// new BaseColor(0xAAAAAA);

            PdfPTable table = null;
            int maxRecords = 4;
            int efficiencyRecordCount = aggEfficiencyRecords.length;

            Paragraph sunHeading = new Paragraph("This section only contains an aggregrated view of the efficiency records since the measure has a large number of efficiency records (" + this.efficiencyRecordCount + " records).", new Font(FontFamily.HELVETICA, 12, Font.NORMAL));
            sunHeading.setAlignment(Element.ALIGN_LEFT);
            document.add(sunHeading);
            
            createEmptySectionContent(document, "\n");
            
            //reset if necessary
            maxRecords = (maxRecords > efficiencyRecordCount ? efficiencyRecordCount : maxRecords);
            if (efficiencyRecordCount > 0) {
                for (int i = 0; i < efficiencyRecordCount; i++) {
    
                    SumEffRec sumEfficiencyRecord = aggEfficiencyRecords[i];
    
                    if (i % maxRecords == 0) {
    
                        //don't draw when i = 0
                        if (i != 0) {
                            
                            /*
                             * create table and header
                             */
                            table = new PdfPTable(maxRecords + 1);
                            table.setWidthPercentage(100);
        
                            float[] columnWidths = new float[maxRecords + 1];
                            for (int j = 0; j < columnWidths.length; j++) {
        
                                if (j == 0) {
                                    columnWidths[j] = 1.2f;
                                } else {
                                    columnWidths[j] = 1;
                                }
                            }
                            table.setTotalWidth(columnWidths);
                            table.getDefaultCell().setBorderColor(borderColor);
        

                            /*
                             * build table
                             */
                            for (PdfPCell pdfPCell : pollutantList) {
                                table.addCell(pdfPCell);
                            }
        
                            for (PdfPCell pdfPCell : maxCEList) {
                                table.addCell(pdfPCell);
                            }
        
                            for (PdfPCell pdfPCell : minCEList) {
                                table.addCell(pdfPCell);
                            }
        
                            for (PdfPCell pdfPCell : avgCEList) {
                                table.addCell(pdfPCell);
                            }
        
                            for (PdfPCell pdfPCell : maxCPTList) {
                                table.addCell(pdfPCell);
                            }
        
                            for (PdfPCell pdfPCell : minCPTList) {
                                table.addCell(pdfPCell);
                            }
        
                            for (PdfPCell pdfPCell : avgCPTList) {
                                table.addCell(pdfPCell);
                            }
        
                            for (PdfPCell pdfPCell : avgREList) {
                                table.addCell(pdfPCell);
                            }
        
                            for (PdfPCell pdfPCell : avgRPList) {
                                table.addCell(pdfPCell);
                            }
        
                            if (i > maxRecords)
                                this.createEmptySectionContent(document);

                            document.add(table);
                            
                            createEmptySectionContent(document, "\n");
                        }
        
                        pollutantList.clear();
                        maxCEList.clear();
                        minCEList.clear();
                        avgCEList.clear();
                        maxCPTList.clear();
                        minCPTList.clear();
                        avgCPTList.clear();
                        avgREList.clear();
                        avgRPList.clear();
    
                        PdfPCell labelCell = new PdfPCell(new Phrase("Pollutant:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        pollutantList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Maximum Control Efficiency:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        maxCEList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Minimum Control Efficiency:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        minCEList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Average Control Efficiency:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        avgCEList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Maximum CPT:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        maxCPTList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Minimum CPT:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        minCPTList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Average CPT:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        avgCPTList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Average Rule Effectiveness:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        avgREList.add(labelCell);
    
                        labelCell = new PdfPCell(new Phrase("Average Rule Penetration:", BOLD_FONT));
                        labelCell.setHorizontalAlignment(labelHAlign);
                        labelCell.setVerticalAlignment(labelVAlign);
                        labelCell.setBorderColor(borderColor);
                        labelCell.setPadding(padding);
                        labelCell.setBackgroundColor(backgroundColor);
                        avgRPList.add(labelCell);
    
                    }
    
                    PdfPCell valueCell = new PdfPCell(new Phrase(sumEfficiencyRecord.getPollutant().getName(), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    pollutantList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getFloatAsString(sumEfficiencyRecord.getMaxCE()), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    maxCEList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getFloatAsString(sumEfficiencyRecord.getMinCE()),
                            REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    minCEList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getFloatAsString(sumEfficiencyRecord.getAvgCE()),
                            REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    avgCEList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getFloatAsStringSpecial(sumEfficiencyRecord.getMaxCPT(),
                            "", COST_FORMAT), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    maxCPTList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getFloatAsStringSpecial(
                            sumEfficiencyRecord.getMinCPT(), "", COST_FORMAT), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    minCPTList.add(valueCell);
    
                    valueCell = new PdfPCell(new Phrase(this.getFloatAsStringSpecial(
                            sumEfficiencyRecord.getAvgCPT(), "", COST_FORMAT), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    avgCPTList.add(valueCell);
    
                    valueCell = new PdfPCell(
                            new Phrase(this.getFloatAsString(sumEfficiencyRecord.getAvgRE()), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    avgREList.add(valueCell);
    
                    valueCell = new PdfPCell(
                            new Phrase(this.getFloatAsString(sumEfficiencyRecord.getAvgRP()), REGULAR_FONT));
                    valueCell.setHorizontalAlignment(valueHAlign);
                    valueCell.setVerticalAlignment(valueVAlign);
                    valueCell.setBorderColor(borderColor);
                    valueCell.setPadding(padding);
                    avgRPList.add(valueCell);
    
                    // if (efficiencyRecord == efficiencyRecords[efficiencyRecords.length - 1]) {
                    // this.createSectionBreak(document, 10, 10, .5f, 100);
                    // }
                }

                table = new PdfPTable((efficiencyRecordCount > maxRecords ? (efficiencyRecordCount % maxRecords) + 1 : maxRecords + 1));
                table.setWidthPercentage(100);

                float[] columnWidths = new float[(efficiencyRecordCount > maxRecords ? (efficiencyRecordCount % maxRecords) + 1 : maxRecords + 1)];
                for (int j = 0; j < columnWidths.length; j++) {

                    if (j == 0) {
                        columnWidths[j] = 1.2f;
                    } else {
                        columnWidths[j] = 1;
                    }
                }
                table.setTotalWidth(columnWidths);
                table.getDefaultCell().setBorderColor(borderColor);

                /*
                 * build table
                 */
                for (PdfPCell pdfPCell : pollutantList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : maxCEList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : minCEList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : avgCEList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : maxCPTList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : minCPTList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : avgCPTList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : avgREList) {
                    table.addCell(pdfPCell);
                }
    
                for (PdfPCell pdfPCell : avgRPList) {
                    table.addCell(pdfPCell);
                }
    
                document.add(table);
            }
        }
    }

    private void createReferencesSection(Document document) throws DocumentException {

        this.createSectionTitle(document, "References");

        Reference[] references = controlMeasure.getReferences();
        if (references == null || references.length == 0) {
            this.createEmptySectionContent(document);
        } else {

            List list = new List(false, 10);
            list.setSymbolIndent(10);
            list.setListSymbol(new Chunk((char) 127));

            for (Reference reference : references) {

                if (reference != null) {
                    list.add(reference.getDescription());
                }
            }

            document.add(list);
        }
    }

    private void createPropertiesSection(Document document) throws DocumentException {

        this.createSectionTitle(document, "Other information");

        this.createSectionBreak(document, 5, 5, .5f, 100);

        ControlMeasureProperty[] properties = controlMeasure.getProperties();
        BaseColor borderColor = COLORLESS_BORDER;
        for (ControlMeasureProperty property : properties) {

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1, 4 });
            table.getDefaultCell().setBorderColor(borderColor);

            PdfPCell labelCell = new PdfPCell(new Phrase(this.createPropertyLabel(property), BOLD_FONT));
            labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            labelCell.setVerticalAlignment(Element.ALIGN_TOP);
            labelCell.setBorderColor(borderColor);
            labelCell.setPadding(0);
            table.addCell(labelCell);

            PdfPCell valueCell = new PdfPCell(new Phrase(this.createPropertyValue(property), REGULAR_FONT));
            valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            valueCell.setVerticalAlignment(Element.ALIGN_TOP);
            valueCell.setBorderColor(borderColor);
            valueCell.setPadding(0);
            table.addCell(valueCell);

            document.add(table);

            this.createSectionBreak(document, 5, 5, .5f, 100);
        }
    }

    private void generate() throws DocumentException, MalformedURLException, IOException {

        Document document = this.initializeDocument();

        this.createHeader(document);

        this.createSectionBreak(document);

        for (int controlMeasureId : controlMeasureIds) {
            
            this.controlMeasure = getControlMeasure(controlMeasureId);
            this.sccs = getSCCs(controlMeasureId);
            
            this.efficiencyRecordCount = getEfficiencyRecordCount(controlMeasureId);
            if (this.efficiencyRecordCount < 100) {
                this.efficiencyRecords = getEfficiencyRecords(controlMeasureId);
            } else {
                //
                this.aggEfficiencyRecords = aggregateEfficiencyRecordDao.getAggregrateEfficiencyRecords(controlMeasureId, this.dbServer);
            }
            
            this.createSummarySection(document);

            this.createSectionBreak(document);

            if (this.efficiencyRecordCount < 100) {
                this.createAlternate2EfficiencyRecordsSection(document);
            } else {
                this.createSummaryEfficiencyRecordsSection(document);
            }

            this.createSectionBreak(document);

            this.createEquationsSection(document);

            this.createSectionBreak(document);

            this.createSCCsSection(document);

            this.createSectionBreak(document);

            // this.createEfficiencyRecordsSection(document);
            //
            // this.addSectionBreak(document);

            this.createReferencesSection(document);

            this.createSectionBreak(document);

            this.createPropertiesSection(document);

            this.createNewPageBreak(document);
        }

        document.close();
    }

    private String createPropertyValue(ControlMeasureProperty property) {

        StringBuilder sb = new StringBuilder();
        String propertyValue = property.getValue();

        String units = property.getUnits();
        if (units != null && units.trim().length() > 0) {

            if (units.equals("%")) {
                sb.append(propertyValue).append(units);
            } else if (units.startsWith("$/")) {
                sb.append("$").append(propertyValue).append(units.substring(1));
            } else {
                sb.append(propertyValue).append(" ").append(units);
            }

        } else {
            sb.append(propertyValue);
        }

        return sb.toString();
    }

    private String createPropertyLabel(ControlMeasureProperty property) {

        StringBuilder sb = new StringBuilder();
        sb.append(property.getName()).append(":");

        return sb.toString();
    }

//    private void processProperties() {
//
//        this.propertyMap.clear();
//        ControlMeasureProperty[] properties = controlMeasure.getProperties();
//        for (ControlMeasureProperty property : properties) {
//            this.propertyMap.put(property.getName(), property);
//        }
//    }

    private String getSectorsAsString(Sector[] sectors) {

        StringBuilder sb = new StringBuilder();
        if (sectors == null || sectors.length == 0) {
            sb.append(N_A_STRING);
        } else {

            for (Sector sector : sectors) {
                sb.append(sector.getName()).append(", ");
            }

            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    private String getMonthsAsString(ControlMeasureMonth[] months) {

        StringBuilder sb = new StringBuilder();
        if (months == null || months.length == 0) {
            sb.append(N_A_STRING);
        } else {

            for (ControlMeasureMonth month : months) {
                sb.append(month.toString()).append(", ");
            }

            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    private String getControlTechnologyAsString(ControlTechnology controlTechnology) {
        return controlTechnology != null ? controlTechnology.getName() : N_A_STRING;
    }

    private String getDateAsString(Date date) {
        return date != null ? date.toString() : N_A_STRING;
    }

    private String getSourceGroupAsString(SourceGroup sourceGroup) {
        return sourceGroup != null ? sourceGroup.getName() : N_A_STRING;
    }

    private String getIntegerAsString(Integer i) {
        return i != null ? Integer.toString(i) : N_A_STRING;
    }

    private String getDoubleAsString(Double d) {
        return this.getDoubleAsString(d, N_A_STRING);
    }

    private String getDoubleAsString(Double d, String defaultValue) {
        return d != null ? Double.toString(d) : defaultValue;
    }

//    private String getDoubleAsString(Double d, String defaultValue, Format format) {
//        return d != null ? format.format(d) : defaultValue;
//    }

    private String getDoubleAsStringSpecial(Double d, String defaultValue, Format format) {
        return d != null ? format.format(d * 1.0) : defaultValue;
    }

    private String getFloatAsString(Float f) {
        return f != null ? Double.toString(f) : N_A_STRING;
    }

    private String getFloatAsStringSpecial(Float d, String defaultValue, Format format) {
        return d != null ? format.format(d * 1.0) : defaultValue;
    }

//    private String getDoubleAsPercentString(Double d) {
//        return d != null ? Double.toString(d * 100) + "%" : N_A_STRING;
//    }
//
//    private String getFloatAsPercentString(Float f) {
//        return f != null ? Double.toString(f * 100) + "%" : N_A_STRING;
//    }

    private Scc[] getSCCs(int controlMeasureId) throws EmfException {
        return service.getSccsWithDescriptions(controlMeasureId);
    }

    private EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId) throws EmfException {
       return service.getEfficiencyRecords(controlMeasureId);
    }

    private ControlMeasure getControlMeasure(int controlMeasureId) throws EmfException {
        return service.getMeasure(controlMeasureId);
    }

    private int getEfficiencyRecordCount(int controlMeasureId) throws EmfException {
        return service.getEfficiencyRecordCount(controlMeasureId);
    }

    private ControlMeasure[] getSummaryControlMeasures() throws EmfException {
        //build comma delimited list of measure ids to be used in WHERE filter...
        String cmIdList = Arrays.toString(controlMeasureIds);
        cmIdList = cmIdList.replace("[", "").replace("]", "");
        
        return service.getSummaryControlMeasures("cm.id in (" + cmIdList + ")");
    }
    
    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMExport");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }
    
    public void run() {
        try {
            dbServer = dbServerFactory.getDbServer();
            
            setStatus("Started generating Control Measure Control Measure AT-A-GLANCE PDF Report.  This could take several minutes to finish.");
            generate();
            //lets add a filedownload item for the user, so they can download the file
            fileDownloadDao.add(user, new Date(), this.outputFile.getName(), "PDF", true);

            
            setStatus("Completed generating Control Measure Control Measure AT-A-GLANCE PDF Report.  Report was exported to " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            setStatus("Exception generating Control Measure Control Measure AT-A-GLANCE PDF Report: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
