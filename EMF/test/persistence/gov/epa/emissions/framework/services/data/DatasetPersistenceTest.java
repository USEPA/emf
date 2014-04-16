package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.ExImDbUpdate;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.hibernate.Query;

public class DatasetPersistenceTest extends HibernateTestCase {

    private String datasetName;

    private DataCommonsDAO dcDao;

    private UserDAO userDAO;

    protected void setUp() throws Exception {
        super.setUp();
        datasetName = "A1" + new Random().nextLong();
        dcDao = new DataCommonsDAO();
        userDAO = new UserDAO();
    }

    protected void doTearDown() throws Exception {
        DbUpdate emissionsUpdate = new PostgresDbUpdate(emissions().getConnection());
        emissionsUpdate.deleteAll(emissions().getName(), "versions");

        ExImDbUpdate eximUpdate = new ExImDbUpdate();
        eximUpdate.deleteAllDatasets();

        super.doTearDown();
    }

    public void testVerifySimplePropertiesAreStored() throws Exception {
        Country country = new Country("FR" + Math.random());
        Project project = new Project("P1" + Math.random());
        Sector sector = new Sector("", "S1" + Math.random());
        EmfDataset dataset = new EmfDataset();
        User owner = userDAO.get("emf", session);

        Region region = new Region("USA" + Math.random());
        try {
            dcDao.add(country, session);
            dcDao.add(project, session);
            dcDao.add(region, session);

            dataset.setAccessedDateTime(new Date());
            dataset.setCountry(country);
            dataset.setCreatedDateTime(new Date());
            dataset.setCreator(owner.getUsername());
            dataset.setDescription("DESCRIPTION");
            dataset.setModifiedDateTime(new Date());
            
            String newName = datasetName;
            if ( newName != null) {
                newName = newName.trim();
            } else {
                throw new EmfException("Dataset name is null");
            }
            dataset.setName(newName);
            
            dataset.setProject(project);
            dataset.setRegion(region);
            
            dataset.setSectors(new Sector[] { sector });
            dataset.setStartDateTime(new Date());
            dataset.setStatus("imported");
            dataset.setYear(42);
            dataset.setUnits("orl");
            dataset.setTemporalResolution("t1");
            dataset.setStopDateTime(new Date());

            DatasetType type = load("ORL Nonpoint Inventory");
            dataset.setDatasetType(type);

            KeyVal kv = new KeyVal();
            kv.setValue("bar-1");
            kv.setKeyword(new Keyword("bar-key" + Math.random()));
            dataset.addKeyVal(kv);

            save(dataset);
        } finally {
            remove(dataset);
            remove(sector);
            remove(country);
            remove(region);
            remove(project);
        }

    }

    private DatasetType load(String name) {
        Query query = session.createQuery("SELECT type FROM DatasetType AS type WHERE name='" + name + "'");
        List list = query.list();
        return list.size() == 1 ? (DatasetType) list.get(0) : null;
    }

}
