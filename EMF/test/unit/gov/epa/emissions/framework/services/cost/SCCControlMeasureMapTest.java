package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.cost.analysis.SCCControlMeasureMap;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import junit.framework.TestCase;

public class SCCControlMeasureMapTest extends TestCase {
    
    public void testShouldSelectNoControlsAs_Source_SCCDoesNotMatchAny_CMs(){
        Scc[] cmSccs = new Scc[]{new Scc("4343561", "")};
        ControlMeasure cm1 = controlMeasure("cm1", 0.0,0.0, cmSccs);
        
        String[] inventorySCCs = {"20211501","40150201"};
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,new ControlMeasure[]{cm1},"NOx",2000);
        assertNull(sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]));
        assertNull(sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]));
        
    }
    
    public void testShouldReturnNullWhenNoControlMeasureSpecified(){
        
        String[] inventorySCCs = {"20211501","40150201"};
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,new ControlMeasure[]{},"NOx",2000);
        assertNull(sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]));
        assertNull(sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]));
        
    }
    
    public void testShouldSelectTheOnlyControlMeasureAvailable(){
        Scc[] cmSccs = new Scc[]{new Scc("1020302",""),new Scc("20211501",""),new Scc("40150201","")};
        ControlMeasure cm1 = controlMeasure("cm1", 0.0,0.0, cmSccs);
        
        String[] inventorySCCs = {"20211501","40150201"};
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,new ControlMeasure[]{cm1},"NOx",2000);
        assertEquals("cm1",sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]).getName());
        assertEquals("cm1",sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]).getName());
        
    }
    
    
    public void testShouldSelectControlMeasureWithMostEfficiency(){
        Scc[] cmSccs1 = new Scc[]{new Scc("1020302",""),new Scc("20211501", ""),new Scc("40150201", "")};
        ControlMeasure cm1 = controlMeasure("cm1", 0.0,0.0, cmSccs1);
        
        Scc[] cmSccs2 = new Scc[]{new Scc("1020402", ""),new Scc("20211501",""),new Scc("40150201", "")};
        ControlMeasure cm2 = controlMeasure("cm2", 10.0,0.0, cmSccs2);
        
        Scc[] cmSccs3 = new Scc[]{new Scc("1020302", ""),new Scc("20211501", ""),new Scc("40150401", "")};
        ControlMeasure cm3 = controlMeasure("cm3", -10.0,0.0, cmSccs3);
        
        
        ControlMeasure[] controlMeasures = new ControlMeasure[]{cm1,cm2,cm3};
        String[] sccs = {"20211501","40150201"};
        
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(sccs,controlMeasures,"NOx",2000);
        assertEquals("cm2",sccCMMap.getMaxRedControlMeasure(sccs[0]).getName());
        assertEquals("cm2",sccCMMap.getMaxRedControlMeasure(sccs[1]).getName());
        
    }
    
    public void testShouldSelectLeastCostControlMeasureWhenMoreThanOneControlMeasureHasMaxEfficiency(){
        Scc[] cmSccs1 = new Scc[]{new Scc("1020302",""),new Scc("20211501", ""),new Scc("40150201", "")};
        ControlMeasure cm1 = controlMeasure("cm1", 0.0,0.0, cmSccs1);
        
        Scc[] cmSccs2 = new Scc[]{new Scc("1020402", ""),new Scc("20211501",""),new Scc("40150201", "")};
        ControlMeasure cm2 = controlMeasure("cm2", 10.0,0.0, cmSccs2);
        
        Scc[] cmSccs3 = new Scc[]{new Scc("1020302", ""),new Scc("20211501", ""),new Scc("40150401", "")};
        ControlMeasure cm3 = controlMeasure("cm3", -10.0,0.0, cmSccs3);
        
        Scc[] cmSccs4 = new Scc[]{new Scc("1067302", ""),new Scc("20211501",""),new Scc("40150401", "")};
        ControlMeasure cm4 = controlMeasure("cm4", 10.0,80.0, cmSccs4);
        
        
        String[] inventorySCCs = {"20211501","40150201"};
        ControlMeasure[] controlMeasures = new ControlMeasure[]{cm1,cm2,cm3,cm4};
        
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,controlMeasures,"NOx",2000);
        assertEquals("cm2",sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]).getName());
        assertEquals("cm2",sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]).getName());
        
    }
    
    
    public void testShouldSelect_One_Of_the_ControlMeasures_WithEqualEfficiencyAndCost(){
        Scc[] cmSccs1 = new Scc[]{new Scc("1020302",""),new Scc("20211501",""),new Scc("40150201","")};
        ControlMeasure cm1 = controlMeasure("cm1", 10.0,100.0, cmSccs1);
        
        Scc[] cmSccs2 = new Scc[]{new Scc("1020402",""),new Scc("20211501",""),new Scc("40150201", "")};
        ControlMeasure cm2 = controlMeasure("cm2", 10.0,100.0, cmSccs2);
        
        Scc[] cmSccs3 = new Scc[]{new Scc("1020702",""),new Scc("20211501",""),new Scc("40150401", "")};
        ControlMeasure cm3 = controlMeasure("cm3", 0.0,80.0, cmSccs3);
        
        Scc[] cmSccs4 = new Scc[]{new Scc("1067302",""),new Scc("20211501",""),new Scc("40150401","")};
        ControlMeasure cm4 = controlMeasure("cm4", 10.0,100.0, cmSccs4);
        
        
        String[] inventorySCCs = {"20211501","40150201"};
        ControlMeasure[] controlMeasures = new ControlMeasure[]{cm1,cm2,cm3,cm4};
        
        SCCControlMeasureMap sccCMMap = new SCCControlMeasureMap(inventorySCCs,controlMeasures,"NOx",2000);
        assertEquals("cm1",sccCMMap.getMaxRedControlMeasure(inventorySCCs[0]).getName());
        assertEquals("cm1",sccCMMap.getMaxRedControlMeasure(inventorySCCs[1]).getName());
        
    }

    private ControlMeasure controlMeasure(String name, double addEfficiency, double addCost, Scc[] cmSccs) {
        ControlMeasure cm = new ControlMeasure();
        cm.setName(name);
        
        EfficiencyRecord efficiencyRecord = new EfficiencyRecord();
        efficiencyRecord.setPollutant(new Pollutant("NOx"));
        efficiencyRecord.setEfficiency((double) (50+addEfficiency));
        cm.setEfficiencyRecords(new EfficiencyRecord[]{efficiencyRecord});
        
        cm.setSccs(cmSccs);
        return cm;
    }

}
