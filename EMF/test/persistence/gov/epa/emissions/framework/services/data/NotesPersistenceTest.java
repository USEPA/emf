package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.persistence.ExImDbUpdate;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class NotesPersistenceTest extends HibernateTestCase {

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

    public void testShouldGetAllNoteTypes() throws Exception {
        List allNoteTypes = this.loadAllNoteTypes();
        assertEquals("9 Node Types", 9,allNoteTypes.size());
    }

    public void testShouldGetOneNoteType() throws Exception {
        NoteType notetype = loadNoteType("Observation");
        assertEquals(notetype.getType(), "Observation");
    }

    public void testShouldAddANote() throws Exception {
        Random rando = new Random();
        long id = Math.abs(rando.nextInt());
        NoteType notetype;
        Note note = null;

        User user = null;
        EmfDataset dataset = null;

        try {
            notetype = loadNoteType("Observation");
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
            note = new Note(user, datasetFromDB.getId(), new Date(), "NOTE DETAILS", "NOTE NAME", notetype, "abcd",
                    dataset.getDefaultVersion());
            save(note);
            Note noteFromDB = loadNote("NOTE NAME");
            assertNotNull(noteFromDB);

        } finally {
            remove(note);
            remove(dataset);
            remove(user);
        }
    }

    private Note loadNote(String name) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Note.class).add(Restrictions.eq("name", name));
            tx.commit();

            return (Note) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private List loadAllNoteTypes() {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(NoteType.class);
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

    private NoteType loadNoteType(String type) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(NoteType.class).add(Restrictions.eq("type", type));
            tx.commit();

            return (NoteType) crit.uniqueResult();
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
