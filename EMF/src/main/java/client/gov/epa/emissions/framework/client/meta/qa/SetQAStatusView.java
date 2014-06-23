package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;

public interface SetQAStatusView extends ManagedView {
    void display(QAStep[] steps, User user);

    void observe(SetQAStatusPresenter presenter);

    void save() throws EmfException;
}
