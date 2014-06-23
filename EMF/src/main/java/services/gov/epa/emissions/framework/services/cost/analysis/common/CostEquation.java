package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;

public interface CostEquation {

    public Double getAnnualCost() throws EmfException;

    public Double getCapitalCost() throws EmfException;
    
    public Double getAnnualizedCapitalCost() throws EmfException;

    public Double getOperationMaintenanceCost() throws EmfException;
    
    public Double getComputedCPT() throws EmfException;

}
