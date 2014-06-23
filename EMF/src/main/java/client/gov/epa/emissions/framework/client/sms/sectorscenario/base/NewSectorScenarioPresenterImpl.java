package gov.epa.emissions.framework.client.sms.sectorscenario.base;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.sms.sectorscenario.SectorScenarioManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenarioService;
import gov.epa.emissions.framework.services.sms.SectorScenario;

import java.util.Date;

public class NewSectorScenarioPresenterImpl implements NewSectorScenarioPresenter{

    protected EmfSession session;

    private NewSectorScenarioView view;
    
    protected SectorScenarioManagerPresenter managerPresenter;
    
    public NewSectorScenarioPresenterImpl(NewSectorScenarioView view, EmfSession session, 
            SectorScenarioManagerPresenter managerPresenter) {
        this.session = session;
        this.view = view;
        this.managerPresenter = managerPresenter;
    }
    
    public NewSectorScenarioPresenterImpl(EmfSession session, 
            SectorScenarioManagerPresenter managerPresenter) {
        this.session = session;
        this.managerPresenter = managerPresenter;
    }

    public void display() throws EmfException {
        view.observe(this);
        view.display();
    }
    
    public EmfSession getSession(){
        return session; 
    }
    
    public void addSectorScenario(SectorScenario sectorScenario) throws EmfException {
        validateNameAndAbbre(sectorScenario);
        sectorScenario.setLastModifiedDate(new Date());
        sectorScenario.setStraightEecsMatch(false);
        //make sure an set Id, this is needed for the manager, so we know which one to edit or view
        sectorScenario.setId(service().addSectorScenario(sectorScenario));
        
        //SectorScenario loaded = service().getById(id);
        managerPresenter.addNewSSToTableData(sectorScenario);
    }
       
    private void validateNameAndAbbre(SectorScenario sectorScenario) throws EmfException {
        // emptyName
        String name = sectorScenario.getName();
        String abbre = sectorScenario.getAbbreviation();
        if (name.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the name.");

        if (abbre.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the abbre.");

        if (isDuplicate(name))
            throw new EmfException("A Sector Scenario named '" + name + "' already exists.");
    
        if (isDuplicateAbbre(abbre))
            throw new EmfException("A Sector Scenario with abbre  '" + abbre + "' already exists.");
    }

    private boolean isDuplicate(String name) throws EmfException {
        int id = service().isDuplicateName(name);
        return (id != 0);
    }
    
    private boolean isDuplicateAbbre(String abbre) throws EmfException {
        int id = service().isDuplicateAbbre(abbre);
        return (id != 0);
    }
    
    private SectorScenarioService service(){    
        return session.sectorScenarioService();
    }
}