package gov.epa.emissions.framework.services.sms;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;

import java.util.Date;
import java.util.List;

public interface SectorScenarioService extends EMFService {

    SectorScenario[] getSectorScenarios() throws EmfException;
    
    SectorScenarioOutput[] getSectorScenarioOutputs(int sectorScenarioId) throws EmfException;
    
    int addSectorScenario(SectorScenario element) throws EmfException;
    
    void removeSectorScenarios(int[] ids, User user) throws EmfException;

    SectorScenario obtainLocked(User owner, int id) throws EmfException;

    void releaseLocked(User user, int id) throws EmfException;

    SectorScenario updateSectorScenario(SectorScenario element) throws EmfException;
    
    SectorScenario updateSectorScenarioWithLock(SectorScenario element) throws EmfException;
    
    void runSectorScenario (User user, int sectorScenarioId) throws EmfException;

    List<SectorScenario> getSectorScenariosByRunStatus(String runStatus) throws EmfException;
    
    void stopRunSectorScenario(int sectorScenarioId) throws EmfException;

    String sectorScenarioRunStatus(int id) throws EmfException;

    int isDuplicateName(String name) throws EmfException;

    int isDuplicateAbbre(String abbre) throws EmfException;

    int copySectorScenario(int id, User creator) throws EmfException;

    SectorScenario getById(int id) throws EmfException;

    void setSectorScenarioRunStatusAndCompletionDate(int id, String runStatus, Date completionDate) throws EmfException;
    //StrategyType[] getEquaitonTypes();
    
    Long getSectorScenarioRunningCount() throws EmfException;

    public String getDefaultExportDirectory() throws EmfException;

    public String getStrategyRunStatus(int id) throws EmfException;
    
    public String[] getDistinctSectorListFromDataset(int datasetId, int versionNumber) throws EmfException;
}
