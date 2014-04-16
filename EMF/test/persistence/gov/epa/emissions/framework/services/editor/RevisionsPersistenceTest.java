package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.services.persistence.ExImDbUpdate;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class RevisionsPersistenceTest extends HibernateTestCase {

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

    public void testShouldGetZeroRevisionsInitially() throws Exception {
        List allRevisions = this.loadAllRevisions();
        assertEquals("0 Revisions", allRevisions.size(), 0);
    }

    public void testShouldAddARevision() throws Exception {
        Random rando = new Random();
        long id = Math.abs(rando.nextInt());
        Revision rev = null;

        User user = null;
        EmfDataset dataset = null;

        try {
            user = new User("Falcone", "UNC", "919-966-9572", "falcone@unc.edu", "falcone" + id, "falcone123", false,
                    false);
            save(user);

            dataset = new EmfDataset();
            
            String newName = user.getUsername() + "_" + id;
            if ( newName != null) {
                newName = newName.trim();
            } else {
                throw new EmfException("Dataset name is null");
            }
            dataset.setName(newName);

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

            EmfDataset datasetFromDB = loadDataset(user.getUsername() + "_" + id);
            rev = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT ONE",
                    "WHY ONE", "NOTE ONE");

            save(rev);
            Revision revFromDB = loadRevision("WHAT ONE");
            assertNotNull(revFromDB);

        } finally {
            remove(rev);
            remove(dataset);
            remove(user);
        }
    }

    public void testShouldAddAndGetThreeRevisions() throws Exception {
        Random rando = new Random();
        long id = Math.abs(rando.nextInt());
        Revision rev1 = null, rev2 = null, rev3 = null;

        User user = null;
        EmfDataset dataset = null;

        try {
            user = new User("Falcone", "UNC", "919-966-9572", "falcone@unc.edu", "falcone" + id, "falcone123", false,
                    false);
            save(user);

            dataset = new EmfDataset();
            
            String newName = user.getUsername() + "_" + id;
            if ( newName != null) {
                newName = newName.trim();
            } else {
                throw new EmfException("Dataset name is null");
            }
            dataset.setName(newName);
            
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

            EmfDataset datasetFromDB = loadDataset(user.getUsername() + "_" + id);
            rev1 = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT ONE",
                    "WHY ONE", "NOTE ONE");
            save(rev1);

            rev2 = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT TWO",
                    "WHY TWO", "NOTE TWO");
            save(rev2);

            rev3 = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT THREE",
                    "WHY THREE", "NOTE THREE");
            save(rev3);

            List allRevisionsFromDB = loadAllRevisions();
            assertEquals("Should return 3 revisions", allRevisionsFromDB.size(), 3);

        } finally {
            remove(rev1);
            remove(rev2);
            remove(rev3);
            remove(dataset);
            remove(user);
        }
    }

    private Revision loadRevision(String name) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Revision.class).add(Restrictions.eq("what", name));
            tx.commit();

            return (Revision) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private List loadAllRevisions() {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Revision.class);
            tx.commit();

            return crit.list();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private EmfDataset loadDataset(String name) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(EmfDataset.class).add(Restrictions.eq("name", name));
            tx.commit();

            return (EmfDataset) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private DatasetType loadDatasetType(String name) {
        Query query = session.createQuery("SELECT types FROM DatasetType AS types WHERE name='" + name + "'");
        List list = query.list();
        return list.size() == 1 ? (DatasetType) list.get(0) : null;
    }

}
