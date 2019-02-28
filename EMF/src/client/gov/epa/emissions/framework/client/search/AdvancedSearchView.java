package gov.epa.emissions.framework.client.search;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategyManagerWindow;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface AdvancedSearchView<F, M> extends ManagedView {
    public static enum ListSelectionType {
        SINGLE_SELECTION,
        MULTIPLE_SELECTION
    }

    void display(M[] modelItems);

    void observe(AdvancedSearchPresenter<F, M> presenter);

    void refresh(M[] modelItems);

    String getNameContains();

    void showMessage(String message);

    void showError(String message);

    void clearMessage();

    void notifyAdvancedSearchOff();
    
    void populate();

    void showUsers(boolean show, ListSelectionType selectionType);

    void showDataTypes(boolean show, ListSelectionType selectionType);

    void showName(boolean show);

    void showDescription(boolean show);

    void showProjects(boolean show, ListSelectionType selectionType);

    void showKeywords(boolean show, ListSelectionType selectionType);

    void showCases(boolean show, ListSelectionType selectionType);

    void showControlPrograms(boolean show, ListSelectionType selectionType);
}
