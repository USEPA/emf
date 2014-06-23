package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.controlmeasure.Sccs;


public class SCCFindPresenter {

    private SCCTableData tableData;

    private ControlMeasuresManagerView parentView;

    public SCCFindPresenter(ControlMeasuresManagerView parentView, SCCSelectionView view) {
        this.parentView = parentView;
    }

    public void display(SCCSelectionView view) throws Exception {
        Sccs sccs = new Sccs();
        String emfHome = System.getProperty("EMF_HOME");
        if(emfHome == null)
            emfHome = ".";
        SccFileReader reader = new SccFileReader(emfHome + "/config/ref/delimited/scc.txt", sccs);
        reader.read();
        view.observe(this);
        this.tableData = new SCCTableData(sccs.getSccs());
        view.display(tableData);

    }

    public void doFind(Scc[] sccs) {
        parentView.doFind(sccs);
    }

}
