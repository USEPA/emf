package gov.epa.emissions.framework.client.cost.controlstrategy;

import java.io.File;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyView;
import gov.epa.emissions.framework.client.cost.controlstrategy.groups.StrategyGroupManagerView;
import gov.epa.emissions.framework.client.cost.controlstrategy.viewer.ViewControlStrategyView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;

public interface ControlStrategiesManagerPresenter {

    void display() throws EmfException;

    void doRefresh() throws EmfException;

    void doClose();

    void doNew(ControlStrategyView view);

    void doEdit(EditControlStrategyView view, ControlStrategy controlStrategy) throws EmfException;

    void doView(ViewControlStrategyView view, ControlStrategy controlStrategy) throws EmfException;

//    void doRemove(ControlStrategy[] strategies) throws EmfException;

    void doRemove(int[] ids) throws EmfException;

//    void doSaveCopiedStrategies(ControlStrategy coppied, String name) throws EmfException;

    void doSaveCopiedStrategies(int id, User creator) throws EmfException;

    LightControlMeasure[] getControlMeasures();

    void loadControlMeasures() throws EmfException;

    void viewControlStrategyComparisonResult(int[] ids, String string) throws EmfException;

    void summarizeControlStrategies(int[] ids, File localFile) throws EmfException;
    
    public void doDisplayStrategyGroups(StrategyGroupManagerView view) throws EmfException;
}