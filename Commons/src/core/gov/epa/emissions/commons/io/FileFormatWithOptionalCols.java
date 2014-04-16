package gov.epa.emissions.commons.io;

import java.util.List;


public interface FileFormatWithOptionalCols extends FileFormat {

    Column[] optionalCols();

    Column[] minCols();

    /**
     * adds 'fillers' for optional cols as well as cols specific to the Table
     * 
     * @param data
     *            contains data loaded (by the File Reader) from the corresponding line in the input file. i.e.
     *            represents a line in the input file
     * 
     */
    void fillDefaults(List<Column> data, long datasetId);

}
