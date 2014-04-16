package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.io.FileFormat;

public interface IDAFileFormat extends FileFormat{
    
    public void addPollutantCols(String[] pollutants);
}
