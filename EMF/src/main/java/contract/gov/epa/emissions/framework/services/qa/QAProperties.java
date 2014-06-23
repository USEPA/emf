package gov.epa.emissions.framework.services.qa;

public class QAProperties {

    public static String[] status() {
        return new String[] { initialStatus(), "Skipped", "In Progress", "Complete", "Failed", "Generated" };
    }

    public static String initialStatus() {
        return "Not Started";
    }
    
    public static String getStatus(String stub) {
        String[] statuses = status();
        String status = stub.toUpperCase();
        
        for ( int i = 0; i < statuses.length; i++) {
            String tempStatus = statuses[i];
            if (tempStatus.toUpperCase().startsWith(status))
                return statuses[i];
        }
        
        return initialStatus();
    }
}
