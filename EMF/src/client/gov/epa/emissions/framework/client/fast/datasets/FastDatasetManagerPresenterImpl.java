package gov.epa.emissions.framework.client.fast.datasets;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastDataset;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;
import gov.epa.emissions.framework.services.fast.FastNonPointDataset;
import gov.epa.emissions.framework.services.fast.FastService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.List;

public class FastDatasetManagerPresenterImpl implements RefreshObserver, FastDatasetManagerPresenter {

    private FastDatasetManagerView view;

    private EmfSession session;

    //private List<FastDatasetWrapper> datasetWrappers;

    public FastDatasetManagerPresenterImpl(EmfSession session, FastDatasetManagerView view) {

        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {

        FastDataset[] fastDatasets = service().getFastDatasets();

        view.display(this.createFastDatasetWrappers(fastDatasets));
        view.observe(this);
    }

    private List<FastDatasetWrapper> createFastDatasetWrappers(FastDataset[] fastDatasets) {

        List<FastDatasetWrapper> datasetsWrappers = new ArrayList<FastDatasetWrapper>(fastDatasets.length);

        for (FastDataset fastDataset : fastDatasets) {
            datasetsWrappers.add(new FastDatasetWrapper(fastDataset));
        }

        return datasetsWrappers;
    }

    private FastService service() {
        return session.fastService();
    }

    public void doRefresh() throws EmfException {

        FastDataset[] fastDatasets = service().getFastDatasets();
        view.refresh(this.createFastDatasetWrappers(fastDatasets));
    }

    public void doClose() {
        //
    }   

    public void doNew() throws EmfException {

        throw new EmfException("New not implemented.");

        // FastView creatorView = new FastCreatorWindow(this.view.getDesktopManager(), session, this.view
        // .getParentConsole());
        // FastPresenter presenter = new FastCreatorPresenterImpl(session, creatorView, this);
        // presenter.doDisplay();
    }

    public void doView(int id) throws EmfException {

        throw new EmfException("View not implemented.");
    }

    public void doEdit(int id) throws EmfException {

        throw new EmfException("Edit not implemented.");

        // FastView editorView = new FastEditorWindow(this.view.getDesktopManager(), session,
        // this.view.getParentConsole());
        // FastPresenter presenter = new FastEditorPresenterImpl(id, session, editorView, this);
        // presenter.doDisplay();
    }

    public void doRemove(FastDatasetWrapper wrapper) throws EmfException {

        if (wrapper.isPoint()) {
            service().removeFastDataset(wrapper.getId(), this.session.user());
        } else if (wrapper.isNonPoint()) {
            service().removeFastNonPointDataset(wrapper.getId(), this.session.user());
        }
    }

    public void doSaveDataset(FastDatasetWrapper fastDatasetWrapper) throws EmfException {

        if (fastDatasetWrapper.isPoint()) {
            service().addFastDataset(fastDatasetWrapper.getPointDataset());
        } else if (fastDatasetWrapper.isNonPoint()) {
            FastNonPointDataset nonPointDataset = fastDatasetWrapper.getNonPointDataset();
            service().addFastNonPointDataset(nonPointDataset, this.session.user());
        }
    }

    public void doControl(int id) throws EmfException {

        throw new EmfException("Control not implemented.");
    }

    public void loadDatasets() {

//        FastDataset[] fastDatasets = service().getFastDatasets();
//        this.datasetWrappers = this.createFastDatasetWrappers(fastDatasets);
    }

    public DatasetType getDatasetType(String name) throws EmfException {
        return session.getLightDatasetType(name);
    }
}
