package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ControlProgramSummaryTabPresenter  implements ControlProgramTabPresenter {
    private ControlProgramSummaryTab view;
    
    private EmfSession session;
    
    private ControlProgram controlProgram;

    public ControlProgramSummaryTabPresenter(ControlProgramSummaryTab view, 
            ControlProgram controlProgram, EmfSession session) {
        this.controlProgram = controlProgram;
        this.session = session;
        this.view = view;
    }
    
    public void doDisplay() throws EmfException  {
        view.observe(this);
        view.display(this.controlProgram);
    }

    public void doSave(ControlProgram controlProgram) throws EmfException {
        view.save(controlProgram);
    }

    public void doChangeControlProgramType(ControlProgramType controlProgramType) {
        this.controlProgram.setControlProgramType(controlProgramType);
    }

    public DatasetType[] getDatasetTypes() {
        return session.getLightDatasetTypes();
     }

     public EmfDataset[] getDatasets(DatasetType type) throws EmfException
 {
         if (type == null)
             return new EmfDataset[0];

         return session.dataService().getDatasets(type);
     }

     public Version[] getVersions(EmfDataset dataset) throws EmfException 
     {
         if (dataset == null) {
             return new Version[0];
         }
         return session.dataEditorService().getVersions(dataset.getId());
     }
     
     public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
         view.clearMsgPanel();
         PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
         presenter.doDisplay(propertiesView);
     }

     public EmfDataset getDataset(int id) throws EmfException {
         return session.dataService().getDataset(id);
     }
}
