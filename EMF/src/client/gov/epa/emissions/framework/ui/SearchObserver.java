package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.services.EmfException;

public interface SearchObserver<F, M> {

    M[] doSearch(F filter) throws EmfException;

}
