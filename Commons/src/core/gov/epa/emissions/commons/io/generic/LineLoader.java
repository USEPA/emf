package gov.epa.emissions.commons.io.generic;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.DataLoader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.Reader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LineLoader implements DataLoader {
    private Datasource datasource;

    public LineLoader(Datasource datasource, TableFormat tableFormat) {
        this.datasource = datasource;
    }

    // completed: if datasource is emissions and table is versions, it will trhow an exception
    public void load(Reader reader, Dataset dataset, String table) throws ImporterException {
        OptimizedTableModifier dataModifier = null;
        try {
            dataModifier = dataModifier(datasource, table);
            insertRecords(dataset, reader, dataModifier);
        } catch (Exception e) {
            e.printStackTrace();
            //            try {
            //                dropData(table, dataset, dataModifier);
            //            } catch ( Exception e1) {
            //                //
            //                throw new ImporterException("could not load dataset - '" + dataset.getName() + "' into table - " + table + ": " + e.getMessage() + "; " + e1.getMessage());
            //            }
            throw new ImporterException("could not load dataset - '" + dataset.getName() + "' into table - " + table, e);
        } finally {
            close(dataModifier);
        }
    }

    private OptimizedTableModifier dataModifier(Datasource datasource, String table) throws ImporterException {
        try {
            return new OptimizedTableModifier(datasource, table); 
// VERSIONS TABLE - completed
        } catch (SQLException e) {
            throw new ImporterException(e.getMessage());
        }
    }

    private void close(OptimizedTableModifier dataModifier) throws ImporterException {
        try {
            if (dataModifier != null)
                dataModifier.close();
        } catch (SQLException e) {
            throw new ImporterException(e.getMessage());
        }
    }

//    private void dropData(String table, Dataset dataset, OptimizedTableModifier dataModifier) throws ImporterException {
//        try {
//            String key = tableFormat.key();
//            long value = dataset.getId();
//            dataModifier.dropData(key, value);
//        } catch (SQLException e) {
//            throw new ImporterException("could not drop data from table " + table, e);
//        }
//    }

    private void insertRecords(Dataset dataset, Reader reader, OptimizedTableModifier dataModifier) throws Exception {
        dataModifier.start();
        try {
            for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
                String[] data = data(dataset, record);
                dataModifier.insert(data);
            }
            dataModifier.finish();
        } catch (ImporterException e) {
            dataModifier.finish();
            dataModifier.dropData("dataset_id", dataset.getId());
            throw new ImporterException(e.getMessage());
        } catch (Exception e) {
            dataModifier.finish();
            dataModifier.dropData("dataset_id", dataset.getId());
            throw new ImporterException(e.getMessage());
        } finally {
            //dataModifier.finish();
        }
    }

    private String[] data(Dataset dataset, Record record) {
        List data = new ArrayList();
        data.add("" + dataset.getId());
        data.addAll(record.tokens());

        return (String[]) data.toArray(new String[0]);
    }

}
