package gov.epa.emissions.framework.services.casemanagement;

import java.util.List;

import gov.epa.emissions.framework.services.EmfException;

public interface EMFCaseFile {

    void readParameters(List<String> attributes, StringBuffer msg) throws EmfException;
    
    void readInputs(List<String> attributes, StringBuffer msg) throws EmfException;
    
    String getAttributeValue(String attribute) throws EmfException;
    
    String[] getInputValue(String envVar);
    
    String getParameterValue(String envVar);
    
    String getMessages();
}
