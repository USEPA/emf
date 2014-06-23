/*
 * $Source: /cvsroot/emisview/EMF/src/client/gov/epa/emissions/framework/client/cost/controlmeasure/ControlMeasurePDFReportGenerator.java,v $
 * $Revision: 1.6 $
 * $Author: ddelvecchio $
 * $Date: 2013/07/28 04:48:49 $
 */
package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.ControlMeasureMonth;
import gov.epa.emissions.framework.services.cost.ControlMeasureProperty;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

public class ControlMeasurePDFReportGenerator {

    private EmfSession session;

    private File outputFile;

    private ControlMeasure controlMeasure;

    private Map<String, ControlMeasureProperty> propertyMap;

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

    public ControlMeasurePDFReportGenerator() {

        this.propertyMap = new HashMap<String, ControlMeasureProperty>();
        // this.nameToPrettyNameMap = new HashMap<String, String>();
        // this.nameToPrettyNameMap.put("APPLICATION", "Application");
        // this.nameToPrettyNameMap.put("At-A-Glance Cost Basis", "Cost Basis");
        // this.nameToPrettyNameMap.put("CE_TEXT", "Control Efficiency:");
        // this.nameToPrettyNameMap.put("DISCUSSION", "Additional Information");
        // this.nameToPrettyNameMap.put("REFERENCES", "References");
        // this.nameToPrettyNameMap.put("RULE", "Rule Name");
        // this.nameToPrettyNameMap.put("At-A-Glance Cost Effectiveness", "Cost Effectiveness");
        //
        // this.nameToPrettyNameMap.put("ADMIN_PCT", "ADMIN_PCT");
        // this.nameToPrettyNameMap.put("CHEM_PCT", "CHEM_PCT");
        // this.nameToPrettyNameMap.put("CPTON_H", "CPTON_H");
        // this.nameToPrettyNameMap.put("CPTON_L", "CPTON_L");
        // this.nameToPrettyNameMap.put("CTRL_EFF_T", "CTRL_EFF_T");
        // this.nameToPrettyNameMap.put("ELEC_PCT", "ELEC_PCT");
        // this.nameToPrettyNameMap.put("ELEC_RT", "ELEC_RT");
        // this.nameToPrettyNameMap.put("FUEL_PCT", "FUEL_PCT");
        // this.nameToPrettyNameMap.put("HG_CE_T", "HG_CE_T");
        // this.nameToPrettyNameMap.put("INSRNC_PCT", "INSRNC_PCT");
        // this.nameToPrettyNameMap.put("MNTLBR_PCT", "MNTLBR_PCT");
        // this.nameToPrettyNameMap.put("MNTLBR_RT", "MNTLBR_RT");
        // this.nameToPrettyNameMap.put("MNTMTL_PCT", "MNTMTL_PCT");
        // this.nameToPrettyNameMap.put("NG_RT", "NG_RT");
        // this.nameToPrettyNameMap.put("OPLBR_PCT", "OPLBR_PCT");
        // this.nameToPrettyNameMap.put("OPLBR_RT", "OPLBR_RT");
        // this.nameToPrettyNameMap.put("OTHR_PCT", "OTHR_PCT");
        // this.nameToPrettyNameMap.put("OVRHD_PCT", "OVRHD_PCT");
        // this.nameToPrettyNameMap.put("PROPTX_PCT", "PROPTX_PCT");
        // this.nameToPrettyNameMap.put("RPLMTL_PCT", "RPLMTL_PCT");
        // this.nameToPrettyNameMap.put("SPVLBR_PCT", "SPVLBR_PCT");
        // this.nameToPrettyNameMap.put("STEAM_PCT", "STEAM_PCT");
        // this.nameToPrettyNameMap.put("TDIR_PCT", "TDIR_PCT");
        // this.nameToPrettyNameMap.put("TINDIR_PCT", "TINDIR_PCT");
        // this.nameToPrettyNameMap.put("UTIL_PCT", "UTIL_PCT");
        // this.nameToPrettyNameMap.put("WSTDSP_PCT", "WSTDSP_PCT");
    }

    public void setSession(EmfSession session) {
        this.session = session;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setControlMeasure(ControlMeasure controlMeasure) {

        this.controlMeasure = controlMeasure;
        if (this.controlMeasure != null) {
            this.processProperties();
        }
    }

    private Document initializeDocument() throws FileNotFoundException, DocumentException {

        String title = "Control Measure AT-A-GLANCE Report: " + this.controlMeasure.getName() + " ("
                + this.controlMeasure.getAbbreviation() + ")";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(this.outputFile));

        document.open();

        document.addTitle(title);
        document.addAuthor(this.session.user().getUsername());
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

        String sunHeadingString = "Control Measure: " + this.controlMeasure.getName() + " ("
                + this.controlMeasure.getAbbreviation() + ")";
        Paragraph sunHeading = new Paragraph(sunHeadingString, new Font(FontFamily.HELVETICA, 12, Font.NORMAL));
        sunHeading.setAlignment(Element.ALIGN_CENTER);
        document.add(sunHeading);
    }

    private void createSectionBreak(Document document) throws DocumentException {
        this.createSectionBreak(document, 10, 10, 1, 100);
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
        Scc[] sccs = this.getSCCs(session, controlMeasure.getId());
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

        EfficiencyRecord[] efficiencyRecords = this.getEfficiencyRecords(session, controlMeasure.getId());
        if (efficiencyRecords == null || efficiencyRecords.length == 0) {
            this.createEmptySectionContent(document);
        } else {

            BaseColor borderColor = COLORED_BORDER;

            int labelHAlign = Element.ALIGN_RIGHT;
            int labelVAlign = Element.ALIGN_CENTER;
            int valueHAlign = Element.ALIGN_CENTER;
            int valueVAlign = Element.ALIGN_CENTER;
            int padding = 4;

            int maxRecords = 4;
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
            for (int i = 0; i < efficiencyRecords.length; i++) {

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
                        "$0", COST_FORMAT), REGULAR_FONT));
                valueCell.setHorizontalAlignment(valueHAlign);
                valueCell.setVerticalAlignment(valueVAlign);
                valueCell.setBorderColor(borderColor);
                valueCell.setPadding(padding);
                cptList.add(valueCell);

                valueCell = new PdfPCell(new Phrase(this.getDoubleAsStringSpecial(
                        efficiencyRecord.getRefYrCostPerTon(), "$0", COST_FORMAT), REGULAR_FONT));
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

                    document.add(table);
                }

                // if (efficiencyRecord == efficiencyRecords[efficiencyRecords.length - 1]) {
                // this.createSectionBreak(document, 10, 10, .5f, 100);
                // }
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

    public void generate() throws DocumentException, MalformedURLException, IOException {

        Document document = this.initializeDocument();

        this.createHeader(document);

        this.createSectionBreak(document);

        this.createSummarySection(document);

        this.createSectionBreak(document);

        this.createAlternate2EfficiencyRecordsSection(document);

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

        // document.add(new Paragraph("(BTW, there's a second page!)"));
        // document.newPage();
        //
        // paragraph = new Paragraph("a CoST-generated report!");
        // document.add(paragraph);
        //
        // paragraph = new Paragraph("Here's some text, followed by 50 pixels of vertical space");
        // paragraph.setSpacingAfter(50);
        // document.add(paragraph);
        //
        // paragraph = new Paragraph("Here's a table (and this text is centered horizontally):");
        // paragraph.setAlignment(Element.ALIGN_CENTER);
        // paragraph.setSpacingAfter(10);
        // document.add(paragraph);
        //
        // BaseColor borderColor = new BaseColor(127, 63, 63);
        // float borderWidth = 1.2f;
        //
        // PdfPTable table = new PdfPTable(4);
        //
        // table.getDefaultCell().setGrayFill(0.8f);
        // table.getDefaultCell().setBorderColor(borderColor);
        // table.getDefaultCell().setBorderWidth(borderWidth);
        //
        // PdfPCell cell = new PdfPCell(new Paragraph("This is a Header Spanning Title (using Courier)", new Font(
        // FontFamily.COURIER, 14, Font.BOLD)));
        // cell.setColspan(4);
        // cell.setGrayFill(0.95f);
        // cell.setBorderColor(borderColor);
        // cell.setBorderWidth(borderWidth);
        //
        // table.addCell(cell);
        // table.addCell("1.1");
        // table.addCell("1.2");
        // table.addCell("1.3");
        // table.addCell("1.4");
        // table.addCell("2.1");
        // table.addCell("2.2");
        // table.addCell("2.3");
        // table.addCell("2.4");
        //
        // document.add(table);
        //
        // Font font = new Font(FontFamily.HELVETICA);
        // font.setStyle(Font.STRIKETHRU);
        // paragraph = new Paragraph("This is a mistake, so there is a strike through it (BTW, this text is Helvetica)",
        // font);
        // paragraph.setSpacingBefore(10);
        // paragraph.setSpacingAfter(10);
        // document.add(paragraph);
        //
        // Font underlinedFont = new Font(FontFamily.TIMES_ROMAN);
        // underlinedFont.setStyle(Font.UNDERLINE);
        // document.add(new Paragraph("Here's a list (using Times Roman):", underlinedFont));
        //
        // Font timesFont = new Font(FontFamily.TIMES_ROMAN);
        // List list = new List(true);
        // list.add(new ListItem("First Item", timesFont));
        // list.add(new ListItem("Second Item", timesFont));
        // document.add(list);
        //
        // paragraph = new Paragraph("Here's an interesting image I found:");
        // paragraph.setSpacingBefore(20);
        // document.add(paragraph);
        //
        // Image image = new Jpeg(new URL("file:///C:/epa-logo.jpg"));
        // document.add(image);
        //
        // Font anchorFont = new Font();
        // anchorFont.setStyle(Font.UNDERLINE);
        // anchorFont.setColor(31, 31, 255);
        //
        // Anchor externalLink = new Anchor("Here's an external link to iText's website (the creators of this API)",
        // anchorFont);
        // externalLink.setReference("http://www.lowagie.com/iText/");
        //
        // document.add(externalLink);
        //
        // paragraph = new Paragraph();
        // paragraph.setSpacingAfter(10);
        // paragraph.setSpacingBefore(10);
        // document.add(paragraph);
        //
        // Anchor internalLink = new Anchor("This is an internal link to the top of the document", anchorFont);
        // internalLink.setReference("#top");
        // document.add(internalLink);
        //
        // paragraph = new Paragraph("Pretty cool, huh?");
        // paragraph.setSpacingBefore(10);
        // document.add(paragraph);

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

    private void processProperties() {

        this.propertyMap.clear();
        ControlMeasureProperty[] properties = controlMeasure.getProperties();
        for (ControlMeasureProperty property : properties) {
            this.propertyMap.put(property.getName(), property);
        }
    }

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

//    private String getDoubleAsPercentString(Double d) {
//        return d != null ? Double.toString(d * 100) + "%" : N_A_STRING;
//    }
//
//    private String getFloatAsPercentString(Float f) {
//        return f != null ? Double.toString(f * 100) + "%" : N_A_STRING;
//    }

    private Scc[] getSCCs(EmfSession session, int controlMeasureId) throws EmfException {

        ControlMeasureService service = session.controlMeasureService();
        Scc[] sccs = service.getSccsWithDescriptions(controlMeasureId);

        return sccs;
    }

    private EfficiencyRecord[] getEfficiencyRecords(EmfSession session, int controlMeasureId) throws EmfException {

        ControlMeasureService service = session.controlMeasureService();
        EfficiencyRecord[] efficiencyRecords = service.getEfficiencyRecords(controlMeasureId);

        return efficiencyRecords;
    }
}
