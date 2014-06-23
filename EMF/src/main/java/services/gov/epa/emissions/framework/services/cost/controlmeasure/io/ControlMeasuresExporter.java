package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresCOPYExport;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Date;

public class ControlMeasuresExporter implements Exporter {

    private ControlMeasure[] controlMeasures;

    private File folder;

    private String prefix;
    
    private User user;

    private HibernateSessionFactory factory;
    
//    private String delimiter;
//    
//    private String[] cmAbbrevSccs;

    private long exportedLinesCount = 0;
    
    private DbServer dbServer;
    
    private PostgresCOPYExport postgresCOPYExport;
//    private EquationTypeMap equationTypeMap;
    private String idList = "-9999"; //add some unknown id, makes it easier when building the delimited list.

    public ControlMeasuresExporter(File folder, String prefix, ControlMeasure[] controlMeasures, String[] sccs,
            User user, HibernateSessionFactory factory, DbServerFactory dbServerFactory) {
        this.controlMeasures = controlMeasures;
        this.folder = folder;
        this.prefix = prefix;
        this.user = user;
        this.factory = factory;
//        this.cmAbbrevSccs = sccs;
//        this.delimiter = ",";
        this.dbServer = dbServerFactory.getDbServer();
        this.postgresCOPYExport = new PostgresCOPYExport(dbServer);
 //       this.equationTypeMap = new EquationTypeMap(getEquationTypes());
        for (ControlMeasure controlMeasure : controlMeasures) 
            idList += "," + controlMeasure.getId();
    }

    public void run() throws ExporterException {
        try {
            addStatus("Start exporting control measures to folder: " + folder.getAbsolutePath() +  ".");
            writeExportFiles();
            addStatus("Export control measures finished.");
            exportedLinesCount = controlMeasures.length;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExporterException("Export control measures failed. Reason: " + e.getMessage());
        } finally {
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

//    private PrintWriter openExportFile(String fileName) throws IOException {
//        File file = new File(folder, prefix + fileName);
//        
////        return new PrintWriter(new BufferedWriter(new FileWriter(file)));
//        return new PrintWriter(new CustomCharSetOutputStreamWriter(new FileOutputStream(file)));
//    }
    
    private void writeExportFiles() throws ExporterException {
        writeSummaryFile();
        writeEfficiencyFile();
        writeSccFile();
        writeEquationFile();
        writeReferenceFile();
        writePropertyFile();
    }

    private void writeEquationFile() throws ExporterException {
        File file = new File(folder, prefix + "_equations.csv");
        String selectQuery = 
            "   select m.abbreviation as CMAbbreviation, "
            + "     et.name as CMEqnType, "
            + "     p.name as Pollutant, "
            + "     eq.cost_year as CostYear, "
            + "     eq.value1 as Var1, "
            + "     eq.value2 as Var2, "
            + "     eq.value3 as Var3, "
            + "     eq.value4 as Var4, "
            + "     eq.value5 as Var5, "
            + "     eq.value6 as Var6, "
            + "     eq.value7 as Var7, "
            + "     eq.value8 as Var8, "
            + "     eq.value9 as Var9, "
            + "     eq.value10 as Var10, "
            + "     eq.value11 as Var11 "
            + " from emf.control_measures m "
            + "     inner join emf.control_measure_equations eq "
            + "     on eq.control_measure_id = m.id "
            + "     inner join emf.equation_types et "
            + "     on et.id = eq.equation_type_id "
            + "     inner join emf.pollutants p "
            + "     on p.id = eq.pollutant_id "
            + " where m.id in (" + idList + ")";
        
        postgresCOPYExport.export(selectQuery, file.getAbsolutePath());
        
//        PrintWriter equationWriter = openExportFile("_equations.csv");
//        CMEquationFileFormat fileFormat = new CMEquationFileFormat();
//        String[] colNames = fileFormat.cols();
//        
//        for (int i = 0; i < colNames.length; i++) {
//            if (i == colNames.length - 1) {
//                equationWriter.write(colNames[i]);
//                break;
//            }
//            
//            equationWriter.write(colNames[i] + delimiter);
//        }
//        
//        equationWriter.write(System.getProperty("line.separator"));
//        
//        for (int j = 0; j < controlMeasures.length; j++) {
//            for (ControlMeasureEquation equation : controlMeasures[j].getEquations()) {
//                equationWriter.write(equationRecord(controlMeasures[j], equation, fileFormat.cols().length));
//                equationWriter.write(System.getProperty("line.separator"));
//            }
//        }
//
//        equationWriter.close();
    }
    
//    private String equationRecord(ControlMeasure measure, ControlMeasureEquation equation, int size) {
//        String equationRecord = ""; 
//        equationRecord += addQuote(measure.getAbbreviation())+ delimiter;
//        
//        ControlMeasureEquation cMequation[]=measure.getEquations();
//        EquationType equationType=cMequation[0].getEquationType();
//		//add Equation Type
//        equationRecord += addQuote(equationType.getName())+ delimiter;
//		//add Pollutant
//        equationRecord += addQuote(equation.getPollutant().getName())+ delimiter;
//		//add Cost Year
//        equationRecord += equation.getCostYear()+ delimiter;
//
//        for (int k=0; k< cMequation.length; k++) {
//            
//            Double value = null;
//            value = cMequation[k].getValue1();
//            equationRecord += (value==null ? "" : value)+ delimiter;         
//            value = cMequation[k].getValue2();
//            equationRecord += (value==null ? "" : value)+ delimiter;         
//            value = cMequation[k].getValue3();
//            equationRecord += (value==null ? "" : value)+ delimiter;         
//            value = cMequation[k].getValue4();
//            equationRecord += (value==null ? "" : value)+ delimiter;         
//            value = cMequation[k].getValue5();
//            equationRecord += (value==null ? "" : value)+ delimiter;         
//            value = cMequation[k].getValue6();
//            equationRecord += (value==null ? "" : value)+ delimiter;         
//            value = cMequation[k].getValue7();
//            equationRecord += (value==null ? "" : value)+ delimiter;         
//            value = cMequation[k].getValue8();
//            equationRecord += (value==null ? "" : value)+ delimiter;         
//            value = cMequation[k].getValue9();
//            equationRecord += (value==null ? "" : value)+ delimiter;         
//            value = cMequation[k].getValue10();
//            equationRecord += (value==null ? "" : value);         
//        }
//        
//        return equationRecord; 
//    }
    
        
    private void writeSummaryFile() throws ExporterException {
        File file = new File(folder, prefix + "_summary.csv");
        String selectQuery = 
            "   select m.name as CMName, m.abbreviation as CMAbbreviation, p.name as MajorPoll, ct.name as ControlTechnology,sg.name as SourceGroup,"
            + "     ("
            + "         select string_agg(s.name, '|') "
            + "         from emf.control_measure_sectors cms "
            + "             inner join emf.sectors s "
            + "             on s.id = cms.sector_id "
            + "         where cms.control_measure_id = m.id "
            + "         group by cms.control_measure_id "
            + "     ) as Sector, "
            + "     cmc.name as Class, "
            + "     m.equipment_life as EquipLife, "
            + "     ("
            + "         select string_agg(nei_device_code::character varying(50), '|') "
            + "         from emf.control_measure_nei_devices cmnd "
            + "         where cmnd.control_measure_id = m.id "
            + "         group by cmnd.control_measure_id "
            + "     ) as NEIDeviceCode, "
            + "     date_part('year', m.date_reviewed) as DateReviewed, "
            + "     ( "
            + "         select string_agg(cmr.reference_id || '', '|')  "
            + "         from emf.control_measure_references cmr "
            + "         where cmr.control_measure_id = m.id "
            + "         group by cmr.control_measure_id "
            + "     ) as DataSource, "
            + "     ( "
            + "         select string_agg(cmm.\"month\" || '', '|')  "
            + "         from emf.control_measure_months cmm "
            + "         where cmm.control_measure_id = m.id "
            + "             and cmm.\"month\" <> 0 "
            + "         group by cmm.control_measure_id "
            + "     ) as Months, "
            + "     m.description as Description "
            + " from emf.control_measures m "
            + "     inner join emf.pollutants p "
            + "     on p.id = m.major_pollutant "
            + "     left outer join emf.control_technologies ct "
            + "     on ct.id = m.control_technology "
            + "     left outer join emf.source_groups sg "
            + "     on sg.id = m.source_group "
            + "     left outer join emf.control_measure_classes cmc "
            + "     on cmc.id = m.cm_class_id "
            + " where m.id in (" + idList + ")";
        postgresCOPYExport.export(selectQuery, file.getAbsolutePath());
        
/*        PrintWriter summaryWriter = openExportFile("_summary.csv");
        CMSummaryFileFormat fileFormat = new CMSummaryFileFormat();
        String[] colNames = fileFormat.cols();
        
        for (int i = 0; i < colNames.length; i++) {
            if (i == colNames.length - 1) {
                summaryWriter.write(colNames[i]);
                break;
            }
            
            summaryWriter.write(colNames[i] + delimiter);
        }
        
        summaryWriter.write(System.getProperty("line.separator"));
        
        for (int j = 0; j < controlMeasures.length; j++) {
            summaryWriter.write(summaryRecord(controlMeasures[j]));
            summaryWriter.write(System.getProperty("line.separator"));
        }
        
        summaryWriter.close();
*/    }
    
//    private String summaryRecord(ControlMeasure measure) {
//        String name = measure.getName();
//        String summaryRecord = addQuote(name) + delimiter;
//        summaryRecord += addQuote(measure.getAbbreviation())+ delimiter;
//        Pollutant majPollutant = measure.getMajorPollutant();
//        summaryRecord += (majPollutant == null ? "" : addQuote(majPollutant.getName())) + delimiter;
//        ControlTechnology ct = measure.getControlTechnology();
//        summaryRecord += (ct == null ? "" : addQuote(ct.getName())) + delimiter;
//        SourceGroup sg = measure.getSourceGroup();
//        summaryRecord += (sg == null ? "" : addQuote(sg.getName())) + delimiter;
//        summaryRecord += addQuote(formatSectors(measure.getSectors())) + delimiter;
//        summaryRecord += measure.getCmClass() + delimiter;
//        summaryRecord += measure.getEquipmentLife() + delimiter;
//        summaryRecord += /*TODO measure.getDeviceCode()*/ "" + delimiter;
//        Date dateRev = measure.getDateReviewed();
//        summaryRecord += (dateRev == null ? "" : dateRev.toString()) + delimiter;
//        summaryRecord += addQuote(measure.getDataSouce()) + delimiter;
//        summaryRecord += addQuote(measure.getDescription());
//        
//        return summaryRecord;
//    }
    
//    private String addQuote(String outString){
//        return "\"" + outString + "\"";
//    
//    }

//    private String formatSectors(Sector[] sectors) {
//        String sectorString = "";
//        
//        for (int i = 0; i < sectors.length; i++) {
//            if (i == sectors.length - 1) {
//                sectorString += sectors[i].getName();
//                break;
//            }
//            
//            sectorString += sectors[i].getName() + "|";
//        }
//        
//        return sectorString;
//    }

    private void writeEfficiencyFile() throws ExporterException {
        File file = new File(folder, prefix + "_efficiencies.csv");
        String selectQuery = 
            "   select m.abbreviation as CMAbbreviation, "
            + "     p.name as Pollutant, "
            + "     er.locale as Locale, "
            + "     er.effective_date as \"Effective Date\", "
            + "     er.existing_measure_abbr as ExistingMeasureAbbr, "
            + "     er.existing_dev_code as NEIExistingDevCode, "
            + "     er.min_emis as MinEmissions, "
            + "     er.max_emis as MaxEmissions, "
            + "     er.efficiency as ControlEfficiency, "
            + "     er.cost_year as CostYear, "
            + "     er.cost_per_ton as CostPerTon, "
            + "     er.rule_effectiveness as RuleEff, "
            + "     er.rule_penetration as RulePen, "
            + "     er.equation_type as EquationType, "
            + "     er.cap_rec_factor as CapRecFactor, "
            + "     er.discount_rate as DiscountRate, "
            + "     er.cap_ann_ratio as CapAnnRatio, "
            + "     er.incremental_cost_per_ton as IncrementalCPT, "
            + "     er.detail as Details, "
            + "     er.ref_yr_cost_per_ton as RefYrCostPerTon "
            + " from emf.control_measures m "
            + "     inner join emf.control_measure_efficiencyrecords er "
            + "     on er.control_measures_id = m.id "
            + "     inner join emf.pollutants p "
            + "     on p.id = er.pollutant_id "
            + " where m.id in (" + idList + ")";
        
        postgresCOPYExport.export(selectQuery, file.getAbsolutePath());
//        PrintWriter efficienciesWriter = openExportFile("_efficiencies.csv");
//        CMEfficiencyFileFormatv3 fileFormat = new CMEfficiencyFileFormatv3();
//        String[] colNames = fileFormat.cols();
//        ControlMeasureDAO dao = new ControlMeasureDAO();
//        Session session = factory.getSession();
//        
//        for (int i = 0; i < colNames.length; i++) {
//            if (i == colNames.length - 1) {
//                efficienciesWriter.write(colNames[i]);
//                break;
//            }
//            
//            efficienciesWriter.write(colNames[i] + delimiter);
//        }
//        
//        efficienciesWriter.write(System.getProperty("line.separator"));
//        try {
//            for (int j = 0; j < controlMeasures.length; j++) {
//                EfficiencyRecord[] records = (EfficiencyRecord[]) dao.getEfficiencyRecords(controlMeasures[j].getId(), session).toArray(new EfficiencyRecord[0]);
//                writeEfficiencyRecords(efficienciesWriter, controlMeasures[j].getAbbreviation(), records);
//            }
//            session.clear();
//        } finally {
//            session.close();
//        }
//        efficienciesWriter.close();
    }
    
//    private void writeEfficiencyRecords(PrintWriter writer, String abbreviation, EfficiencyRecord[] records) {
//        for (int i = 0; i < records.length; i++) {
//            writer.write(efficiencyRecord(abbreviation, records[i]));
//            writer.write(System.getProperty("line.separator"));
//        }
//    }
//
//    private String efficiencyRecord(String abbreviation, EfficiencyRecord record) {
//        String efficiencyRecord = addQuote(abbreviation) + delimiter;
//        Pollutant pollutant = record.getPollutant();
//        efficiencyRecord += (pollutant == null ? "" : pollutant.getName()) + delimiter;
//        efficiencyRecord += (record.getLocale() == null? "": addQuote(record.getLocale()))+ delimiter;
//        Date effectiveDate = record.getEffectiveDate();
//        efficiencyRecord += (effectiveDate == null ? "" : effectiveDate.toString()) + delimiter;
//        efficiencyRecord += addQuote(record.getExistingMeasureAbbr())+ delimiter;
//        efficiencyRecord += record.getExistingDevCode() + delimiter;
//        Double minEmis = record.getMinEmis();
//        efficiencyRecord += (minEmis == null ? "" : minEmis.toString()) + delimiter;
//        Double maxEmis = record.getMaxEmis();
//        efficiencyRecord += (maxEmis == null ? "" : maxEmis.toString()) + delimiter;
//        efficiencyRecord += record.getEfficiency() + delimiter;
//        efficiencyRecord += (record.getCostYear()== null ? "" :record.getCostYear())+ delimiter;
//        efficiencyRecord += (record.getCostPerTon() == null ? "" : record.getCostPerTon()) + delimiter;
//        efficiencyRecord += record.getRuleEffectiveness() + delimiter;
//        efficiencyRecord += record.getRulePenetration() + delimiter;
//        efficiencyRecord += addQuote(record.getEquationType()) + delimiter;
//        efficiencyRecord += (record.getCapRecFactor()== null ? "" : record.getCapRecFactor())+ delimiter;
//        efficiencyRecord += (record.getDiscountRate() == null ? "" :record.getDiscountRate())+ delimiter;
//        efficiencyRecord += (record.getCapitalAnnualizedRatio()== null ? "" : record.getCapitalAnnualizedRatio())+ delimiter;
//        efficiencyRecord += (record.getIncrementalCostPerTon()== null ? "" : record.getIncrementalCostPerTon()) + delimiter;
//        
//        efficiencyRecord += addQuote(record.getDetail());
//        
//        return efficiencyRecord;
//    }

    private void writeSccFile() throws ExporterException {
        File file = new File(folder, prefix + "_SCCs.csv");
        String selectQuery = 
            "   select m.abbreviation as CMAbbreviation, "
            + "     scc.name as SCC, "
            + "     scc.status as Status "
            + " from emf.control_measures m "
            + "     inner join emf.control_measure_sccs scc "
            + "     on scc.control_measures_id = m.id "
            + " where m.id in (" + idList + ")";
        
        postgresCOPYExport.export(selectQuery, file.getAbsolutePath());
//        PrintWriter sccsWriter = openExportFile("_SCCs.csv");
//        CMSCCsFileFormat fileFormat = new CMSCCsFileFormat();
//        String[] colNames = fileFormat.cols();
//        
//        for (int i = 0; i < colNames.length; i++) {
//            if (i == colNames.length - 1) {
//                sccsWriter.write(colNames[i]);
//                break;
//            }
//            
//            sccsWriter.write(colNames[i] + delimiter);
//        }
//        
//        sccsWriter.write(System.getProperty("line.separator"));
//        
//        for (int j = 0; j < cmAbbrevSccs.length; j++) {
//            sccsWriter.write(cmAbbrevSccs[j] + delimiter);
//            sccsWriter.write(System.getProperty("line.separator"));
//        }
//        
//        sccsWriter.close();
    }

    private void writeReferenceFile() throws ExporterException {
        File file = new File(folder, prefix + "_Refs.csv");
        String selectQuery = 
            "   select distinct r.id as \"DataSource\", "
            + "     r.description as \"Description\" "
            + " from  emf.\"references\" r "
            + "     inner join emf.control_measure_references mr "
            + "     on mr.reference_id = r.id "
            + " where mr.control_measure_id in (" + idList + ") "
            + " ";
        
        postgresCOPYExport.export(selectQuery, file.getAbsolutePath());
    }

    private void writePropertyFile() throws ExporterException {
        File file = new File(folder, prefix + "_Props.csv");
//        "CMAbbreviation", "Name", 
//        "Category", "Units", "Data_Type", 
//        "DB_FieldName", "Value"
        String selectQuery = 
            "   select m.abbreviation as \"CMAbbreviation\", "
            + "     mp.name as \"Name\", "
            + "     coalesce(mpc.name,'') as \"Category\", "
            + "     mp.units as \"Units\", "
            + "     mp.data_type as \"Data_Type\", "
            + "     mp.db_field_name as \"DB_FieldName\", "
            + "     mp.\"value\" as \"Value\" "
            + " from emf.control_measures m "
            + "     inner join emf.control_measure_properties mp "
            + "     on mp.control_measure_id = m.id "
            + "     left outer join emf.control_measure_property_categories mpc "
            + "     on mpc.id = mp.control_measure_property_category_id "
            + " where m.id in (" + idList + ")";
        
        postgresCOPYExport.export(selectQuery, file.getAbsolutePath());
    }

    private void addStatus(String message) {
        setStatus(message);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMExport");
        endStatus.setMessage(message + "\n");
        endStatus.setTimestamp(new Date());

        new StatusDAO(factory).add(endStatus);
    }
    
    public void setDelimiter(String delimiter) {
        //this.delimiter = delimiter;
    }
    
    public void export(File file) throws ExporterException {
        // NOTE Auto-generated method stub
        throw new ExporterException("Not used method...");
    }

    public long getExportedLinesCount() {
        return this.exportedLinesCount;
    }
    
}
