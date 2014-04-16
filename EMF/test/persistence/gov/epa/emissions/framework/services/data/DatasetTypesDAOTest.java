package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.qa.QADAO;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

public class DatasetTypesDAOTest extends ServiceTestCase {

    private DatasetTypesDAO dao;

    private UserDAO userDao;

    protected void doSetUp() throws Exception {
        dao = new DatasetTypesDAO();
        userDao = new UserDAO();
    }

    protected void doTearDown() {// no op
    }

    public void testShouldGetAllDatasetTypes() {
        List types = dao.getAll(session);
        assertTrue(types.size() >= 10);
    }

    private DatasetType currentDatasetType(DatasetType target) {
        List list = dao.getAll(session);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            DatasetType element = (DatasetType) iter.next();
            if (element.equals(target))
                return element;
        }

        return null;
    }

    public void testShouldGetDatasetTypeLock() {
        User owner = userDao.get("emf", session);
        List types = dao.getAll(session);
        DatasetType type = (DatasetType) types.get(0);

        DatasetType locked = dao.obtainLocked(owner, type, session);
        assertEquals(locked.getLockOwner(), owner.getUsername());

        DatasetType loadedFromDb = currentDatasetType(type);
        assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
    }

    public void testShouldGetLockOnDatasetTypeContainingKeyVals() {
        User owner = userDao.get("emf", session);

        DatasetType newType = new DatasetType("name" + Math.random());
        newType.setDescription("MyDatasetType, newly added type.");
        Keyword keyword = new Keyword("test");
        KeyVal keyVal = new KeyVal(keyword, "val");
        newType.setKeyVals(new KeyVal[] { keyVal });

        try {
            dao.add(newType, session);

            DatasetType locked = dao.obtainLocked(owner, newType, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());

            DatasetType loadedFromDb = currentDatasetType(newType);
            assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
        } finally {
            remove(newType);
            remove(keyword);
        }
    }

    public void testShouldReleaseDatasetTypeLock() {
        User owner = userDao.get("emf", session);
        List list = dao.getAll(session);
        DatasetType type = (DatasetType) list.get(0);

        DatasetType locked = dao.obtainLocked(owner, type, session);
        DatasetType released = dao.releaseLocked(owner, locked, session);
        assertFalse("Should have released lock", released.isLocked());

        DatasetType loadedFromDb = currentDatasetType(type);
        assertFalse("Should have released lock", loadedFromDb.isLocked());
    }

    public void testShouldUpdateDatasetType() throws EmfException {
        List types = dao.getAll(session);
        DatasetType type = (DatasetType) types.get(0);
        String name = type.getName();

        User owner = userDao.get("emf", session);

        DatasetType modified1 = dao.obtainLocked(owner, type, session);
        assertEquals(modified1.getLockOwner(), owner.getUsername());
        modified1.setName("TEST");

        DatasetType modified2 = dao.update(modified1, session);
        assertEquals("TEST", modified1.getName());
        assertEquals(modified2.getLockOwner(), null);

        // restore
        DatasetType modified = dao.obtainLocked(owner, type, session);
        modified.setName(name);

        DatasetType modified3 = dao.update(modified, session);
        assertEquals(type.getName(), modified3.getName());
    }

    public void testShouldUpdateDatasetTypeByAddingNewQAStepTemplate() throws Exception {
        DatasetType type = new DatasetType();
        type.setName("test-type");
        type.setDescription("test-desc");
        super.add(type);

        try {
            User owner = userDao.get("emf", session);
            DatasetType modified = dao.obtainLocked(owner, type, session);

            QAStepTemplate template = new QAStepTemplate();
            template.setName("step1");
            modified.addQaStepTemplate(template);

            DatasetType updated = dao.update(modified, session);

            QAStepTemplate[] results = updated.getQaStepTemplates();
            assertEquals(1, results.length);
            assertEquals("step1", results[0].getName());
        } finally {
            super.remove(type);
        }
    }

    public void testShouldUpdateDatasetTypeByRemovingQAStepTemplate() throws Exception {
        DatasetType type = new DatasetType();
        type.setName("test-type");
        type.setDescription("test-desc");

        QAStepTemplate template = new QAStepTemplate();
        template.setName("step1");
        type.addQaStepTemplate(template);

        super.add(type);

        try {
            User owner = userDao.get("emf", session);
            DatasetType modified = dao.obtainLocked(owner, type, session);

            modified.setQaStepTemplates(new QAStepTemplate[0]);// clear templates
            DatasetType updated = dao.update(modified, session);

            QAStepTemplate[] results = updated.getQaStepTemplates();
            assertEquals(0, results.length);
        } finally {
            super.remove(type);
        }
    }

    public void testShouldUpdateQAStepTemplate() throws Exception {
        DatasetType type = new DatasetType();
        type.setName("test-type");
        type.setDescription("test-desc");

        QAStepTemplate template = new QAStepTemplate();
        template.setName("step1");
        type.addQaStepTemplate(template);
        QAProgram program = program();
        super.add(type);

        try {
            User owner = userDao.get("emf", session);
            DatasetType modified = dao.obtainLocked(owner, type, session);

            QAStepTemplate[] templates = modified.getQaStepTemplates();
            assertEquals(1, templates.length);
            QAStepTemplate updateTemplate = templates[0];
            updateTemplate.setName("updated-template");

            updateTemplate.setProgram(program);

            DatasetType updated = dao.update(modified, session);

            QAStepTemplate[] results = updated.getQaStepTemplates();
            assertEquals(1, results.length);
            assertEquals("updated-template", results[0].getName());
            assertEquals("SQL", results[0].getProgram().getName());
        } finally {
            super.remove(type);
        }
    }
    
    private QAProgram program() throws Exception {
        Session session = sessionFactory().getSession();
        try {
            return new QADAO().getQAPrograms(session)[0];
        } finally {
            session.close();
        }
    }

}
