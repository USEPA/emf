package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserView;
//import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.exim.ExImService;

/**
 * updates the Datasets Browser once View is closed.
 */
public class DatasetsBrowserAwareImportPresenter extends ImportPresenter {

//    private DataService dataServices;

//    private DatasetsBrowserView browser;

    public DatasetsBrowserAwareImportPresenter(EmfSession session, User user, ExImService eximServices, DataService dataServices,
            DatasetsBrowserView browser) {
        super(session, user, eximServices);
 //       this.dataServices = dataServices;
 //       this.browser = browser;
    }

    public void doDone() {
        // Do not refresh due to time required
//        try {
//            browser.refresh(dataServices.getDatasets());
//        } catch (EmfException e) {
//            browser.showError("Could not refresh Datasets");
//            return;
//        }

        super.doDone();
    }

}
