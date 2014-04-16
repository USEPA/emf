package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.db.DatabaseRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class VersionedRecord extends DatabaseRecord {

    private int recordId;

    private int datasetId;

    private int version;

    private String deleteVersions;

    public VersionedRecord() {// needed for serialization
    }

    public VersionedRecord(int recordId) {
        this.recordId = recordId;
    }

    /**
     * @return Returns the deleteVersions.
     */
    public String getDeleteVersions() {
        return deleteVersions;
    }

    /**
     * @param deleteVersions
     *            The deleteVersions to set.
     */
    public void setDeleteVersions(String deleteVersions) {
        if (deleteVersions == null || deleteVersions.trim().isEmpty()) {
            this.deleteVersions = deleteVersions;
            return;
        }
        
        //assuming the deleteVersions is delimited by comma
        String[] tokens = deleteVersions.split(",");
        List<String> versions = new ArrayList<String>();
        
        for (int i = 0; i < tokens.length; i++)
            if (tokens[i] != null && !tokens[i].trim().isEmpty()) versions.add(tokens[i].trim());

        TreeSet<String> set = new TreeSet<String>(versions);
        List<String> unikVersions = new ArrayList<String>(set);
        String delVerStr = "";
        
        for (String ver : unikVersions)
            delVerStr += ver + ",";
        
        this.deleteVersions = delVerStr.substring(0, delVerStr.lastIndexOf(","));
    }

    public int getVersion() {
        return version;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String[] dataForInsertion(Version version) {
        List<String> data = new ArrayList<String>();

        data.add(0, "");// record id
        data.add(1, datasetId + "");
        data.add(2, version.getVersion() + "");// version
        data.add(3, "");// delete versions

        data.addAll(numVersionCols(), tokensStrings(tokens()));// add all specified data

        return data.toArray(new String[0]);
    }

    private List<String> tokensStrings(List tokens) {
        List<String> stringTokens = new ArrayList<String>();
        for (int i = 0; i < tokens.size(); i++) {
            Object object = tokens.get(i);
            String stringValue = (object == null) ? "" : "" + object;
            stringTokens.add(stringValue);
        }
        return stringTokens;
    }

    public int numVersionCols() {
        return 4;
    }

}
