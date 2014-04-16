package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.ExImDbUpdate;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.hibernate.Query;

public class AccessLogPersistenceTest extends HibernateTestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void doTearDown() throws Exception {
        DbUpdate emissionsUpdate = new PostgresDbUpdate(emissions().getConnection());
        emissionsUpdate.deleteAll(emissions().getName(), "versions");

        ExImDbUpdate eximUpdate = new ExImDbUpdate();
        eximUpdate.deleteAllDatasets();

        super.doTearDown();
    }

    public void testVerifyOneAccessLogLogged() throws Exception {

        Random rando = new Random();
        long id = Math.abs(rando.nextInt());

        User user = null;
        EmfDataset dataset = null;
        AccessLog alog = null;

        try {
            user = new User("Falcone", "UNC", "919-966-9572", "falcone@unc.edu", "falcone" + id, "falcone123", false,
                    false);
            save(user);

            dataset = new EmfDataset();
            
            String userName = user.getUsername();
            if ( userName != null) {
                userName = userName.trim();
            } else {
                throw new EmfException("User name is null");
            }
            dataset.setName(userName + "_" + id);
            
            dataset.setAccessedDateTime(new Date());
            dataset.setCreatedDateTime(new Date());
            dataset.setCreator(user.getUsername());
            dataset.setDatasetType(loadDatasetType("External File (External)"));
            dataset.setDescription("DESCRIPTION");
            dataset.setModifiedDateTime(new Date());
            dataset.setStartDateTime(new Date());
            dataset.setStatus("imported");
            dataset.setYear(42);
            dataset.setUnits("orl");
            dataset.setTemporalResolution("t1");
            dataset.setStopDateTime(new Date());
            save(dataset);

            alog = new AccessLog(user.getUsername(), dataset.getId(), new Date(), "0", "description", "folderPath");
            alog.setDatasetname("");
            alog.setEnddate(new Date());
            alog.setTimereqrd(12345.0);
            save(alog);

            List allLogs = loadAccessLogs();
            Object[] logAr = allLogs.toArray();
            assertEquals(alog.getDatasetId(), ((AccessLog) logAr[0]).getDatasetId());

        } finally {
            remove(alog);
            remove(dataset);
            remove(user);

        }
    }

    private List loadAccessLogs() {
        Query query = session.createQuery("SELECT al FROM AccessLog AS al");
        List list = query.list();
        return list;
    }

    private DatasetType loadDatasetType(String name) {
        Query query = session.createQuery("SELECT type FROM DatasetType AS type WHERE name='" + name + "'");
        List list = query.list();
        return list.size() == 1 ? (DatasetType) list.get(0) : null;
    }

}
