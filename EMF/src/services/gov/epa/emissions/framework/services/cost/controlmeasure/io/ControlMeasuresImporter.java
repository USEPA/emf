package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.AggregateEfficiencyRecordDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ControlMeasuresImporter implements Importer {

    private static Log log = LogFactory.getLog(ControlMeasuresImporter.class);

    private CMImporters cmImporters;

    private Map controlMeasures;
    
    private CMEfficiencyImporter efficiency;

    private StatusDAO statusDao;

    private User user;

    private ControlMeasureDAO controlMeasuresDao;
    
    private TableFormat effRecTableFormat;

    private Datasource datasource;

    private OptimizedTableModifier modifier;

    private HibernateSessionFactory sessionFactory;

    private DbServer dbServer;
    
    private AggregateEfficiencyRecordDAO aerDAO;
    
    private EfficiencyRecordGenerator generator;

    private boolean truncate;

    private int[] sectorIds;
    
    private boolean forScan = false;

    public ControlMeasuresImporter(File folder, String[] fileNames, User user, boolean truncate, int[] sectorIds, HibernateSessionFactory factory, DbServerFactory dbServerFactory)
            throws EmfException, ImporterException {
        File[] files = fileNames(folder, fileNames);
        this.user = user;
        this.truncate = truncate;
        this.sectorIds = sectorIds;
        this.sessionFactory = factory;
        this.dbServer = dbServerFactory.getDbServer();
        this.statusDao = new StatusDAO(factory);
        ControlMeasuresImportIdentifier types = new ControlMeasuresImportIdentifier(files, user, factory, dbServer);
        this.cmImporters = types.cmImporters();
        this.efficiency = cmImporters.efficiencyImporter();
        this.controlMeasures = new HashMap();
        this.controlMeasuresDao = new ControlMeasureDAO();
        this.aerDAO = new AggregateEfficiencyRecordDAO();
        this.effRecTableFormat = new EfficiencyRecordTableFormat(dbServer.getSqlDataTypes());
        this.datasource = dbServer.getEmfDatasource();
        this.modifier = dataModifier("control_measure_efficiencyrecords", datasource);
        this.generator = new EfficiencyRecordGenerator();
    }

    //zero item is this first item is this...
//    public int validate() throws ImporterException, FileNotFoundException {
//        //First parse summary file, look at map "controlMeasures" and look at size...
//        parseSummary(controlMeasures);
//        
//        return controlMeasures.size();
//        
//    }
    
    public int getControlMeasureCountInSummaryFile() throws ImporterException, FileNotFoundException {
        Map tmpControlMeasures = new HashMap(); // don't use the global Map to avoid side effect
        parseSummary(tmpControlMeasures);        
        return tmpControlMeasures.size();
    }
    
    public void run() throws ImporterException {
        
        setStatus("Started importing control measures");
        setDetailStatus("Started importing control measures\n");
        ControlMeasure[] measures;
        EfficiencyRecord[] efficiencyRecords;
        int efficiencyRecordCount = 0;
        try {
            
            Map<Integer, Reference> referenceMap = new HashMap<Integer, Reference>();
            Map<Integer, Integer> idMap = new HashMap<Integer, Integer>();

            this.runReference(referenceMap, idMap);
            
            //import summary file
            runSummary(controlMeasures);
            //import scc file
            runSCC(controlMeasures);
            //import equation file
            runEquation(controlMeasures);
            //import property file
            runProperty(controlMeasures);
            measures = controlMeasures();
            
            this.updateControlMeasureReferences(measures, referenceMap, idMap);
            
            setDetailStatus("Saving measure and SCC information to the database\n");
            saveMeasureAndSCCs(measures, user);
            //this is needed so we know what the Ids are for the saved measures
            updateControlMeasuresMap(measures);
            setStatus("Started reading efficiency record file");
            modifier.start();
            //process 20000 eff rec at a time...
            while (!isLastEfficiencyRecord()) {
                efficiencyRecords = runEfficiencyRecords();
                saveEfficiencyRecords(efficiencyRecords);
                efficiencyRecordCount += efficiencyRecords.length;
                //setStatus("Processed " + efficiencyRecordCount + " efficiency records");
                efficiencyRecords = null;
                System.gc();
            }
            //make sure you finish the modifier here, make sure all eff records are inserted
            //before creating the agg records...
            try {
                modifier.finish();
                modifier.close();
            } catch (SQLException e) {
                // NOTE Auto-generated catch block
            } finally {
                //
            }
            setDetailStatus("Creating and saving aggregated efficiency records to the database\n");
            aerDAO.updateAggregateEfficiencyRecords(measures, dbServer);
            setStatus("Finished reading efficiency record file");
            setDetailStatus("Finished reading efficiency record file\n");
            
        } catch (Exception e) {
            logError("Failed to import all control measures", e);
            setStatus("Failed to import all control measures, see the import control measures status field for more detailed information on the failure: " + e.getMessage());
            setDetailStatus("Failed to import all control measures: " + e.getMessage() + "\n");
            throw new ImporterException("Failed to import all control measures: " + e.getMessage());
        } finally {
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
            } finally {
                //
            }
        }
        setStatus("Completed importing " + measures.length + " control measures");
        setDetailStatus("Completed importing " + measures.length + " control measures\n");
    }

    public ControlMeasure[] controlMeasures() {
        Iterator keys = controlMeasures.keySet().iterator();
        ControlMeasure[] measures = new ControlMeasure[controlMeasures.size()];
        int count = 0;
        while (keys.hasNext()) {
            measures[count++] = (ControlMeasure) controlMeasures.get(keys.next());
        }
        return measures;
    }

    private void updateControlMeasureReferences(ControlMeasure[] controlMeasures, Map<Integer, Reference> referenceMap, Map<Integer, Integer> idMap) {

        for (ControlMeasure controlMeasure : controlMeasures) {
            
            String dataSource = controlMeasure.getDataSouce();
            List<Integer> dataSourceIds = this.parseDataSource(dataSource);
            
            List<Reference> references = new ArrayList<Reference>();
            for (Integer id: dataSourceIds) {
                
                Integer mappedId = idMap.get(id);

                if (mappedId == null) {
                    setDetailStatus("Control measure " + controlMeasure.getAbbreviation() + ", " + controlMeasure.getName()
                            + " contains undefined reference '" + id + "'. Skipping reference.\n");
                }
                else {

                    Reference reference = referenceMap.get(mappedId);
                    if (reference == null) {
                        setDetailStatus("Unable to find reference with id '" + id + "' and mapped id '" + mappedId
                                + "'. Skipping reference for control measure " + controlMeasure.getAbbreviation() + ", " + controlMeasure.getName()
                                    + " .\n");
                    } else {
                        if (!references.contains(reference)) {
                            references.add(reference);
                        } else {
                            setDetailStatus("Control measure " + controlMeasure.getAbbreviation() + ", " + controlMeasure.getName()
                                    + " contains undefined reference '" + id + "'. Skipping reference.\n");
                        }
                    }
                }

            }           
            
            controlMeasure.setReferences(references.toArray(new Reference[0]));
        }
        
    }

    private List<Integer> parseDataSource(String datasource) {
        
        List<Integer> sourceIds = new ArrayList<Integer>();

        StringTokenizer tokenizer = new StringTokenizer(datasource, "|");
        while (tokenizer.hasMoreElements()) {
        
            String source = (String) tokenizer.nextElement();
            source = source.trim();
            try {
                
                int id = Integer.valueOf(source);
                sourceIds.add(id);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        return sourceIds;
    }
    
    private void updateControlMeasuresMap(ControlMeasure[] measures) {
        controlMeasures = new HashMap();
        for (int i = 0; i < measures.length; i++) {
            controlMeasures.put(measures[i].getAbbreviation(), measures[i]);
        }
    }

    private void runSummary(Map controlMeasures) throws ImporterException, FileNotFoundException {
        setStatus("Started reading summary file");
        CMSummaryImporter summary = cmImporters.summaryImporter();
        summary.run(controlMeasures);
        setStatus("Finished reading summary file");
    }

    private void parseSummary(Map controlMeasures) throws ImporterException, FileNotFoundException {
//        setStatus("Started reading summary file");
        CMSummaryImporter summary = cmImporters.summaryImporter();
        summary.setForScan( this.forScan);
        summary.run(controlMeasures);
//        setStatus("Finished reading summary file");
    }

    private EfficiencyRecord[] runEfficiencyRecords() throws ImporterException {
        return efficiency.parseEfficiencyRecords(controlMeasures);
    }

    private boolean isLastEfficiencyRecord() {
        return efficiency.isEnd();

    }

    private void runSCC(Map controlMeasures) throws ImporterException {
        setStatus("Started reading SCC file");
        CMSCCImporter sccImporter = cmImporters.sccImporter();
        sccImporter.run(controlMeasures);
        setStatus("Finished reading SCC file");
    }

    private void runEquation(Map controlMeasures) throws ImporterException {
        CMEquationImporter equationImporter = cmImporters.equationImporter();
        if (equationImporter != null) {
            setStatus("Started reading equation file");
            equationImporter.run(controlMeasures);
            setStatus("Finished reading equation file");
        } else
            setStatus("Warning, no equation file was specified");
    }

    private void runProperty(Map controlMeasures) throws ImporterException, FileNotFoundException {
        CMPropertyImporter propertyImporter = cmImporters.propertyImporter();
        if (propertyImporter != null) {
            setStatus("Started reading property file");
            propertyImporter.run(controlMeasures);
            setStatus("Finished reading property file");
        } else
            setStatus("Warning, no property file was specified");
    }

    private void runReference(Map<Integer, Reference> referenceMap, Map<Integer, Integer> idMap) throws ImporterException, FileNotFoundException {

        CMReferenceImporter referenceImporter = cmImporters.referenceImporter();
        if (referenceImporter != null) {
            setStatus("Started reading reference file: " + referenceImporter.getFileName());
            referenceImporter.run(referenceMap, idMap);
            setStatus("Finished reading reference file: " + referenceImporter.getFileName());
        } else
            setStatus("Warning, no reference file was specified");
    }

    private OptimizedTableModifier dataModifier(String table, Datasource datasource) throws EmfException {
        try {
            return new OptimizedTableModifier(datasource, table); 
            // VERSIONS TABLE - completed - should throw exceptions
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

    private void insertRecord(Record record, OptimizedTableModifier dataModifier) throws EmfException {
        try {
            int colsSize = effRecTableFormat.cols().length;
            if (record.size() < colsSize)
                throw new EmfException("The number of tokens in the record are " + record.size()
                        + ", It's less than the number of columns expected(" + colsSize + ")");
            dataModifier.insert(record.getTokens());
        } catch (Exception e) {
            throw new EmfException("Error processing insert query: " + e.getMessage());
        }
    }

    private void doBatchInsert(EfficiencyRecord[] efficiencyRecords) throws EmfException {
        for (int i = 0; i < efficiencyRecords.length; i++) {
            Record record = generator.getRecord(efficiencyRecords[i]);
//            log.error(efficiencyRecords[i].getCostPerTon());
            insertRecord(record, modifier);
        }
    }

    private void saveMeasureAndSCCs(ControlMeasure[] measures, User user) throws ImporterException {
        Date date = new Date();
        List messages = new ArrayList(); 
        int cmId = 0;
        for (int i = 0; i < measures.length; i++) {
            Session session = sessionFactory.getSession();
            measures[i].setCreator(user);
            measures[i].setLastModifiedTime(date);
            measures[i].setLastModifiedBy(user.getName());
            try {
                cmId = controlMeasuresDao.addFromImporter(measures[i],measures[i].getSccs(), user, session, dbServer);
                measures[i].setId(cmId);
            } catch (EmfException e) {
                e.printStackTrace();
                messages.add(e.getMessage() + "\n");
//                throw new EmfException(e.getMessage() + " " + e.getMessage() + " " + measures[i].getName() + " " + measures[i].getAbbreviation());
            } catch (Exception e) {
                e.printStackTrace();
                messages.add(e.getMessage() + "\n");
//              throw new EmfException(e.getMessage() + " " + e.getMessage() + " " + measures[i].getName() + " " + measures[i].getAbbreviation());
            } 
//            if ( i % 20 == 0 ) { //20, same as the JDBC batch size
//                //flush a batch of inserts and release memory:
//                session.flush();
//                session.clear();
//            }
            session.flush();
            session.clear();
            session.close();
       }
        if (messages.size() > 0) {
            setDetailStatus(messages);
            throw new ImporterException("error(s) were encountered reading the summary or SCC file.");
        }
    }

//    public void saveEfficiencyRecordsViaHibernateStatelessSession(EfficiencyRecord[] efficiencyRecords) {
//        List messages = new ArrayList(); 
//        int id = 0;
//        int count = 0;
//        StatelessSession session = sessionFactory.getStatelessSession();
//        Transaction tx = session.beginTransaction();
//        for (int i = 0; i < efficiencyRecords.length; i++) {
//            try {
//                id = controlMeasuresDao.addEfficiencyRecord(efficiencyRecords[i], session);
//                efficiencyRecords[i].setId(id);
//                count++;
//            } catch (EmfException e) {
//                messages.add(e.getMessage());
//            }
//        }
//        tx.commit();
//        session.close();
//        if (messages.size() > 0) setStatus(messages);
//        addCompletedStatus(count, "efficiency records");
//    }
//    
//    public void saveEfficiencyRecordsViaHibernateSession(EfficiencyRecord[] efficiencyRecords) {
//        List messages = new ArrayList(); 
//        int id = 0;
//        int count = 0;
//        Session session = sessionFactory.getSession();
//        for (int i = 0; i < efficiencyRecords.length; i++) {
//            try {
//                id = controlMeasuresDao.addEfficiencyRecord(efficiencyRecords[i], session);
//                efficiencyRecords[i].setId(id);
//                count++;
//            } catch (EmfException e) {
//                messages.add(e.getMessage());
//            }
//            if ( i % 20 == 0 ) { //20, same as the JDBC batch size
//                //flush a batch of inserts and release memory:
//                session.flush();
//                session.clear();
//            }
//        }
//        session.flush();
//        session.clear();
//        session.close();
//        if (messages.size() > 0) setStatus(messages);
//        addCompletedStatus(count, "efficiency records");
//    }

    public void saveEfficiencyRecords(EfficiencyRecord[] efficiencyRecords) throws EmfException {
        doBatchInsert(efficiencyRecords);
    }

    private File[] fileNames(File folder, String[] fileNames) throws ImporterException {
        int length = fileNames.length;
        if (length < 3 || length > 6) {
            throw new ImporterException("Select between 3 to 6 files");
        }
        File[] files = new File[length];
        for (int i = 0; i < length; i++)
            files[i] = new File(folder, fileNames[i]);

        FileVerifier verifier = new FileVerifier();
        for (int i = 0; i < length; i++)
            verifier.shouldExist(files[i]);

        return files;
    }

    private void setDetailStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImportDetailMsg");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

    private void setDetailStatus(List messages) {
        for (int i = 0; i < messages.size(); i++) {
            setDetailStatus((String)messages.get(i));
        }
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImport");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

//    private void setStatus(List messages) {
//        for (int i = 0; i < messages.size(); i++) {
//            setStatus((String)messages.get(i));
//        }
//    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }

    public static void main(String[] args) {
        
        String test = "1|2|11|12";
        StringTokenizer tokenizer = new StringTokenizer(test, "|");
        while (tokenizer.hasMoreElements()) {
        
            String string = (String) tokenizer.nextElement();
            System.out.println(string);
        }

        String[] split = test.split("|");
        for (String string : split) {
            System.out.println(string);
        }
        
    }

    public void setForScan(boolean forScan) {
        this.forScan = forScan;
    }

    public boolean isForScan() {
        return forScan;
    }
}
