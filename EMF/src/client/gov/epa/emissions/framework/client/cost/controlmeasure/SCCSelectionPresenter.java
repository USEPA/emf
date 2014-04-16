package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.controlmeasure.Sccs;


public class SCCSelectionPresenter {

    private SCCTableData tableData;

    private CMSCCTab parentView;

    public SCCSelectionPresenter(CMSCCTab parentView, SCCSelectionView view) {
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

    public void doAdd(Scc[] sccs) {
        parentView.add(sccs);
    }

}
