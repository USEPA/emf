package gov.epa.emissions.framework.client.fast.analyzer;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

public interface FastAnalysisManagerPresenter {

    void doRefresh() throws EmfException;

    void doClose();

    void doNew() throws EmfException;

    void doEdit(int id) throws EmfException;

    void doView(int id) throws EmfException;

    void doRemove(int[] ids) throws EmfException;

    void doSaveCopiedAnalysis(int id, User creator) throws EmfException;

    void doAnalysis(int id[]) throws EmfException;

    void doExport(int id[]) throws EmfException;

    void display() throws EmfException;
    
    void loadAnalyses() throws EmfException;
}