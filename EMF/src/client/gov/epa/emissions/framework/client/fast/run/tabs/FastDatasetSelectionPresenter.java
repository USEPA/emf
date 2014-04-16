package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastDataset;

import java.util.ArrayList;
import java.util.List;

public class FastDatasetSelectionPresenter {

    private EmfSession session;

    private FastDatasetSelectionView view;

    private static String lastNameContains = null;

    private static List<FastDataset> lastDatasets = new ArrayList<FastDataset>();

    public FastDatasetSelectionPresenter(FastDatasetSelectionView view, EmfSession session) {

        this.session = session;
        this.view = view;
    }

    public void display(boolean selectSingle) throws Exception {

        view.observe(this);
        view.display(selectSingle);
    }

    public void refreshDatasets(String nameContaining) throws EmfException {

        if (lastDatasets != null && nameContaining.equals(lastNameContains)
                && lastDatasets.size() == session.fastService().getFastDatasetCount()) {

            // nothing has changed since last time, so just refresh with the previously retrieved list
            System.out.println("Using previously retrieved datasets: name=" + nameContaining);
            view.refreshDatasets(lastDatasets);
        } else {

            System.out.println("Getting new datasets");
            // lastDatasets = session.fastService().getFastDatasets(nameContaining);

            FastDataset[] pointDatasets = session.fastService().getFastDatasets();
            lastDatasets = this.createFastDatasetList(pointDatasets);

            view.refreshDatasets(lastDatasets);
        }

        lastNameContains = nameContaining;
    }

    private List<FastDataset> createFastDatasetList(FastDataset[] pointDatasets) {

        List<FastDataset> datasetsWrappers = new ArrayList<FastDataset>(pointDatasets.length);
        for (FastDataset pointDataset : pointDatasets) {
            datasetsWrappers.add(pointDataset);
        }

        return datasetsWrappers;
    }

    public List<FastDataset> getDatasets() {
        return view.getDatasets();
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {

        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public EmfSession getSession() {
        return session;
    }

    public FastDataset getDatasets(int id) throws EmfException {
        return session.fastService().getFastDataset(id);
    }

}