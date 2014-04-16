package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.generic.GenericExporter;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.StringTokenizer;

public class TemporalReferenceExporter extends GenericExporter {

    public TemporalReferenceExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizedBatchSize) {
        this(dataset, rowFilters, dbServer, new NonVersionedDataFormatFactory(), optimizedBatchSize, null, null, null);
    }

    public TemporalReferenceExporter(Dataset dataset, String rowFilters, DbServer dbServer, 
            DataFormatFactory dataFormatFactory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        super(dataset, rowFilters, dbServer, new TemporalReferenceFileFormat(dbServer.getSqlDataTypes(), dataFormatFactory.defaultValuesFiller()),
                dataFormatFactory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
    }

    protected void writeHeaders(PrintWriter writer, Dataset dataset) throws SQLException {
        String header = dataset.getDescription();
        String lasttoken = null;
        String lastHeaderLine = null;

        if (header != null && !header.trim().isEmpty()) {
            StringTokenizer st = new StringTokenizer(header, "#");
            while (st.hasMoreTokens()) {
                lasttoken = st.nextToken();
                int index = lasttoken.indexOf("/POINT DEFN/");
                if (index < 0)
                    writer.print("#" + lasttoken);
                else {
                    if (index > 0)
                        writer.print("#" + lasttoken.substring(0, index));
                        
                    lastHeaderLine = lasttoken.substring(index);
                }
            }

            printExportInfo(writer);

            if (lastHeaderLine != null)
                writer.print(lastHeaderLine);
        }
    }

}
