package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessToken;

public interface TablePaginator {

    void doDisplayNext() throws EmfException;

    void doDisplayPrevious() throws EmfException;

    int pageNumber();

    void doDisplay(int pageNumber) throws EmfException;

    void reloadCurrent() throws EmfException;

    void doDisplayFirst() throws EmfException;

    void doDisplayLast() throws EmfException;

    void doDisplayPageWithRecord(int record) throws EmfException;

    int getTotalRecords() throws EmfException;

    DataAccessToken token();

    boolean isCurrent(int record);

    void clear();

}