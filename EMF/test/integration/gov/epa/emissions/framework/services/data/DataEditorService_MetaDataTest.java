package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class DataEditorService_MetaDataTest extends ServiceTestCase {

    private DataEditorServiceImpl service;

    private Datasource datasource;

    private EmfDataset dataset;

    private String table;

    private DataAccessToken token;

    private User user;

    protected void doSetUp() throws Exception {
        service = new DataEditorServiceImpl(emf(), super.dbServer(), sessionFactory(configFile()));
        UserService userService = new UserServiceImpl(sessionFactory(configFile()));

        datasource = emissions();
        dataset = new EmfDataset();
        table = "test" + new Date().getTime();
        
        String newName = table;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);
        
        setTestValues(dataset);

        doImport(dataset);

        Versions versions = new Versions();
        Version v1 = versions.derive(versionZero(), "v1", user, session);
        openSession(userService, v1);
    }

    private void openSession(UserService userService, Version v1) throws EmfException {
        token = token(v1);
        user = userService.getUser("emf");
        token = service.openSession(user, token, 5);
    }

    private void doImport(EmfDataset dataset) throws Exception {
        Version version = new Version();
        version.setVersion(0);

        File file = new File("test/data/orl/nc", "onroad-15records.txt");
        DataFormatFactory formatFactory = new VersionedDataFormatFactory(version, dataset);
        Importer importer = new ORLOnRoadImporter(file.getParentFile(), new String[] { file.getName() }, dataset,
                getDbServerInstance(), sqlDataTypes(), formatFactory);
        new VersionedImporter(importer, dataset, getDbServerInstance(), lastModifiedDate(file.getParentFile(),file.getName())).run();
    }

    private void setTestValues(EmfDataset dataset) {
        dataset.setId(Math.abs(new Random().nextInt()));
        dataset.setCreator("tester");
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(new Date());
        dataset.setAccessedDateTime(new Date());
    }

    protected void doTearDown() throws Exception {
        service.closeSession(user, token);
        dropTable(dataset.getName(), datasource);
        dropData("versions", datasource);
    }

    private DataAccessToken token(Version version) {
        return token(version, dataset.getName());
    }

    private DataAccessToken token(Version version, String table) {
        DataAccessToken result = new DataAccessToken(version, table);

        return result;
    }

    private Version versionZero() {
        Versions versions = new Versions();
        return versions.get(dataset.getId(), 0, session);
    }

    public void testShouldGetTheMetaDataForTable() throws Exception {
        TableMetadata tmd = service.getTableMetadata(table);
        assertEquals("Should have 17 columns", tmd.getCols().length, 17);
        ColumnMetaData[] cmd = tmd.getCols();
        assertEquals("column name should match", cmd[0].getName(), "record_id");
        assertEquals("column name should match", cmd[0].getType(), "java.lang.Integer");
        assertEquals("column name should match", cmd[0].getSize(), 11);

        assertEquals("column name should match", cmd[1].getName(), "dataset_id");
        assertEquals("column name should match", cmd[1].getType(), "java.lang.Long");
        assertEquals("column name should match", cmd[1].getSize(), 20);

        assertEquals("column name should match", cmd[2].getName(), "version");
        assertEquals("column name should match", cmd[2].getType(), "java.lang.Integer");
        assertEquals("column name should match", cmd[2].getSize(), 11);

        assertEquals("column name should match", cmd[3].getName(), "delete_versions");
        assertEquals("column name should match", cmd[3].getType(), "java.lang.String");
//        assertEquals("column name should match", cmd[3].getSize(), -1);
        assertEquals("column name should match", cmd[3].getSize(), 2147483647);

    }
    
    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder,fileName).lastModified());
    }

}
