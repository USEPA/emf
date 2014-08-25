package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;

public interface TableView {

    void display(Page page);

    void clear();
    
    void scrollToPageEnd();

    TableMetadata tableMetadata();

    void observe(TablePresenter presenter);

    void updateFilteredRecordsCount(int filtered);
    
    String getRowFilter();
}