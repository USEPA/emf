package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessToken;

public interface TablePresenterDelegate {

    void display() throws EmfException;

    void reloadCurrent() throws EmfException;

    void doDisplayNext() throws EmfException;

    void doDisplayPrevious() throws EmfException;

    void doDisplay(int pageNumber) throws EmfException;

    void doDisplayFirst() throws EmfException;

    void doDisplayLast() throws EmfException;

    void doDisplayPageWithRecord(int record) throws EmfException;

    int totalRecords() throws EmfException;

    void updateFilteredCount(int totalRecords) throws EmfException;

    DataAccessToken token();

    void setRowAndSortFilter(String rowFilter, String sortOrder);
        
    void doApplyConstraints(String rowFilter, String sortOrder) throws EmfException;

    void doApplyFormat() throws EmfException;

    int pageNumber();

}