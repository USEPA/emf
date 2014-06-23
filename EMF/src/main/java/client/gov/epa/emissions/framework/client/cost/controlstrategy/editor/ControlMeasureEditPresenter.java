package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataEditorService;

public class ControlMeasureEditPresenter{

    private EditControlStrategyMeasuresTab parentView;
    private EmfSession session;

    public ControlMeasureEditPresenter(EditControlStrategyMeasuresTab parentView, ControlMeasureEditView view, 
            EmfSession session) {
        this.parentView = parentView;
        this.session = session;
    }

    public void display(ControlMeasureEditView view) throws Exception {
        view.observe(this);
        view.display();
    }

    public EmfDataset[] getDatasets(DatasetType type) throws EmfException
    {
            if (type == null)
                return new EmfDataset[0];

            return dataService().getDatasets(type);
    }
    
    public DatasetType getDatasetType(String datasetType)
    {
            return session.getLightDatasetType(datasetType);
    }
    
    public Version[] getVersions(EmfDataset dataset) throws EmfException 
    {
        if (dataset == null) {
            return new Version[0];
        }
        return dataEditorService().getVersions(dataset.getId());
    }

    private DataService dataService() {
        return session.dataService();
    }
    
    private DataCommonsService dataCommonsService() {
        return session.dataCommonsService();
    }

    private DataEditorService dataEditorService() {
        return session.dataEditorService();
    }

    public void doEdit(Double applyOrder, Double rulePenetration, 
            boolean overrideRulePenetration, Double ruleEffective, 
            boolean overrideRuleEffectiveness, EmfDataset ds, 
            Integer ver) {
        parentView.edit(applyOrder, rulePenetration, 
                overrideRulePenetration, ruleEffective, 
                overrideRuleEffectiveness, ds, 
                ver);
    }
}
