package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.services.EmfException;

public interface GenericManagerView<M, P, F> extends ManagedView {

    void display(M[] modelItems) throws EmfException;

    void observe(P presenter);

    void refresh(M[] modelItems) throws EmfException;

    M[] search(F modelFilter);
}
