package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportWindow;
import gov.epa.emissions.framework.client.meta.info.InfoTabView;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabView;
import gov.epa.emissions.framework.client.meta.qa.EditableQATabView;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;
import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;

public interface PropertiesEditorPresenter extends LightSwingWorkerPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void set(EditableSummaryTabView summary);

    void set(EditableKeywordsTabView keywordsView) throws EmfException;

    void set(EditNotesTabView view) throws EmfException;
    
    void set(EditableQATabView qatab) throws EmfException;
    
    void set(DataTabView view);
    
    void set(RevisionsTabView view) throws EmfException;
    
    public void set(InfoTabView view) throws EmfException;

    EmfSession getSession();

    void doExport(ExportWindow view, ExportPresenter presenter);
    
    EditableKeywordsTabPresenter getKeywordsPresenter();

}