package gov.epa.emissions.framework.services.cost.data;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class ControlStrategyResultsSummary {

    private ControlStrategyResult[] strategyResults;
    
    private User user;
    
    public ControlStrategyResultsSummary(ControlStrategyResult[] strategyResults){
        this.strategyResults = strategyResults;
    }

    public float getStrategyTotalCost() {
        float totalCost = 0;
        
        for (int i = 0; i < strategyResults.length; i++)
            totalCost += strategyResults[i].getTotalCost() != null ? strategyResults[i].getTotalCost() : 0.0;
        
        return totalCost;
    }

    public float getStrategyTotalReduction() {
        float totalReduction = 0;
        
        for (int i = 0; i < strategyResults.length; i++)
            totalReduction += strategyResults[i].getTotalReduction() != null ? strategyResults[i].getTotalReduction() : 0.0;
        
        return totalReduction;
    }
    
    public String getStartTime() {
        int earliestStartIndex = 0;
        long earliestStartTime = strategyResults[earliestStartIndex].getStartTime().getTime();
        
        for (int i = 0; i < strategyResults.length; i++) {
            long laterTime = strategyResults[i].getStartTime().getTime();
            if (laterTime < earliestStartTime) {
                earliestStartTime = laterTime;
                earliestStartIndex = i;
            }
        }
            
        return CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(strategyResults[earliestStartIndex].getStartTime());
    }
    
    public String getCompletionTime() {
        int latestCompletionIndex = strategyResults.length - 1;
        long latestCompletionTime = strategyResults[latestCompletionIndex].getStartTime().getTime();
        
        for (int i = 0; i < strategyResults.length; i++) {
            long laterTime = strategyResults[i].getCompletionTime() != null 
                                ? strategyResults[i].getCompletionTime().getTime()
                                : latestCompletionTime;
            if (laterTime > latestCompletionTime) {
                latestCompletionTime = laterTime;
                latestCompletionIndex = i;
            }
        }
        
        return CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(strategyResults[latestCompletionIndex].getCompletionTime());
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public User getUser() {
        return this.user;
    }
    
    public String getRunStatus() {
        String runStatus = "";
        
        for (int i = 0; i < strategyResults.length; i++) {
            runStatus = strategyResults[i].getRunStatus();
//            if (status.indexOf("Failed") >= 0 || status.indexOf("failed") >= 0)
//                runStatus += status + System.getProperty("line.separator");
        }
        
        return runStatus;//.length() == 0 ? "Completed successfully." : "Failed at: " + runStatus;
    }
    
}
