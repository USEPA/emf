package gov.epa.emissions.framework.services.cost;

import java.text.SimpleDateFormat;
import java.util.Date;

import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.data.ControlStrategyResultsSummary;
import junit.framework.TestCase;

public class ControlStrategyResultsSummaryTest extends TestCase {
    
    SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public void testShouldGiveCorrectTotalValues() {
        ControlStrategyResult result1 = createStrategyResult(new Date(10000), "Created for input dataset: test");
        ControlStrategyResult result2 = createStrategyResult(new Date(11000), "Succeeded. Three");
        ControlStrategyResult result3 = createStrategyResult(new Date(10000), "Succeeded. Three");
        ControlStrategyResult result4 = createStrategyResult(new Date(12000), "Succeeded. Four");
        ControlStrategyResult result5 = createStrategyResult(new Date(500), "Succeeded. Five");
        
        ControlStrategyResult[] results = new ControlStrategyResult[] {
                result1, result2, result3, result4, result5
        };
        
        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(results);
        assertEquals(Float.valueOf(500), Float.valueOf(summary.getStrategyTotalCost()));
        assertEquals(Float.valueOf(5000), Float.valueOf(summary.getStrategyTotalReduction()));
        assertEquals("Completed successfully.", summary.getRunStatus());
        assertEquals(dateFormatter.format(new Date(500)).toString(), summary.getStartTime().toString());
        assertEquals(dateFormatter.format(new Date(12000)).toString(), summary.getCompletionTime().toString());
    }

    public void testShouldGiveFailedRunStatus() {
        ControlStrategyResult result1 = createStrategyResult(new Date(10000), "Created for input dataset: test");
        ControlStrategyResult result2 = createStrategyResult(new Date(11000), "Succeeded. Three");
        ControlStrategyResult result3 = createStrategyResult(new Date(10000), "Failed. Three");
        ControlStrategyResult result4 = createStrategyResult(new Date(10000), "Failed. Four");
        
        ControlStrategyResult[] results = new ControlStrategyResult[] {result1, result2, result3, result4};
        
        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(results);
        assertEquals("Failed at: Failed. Three" + System.getProperty("line.separator") +
                "Failed. Four" + System.getProperty("line.separator"), summary.getRunStatus());
    }
    
    private ControlStrategyResult createStrategyResult(Date date, String status) {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setStartTime(date);
        result.setCompletionTime(date);
        result.setTotalCost(100.0);
        result.setTotalReduction(1000.0);
        result.setRunStatus(status);
        
        return result;
    }
}
