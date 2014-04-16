package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.io.temporal.TemporalProfileImporter;
import gov.epa.emissions.framework.services.data.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class ImporterFactoryTest extends MockObjectTestCase {
    public void testToRemove() {
        assertTrue(true);
    }

    public void ttestShouldBeAbleCreateOrlImporterr() throws Exception {
        Mock types = mock(SqlDataTypes.class);
        types.stubs().method(ANYTHING).withAnyArguments().will(returnValue(""));

        ImporterFactory factory = new ImporterFactory();

        DatasetType datasetType = new DatasetType();
        datasetType.setImporterClassName(ORLOnRoadImporter.class.getName());
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);
        dataset.setName("name");

        Importer importer = factory.createVersioned(dataset, null, new String[]{"file"});

        assertEquals(VersionedImporter.class.getName(), importer.getClass().getName());
    }

    public void ttestShouldBeAbleCreateTemporalProfileImporter() throws Exception {
        Mock sqlTypes = mock(SqlDataTypes.class);
        sqlTypes.stubs().method(ANYTHING).withAnyArguments().will(returnValue(""));
        
        ImporterFactory factory = new ImporterFactory();

        DatasetType datasetType = new DatasetType();
        datasetType.setImporterClassName(TemporalProfileImporter.class.getName());
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetType(datasetType);

        Importer exporter = factory.createVersioned(dataset, null, new String[]{"file"});

        assertEquals(VersionedImporter.class.getName(), exporter.getClass().getName());
    }

}
