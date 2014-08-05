package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class NotesTabPresenter  implements LightSwingWorkerPresenter{

    private EmfDataset dataset;

    private DataCommonsService service;
    private EmfSession session;
    
    private NotesTabView view;

    public NotesTabPresenter(EmfDataset dataset, EmfSession session) {
        this.dataset = dataset;
        this.service = session.dataCommonsService();
    }

    public void display(NotesTabView view) throws EmfException {
        this.view = view;
        view.display(getDatasetNotes(), this);
    }

    public void doViewNote(DatasetNote note, NoteView view) {
        view.display(note);
    }
    
    public DatasetNote[] getDatasetNotes() throws EmfException{
        return service.getDatasetNotes(dataset.getId());
    }

    @Override
    public Object[] swProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public Object[] refreshProcessData() throws EmfException {
        return getDatasetNotes();
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.doRefresh( (DatasetNote[]) objs );
    }

    @Override
    public Object[] saveProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void saveDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }
}
