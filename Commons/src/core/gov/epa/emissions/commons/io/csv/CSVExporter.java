package gov.epa.emissions.commons.io.csv;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.other.SMKReportExporter;

import java.sql.Types;

public class CSVExporter extends SMKReportExporter {

    public CSVExporter(Dataset dataset, String rowFilter,  DbServer dbServer, Integer optimizedBatchSize) {
        super(dataset, rowFilter, dbServer, optimizedBatchSize);
        setup();
    }

    public CSVExporter(Dataset dataset, String rowFilter, DbServer dbServer, DataFormatFactory formatFactory,
            Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition) {
        super(dataset, rowFilter, dbServer, formatFactory, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
        setup();
    }

    private void setup() {
        super.setDelimiter(",");
        super.setInlineCommentChar(findInlineCommentDelimiter());
    }

    private String findInlineCommentDelimiter() {
        if (dataset.getDatasetType() != null && dataset.getDatasetType().getKeyVals() != null) {
            for (KeyVal keyVal : dataset.getDatasetType().getKeyVals()){
                if (keyVal.getName().equals(Dataset.inline_comment_char))
                    return (keyVal.getValue() == null || keyVal.getValue().trim().isEmpty() ? null : keyVal.getValue()); 
            }
        }
        return null; 
    }

    protected String formatValue(String[] cols, int colType, int index, String value) {
        if (colType == Types.BIGINT)
            return value;
        
        if (colType == Types.DECIMAL)
            return new Double(Double.valueOf(value)).toString();
        
        if (colType == Types.DOUBLE)
            return new Double(Double.valueOf(value)).toString();
        
        if (colType == Types.FLOAT)
            return new Double(Double.valueOf(value)).toString();
        
        if (colType == Types.INTEGER)
            return value;
        
        if (colType == Types.NUMERIC)
            return new Double(Double.valueOf(value)).toString();
        
        if (colType == Types.REAL)
            return new Double(Double.valueOf(value)).toString();
        
        if (colType == Types.SMALLINT)
            return value;
        
        return "\"" + value + "\"";
    }

}
