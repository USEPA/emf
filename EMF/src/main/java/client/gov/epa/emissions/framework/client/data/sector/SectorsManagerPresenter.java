package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

public class SectorsManagerPresenter {

    private SectorsManagerView view;

    private DataCommonsService service;

    private EmfSession session;

    public SectorsManagerPresenter(EmfSession session, SectorsManagerView view, DataCommonsService service) {
        this.session = session;
        this.view = view;
        this.service = service;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(service.getSectors());
    }

    public void doClose() {
        view.disposeView();
    }

    public void doEdit(Sector sector, EditableSectorView editSectorView, ViewableSectorView displaySectorView)
            throws EmfException {
        EditableSectorPresenter p = new EditableSectorPresenterImpl(session, editSectorView, displaySectorView, sector);
        edit(p);
    }

    public void displayNewSector(Sector sector, NewSectorView newSectorView) {
        NewSectorPresenter p = new NewSectorPresenter(session, sector, newSectorView);
        p.doDisplay();
    }

    void edit(EditableSectorPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void doView(Sector sector, ViewableSectorView viewable) {
        ViewableSectorPresenter p = new ViewableSectorPresenterImpl(viewable, sector);
        view(p);
    }

    void view(ViewableSectorPresenter presenter) {
        presenter.doDisplay();
    }

    public void doRefresh() throws EmfException {
        view.refresh(service.getSectors());
    }

}
