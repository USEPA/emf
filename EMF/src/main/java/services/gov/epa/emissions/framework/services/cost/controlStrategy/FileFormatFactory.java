package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.orl.ORLMergedFileFormat;
import gov.epa.emissions.commons.io.orl.ORLNonPointFileFormat;
import gov.epa.emissions.commons.io.orl.ORLNonRoadFileFormat;
import gov.epa.emissions.commons.io.orl.ORLOnRoadFileFormat;
import gov.epa.emissions.commons.io.orl.ORLPointFileFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;

public class FileFormatFactory {

    private SqlDataTypes types;
    
    public FileFormatFactory(DbServer dbServer) throws Exception {
        this.types = dbServer.getSqlDataTypes();
    }

    public TableFormat tableFormat(DatasetType type) throws Exception {
        return tableFormat(type, false);
    }

    public TableFormat tableFormat(DatasetType type, boolean suppressException) throws Exception {
        if (type.getName().equals(DatasetType.orlNonpointInventory))
            return new VersionedTableFormat(new ORLNonPointFileFormat(types), types);

        if (type.getName().equals(DatasetType.orlNonroadInventory))
            return new VersionedTableFormat(new ORLNonRoadFileFormat(types), types);

        if (type.getName().equals(DatasetType.orlOnroadInventory))
            return new VersionedTableFormat(new ORLOnRoadFileFormat(types), types);

        if (type.getName().equals(DatasetType.orlPointInventory))
            return new VersionedTableFormat(new ORLPointFileFormat(types), types);

        if (type.getName().equals(DatasetType.orlMergedInventory))
            return new VersionedTableFormat(new ORLMergedFileFormat(types), types);
        
        if (type.getName().equals(DatasetType.FLAT_FILE_2010_POINT))
            return new VersionedTableFormat(type.getFileFormat(), types);

        if (type.getName().equals(DatasetType.FLAT_FILE_2010_NONPOINT))
            return new VersionedTableFormat(type.getFileFormat(), types);

        if (!suppressException)
            throw new Exception("The dataset type '" + type.getName() + "' is not supported for inventory output");
        
        return null;
    }
}
