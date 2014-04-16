package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataEditorService;

public class ControlMeasureSelectionPresenter {

    private ControlMeasureTableData tableData;
    private EditControlStrategyMeasuresTab parentView;
    private LightControlMeasure[] controlMeasures;
    private EmfSession session;
    private ControlStrategy controlStrategy;

    public ControlMeasureSelectionPresenter(EditControlStrategyMeasuresTab parentView, ControlMeasureSelectionView view, 
            EmfSession session, LightControlMeasure[] controlMeasures, ControlStrategy controlStrategy) {
        this.parentView = parentView;
        this.controlMeasures = controlMeasures;
        this.session = session;
        this.controlStrategy = controlStrategy;
    }
    
    private boolean isCreatorSuperUser(ControlStrategy controlStrategy) throws EmfException {
        try {
            User currentUser = controlStrategy.getCreator(); // session.user();
            String costSUs = session.controlStrategyService().getCoSTSUs(); //presenter.getCoSTSUs();
            //if this is found, then every one is considered an SU (really used for State Installations....)
            if (costSUs.equals("ALL_USERS")) return true;
            StringTokenizer st = new StringTokenizer(costSUs,"|");
            while ( st.hasMoreTokens()) {
                String token = st.nextToken();
                if ( token.equals( currentUser.getUsername())) {
                    return true;
                }
            }
            return false;

        } catch (EmfException e1) {
            e1.printStackTrace();
            throw e1;
        }        
    }    
   
    private LightControlMeasure[] filterControlMeasures (LightControlMeasure[] controlMeasures, ControlStrategy controlStrategy) throws EmfException {
        if ( controlMeasures == null) { 
            throw new EmfException("The method filterControlMeasures's parameter controlMeasures is null.");
        }
        if ( controlStrategy == null) { 
            throw new EmfException("The method filterControlMeasures's parameter controlStrategy is null.");
        }
        User user = controlStrategy.getCreator(); //session.user();
        List<LightControlMeasure> filteredCMs = new ArrayList<LightControlMeasure>();
        for ( LightControlMeasure lcm : controlMeasures) {
            if ( lcm == null) {
                continue;
            }
            if ( lcm.getCmClass().getName().trim().toLowerCase().equals("temporary") &&
                 !lcm.getCreator().equals(user)) {
                continue;
            }
            filteredCMs.add(lcm);
        }
        return filteredCMs.toArray(new LightControlMeasure[0]);
    }
    
    public void display(ControlMeasureSelectionView view) throws Exception {
        view.observe(this);
        
        LightControlMeasure[] filteredControlMeasures = null;
        
        if ( !isCreatorSuperUser( this.controlStrategy)) {
            filteredControlMeasures = filterControlMeasures(this.controlMeasures, this.controlStrategy);
        } else {
            filteredControlMeasures = controlMeasures;
        }
        
        this.tableData = new ControlMeasureTableData(filteredControlMeasures); //controlMeasures);
        view.display(tableData);

    }

    public void doAdd(LightControlMeasure[] cms, Double applyOrder, Double rulePenetration, Double ruleEffective, EmfDataset ds, Integer ver) {
        parentView.add(cms, applyOrder, 
                rulePenetration, ruleEffective, 
                ds, ver);
        
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

}
