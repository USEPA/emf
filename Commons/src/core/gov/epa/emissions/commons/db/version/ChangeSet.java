package gov.epa.emissions.commons.db.version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChangeSet {

    private List newRecords;

    private List deletedRecords;

    private List updatedRecords;

    private Version version;

    public ChangeSet() {
        this.newRecords = new ArrayList();
        this.deletedRecords = new ArrayList();
        this.updatedRecords = new ArrayList();
    }

    public void addNew(VersionedRecord record) {
        newRecords.add(record);
    }

    public void addDeleted(VersionedRecord record) {
        if (containsNew(record)) {
            newRecords.remove(record);
            return;
        }
        
        if (containsUpdated(record))
            updatedRecords.remove(record);

        deletedRecords.add(record);
    }

    public void addUpdated(VersionedRecord record) {
        updatedRecords.add(record);
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public VersionedRecord[] getDeletedRecords() {
        return (VersionedRecord[]) deletedRecords.toArray(new VersionedRecord[0]);
    }

    public void setDeletedRecords(VersionedRecord[] records) {
        this.deletedRecords.clear();
        this.deletedRecords.addAll(Arrays.asList(records));
    }

    public VersionedRecord[] getNewRecords() {
        return (VersionedRecord[]) newRecords.toArray(new VersionedRecord[0]);
    }

    public void setNewRecords(VersionedRecord[] records) {
        this.newRecords.clear();
        this.newRecords.addAll(Arrays.asList(records));
    }

    public VersionedRecord[] getUpdatedRecords() {
        return (VersionedRecord[]) updatedRecords.toArray(new VersionedRecord[0]);
    }

    public void setUpdatedRecords(VersionedRecord[] records) {
        this.updatedRecords.clear();
        this.updatedRecords.addAll(Arrays.asList(records));
    }

    public void addDeleted(VersionedRecord[] records) {
        for (int i = 0; i < records.length; i++)
            addDeleted(records[i]);
    }

    public boolean containsUpdated(VersionedRecord record) {
        return updatedRecords.contains(record);
    }

    public boolean containsNew(VersionedRecord record) {
        return newRecords.contains(record);
    }

    public boolean hasChanges() {
        if (!newRecords.isEmpty() || !deletedRecords.isEmpty() || !updatedRecords.isEmpty())
            return true;

        return false;
    }

    public int getVersionNumber() {
        return version.getVersion();
    }

    public void clear() {
        deletedRecords.clear();
        newRecords.clear();
        updatedRecords.clear();
    }

    public int netIncrease() {
        return newRecords.size() - deletedRecords.size();
    }
    
    public void print() {
        System.out.print("\n\n=== New records: ===\n\n");
        for ( Object obj : this.newRecords) {
            VersionedRecord vr = (VersionedRecord) obj;
            vr.print();
        }
        System.out.print("\n\n=== Updated records: ===\n\n");
        for ( Object obj : this.updatedRecords) {
            VersionedRecord vr = (VersionedRecord) obj;
            vr.print();
        }
        System.out.print("\n\n=== Deleted records: ===\n\n");
        for ( Object obj : this.deletedRecords) {
            VersionedRecord vr = (VersionedRecord) obj;
            vr.print();
        }
    }
}
