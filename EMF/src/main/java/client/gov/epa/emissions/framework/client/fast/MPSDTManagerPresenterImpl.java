package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.services.fast.FastService;

public class MPSDTManagerPresenterImpl implements MPSDTManagerPresenter {

    private EmfSession session;

    private MPSDTManagerView view;

    public MPSDTManagerPresenterImpl(EmfSession session, MPSDTManagerView view) {

        this.session = session;
        this.view = view;
    }

    public void doDisplay() {

        view.observe(this);
        view.display();
    }

    public void doClose() {
        view.disposeView();
    }

    private FastService getService() {
        return this.session.fastService();
    }

    public FastRun[] getRuns() {

        FastRun[] runs = new FastRun[0];
        try {
            runs = this.getService().getFastRuns();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        return runs;
    }
}
