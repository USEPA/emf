package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;

public interface ControlProgramService extends EMFService {

    ControlProgram[] getControlPrograms() throws EmfException;
    
    ControlProgram getControlProgram(int id) throws EmfException;
    
    ControlProgramType[] getControlProgramTypes() throws EmfException;
    
    int addControlProgram(ControlProgram element) throws EmfException;
    
    void removeControlPrograms(int[] ids, User user) throws EmfException;

    ControlProgram obtainLocked(User owner, int id) throws EmfException;

    void releaseLocked(User user, int id) throws EmfException;

    ControlProgram updateControlProgram(ControlProgram element) throws EmfException;
    
    ControlProgram updateControlProgramWithLock(ControlProgram element) throws EmfException;
    
    int copyControlProgram(int id, User creator) throws EmfException;

}
