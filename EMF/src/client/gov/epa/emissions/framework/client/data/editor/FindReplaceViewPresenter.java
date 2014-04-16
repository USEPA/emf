package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;
import gov.epa.emissions.framework.services.EmfException;

public class FindReplaceViewPresenter {
    
    private FindReplaceWindowView view;
    
    private EmfSession session;

    private TablePresenter tablePresenter;
    
    public FindReplaceViewPresenter(TablePresenter tablePresenter, FindReplaceWindowView view, EmfSession session) {
        this.view = view;
        this.session = session;
        this.tablePresenter = tablePresenter;
    }
    
    public void displayView() {
        view.observe(this);
        view.display();
    }
    
    public void replaceColValues(String table, String colName, String find, String replaceWith, Version version, String rowFilter) throws EmfException {
        this.session.dataService().replaceColValues(table, colName, find, replaceWith, version, rowFilter);
    }
    
    public void replaceValues(String table, String findFilter, String replaceWith, Version version, String rowFilter) throws EmfException {
        this.session.dataService().replaceColValues(table, findFilter, replaceWith, version, rowFilter);
    }
    
    public void applyConstraint(String rowFilter, String sortOrder) throws EmfException {
        tablePresenter.doApplyConstraints(rowFilter, sortOrder);
    }
    
}
