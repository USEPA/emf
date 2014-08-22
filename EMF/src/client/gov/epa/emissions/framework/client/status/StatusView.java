package gov.epa.emissions.framework.client.status;

import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.services.basic.Status;

public interface StatusView extends EmfView {

    void update();

    void notifyError(String message);

    void observe(StatusPresenter presenter);

    void clear();

}
