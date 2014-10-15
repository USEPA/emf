package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.framework.client.data.viewer.TablePresenter;
import gov.epa.emissions.framework.services.EmfException;

public interface EditableTablePresenter extends TablePresenter {

    boolean hasChanges();

    boolean submitChanges() throws EmfException;

    void reloadCurrent() throws EmfException;

}