package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlMeasureMonth;
import gov.epa.emissions.framework.services.cost.ControlMeasureNEIDevice;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class CMSummaryRecordReader {

    private CMFileFormat fileFormat;

    private Pollutants pollutants;

    private ControlTechnologies controlTechnologies;

    private SourceGroups sourceGroups;

    private Sectors sectors;

    private ControlMeasureClasses controlMeasureClasses;

    private List namesList;

    private List abbrevList;

    private CMAddImportStatus cmAddImportStatus;

    private int errorCount = 0;

    private int errorLimit = 100;

    public CMSummaryRecordReader(CMFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) {
        this.fileFormat = fileFormat;
        this.cmAddImportStatus = new CMAddImportStatus(user, sessionFactory);
        pollutants = new Pollutants(sessionFactory);
        controlTechnologies = new ControlTechnologies(sessionFactory);
        sourceGroups = new SourceGroups(sessionFactory);
        sectors = new Sectors(sessionFactory);
        controlMeasureClasses = new ControlMeasureClasses(sessionFactory);
        this.namesList = new ArrayList();
        this.abbrevList = new ArrayList();
    }

    public ControlMeasure parse(Record record, int lineNo) throws ImporterException {
        String[] tokens = null;
        try {
            tokens = modify(record);
            return measure(tokens, lineNo);
        } catch (ImporterException e) {
            cmAddImportStatus.addStatus(lineNo, new StringBuffer(format(e.getMessage())));
            throw e;
//            return null;
        }
    }

    private ControlMeasure measure(String[] tokens, int lineNo) throws ImporterException {
        StringBuffer sb = new StringBuffer();

        ControlMeasure cm = null;
        if (constraintCheck(tokens, sb)) {
            cm = new ControlMeasure();
            name(cm, tokens[0]);
            abbrev(cm, tokens[1]);
            majorPollutant(cm, tokens[2], sb);
            controlTechnology(cm, tokens[3], sb);
            sourceGroup(cm, tokens[4], sb);
            sector(cm, tokens[5], sb);
            cmClass(cm, tokens[6], sb);
            equipLife(cm, tokens[7], sb);
            neiDeviceCodes(cm, tokens[8], sb);
            dateReviewed(cm, tokens[9], sb);
            datasource(cm, tokens[10]);
            if (tokens.length > 12) {
                month(cm, tokens[11], sb);
                description(cm, tokens[12]);
            } else {
                month(cm, "", sb);
                description(cm, tokens[11]);
            }
        }
        if (sb.length() > 0) {
            errorCount++;
            cmAddImportStatus.addStatus(lineNo, sb);
        }
        if (errorCount >= errorLimit) throw new ImporterException("The maximum allowable error limit (" + errorLimit + ") has been reached while parsing the control measure summary records.");
        return cm;
    }

    private boolean constraintCheck(String[] tokens, StringBuffer sb) {
        if (tokens[0].length() == 0) {
            sb.append(format("name should not be empty"));
            return false;
        }

        if (tokens[1].length() == 0) {
            sb.append(format("abbreviation should not be empty"));
            return false;
        }
        if (namesList.contains(tokens[0])) {
            sb.append(format("name already in the file: " + tokens[0]));
            return false;
        }
        namesList.add(tokens[0]);

        if (abbrevList.contains(tokens[1])) {
            sb.append(format("abbreviation already in the file: " + tokens[1]));
            return false;
        }
        namesList.add(tokens[1]);
        return true;

    }

    private void name(ControlMeasure cm, String token) {
        cm.setName(token.replaceAll("\"\"", "\""));
    }

    private void abbrev(ControlMeasure cm, String token) {
        cm.setAbbreviation(token);
    }

    private void majorPollutant(ControlMeasure cm, String name, StringBuffer sb) {
        if (name.length() == 0) {
            sb.append(format("major pollutant should not be empty"));
            return;
        }

        try {
            Pollutant pollutant = pollutants.getPollutant(name);
            cm.setMajorPollutant(pollutant);
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void controlTechnology(ControlMeasure cm, String name, StringBuffer sb) {
        if (name.length() == 0)
            return;

        try {
            ControlTechnology ct = controlTechnologies.getControlTechnology(name);
            cm.setControlTechnology(ct);
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void sourceGroup(ControlMeasure cm, String name, StringBuffer sb) {
        if (name.length() == 0)
            return;

        try {
            SourceGroup sourceGroup = sourceGroups.getSourceGroup(name);
            cm.setSourceGroup(sourceGroup);
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void sector(ControlMeasure cm, String name, StringBuffer sb) {
        if (name.length() == 0)
            return;

        try {
            Sector[] sectors = getSectors(name);
            cm.setSectors(sectors);
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void month(ControlMeasure cm, String months, StringBuffer sb) {
        List<ControlMeasureMonth> monthList = new ArrayList<ControlMeasureMonth>();
        if (months.length() == 0) {
            ControlMeasureMonth month = new ControlMeasureMonth();
            month.setMonth((short)0);
            monthList.add(month);
        } else {
            StringTokenizer stringTokenizer = new StringTokenizer(months, "|");
            while (stringTokenizer.hasMoreTokens()) {
                short monthNo = Short.parseShort(stringTokenizer.nextToken());
                //monthNo = -1 -- means the measure is not applicable for any month
                if (monthNo == -1) {
                    ControlMeasureMonth month = new ControlMeasureMonth();
                    month.setMonth(monthNo);
                    //make sure you get rid of any months already added
                    monthList.clear();
                    monthList.add(month);
                    break;
                } else if (monthNo >= 1 && monthNo <= 12) {
                    ControlMeasureMonth month = new ControlMeasureMonth();
                    month.setMonth(monthNo);
                    monthList.add(month);
                } else {
                    sb.append(format("Unknown month, month must be between 1 and 12"));
                    return;
                }
            }
        }
        cm.setMonths(monthList.toArray(new ControlMeasureMonth[0]));
    }

    private Sector[] getSectors(String name) throws CMImporterException {
        if (name.indexOf("|") < 0)
            return new Sector[] { sectors.getSector(name.trim().toLowerCase()) };
        
        StringTokenizer st = new StringTokenizer(name, "|");
        String[] names = new String[st.countTokens()];
        Sector[] sarray = new Sector[names.length];
        
        for (int i = 0; i < names.length; i++)
            names[i] = st.nextToken().trim().toLowerCase();
        
        for (int i = 0; i < names.length; i++) {
            sarray[i] = sectors.getSector(names[i].trim().toLowerCase());
        }
        
        return sarray;
    }

    private void equipLife(ControlMeasure cm, String equipLife, StringBuffer sb) {
        try {
            if (equipLife != null 
                    && !equipLife.trim().isEmpty() 
                    && !equipLife.trim().toLowerCase().equals("null")
                    && !equipLife.trim().equals("0")
                    && !new Float(equipLife.trim()).equals(new Float(0)))
                cm.setEquipmentLife(new Float(equipLife.trim()));
        } catch (NumberFormatException e) {
            sb.append(format("equip life should be a floating point value: " + equipLife));
        }
    }

    private void cmClass(ControlMeasure cm, String clazz, StringBuffer sb) {
        if (clazz.length() == 0) {
            sb.append(format("class should not be empty"));
            return;
        }
        try {
            ControlMeasureClass cmClass = controlMeasureClasses.getControlMeasureClass(clazz);
            cm.setCmClass(cmClass);
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void neiDeviceCodes(ControlMeasure cm, String neiDeviceCodeList, StringBuffer sb) {
        List<ControlMeasureNEIDevice> neiDeviceList = new ArrayList<ControlMeasureNEIDevice>();
        if (neiDeviceCodeList.length() > 0) {
            StringTokenizer stringTokenizer = new StringTokenizer(neiDeviceCodeList, "|");
            while (stringTokenizer.hasMoreTokens()) {
                try {
                    int neiDeviceCode = Integer.parseInt(stringTokenizer.nextToken());
                    ControlMeasureNEIDevice neiDevice = new ControlMeasureNEIDevice();
                    neiDevice.setNeiDeviceCode(neiDeviceCode);
                    neiDeviceList.add(neiDevice);
                  } catch (NumberFormatException e) {
                      sb.append(format("device code(s) must be integer values: " + neiDeviceCodeList));
                  }
            }
        }
        cm.setNeiDevices(neiDeviceList.toArray(new ControlMeasureNEIDevice[0]));
    }

    private void dateReviewed(ControlMeasure cm, String date, StringBuffer sb) {
        if (date == null || date.trim().length() == 0)
            return;
        try {
            if (date.length() != 4) throw new ParseException("Invalid year format.", 5);
            Date dateReviewed = CustomDateFormat.parse_YYYY(date);
            cm.setDateReviewed(dateReviewed);
        } catch (ParseException e) {
            try {
                Date dateReviewed = CustomDateFormat.parse_MMddyyyy(date);
                cm.setDateReviewed(dateReviewed);
            } catch (ParseException e2) {
                sb.append(format("expected date format YYYY or MM/DD/YYYY, but was: " + date));
            }
        }
    }

    private void datasource(ControlMeasure cm, String dataSouce) {
        cm.setDataSouce(dataSouce);
    }

    private void description(ControlMeasure cm, String description) {
        cm.setDescription(description);
    }

    private String[] modify(Record record) throws ImporterException {
        int sizeDiff = fileFormat.cols().length - record.getTokens().length;
        if (sizeDiff == 0)
            return record.getTokens();

        if (sizeDiff > 0) {
            for (int i = 0; i < sizeDiff; i++) {
                record.add("");
            }
            return record.getTokens();
        }

        throw new ImporterException("The new record has extra tokens");
    }

    private String format(String text) {
        return cmAddImportStatus.format(text);
    }

    public int getErrorCount() {
        return errorCount;
    }
}
