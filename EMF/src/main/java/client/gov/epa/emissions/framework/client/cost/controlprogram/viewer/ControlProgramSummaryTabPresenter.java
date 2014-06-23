package gov.epa.emissions.framework.client.cost.controlprogram.viewer;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ControlProgramSummaryTabPresenter {

    private ControlProgramSummaryTab view;

    private EmfSession session;

    public ControlProgramSummaryTabPresenter(ControlProgramSummaryTab view, EmfSession session) {

        this.session = session;
        this.view = view;
    }

    public void doDisplay() throws EmfException {
        
        view.observe(this);
        view.display();
    }

    public void doChangeControlProgramType(ControlProgramType controlProgramType) {
        // NOTE Auto-generated method stub
    }

    public DatasetType[] getDatasetTypes() {
        return session.getLightDatasetTypes();
    }

    public EmfDataset[] getDatasets(DatasetType type) throws EmfException {
        if (type == null)
            return new EmfDataset[0];

        return session.dataService().getDatasets(type);
    }

    public Version[] getVersions(EmfDataset dataset) throws EmfException {
        if (dataset == null) {
            return new Version[0];
        }
        return session.dataEditorService().getVersions(dataset.getId());
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {

        view.clearMessage();
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public EmfDataset getDataset(int id) throws EmfException {
        return session.dataService().getDataset(id);
    }
}
