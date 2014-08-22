package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.framework.services.EmfException;

public interface TablePresenter {

    void display() throws EmfException;

    void doDisplayNext() throws EmfException;

    void doDisplayPrevious() throws EmfException;

    void doDisplay(int pageNumber) throws EmfException;

    void doDisplayFirst() throws EmfException;

    void doDisplayLast() throws EmfException;

    void doDisplayPageWithRecord(int record) throws EmfException;

    int totalRecords() throws EmfException;
    
    void setTotalRecords(int totalRecords);
    
    int getTotalRecords();

    void doApplyConstraints(String rowFilter, String sortOrder);

    void doApplyFormat() throws EmfException;
}