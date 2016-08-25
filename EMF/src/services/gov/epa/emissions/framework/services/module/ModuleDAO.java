package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class ModuleDAO {

    private HibernateFacade hibernateFacade;

    public ModuleDAO() {
        hibernateFacade = new HibernateFacade();
    }

}
