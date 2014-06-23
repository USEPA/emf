package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.services.editor.ChangeSets.ChangeSetsIterator;
import gov.epa.emissions.framework.tasks.DebugLevels;

public class RecordsFilter {

    public Page filter(Page page, ChangeSets changesets) {
        // TODO: efficiency is O(n^2). Need to optimize, but order should be maintained
        for (ChangeSetsIterator iter = changesets.iterator(); iter.hasNext();) {
            ChangeSet element = iter.next();
            page = filter(page, element);

            if (DebugLevels.DEBUG_19()) {
                System.out.println("RecordsFilter:filter(page, change sets):ChangeSet has changes? "
                        + element.hasChanges());
                System.out.println("Current page null? " + (page == null));

                if (page != null)
                    System.out.println("\tRecordsFilter:filter(page, change sets):Page count: " + page.count()
                            + " Page number: " + page.getNumber());
            }
        }

        return page;
    }

    public Page filter(Page page, ChangeSet changeset) {
        VersionedRecord[] results = filter(page.getRecords(), changeset);
        page.setRecords(results);

        return page;
    }

    public VersionedRecord[] filter(VersionedRecord[] records, ChangeSet changeset) {
        VersionedRecords results = new VersionedRecords();
        results.add(records);

        if (DebugLevels.DEBUG_19()) {
            System.out.println("RecordsFilter:filter():number of records from page: " + records.length);
        }

        doNew(changeset, results);
        doDeleted(changeset, results);
        doUpdated(changeset, results);

        return results.get();
    }

    private void doNew(ChangeSet changeset, VersionedRecords results) {
        VersionedRecord[] newRecords = changeset.getNewRecords();

        if (DebugLevels.DEBUG_19()) {
            System.out.println("RecordsFilter:doNew():newRecords null? " + (newRecords == null));
            if (newRecords != null) {
                // for (int i = 0; i < newRecords.length; i++)
                System.out.println("\tNew record " + 0 + "(id): " + newRecords[0].getRecordId());
            }
        }

        results.add(newRecords);
    }

    private void doDeleted(ChangeSet changeset, VersionedRecords results) {
        VersionedRecord[] deleted = changeset.getDeletedRecords();

        if (DebugLevels.DEBUG_19()) {
            System.out.println("RecordsFilter:doDeleted():deleted null? " + (deleted == null));
            if (deleted != null) {
                for (int i = 0; i < deleted.length; i++)
                    System.out.println("\tDeleted record " + i + "(id): " + deleted[i].getRecordId() + " tokens: "
                            + deleted[i].getTokens()[0]);
            }
        }

        for (int i = 0; i < deleted.length; i++)
            results.remove(deleted[i]);
    }

    private void doUpdated(ChangeSet changeset, VersionedRecords results) {
        VersionedRecord[] updated = changeset.getUpdatedRecords();

        if (DebugLevels.DEBUG_19()) {
            System.out.println("RecordsFilter:doUpdated():updated null? " + (updated == null));
            if (updated != null) {
                for (int i = 0; i < updated.length; i++)
                    System.out.println("\tUpdated record " + i + "(id): " + updated[i].getRecordId() + " tokens: "
                            + updated[i].getTokens()[0]);
            }
        }

        for (int i = 0; i < updated.length; i++)
            results.replace(updated[i].getRecordId(), updated[i]);
    }

}
