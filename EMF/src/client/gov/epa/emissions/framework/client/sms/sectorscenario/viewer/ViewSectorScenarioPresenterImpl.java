package gov.epa.emissions.framework.client.sms.sectorscenario.viewer;
import java.util.Iterator;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioPresenterImpl;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;

public class ViewSectorScenarioPresenterImpl  extends EditSectorScenarioPresenterImpl implements ViewSectorScenarioPresenter {

    //private ViewSectorScenarioView view;

    public ViewSectorScenarioPresenterImpl(SectorScenario sectorScenario, EmfSession session, 
            ViewSectorScenarioView view) {
        super(sectorScenario, session, view);
        //this.view = view; 
    }

    public void doDisplay() throws EmfException {
        view.observe(this);

        sectorScenario = session.sectorScenarioService().getById(sectorScenario.getId());
        
        view.display(sectorScenario);
        
        //make all things non-editable
        ((ViewSectorScenarioView)view).viewOnly();
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            EditSectorScenarioTabPresenter element = (EditSectorScenarioTabPresenter) iter.next();
            element.doViewOnly();
        }
    }


    public void doRefresh() {
        try {
            super.doRefresh();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void viewOnly() {
       // 
    }

}
