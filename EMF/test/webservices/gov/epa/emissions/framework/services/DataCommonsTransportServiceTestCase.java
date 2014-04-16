package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DataCommonsTransportServiceTestCase extends ServiceTestCase {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private DataCommonsService dcs = null;

    private DataCommonsService service;

    private UserService userService;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        service = new DataCommonsServiceImpl(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);

        RemoteServiceLocator rl = new RemoteServiceLocator(DEFAULT_URL);
        dcs = rl.dataCommonsService();
    }

    public void testShouldGetAllNoteTypes() throws EmfException {
        NoteType[] all = dcs.getNoteTypes();
        assertEquals("5 types", all.length, 5);
    }

    public void testShouldAddNote() throws EmfException {
        long id = Math.abs(new Random().nextInt());
        User user = userService.getUser("emf");
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        add(dataset);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());
        Note note = new Note(user, datasetFromDB.getId(), new Date(), "NOTE DETAILS", "NOTE NAME" + id,
                loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());
        dcs.addDatasetNote(new DatasetNote(datasetFromDB.getId(),note));

        try {
            DatasetNote[] notes = service.getDatasetNotes(datasetFromDB.getId());
            assertEquals(notes.length, 1);
        } finally {
            remove(note);
            remove(dataset);
        }
    }

    public void xtestShouldGetAllNotes() throws EmfException {
        long id = Math.abs(new Random().nextInt());
        User user = userService.getUser("emf");
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        add(dataset);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());

        Note note1 = new Note(user, datasetFromDB.getId(), new Date(), "NOTE DETAILS", "NOTE NAME1" + id,
                loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());
        service.addDatasetNote(new DatasetNote(datasetFromDB.getId(),note1));
        Note note2 = new Note(user, datasetFromDB.getId(), new Date(), "NOTE DETAILS", "NOTE NAME2" + id,
                loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());
        service.addDatasetNote(new DatasetNote(datasetFromDB.getId(),note2));

        try {
            DatasetNote[] notes = service.getDatasetNotes(datasetFromDB.getId());
            assertEquals("Two notes should return", notes.length, 2);
        } finally {
            remove(note1);
            remove(note2);
            remove(dataset);
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

    protected void doTearDown() throws Exception {// no op
    }

}
