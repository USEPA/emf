package gov.epa.emissions.commons.io.nif.nonpointNonroad;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.nif.NIFImportHelper;

public class NIFNonRoadTableDatasetTypeUnits extends NIFNonRoadDatasetTypeUnits {

    private String[] tables;

    private Datasource datasource;

    public NIFNonRoadTableDatasetTypeUnits(String[] tables, DbServer dbServer, SqlDataTypes sqlDataTypes,
            DataFormatFactory factory) {
        super(sqlDataTypes, factory);
        this.tables = tables;
        this.datasource = dbServer.getEmissionsDatasource();
    }

    public void process() {
        try {
            associateTables(tables, datasource);
            requiredExist();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private void associateTables(String[] tables, Datasource datasource) throws Exception {
        NIFImportHelper helper = new NIFImportHelper();
        for (int i = 0; i < tables.length; i++) {
            // VERSIONS TABLE - Completed - throws exception if the following case is true
            if ("emissions".equalsIgnoreCase(datasource.getName()) && "versions".equalsIgnoreCase(tables[i].toLowerCase())) {
                throw new Exception("Table versions moved to schema emf."); // VERSIONS TABLE
            }
            String key = helper.notation(datasource, tables[i]);
            FormatUnit formatUnit = keyToDatasetTypeUnit(key);
            if (formatUnit != null) {
                formatUnit.setInternalSource(helper.internalSource(tables[i], tables[i], formatUnit));
            }
        }
    }
}
