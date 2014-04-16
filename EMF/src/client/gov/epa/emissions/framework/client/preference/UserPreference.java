package gov.epa.emissions.framework.client.preference;

public interface UserPreference {

    boolean checkFile(String fileName);

    String inputFolder();

    String outputFolder();

    String userName();
    
    String userPassword();
    
    String localTempDir();
    
    String remoteHost();
    
    String sortFilterPageSize();
}