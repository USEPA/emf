package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.DoubleValue;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class DataServiceTransport implements DataService {
    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfMappings emfMappings;
    
    private EmfCall call;

    public DataServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
        emfMappings = new EmfMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("Data Service");
        
        return call;
    }

    public synchronized EmfDataset[] getDatasets(String nameContains, int userId ) throws EmfException {
        EmfCall call = call();

        call.setOperation("getDatasets");
        call.addStringParam("nameContains");
        call.addIntegerParam("userId");
        call.setReturnType(mappings.datasets());

        return (EmfDataset[]) call.requestResponse(new Object[] {nameContains, userId});
        
    }

    public synchronized EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedDataset");
        call.addParam("owner", mappings.user());
        call.addParam("dataset", mappings.dataset());
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { owner, dataset});
    }

    public synchronized EmfDataset updateDataset(EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateDataset");
        call.addParam("dataset", mappings.dataset());
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { dataset });
    }

    public synchronized EmfDataset releaseLockedDataset(User user, EmfDataset locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedDataset");
        call.addParam("user", mappings.user());
        call.addParam("locked", mappings.dataset());
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { user, locked });
    }

    public synchronized EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException {

        EmfCall call = call();

        call.setOperation("getDatasets");
        call.addParam("datasetType", mappings.datasetType());
        call.setReturnType(mappings.datasets());

        return (EmfDataset[]) call.requestResponse(new Object[] {datasetType});
    
    }

    public synchronized EmfDataset[] getDatasetsWithFilter(int datasetTypeId, String nameContains) throws EmfException {

        EmfCall call = call();

        call.setOperation("getDatasetsWithFilter");
        call.addIntegerParam("datasetTypeId");
        call.addStringParam("nameContains");
        call.setReturnType(mappings.datasets());

        return (EmfDataset[]) call.requestResponse(new Object[] { new Integer(datasetTypeId), nameContains});
    
    }

    public synchronized EmfDataset[] getDatasets(int datasetTypeId, String nameContaining) throws EmfException {

        EmfCall call = call();

        call.setOperation("getDatasets");
        call.addIntegerParam("datasetTypeId");
        call.addStringParam("nameContaining");
        call.setReturnType(mappings.datasets());

        return (EmfDataset[]) call.requestResponse(new Object[] {new Integer(datasetTypeId), nameContaining});
    
    }

    public synchronized void deleteDatasets(User owner, EmfDataset[] datasets) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("deleteDatasets");
        call.addParam("owner", mappings.user());
        call.addParam("datasets", mappings.datasets());
        call.setVoidReturnType();
        
        call.request(new Object[] { owner, datasets });
    }

    public synchronized EmfDataset getDataset(Integer datasetId) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getDataset");
        call.addParam("datasetId", mappings.integer());
        call.setReturnType(mappings.dataset());
        
        return (EmfDataset)call.requestResponse(new Object[]{ datasetId });
    }

    public synchronized EmfDataset getDataset(String datasetName) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getDataset");
        call.addStringParam("datasetName");
        call.setReturnType(mappings.dataset());
        
        return (EmfDataset)call.requestResponse(new Object[]{ datasetName });
    }

    public synchronized String[] getDatasetValues(Integer datasetId) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getDatasetValues");
        call.addParam("datasetId", mappings.integer());
        call.setStringArrayReturnType();
        
        return (String[])call.requestResponse(new Object[]{ datasetId });
    }

    public Version obtainedLockOnVersion(User user, int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainedLockOnVersion");
        call.addParam("user", mappings.user());
        call.addParam("id", mappings.integer());
        call.setReturnType(mappings.version());

        return (Version) call.requestResponse(new Object[] { user, id});
    }

    public void updateVersionNReleaseLock(Version locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateVersionNReleaseLock");
        call.addParam("locked", mappings.version());
        call.setVoidReturnType();

        call.request(new Object[] { locked });
    }
    
    public void checkIfDeletable(User user, int datasetID) throws EmfException {
        EmfCall call = call();

        call.setOperation("checkIfDeletable");
        call.addParam("user", mappings.user());
        call.addIntegerParam("datasetID");
        call.setVoidReturnType();

        call.request(new Object[] { user, new Integer(datasetID) });
    }

    public void purgeDeletedDatasets(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("purgeDeletedDatasets");
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { user });
    }

    public int getNumOfDeletedDatasets(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("getNumOfDeletedDatasets");
        call.addParam("user", mappings.user());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { user });
    }
    
    public int getNumOfDatasets(int datasetTypeId, String nameContains) throws EmfException {
        EmfCall call = call();

        call.setOperation("getNumOfDatasets");
        call.addIntegerParam("datasetTypeId");
        call.addStringParam("nameContains");
        call.setIntegerReturnType();
        
        return (Integer) call.requestResponse(new Object[] {new Integer(datasetTypeId), nameContains });
    }
    
    public int getNumOfDatasets(String nameContains, int userId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getNumOfDatasets");
        call.addStringParam("nameContains");
        call.addIntegerParam("userId");
        call.setIntegerReturnType();
        
        return (Integer) call.requestResponse(new Object[] {nameContains, userId});
    }

    public String getTableAsString(String qualifiedTableName) throws EmfException {
        
        EmfCall call = call();

        call.setOperation("getTableAsString");
        call.addStringParam("qualifiedTableName");
        call.setStringReturnType();

        return (String) call.requestResponse(new Object[] { qualifiedTableName });
    }

    public String getTableAsString(String qualifiedTableName, long recordLimit, long recordOffset) throws EmfException {
        EmfCall call = call();

//        call.getCall().setEncodingStyle(null);
//        call.getCall().getMessageContext().setEncodingStyle(null);
//        call.getCall().getMessageContext().setProperty(Call.CHARACTER_SET_ENCODING, "ISO-8859-1");
//        call.getCall().setProperty(Call.CHARACTER_SET_ENCODING, "ISO-8859-1");
//        try {
//            call.getCall().getResponseMessage().setProperty(Call.CHARACTER_SET_ENCODING, "ISO-8859-1");
//        } catch (SOAPException e) {
//            // NOTE Auto-generated catch block
//            e.printStackTrace();
//        }
//        call.getCall().setop
//        javax.xml.rpc.encoding.TypeMappingRegistry
        call.setOperation("getTableAsString");
        call.addStringParam("qualifiedTableName");
        call.addLongParam("recordLimit");
        call.addLongParam("recordOffset");
        call.setStringReturnType();
//        System.out.println(call.getCall().getMessageContext().getProperty(Call.CHARACTER_SET_ENCODING));
//        System.out.println(call.getCall().getProperty(Call.CHARACTER_SET_ENCODING));
//        call.getCall().getMessageContext().setProperty(Call.CHARACTER_SET_ENCODING, "utf-16");

        return (String) call.requestResponse(new Object[] { qualifiedTableName, new Long(recordLimit), new Long(recordOffset) });
    }

    public long getTableRecordCount(String qualifiedTableName) throws EmfException {
        EmfCall call = call();
        //"UTF-16"
        //call.getCall().setEncodingStyle(org.apache.axis.Constants. "UTF-16");
        //call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
        call.setOperation("getTableRecordCount");
        call.addStringParam("qualifiedTableName");
        call.setLongReturnType();

        return (Long) call.requestResponse(new Object[] { qualifiedTableName });
    }

    public void appendData(User user, int srcDSid, int srcDSVersion, String filter, int targetDSid, int targetDSVersion,
            DoubleValue targetStartLineNumber) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("appendData");
        call.addParam("user", mappings.user());
        call.addIntegerParam("srcDSid");
        call.addIntegerParam("srcDSVersion");
        call.addStringParam("filter");
        call.addIntegerParam("targetDSid");
        call.addIntegerParam("targetDSVersion");
        call.addParam("targetStartLineNumber", emfMappings.doubleValue());
        call.setVoidReturnType();
        
        call.request(new Object[]{user, new Integer(srcDSid), new Integer(srcDSVersion), filter, new Integer(targetDSid),
                new Integer(targetDSVersion), targetStartLineNumber});
    }
    
    public boolean checkTableDefinitions(int srcDSid, int targetDSid) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("checkTableDefinitions");
        call.addIntegerParam("srcDSid");
        call.addIntegerParam("targetDSid");
        call.setBooleanReturnType();
        
        return (Boolean)call.requestResponse(new Object[]{new Integer(srcDSid), new Integer(targetDSid)});
    }

    public void replaceColValues(String table, String colName, String find, String replaceWith, Version version, String rowFilter) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("replaceColValues");
        call.addStringParam("table");
        call.addStringParam("colName");
        call.addStringParam("find");
        call.addStringParam("replaceWith");
        call.addParam("version", mappings.version());
        call.addStringParam("rowFilter");
        call.setVoidReturnType();
        
        call.request(new Object[]{table, colName, find, replaceWith, version, rowFilter});
    }
    
    public void replaceColValues(String table, String findFilter, String replaceWith, Version version, String rowFilter) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("replaceColValues");
        call.addStringParam("table");
        call.addStringParam("findFilter");
        call.addStringParam("replaceWith");
        call.addParam("version", mappings.version());
        call.addStringParam("rowFilter");
        call.setVoidReturnType();
        
        call.request(new Object[]{table, findFilter, replaceWith, version, rowFilter});
    }
    
    public void deleteRecords(User user, String table, Version version, String filter) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("deleteRecords");
        call.addParam("user", mappings.user());
        call.addStringParam("table");
        call.addParam("version", mappings.version());
        call.addStringParam("filter");
        call.setVoidReturnType();
        
        call.request(new Object[]{user, table, version, filter});
    }

    public void copyDataset(int datasetId, Version version, User user) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("copyDataset");
        call.addIntegerParam("datasetId");
        call.addParam("version", mappings.version());
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { new Integer(datasetId), version, user });
    }

    public ExternalSource[] getExternalSources(int datasetId, int limit, String filter) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getExternalSources");
        call.addIntegerParam("datasetId");
        call.addIntegerParam("limit");
        call.addStringParam("filter");
        call.setReturnType(mappings.externalSources());
        
        return (ExternalSource[])call.requestResponse(new Object[]{new Integer(datasetId), new Integer(limit), filter});
    }
    
    public int getNumExternalSources(int datasetId, String filter) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getNumExternalSources");
        call.addIntegerParam("datasetId");
        call.addStringParam("filter");
        call.setIntegerReturnType();
        
        return (Integer)call.requestResponse(new Object[]{new Integer(datasetId), filter});
    }

    public boolean isExternal(int datasetId) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("isExternal");
        call.addIntegerParam("datasetId");
        call.setBooleanReturnType();
        
        return (Boolean)call.requestResponse(new Object[]{new Integer(datasetId)});
    }

    public void addExternalSources(String folder, String[] files, int datasetId) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("addExternalSources");
        call.addStringParam("folder");
        call.addParam("files", mappings.strings());
        call.addIntegerParam("datasetId");
        call.setVoidReturnType();
        
        call.request(new Object[]{folder, files, new Integer(datasetId)});
    }

    public void updateExternalSources(int datasetId, String newDir) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("updateExternalSources");
        call.addIntegerParam("datasetId");
        call.addStringParam("newDir");
        call.setVoidReturnType();
        
        call.request(new Object[]{new Integer(datasetId), newDir});
    }

    public int getNumOfRecords(String table, Version version, String filter) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getNumOfRecords");
        call.addStringParam("table");
        call.addParam("version", mappings.version());
        call.addStringParam("filter");
        call.setIntegerReturnType();
        
        return (Integer)call.requestResponse(new Object[]{table, version, filter});
    }
    
    public synchronized Integer[] getNumOfRecords (int datasetId, 
        Version[] versions, String tableName) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getNumOfRecords");
        call.addIntegerParam("datasetId");
        call.addParam("versions", mappings.versions());
        call.addStringParam("tableName");
        call.setReturnType(mappings.integers());
        
        return (Integer[])call.requestResponse(new Object[]{datasetId, versions, tableName});
    }

//    public void importRemoteEMFDataset(int externalDatasetAccessId, User user) throws EmfException {
//        EmfCall call = call();
//        
//        call.setOperation("importRemoteEMFDataset");
//        call.addIntegerParam("externalDatasetAccessId");
//        call.addParam("user", mappings.user());
//        call.setVoidReturnType();
//        
//        call.request(new Object[]{new Integer(externalDatasetAccessId), user});
//    }

    public String[] getTableColumnDistinctValues(int datasetId, int datasetVersion, String columnName, String whereFilter,
            String sortOrder) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getTableColumnDistinctValues");
        call.addIntegerParam("datasetId");
        call.addIntegerParam("datasetVersion");
        call.addStringParam("columnName");
        call.addStringParam("whereFilter");
        call.addStringParam("sortOrder");
        call.setStringArrayReturnType();
        
        return (String[])call.requestResponse(new Object[] { new Integer(datasetId), new Integer(datasetVersion), 
                columnName, whereFilter, sortOrder});
        
    }

    public EmfDataset[] findDatasets(EmfDataset dataset, String qaStep, String qaArgument, 
            int[] usedByCasesID, String dataValueFilter, boolean unconditional, int userId) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("findDatasets");
        call.addParam("dataset", mappings.dataset());
        call.addStringParam("qaStep");
        call.addStringParam("qaArgument");
        call.addIntArrayParam();
        call.addStringParam("dataValueFilter");
        call.addBooleanParameter("unconditional");
        call.addIntegerParam("userId");
        call.setReturnType(mappings.datasets());
        
        return (EmfDataset[]) call.requestResponse(new Object[]{ dataset, qaStep, qaArgument, usedByCasesID, dataValueFilter, new Boolean(unconditional), userId });
    }

    public void updateVersion(Version locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateVersion");
        call.addParam("locked", mappings.version());
        call.setVoidReturnType();

        call.request(new Object[] { locked });
    }
    
    public boolean checkBizzareCharInColumn(int datasetId, int version, String colName) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("checkBizzareCharInColumn");
        call.addIntegerParam("datasetId");
        call.addIntegerParam("version");
        call.addStringParam("colName");
        call.setBooleanReturnType();
        
        return (Boolean)call.requestResponse(new Object[]{new Integer(datasetId), new Integer(version), colName});
    }

    public synchronized String[] getTableColumns(String table) throws EmfException {
        EmfCall call = call();

        call.setOperation("getTableColumns");
        call.addStringParam("table");
        call.setReturnType(mappings.strings());

        return (String[]) call.requestResponse(new Object[] { table });
        
    }

}
