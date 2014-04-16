package gov.epa.emissions.commons.io.speciation;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.generic.GenericExporter;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.StringTokenizer;

public class SpeciationCrossReferenceExporter extends GenericExporter {

    public SpeciationCrossReferenceExporter(Dataset dataset, String rowFilters, DbServer dbServer, 
            Integer optimizedBatchSize) {
        super(dataset, rowFilters, dbServer, new SpeciationCrossRefFileFormat(dbServer.getSqlDataTypes()), optimizedBatchSize);
    }

    public SpeciationCrossReferenceExporter(Dataset dataset, String rowFilters, DbServer dbServer, 
            DataFormatFactory factory, Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        super(dataset, rowFilters, dbServer, new SpeciationCrossRefFileFormat(dbServer.getSqlDataTypes()), factory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
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
                    String temp = lasttoken.substring(0, index);

                    if (temp != null && !temp.trim().isEmpty())
                        writer.print("#" + temp);
                    
                    lastHeaderLine = lasttoken.substring(index);
                }
            }

            writer.println();
            printExportInfo(writer);

            if (lastHeaderLine != null && lastHeaderLine.contains("/POINT DEFN/"))
                writer.println(lastHeaderLine);
        }
    }

}
