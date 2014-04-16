package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;

public class DatasetTypePersistenceTest extends HibernateTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        dropKeywords("NAME");
    }

    protected void doTearDown() throws Exception {
        dropKeywords("NAME");

        super.doTearDown();
    }

    public void testVerifySimplePropertiesAreStored() throws Exception {
        DatasetType type = new DatasetType();
        type.setDescription("TEST");
        type.setName("NAME");
        type.setMaxFiles(1);
        type.setMinFiles(1);
        type.setDefaultSortOrder("abc");
        save(type);
        DatasetType loadedType = load("NAME");
        assertNotNull("DatasetType with name - 'NAME' should have been persisted ", loadedType);
    }

    public void testVerifyKeywordsAreStored() throws Exception {
        DatasetType type = new DatasetType();
        type.setDescription("TEST");
        type.setName("NAME");
        type.setDefaultSortOrder("abc");
        type.addKeyVal(new KeyVal(new Keyword("key1"), "val1"));
        type.addKeyVal(new KeyVal(new Keyword("key2"), "val2"));

        save(type);
        DatasetType loadedType = load("NAME");
        assertNotNull(loadedType);
        assertEquals(2, loadedType.getKeyVals().length);
        assertEquals("key1", loadedType.getKeyVals()[0].getName());
        assertEquals("key2", loadedType.getKeyVals()[1].getName());
    }

    public void testVerifyUpdatedKeyValIsStored() throws Exception {
        DatasetType type = new DatasetType();
        type.setDescription("TEST");
        type.setName("NAME");
        type.setDefaultSortOrder("abc");
        type.addKeyVal(new KeyVal(new Keyword("key1"), "val1"));
        type.addKeyVal(new KeyVal(new Keyword("key2"), "val2"));

        save(type);

        DatasetType loadedType = load("NAME");
        assertEquals(2, loadedType.getKeyVals().length);
        KeyVal key1 = loadedType.getKeyVals()[0];
        assertEquals("key1", key1.getName());
        assertEquals("val1", key1.getValue());
        key1.setKeyword(new Keyword("updated-key1"));
        key1.setValue("updated-key1");

        update(type);

        DatasetType updatedType = load("NAME");
        KeyVal updatedKey = updatedType.getKeyVals()[0];
        assertEquals("updated-key1", updatedKey.getName());
    }

    private DatasetType load(String name) {
        Query query = session.createQuery("SELECT type FROM DatasetType AS type WHERE name='" + name + "'");
        List list = query.list();
        return list.size() == 1 ? (DatasetType) list.get(0) : null;
    }

    private void dropKeywords(String name) {
        try {
            PostgresDbUpdate update = new PostgresDbUpdate();
            update.deleteAll("emf.dataset_types_keywords");
            update.delete("emf.dataset_types", "name", name + "");
            update.deleteAll("emf.keywords");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void save(DatasetType type) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(type);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void update(DatasetType type) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(type);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
