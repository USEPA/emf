package gov.epa.emissions.commons.io;

public interface TableFormat extends FileFormat {

    String key();
    
    int getOffset();  // Column which base table begins
    
    int getBaseLength(); // base table column length

    Column[] cols(); // get columns meta data for the table
}