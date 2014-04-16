package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;

public interface RevisionEditorPresenter {

    void display(RevisionEditorView view, Revision revision, EmfDataset dataset) throws EmfException;

    void doSave() throws EmfException;
    
    void doRefresh();
}