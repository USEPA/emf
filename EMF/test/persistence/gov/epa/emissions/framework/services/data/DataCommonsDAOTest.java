package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.editor.Revision;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DataCommonsDAOTest extends ServiceTestCase {

    private DataCommonsDAO dao;

    private DatasetDAO datasetDAO;

    private UserDAO userDao;

    protected void doSetUp() throws Exception {
        dao = new DataCommonsDAO();
        userDao = new UserDAO();
        datasetDAO = new DatasetDAO();
    }

    protected void doTearDown() {// no op
    }

    public void testShouldGetAllKeywords() {
        int totalBeforeAdd = dao.getKeywords(session).size();
        Keyword keyword = new Keyword("test" + Math.random());
        add(keyword);

        try {
            List keywords = dao.getKeywords(session);
            assertEquals(totalBeforeAdd + 1, keywords.size());
            assertEquals(keyword, keywords.get(totalBeforeAdd + 0));
        } finally {
            remove(keyword);
        }
    }

    public void testShouldGetAllCountries() {
        int totalBeforeAdd = dao.getCountries(session).size();
        Country country = new Country("test" + Math.random());
        add(country);

        try {
            List countries = dao.getCountries(session);
            assertEquals(totalBeforeAdd + 1, countries.size());
            assertTrue(countries.contains(country));
        } finally {
            remove(country);
        }
    }

    public void testShouldGetAllProjects() {
        int totalBeforeAdd = dao.getProjects(session).size();
        Project project = new Project("test" + Math.random());
        add(project);

        try {
            List list = dao.getProjects(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(project));
        } finally {
            remove(project);
        }
    }

    public void testShouldGetAllRegions() {
        int totalBeforeAdd = dao.getRegions(session).size();
        Region region = new Region("test" + Math.random());
        add(region);

        try {
            List list = dao.getRegions(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(region));
        } finally {
            remove(region);
        }
    }

    public void testShouldGetAllIntendedUses() {
        int totalBeforeAdd = dao.getIntendedUses(session).size();
        IntendedUse newElement = new IntendedUse("test" + Math.random());
        add(newElement);

        try {
            List list = dao.getIntendedUses(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(newElement));
        } finally {
            remove(newElement);
        }
    }

    public void testShouldGetStatuses() {
        clearStatuses();

        User emf = userDao.get("emf", session);
        Status status = newStatus(emf);

        try {
            List afterInsert = dao.getStatuses(emf.getUsername(), session);
            assertEquals(1, afterInsert.size());
        } finally {
            remove(status);
        }
    }

    private void clearStatuses() {
        Transaction tx = session.beginTransaction();
        List all = session.createCriteria(Status.class).list();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            Status element = (Status) iter.next();
            session.delete(element);
        }
        tx.commit();
    }

    public void testShouldMarkCurrentMessagesAsReadAndRemoveThemOnSubsequentFetchOfStatuses() {
        User emf = userDao.get("emf", session);
        newStatus(emf);

        List firstRead = dao.getStatuses(emf.getUsername(), session);
        assertEquals(1, firstRead.size());

        List secondRead = dao.getStatuses(emf.getUsername(), session);
        assertEquals(0, secondRead.size());
    }

    private Status newStatus(User emf) {
        Status status = unreadStatus(emf);

        add(status);

        return status(status.getMessage());
    }

    private Status unreadStatus(User emf) {
        Status status = new Status();
        status.setUsername(emf.getUsername());
        status.setType("type");
        status.setMessage("message");
        status.setTimestamp(new Date());
        return status;
    }

    public void testShouldClearPreviousReadMessagesAnMarkCurrentMessagesAsReadOnGetAll() {
        User emf = userDao.get("emf", session);
        newReadStatus(emf);

        List messages = dao.getStatuses(emf.getUsername(), session);
        assertEquals(0, messages.size());
    }

    public void testShouldPersistStatusOnAdd() {
        User emf = userDao.get("emf", session);
        Status status = unreadStatus(emf);

        dao.add(status, session);

        try {
            List messages = dao.getStatuses(emf.getUsername(), session);
            assertEquals(1, messages.size());
        } finally {
            remove(status);
        }
    }

    public void testShouldPersistRevisionOnAdd() throws EmfException {
        Revision rev = null;

        User user = userDao.get("emf", session);
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        datasetDAO.add(dataset, session);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());
        rev = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT ONE", "WHY ONE",
                "NOTE ONE");

        dao.add(rev, session);

        try {
            List allRevisions = dao.getRevisions(datasetFromDB.getId(), session);
            assertEquals(1, allRevisions.size());
        } finally {
            remove(rev);
            remove(dataset);
        }
    }

    public void testShouldAddANDGetThreeRevisions() throws EmfException {
        Revision rev1 = null, rev2 = null, rev3 = null;

        User user = userDao.get("emf", session);
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        datasetDAO.add(dataset, session);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());

        rev1 = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT ONE",
                "WHY ONE", "NOTE ONE");
        rev2 = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT TWO",
                "WHY TWO", "NOTE TWO");
        rev3 = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT THREE",
                "WHY THREE", "NOTE THREE");
        dao.add(rev1, session);
        dao.add(rev2, session);
        dao.add(rev3, session);
        try {
            List allRevisions = dao.getRevisions(datasetFromDB.getId(), session);
            assertEquals(3, allRevisions.size());
        } finally {
            remove(rev1);
            remove(rev2);
            remove(rev3);
            remove(dataset);
        }
    }

    public void testShouldPersistNoteOnAdd() throws EmfException {
        User user = userDao.get("emf", session);
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        datasetDAO.add(dataset, session);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());
        Note note = new Note(user, datasetFromDB.getId(), new Date(), "NOTE DETAILS", "NOTE NAME",
                loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());
        DatasetNote daNote=new DatasetNote(datasetFromDB.getId(), note);
        
        dao.add(daNote, session);

        try {
            List allNotes = dao.getDatasetNotes(datasetFromDB.getId(), session);
            assertEquals(1, allNotes.size());
        } finally {
            remove(note);
            remove(dataset);
        }
    }

    public void testShouldGetAllNotes() throws EmfException {
        User user = userDao.get("emf", session);
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        datasetDAO.add(dataset, session);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());
        Note note1 = new Note(user, datasetFromDB.getId(), new Date(), "NOTE DETAILS", "NOTE NAME",
                loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());
        Note note2 = new Note(user, datasetFromDB.getId(), new Date(), "NOTE DETAILS2", "NOTE NAME 2",
                loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());

        dao.add(new DatasetNote(datasetFromDB.getId(),note1), session);

        dao.add(new DatasetNote(datasetFromDB.getId(),note2), session);
        try {
            List allNotes = dao.getDatasetNotes(datasetFromDB.getId(), session);
            assertEquals(2, allNotes.size());
        } finally {
            remove(note1);
            remove(note2);
            remove(dataset);
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

    private EmfDataset newDataset() {
        Random rando = new Random();
        long id = Math.abs(rando.nextInt());
        EmfDataset dataset = new EmfDataset();

        dataset.setName("FOO_" + id);
        dataset.setAccessedDateTime(new Date());
        dataset.setCreatedDateTime(new Date());
        dataset.setDescription("DESCRIPTION");
        dataset.setModifiedDateTime(new Date());
        dataset.setStartDateTime(new Date());
        dataset.setStatus("imported");
        dataset.setYear(42);
        dataset.setUnits("orl");
        dataset.setTemporalResolution("t1");
        dataset.setStopDateTime(new Date());

        return dataset;
    }

    public void testShouldGetNoteTypes() {
        List allNoteTypes = dao.getNoteTypes(session);
        assertEquals("9 note types", 9,allNoteTypes.size());
    }

    private Status newReadStatus(User emf) {
        Status status = new Status();
        status.setUsername(emf.getUsername());
        status.setType("type");
        status.setMessage("message");
        status.setTimestamp(new Date());

        status.markRead();

        add(status);

        return status(status.getMessage());
    }

    private Status status(String message) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Status.class).add(Restrictions.eq("message", message));
            tx.commit();

            return (Status) crit.uniqueResult();
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

}
