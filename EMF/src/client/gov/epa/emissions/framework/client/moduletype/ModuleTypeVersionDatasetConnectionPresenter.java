package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.data.DataCommonsService;

public class ModuleTypeVersionDatasetConnectionPresenter {
       
    private ModuleTypeVersionDatasetConnectionView view;
    private ModuleTypeConnectionsObserver connectionsObserver;

    private EmfSession session;

    public ModuleTypeVersionDatasetConnectionPresenter(EmfSession session, ModuleTypeVersionDatasetConnectionView view, ModuleTypeConnectionsObserver connectionsObserver) {
        this.session = session;
        this.view = view;
        this.connectionsObserver = connectionsObserver;
    }

    public void refreshConnections() {
        connectionsObserver.refreshConnections();
    }
    
    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }
}
