package gov.epa.emissions.framework.services.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import gov.epa.emissions.commons.db.version.VersionedRecord;

public class VersionedRecords {

    private List results;

    public VersionedRecords() {
        results = new ArrayList();
    }

    public void add(VersionedRecord[] records) {
        results.addAll(Arrays.asList(records));
    }

    public VersionedRecord[] get() {
        return (VersionedRecord[]) results.toArray(new VersionedRecord[0]);
    }

    public void remove(VersionedRecord record) {
        for (Iterator iter = results.iterator(); iter.hasNext();) {
            VersionedRecord element = (VersionedRecord) iter.next();
            if (element.getRecordId() == record.getRecordId()) {
                results.remove(element);
                return;
            }
        }
    }

    public void replace(int recordId, VersionedRecord record) {
        for (Iterator iter = results.iterator(); iter.hasNext();) {
            VersionedRecord element = (VersionedRecord) iter.next();
            if (element.getRecordId() == recordId) {
                swap(element, record);
                return;
            }
        }
    }

    private void swap(VersionedRecord oldRecord, VersionedRecord newRecord) {
        int index = results.indexOf(oldRecord);
        results.remove(oldRecord);
        results.add(index, newRecord);
    }

}
