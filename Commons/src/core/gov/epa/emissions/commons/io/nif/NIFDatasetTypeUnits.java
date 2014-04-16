package gov.epa.emissions.commons.io.nif;

import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.importer.ImporterException;


public interface NIFDatasetTypeUnits {

    public  void process() throws ImporterException;

    public FormatUnit[] formatUnits();
    
    public String dataTable();

}