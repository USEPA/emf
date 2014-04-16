package gov.epa.emissions.framework.services.cost;


import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.analysis.common.DefaultCostEquation;
import gov.epa.emissions.framework.services.cost.analysis.common.Type1CostEquation;
import gov.epa.emissions.framework.services.cost.analysis.common.Type2CostEquation;
import gov.epa.emissions.framework.services.cost.analysis.common.Type6CostEquation;
import gov.epa.emissions.framework.services.cost.analysis.common.Type8CostEquation;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTableReader;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EquationTypeTest extends ServiceTestCase {

    private double tolerance = 1e-2;
    
    private double discountRate = 7.0;
    
    private double reducedEmission = 1000;
    
    private double minStackFlowRate = 5;
    
//    private BestMeasureEffRecord bestMeasureEffRecord;
    
    public void testEquationType6() throws Exception {
        Type6CostEquation type6 = new Type6CostEquation(getCostYearTable(2000), discountRate);
        
        type6.setUp(reducedEmission, buildBestMeasureEffRecord(15, 0.2), minStackFlowRate);
        
        try {
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 6", 
                    reducedEmission, 1.0, 
                    minStackFlowRate, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null);
            EquationCosts costs = getEquationCosts(sql);
            
            double operatingCostResult = costs.operationMaintenanceCost;//type6.getOperationMaintenanceCost();
            double expectedOperatingCost = 797961.2;

            double annualCost = costs.annualCost;//type6.getAnnualCost();
            double expectdAnnualCost=1176805.61;
            
            double capitalCost = costs.capitalCost;//type6.getCapitalCost();
            double expectedCapitalCost=3450482.3;
            
            double annualizedCapitalCost=costs.annualizedCapitalCost;//type6.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost=378844.41;

            double computedCPT=costs.computedCostPerTon;//type6.getComputedCPT();
            double expectedComputedCPT=1176.81;
             
             System.out.println("begin type 6 test --------------------");
             assertTrue("Check Type 6 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) / expectedOperatingCost < tolerance);
             assertTrue("Check Type 6 annual cost", Math.abs(annualCost - expectdAnnualCost) / expectdAnnualCost < tolerance);
             assertTrue("Check Type 6 capital cost", Math.abs(capitalCost - expectedCapitalCost) / expectedCapitalCost < tolerance);
             assertTrue("Check Type 6 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) / expectedAnnualizedCCost < tolerance);
             assertTrue("Check Type 6 computed CPT", Math.abs(computedCPT - expectedComputedCPT) / expectedComputedCPT < tolerance);
             
             System.out.println("End of type 6 test ------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    public void testEquationType7Small() throws Exception {
        
        try {
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 7", 
                    reducedEmission, 1.0, 
                    minStackFlowRate, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null);
            EquationCosts costs = getEquationCosts(sql);
            
            double operatingCostResult = costs.operationMaintenanceCost;//type6.getOperationMaintenanceCost();
            double expectedOperatingCost = 749944.18;

            double annualCost = costs.annualCost;//type6.getAnnualCost();
            double expectdAnnualCost = 1148032.8;
            
            double capitalCost = costs.capitalCost;//type6.getCapitalCost();
            double expectedCapitalCost = 3625756.9;
            
            double annualizedCapitalCost = costs.annualizedCapitalCost;//type6.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost = 398088.6;

            double computedCPT = costs.computedCostPerTon;//type6.getComputedCPT();
            double expectedComputedCPT = 1148.03;
             
             System.out.println("begin type 6 test --------------------");
             assertTrue("Check Type 6 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) / expectedOperatingCost < tolerance);
             assertTrue("Check Type 6 annual cost", Math.abs(annualCost - expectdAnnualCost) / expectdAnnualCost < tolerance);
             assertTrue("Check Type 6 capital cost", Math.abs(capitalCost - expectedCapitalCost) / expectedCapitalCost < tolerance);
             assertTrue("Check Type 6 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) / expectedAnnualizedCCost < tolerance);
             assertTrue("Check Type 6 computed CPT", Math.abs(computedCPT - expectedComputedCPT) / expectedComputedCPT < tolerance);
             
             System.out.println("End of type 6 test ------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    public void testEquationType7Large() throws Exception {
        
        try {
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 7", 
                    10000.0, 1.0, 
                    1028900.0, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null);
            EquationCosts costs = getEquationCosts(sql);
            
            double operatingCostResult = costs.operationMaintenanceCost;//type6.getOperationMaintenanceCost();
            double expectedOperatingCost = 1.5622694222649E8;

            double annualCost = costs.annualCost;//type6.getAnnualCost();
            double expectdAnnualCost = 1.95069673851936E8;
            
            double capitalCost = costs.capitalCost;//type6.getCapitalCost();
            double expectedCapitalCost = 3.537762593681E8;
            
            double annualizedCapitalCost = costs.annualizedCapitalCost;//type6.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost = 3.88427316254465E7;

            double computedCPT = costs.computedCostPerTon;//type6.getComputedCPT();
            double expectedComputedCPT = 19506.9673851936;
             
             System.out.println("begin type 6 test --------------------");
             assertTrue("Check Type 6 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) / expectedOperatingCost < tolerance);
             assertTrue("Check Type 6 annual cost", Math.abs(annualCost - expectdAnnualCost) / expectdAnnualCost < tolerance);
             assertTrue("Check Type 6 capital cost", Math.abs(capitalCost - expectedCapitalCost) / expectedCapitalCost < tolerance);
             assertTrue("Check Type 6 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) / expectedAnnualizedCCost < tolerance);
             assertTrue("Check Type 6 computed CPT", Math.abs(computedCPT - expectedComputedCPT) / expectedComputedCPT < tolerance);
             
             System.out.println("End of type 6 test ------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    public void testEquationType5() throws Exception {
//        Type5CostEquation type5 = new Type5CostEquation(getCostYearTable(2000), discountRate);        
//        type5.setUp(reducedEmission, buildBestMeasureEffRecord(15, 0.2), minStackFlowRate);
        
        try {
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 5", 
                    reducedEmission, 1.0, 
                    minStackFlowRate, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null);
            EquationCosts costs = getEquationCosts(sql);
            double operatingCostResult = costs.operationMaintenanceCost;//type5.getOperationMaintenanceCost();
            double expectedOperatingCost = 749912;
            
            double annualCost = costs.annualCost;//type5.getAnnualCost();
            double expectdAnnualCost = 1066533.75;
            
            double capitalCost = costs.capitalCost;//type5.getCapitalCost();
            double expectedCapitalCost = 2883763.7;
            
            double annualizedCapitalCost = costs.annualizedCapitalCost;//type5.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost = 316621.75;
            
            double computedCPT = costs.computedCostPerTon;//type5.getComputedCPT();
            double expectedComputedCPT = 1066.53;
 //           System.out.println("begin type 5 test --------------------");
            assertTrue("Check Type 5 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) < tolerance);
            assertTrue("Check Type 5 annual cost", Math.abs(annualCost - expectdAnnualCost) < tolerance);
            assertTrue("Check Type 5 capital cost", Math.abs(capitalCost - expectedCapitalCost) < tolerance);
            assertTrue("Check Type 5 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) < tolerance);
            assertTrue("Check Type 5 computed CPT", Math.abs(computedCPT - expectedComputedCPT) < tolerance);
            
            System.out.println("end type 5 test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    public void testEquationType4() throws Exception {
//        Type4CostEquation type4 = new Type4CostEquation(getCostYearTable(2000), discountRate);
//        type4.setUp(reducedEmission, buildBestMeasureEffRecord(15, 0.2), minStackFlowRate);
        
        try {
           
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 4", 
                    reducedEmission, 1.0, 
                    minStackFlowRate, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null);
            EquationCosts costs = getEquationCosts(sql);
            double operatingCostResult = costs.operationMaintenanceCost;//type4.getOperationMaintenanceCost();
            double expectedOperatingCost = 75864.1;
            
            double annualCost = costs.annualCost;//type4.getAnnualCost();
            double expectdAnnualCost = 184566.18;
            
            double capitalCost = costs.capitalCost;//type4.getCapitalCost();
            double expectedCapitalCost = 990049.18;
            
            double annualizedCapitalCost = costs.annualizedCapitalCost;//type4.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost = 108702.08;
            
            double computedCPT = costs.computedCostPerTon;//type4.getComputedCPT();
            double expectedComputedCPT = 184.57;
           
            assertTrue("Check Type 4 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) < tolerance);
            assertTrue("Check Type 4 annual cost", Math.abs(annualCost - expectdAnnualCost) < tolerance);
            assertTrue("Check Type 4 capital cost", Math.abs(capitalCost - expectedCapitalCost) < tolerance);
            assertTrue("Check Type 4 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) < tolerance);
            assertTrue("Check Type 4 computed CPT", Math.abs(computedCPT - expectedComputedCPT) < tolerance);
            
            System.out.println("end type 4 test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    public void testEquationType3() throws Exception {
//        Type3CostEquation type3 = new Type3CostEquation(getCostYearTable(2000), discountRate);
//        type3.setUp(reducedEmission, buildBestMeasureEffRecord(15, 0.2), minStackFlowRate);
        
        try {
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 3", 
                    reducedEmission, 1.0, 
                    minStackFlowRate, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null);
//            String sql = "select * from public.get_type3_equation_costs(1010, " + discountRate / 100 + ", 15, 0.2, " + reducedEmission + ", 1.0, " + minStackFlowRate + ");";
            EquationCosts costs = getEquationCosts(sql);
            double operatingCostResult = costs.operationMaintenanceCost;//type3.getOperationMaintenanceCost();
//            operatingCostResult = type3.getOperationMaintenanceCost();
            double expectedOperatingCost = 314.5;
            
            double annualCost = costs.annualCost;//type3.getAnnualCost();
//            annualCost = type3.getAnnualCost();
            double expectdAnnualCost = 87149.56;
            
            double capitalCost = costs.capitalCost;//type3.getCapitalCost();
//            capitalCost = type3.getCapitalCost();
            double expectedCapitalCost = 790886.3;
            
            double annualizedCapitalCost=costs.annualizedCapitalCost;//type3.getAnnualizedCapitalCost();
//            annualizedCapitalCost = type3.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost = 86835.06;
            
            double computedCPT=costs.computedCostPerTon;//type3.getComputedCPT();
//            computedCPT = type3.getComputedCPT();
            double expectedComputedCPT = 87.15;

            assertTrue("Check Type 3 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) / expectedOperatingCost < tolerance);
            assertTrue("Check Type 3 annual cost", Math.abs(annualCost - expectdAnnualCost) / expectdAnnualCost < tolerance);
            assertTrue("Check Type 3 capital cost", Math.abs(capitalCost - expectedCapitalCost) / expectedCapitalCost < tolerance);
            assertTrue("Check Type 3 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) / expectedAnnualizedCCost < tolerance);
            assertTrue("Check Type 3 computed CPT", Math.abs(computedCPT - expectedComputedCPT) / expectedComputedCPT < tolerance);
            
            System.out.println("end type 3 test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //
        }
    }
    
    public void testEquationType3large() throws Exception {
//        Type3CostEquation type3 = new Type3CostEquation(getCostYearTable(2000), discountRate);
//        type3.setUp(reducedEmission, buildBestMeasureEffRecord(15, 0.2), 1028000.0);
        
        try {
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 3", 
                    reducedEmission, 1.0, 
                    1028000.0, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null, 
                    null, null);
            EquationCosts costs = getEquationCosts(sql);
             double operatingCostResult = costs.operationMaintenanceCost;//type3.getOperationMaintenanceCost();
             double expectedOperatingCost = 64660535.25;
             
             double annualCost = costs.annualCost;//type3.getAnnualCost();
             double expectdAnnualCost = 76245757.67;
             
             double capitalCost = costs.capitalCost;//type3.getCapitalCost();
             double expectedCapitalCost = 105517209.6;
             
             double annualizedCapitalCost = costs.annualizedCapitalCost;//type3.getAnnualizedCapitalCost();
             double expectedAnnualizedCCost = 11585222.43;
             
             double computedCPT = costs.computedCostPerTon;//type3.getComputedCPT();
             double expectedComputedCPT = 76245.76;
            
             assertTrue("Check Type 3 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) / expectedOperatingCost < tolerance);
             assertTrue("Check Type 3 annual cost", Math.abs(annualCost - expectdAnnualCost) / expectdAnnualCost < tolerance);
             assertTrue("Check Type 3 capital cost", Math.abs(capitalCost - expectedCapitalCost) / expectedCapitalCost < tolerance);
             assertTrue("Check Type 3 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) / expectedAnnualizedCCost < tolerance);
             assertTrue("Check Type 3 computed CPT", Math.abs(computedCPT - expectedComputedCPT) / expectedComputedCPT < tolerance);
             
             System.out.println("end type 3 large test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    

    public void testEquationDefault() throws Exception {
        DefaultCostEquation typeDefault = new DefaultCostEquation(discountRate);
        typeDefault.setUp(reducedEmission, buildBestMeasureEffRecord(15, 0.2));

        System.out.println("begin default ------");
    try {
        String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                6.0, 0.2, 
                200.0, null, 
                reducedEmission, 1.0, 
                null, null, 
                null, null, 
                null, null, 
                null, null, 
                null, null, 
                null, null, 
                null, null);
        EquationCosts costs = getEquationCosts(sql);
        double annualCost = costs.annualCost;//typeDefault.getAnnualCost();
        double expectdAnnualCost = 200000.0;

        double capitalCost = costs.capitalCost;//typeDefault.getCapitalCost();
        double expectedCapitalCost = 1200000.0;
        
        double operatingCostResult = costs.operationMaintenanceCost;//typeDefault.getOperationMaintenanceCost();
        double expectedOperatingCost = 68246.45;
 
        
        double annualizedCapitalCost = costs.annualizedCapitalCost;//typeDefault.getAnnualizedCapitalCost();
        double expectedAnnualizedCCost = 131753.55;
    
        double computedCPT = costs.computedCostPerTon;//typeDefault.getComputedCPT();
 //       System.out.println("Default test computed CPT "+  computedCPT);
        double expectedComputedCPT = 200.0;
    
        assertTrue("Check Type default operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) / expectedOperatingCost < tolerance);
        assertTrue("Check Type default annual cost", Math.abs(annualCost - expectdAnnualCost) / expectdAnnualCost < tolerance);
        assertTrue("Check Type default capital cost", Math.abs(capitalCost - expectedCapitalCost) / expectedCapitalCost < tolerance);
        assertTrue("Check Type default annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) / expectedAnnualizedCCost < tolerance);
        assertTrue("Check Type default computed CPT", Math.abs(computedCPT - expectedComputedCPT) / expectedComputedCPT < tolerance);
    
        System.out.println("End of default cost equation type -----------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
      
    
    public void testEquationType2() throws Exception {
        Type2CostEquation type2 = new Type2CostEquation(getCostYearTable(2000), discountRate);
        
        Double designCapacity = 150.0;
        
        ControlMeasureEquation equation = new ControlMeasureEquation();
        equation.setValue1(110487.6);
        equation.setValue2(0.423);
        equation.setValue3(3440.9);
        equation.setValue4(0.7337);

        type2.setUp(reducedEmission, buildBestMeasureEffRecord(15, 0.2, 
                equation
                ), designCapacity, "MMBtu", "hr");

        try {
            System.out.println("begin type 2 test --------------------");
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 2", 
                    reducedEmission, 1.0, 
                    minStackFlowRate, designCapacity, 
                    "MMBtu", "hr", 
                    110487.6, 0.423, 
                    3440.9, 0.7337, 
                    null, null, 
                    null, null, 
                    null, null);
            EquationCosts costs = getEquationCosts(sql);
            double operatingCostResult = costs.operationMaintenanceCost;
            operatingCostResult = type2.getOperationMaintenanceCost();
            double expectedOperatingCost = -4872.71677397179;
            
            double annualCost = costs.annualCost;
            annualCost = type2.getAnnualCost();
            double expectdAnnualCost = 55233.3631609195;
            
            double capitalCost = costs.capitalCost;
            capitalCost = type2.getCapitalCost();
            double expectedCapitalCost = 547441.007231206;
            
            double annualizedCapitalCost = costs.annualizedCapitalCost;
            annualizedCapitalCost = type2.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost = 60106.07993489129;
            
            double computedCPT = costs.computedCostPerTon;
            computedCPT = type2.getComputedCPT();
            double expectedComputedCPT = 55.2333631609195;
           
            assertTrue("Check Type 2 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) / expectedOperatingCost < tolerance);
            assertTrue("Check Type 2 annual cost", Math.abs(annualCost - expectdAnnualCost) / expectdAnnualCost < tolerance);
            assertTrue("Check Type 2 capital cost", Math.abs(capitalCost - expectedCapitalCost) / expectedCapitalCost < tolerance);
            assertTrue("Check Type 2 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) / expectedAnnualizedCCost < tolerance);
            assertTrue("Check Type 2 computed CPT", Math.abs(computedCPT - expectedComputedCPT) / expectedComputedCPT < tolerance);
            
            System.out.println("end type 2 test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
   
    public void testEquationType1() throws Exception {
        Type1CostEquation type1 = new Type1CostEquation(getCostYearTable(2000), discountRate);
        
        Double designCapacity = 150.0;
        
        ControlMeasureEquation equation = new ControlMeasureEquation();
        equation.setValue1(15.8);
        equation.setValue2(0.24);
        equation.setValue3(0.73);
        equation.setValue4(100.0);
        equation.setValue5(0.681);
        equation.setValue6(0.65);
        type1.setUp(reducedEmission, buildBestMeasureEffRecord(15, 0.2, 
                equation
                ), designCapacity);

        try {
            System.out.println("begin type 1 test --------------------");
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 1", 
                    reducedEmission, 1.0, 
                    null, designCapacity, 
                    "MW", null, 
                    15.8, 0.24, 
                    0.73, 100.0, 
                    0.681, 0.65, 
                    null, null, 
                    null, null);
            EquationCosts costs = getEquationCosts(sql);

            double operatingCostResult = costs.operationMaintenanceCost;//type1.getOperationMaintenanceCost();
            double expectedOperatingCost = 659493.0;
            
            double annualCost = costs.annualCost;//type1.getAnnualCost();
            double expectdAnnualCost = 6648149.79;
            
            double capitalCost = costs.capitalCost;//type1.getCapitalCost();
            double expectedCapitalCost = 54544171.07;
            
            double annualizedCapitalCost = costs.annualizedCapitalCost;//type1.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost = 5988656.79;
            
            double computedCPT = costs.computedCostPerTon;//type1.getComputedCPT();
            double expectedComputedCPT = 6648.15;
             
//             double scalingFactor =type1.getScallingFactor();
//             double expectedScalingFactor =23.01;
            
            assertTrue("Check Type 1 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) / expectedOperatingCost < tolerance);
            assertTrue("Check Type 1 annual cost", Math.abs(annualCost - expectdAnnualCost) / expectdAnnualCost < tolerance);
            assertTrue("Check Type 1 capital cost", Math.abs(capitalCost - expectedCapitalCost) / expectedCapitalCost < tolerance);
            assertTrue("Check Type 1 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) / expectedAnnualizedCCost < tolerance);
            assertTrue("Check Type 1 computed CPT", Math.abs(computedCPT - expectedComputedCPT) / expectedComputedCPT < tolerance);
//             assertTrue("Check Type 1 Scalling Factor", Math.abs(scalingFactor - expectedScalingFactor) < tolerance);

            System.out.println("end type 1 test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    public void testEquationType8() throws Exception {
        Type8CostEquation type8 = new Type8CostEquation(getCostYearTable(2000), discountRate);
        ControlMeasureEquation equation = new ControlMeasureEquation();
        equation.setValue1(13.0);
        equation.setValue2(11.0);
        equation.setValue3(380.0);
        equation.setValue4(28.0);
        equation.setValue5(117.0);
        type8.setUp(reducedEmission, buildBestMeasureEffRecord(15, 0.2, 
                equation
                ), 10.0);

        try {
            System.out.println("begin type 8 test --------------------");
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 8", 
                    reducedEmission, 1.0, 
                    10.0, null, 
                    null, null, 
                    13.0, 11.0, 
                    380.0, 28.0, 
                    117.0, null, 
                    null, null, 
                    null, null);
            EquationCosts costs = getEquationCosts(sql);
            Double operatingCostResult = costs.operationMaintenanceCost;//type8.getOperationMaintenanceCost();
            double expectedOperatingCost = 110.0;
            
            Double annualCost = costs.annualCost;//type8.getAnnualCost();
            double expectdAnnualCost = 129.47;
            
            Double capitalCost = costs.capitalCost;//type8.getCapitalCost();
            double expectedCapitalCost = 130.0;
            
            Double annualizedCapitalCost = costs.annualizedCapitalCost;//type8.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost = 14.27;
            
            Double computedCPT = costs.computedCostPerTon;//computedCPT = type8.getComputedCPT();
            double expectedComputedCPT = 0.1295;
           
            assertTrue("Check Type 8 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) / expectedOperatingCost < tolerance);
            assertTrue("Check Type 8 annual cost", Math.abs(annualCost - expectdAnnualCost) / expectdAnnualCost < tolerance);
            assertTrue("Check Type 8 capital cost", Math.abs(capitalCost - expectedCapitalCost) / expectedCapitalCost < tolerance);
            assertTrue("Check Type 8 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) / expectedAnnualizedCCost < tolerance);
            assertTrue("Check Type 8 computed CPT", Math.abs(computedCPT - expectedComputedCPT) / expectedComputedCPT < tolerance);
            
            System.out.println("end type 8 test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    public void testEquationType8Default() throws Exception {
        Type8CostEquation type8 = new Type8CostEquation(getCostYearTable(2000), discountRate);
        ControlMeasureEquation equation = new ControlMeasureEquation();
        equation.setValue1(13.0);
        equation.setValue2(11.0);
        equation.setValue3(380.0);
        equation.setValue4(28.0);
        equation.setValue5(117.0);
        type8.setUp(reducedEmission, buildBestMeasureEffRecord(15, 0.2, 
                equation
                ), 4.0);

        try {
            System.out.println("begin type 8 default test --------------------");
            String sql = buildCostEquationSQL(discountRate / 100, 15.0, 
                    6.0, 0.2, 
                    200.0, "Type 8", 
                    reducedEmission, 1.0, 
                    4.0, null, 
                    null, null, 
                    13.0, 11.0, 
                    380.0, 28.0, 
                    117.0, null, 
                    null, null, 
                    null, null);
            EquationCosts costs = getEquationCosts(sql);
            double operatingCostResult = costs.operationMaintenanceCost;//type8.getOperationMaintenanceCost();
            double expectedOperatingCost = 28000;
            
            double annualCost = costs.annualCost;//type8.getAnnualCost();
            double expectdAnnualCost = 117000;
            
            double capitalCost = costs.capitalCost;//type8.getCapitalCost();
            double expectedCapitalCost = 380000;
            
            double annualizedCapitalCost = costs.annualizedCapitalCost;//type8.getAnnualizedCapitalCost();
            double expectedAnnualizedCCost = 41721.96;
            
            double computedCPT = costs.computedCostPerTon;//type8.getComputedCPT();
            double expectedComputedCPT = 117.0;
           
            assertTrue("Check Type 8 operating and maintenance cost", Math.abs(operatingCostResult - expectedOperatingCost) / expectedOperatingCost < tolerance);
            assertTrue("Check Type 8 annual cost", Math.abs(annualCost - expectdAnnualCost) / expectdAnnualCost < tolerance);
            assertTrue("Check Type 8 capital cost", Math.abs(capitalCost - expectedCapitalCost) / expectedCapitalCost < tolerance);
            assertTrue("Check Type 8 annualized cost", Math.abs(annualizedCapitalCost - expectedAnnualizedCCost) / expectedAnnualizedCCost < tolerance);
            assertTrue("Check Type 8 computed CPT", Math.abs(computedCPT - expectedComputedCPT) / expectedComputedCPT < tolerance);
            
            System.out.println("end type 8 default test --------------------");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
   
    
    private BestMeasureEffRecord buildBestMeasureEffRecord(float equipmentLife, Double capRecFactor) throws EmfException {
        
        
        ControlMeasure measure=new ControlMeasure();
        measure.setEquipmentLife(equipmentLife);
        measure.setCostYear(2000);
        EfficiencyRecord efficiencyRecord=new EfficiencyRecord();
        efficiencyRecord.setCapRecFactor(capRecFactor);
        efficiencyRecord.setCapitalAnnualizedRatio(6.0);
        efficiencyRecord.setCostYear(2000);
        efficiencyRecord.setCostPerTon(200.0);
        CostYearTable table=getCostYearTable(2000);
        
        BestMeasureEffRecord record= new BestMeasureEffRecord(measure, efficiencyRecord, table);
 //       System.out.println("AdjustedCostPerTon : "+ record.adjustedCostPerTon());
        return record;
            
    }

    private BestMeasureEffRecord buildBestMeasureEffRecord(float equipmentLife, Double capRecFactor, ControlMeasureEquation equation) throws EmfException {
        BestMeasureEffRecord record = buildBestMeasureEffRecord(equipmentLife, capRecFactor);
        record.measure().setEquations(new ControlMeasureEquation[] {equation});
        return record;
            
    }

    private CostYearTable getCostYearTable(int targetYear) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            CostYearTableReader reader = new CostYearTableReader(dbServer, targetYear);
            return reader.costYearTable();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            try {
                dbServer.disconnect();
            } catch (SQLException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

//  Index 0 = annual_cost 
//  Index 1 = capital_cost 
//  Index 2 = operation_maintenance_cost 
//  Index 3 = annualized_capital_cost 
//  Index 4 = computed_cost_per_ton
//  
   private EquationCosts getEquationCosts(String sql) throws Exception {
        DbServer dbServer = null;
        EquationCosts costs = new EquationCosts();
        try {
            dbServer = dbServerFactory.getDbServer();
            ResultSet rs = dbServer.getEmfDatasource().query().executeQuery(sql);
            rs.next();
            costs.annualCost = rs.getDouble(1);
            costs.capitalCost = rs.getDouble(2);
            costs.operationMaintenanceCost = rs.getDouble(3);
            costs.annualizedCapitalCost = rs.getDouble(4);
            costs.computedCostPerTon = rs.getDouble(5);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dbServer != null) dbServer.disconnect();
        }
        return costs;
    }

   private String buildCostEquationSQL(double discountRate, Double equipmentLife,
           Double capAnnRatio, Double capRecFactor,
           Double refYrCostPerTon, String equationType,
           Double emisReduction, Double refYrChainedGDPAdjustmentFactor, 
           Double stackFlowRate, Double designCapacity,
           String designCapacityUnitNumerator, String designCapacityUnitDenominator,
           Double variableCoefficient1, Double variableCoefficient2,
           Double variableCoefficient3, Double variableCoefficient4,
           Double variableCoefficient5, Double variableCoefficient6,
           Double variableCoefficient7, Double variableCoefficient8,
           Double variableCoefficient9, Double variableCoefficient10
           ) {
       String sql = "select * from public.get_strategy_costs(" +
           "true::boolean," +
           "10001," +
           "'ABCDEFGH'," +
           discountRate + "::double precision," +
           (equipmentLife != null ? equipmentLife + "": "null") + "::double precision," +
           (capAnnRatio != null ? capAnnRatio + "": "null") + "::double precision," +
           (capRecFactor != null ? capRecFactor + "": "null") + "::double precision," +
           (refYrCostPerTon != null ? refYrCostPerTon + "": "null") + "::double precision," +
           (emisReduction != null ? emisReduction + "": "null") + "::double precision," +
           (refYrChainedGDPAdjustmentFactor != null ? refYrChainedGDPAdjustmentFactor + "": "null") + "::double precision," +
           (equationType != null ? "'" + equationType + "'": "null") + "::character varying," +
           (variableCoefficient1 != null ? variableCoefficient1 + "": "null") + "::double precision," +
           (variableCoefficient2 != null ? variableCoefficient2 + "": "null") + "::double precision," +
           (variableCoefficient3 != null ? variableCoefficient3 + "": "null") + "::double precision," +
           (variableCoefficient4 != null ? variableCoefficient4 + "": "null") + "::double precision," +
           (variableCoefficient5 != null ? variableCoefficient5 + "": "null") + "::double precision," +
           (variableCoefficient6 != null ? variableCoefficient6 + "": "null") + "::double precision," +
           (variableCoefficient7 != null ? variableCoefficient7 + "": "null") + "::double precision," +
           (variableCoefficient8 != null ? variableCoefficient8 + "": "null") + "::double precision," +
           (variableCoefficient9 != null ? variableCoefficient9 + "": "null") + "::double precision," +
           (variableCoefficient10 != null ? variableCoefficient10 + "": "null") + "::double precision," +
           (stackFlowRate != null ? stackFlowRate + "": "null") + "::double precision," +
           (designCapacity != null ? designCapacity + "": "null") + "::double precision," +
           (designCapacityUnitNumerator != null ? "'" + designCapacityUnitNumerator + "'": "null") + "::character varying," +
           (designCapacityUnitDenominator != null ? "'" + designCapacityUnitDenominator + "'": "null") + "::character varying)";
//       public.get_strategy_costs(
//            use_cost_equations boolean, 
//            control_measure_id integer, 
//            measure_abbreviation character varying(10), 
//            discount_rate double precision, 
//            equipment_life double precision, 
//            capital_annualized_ratio double precision, 
//            capital_recovery_factor double precision, 
//            ref_yr_cost_per_ton double precision,  
//            emis_reduction double precision, 
//            ref_yr_chained_gdp_adjustment_factor double precision,
//            equation_type character varying(255), 
//            variable_coefficient1 double precision, 
//            variable_coefficient2 double precision, 
//            variable_coefficient3 double precision, 
//            variable_coefficient4 double precision, 
//            variable_coefficient5 double precision, 
//            variable_coefficient6 double precision, 
//            variable_coefficient7 double precision, 
//            variable_coefficient8 double precision, 
//            variable_coefficient9 double precision, 
//            variable_coefficient10 double precision, 
//            stack_flow_rate double precision, 
//            design_capacity double precision, 
//            design_capacity_unit_numerator character varying, 
//            design_capacity_unit_denominator
       return sql;
   }

   public class EquationCosts {
        public Double annualCost;
        public Double capitalCost;
        public Double operationMaintenanceCost;
        public Double annualizedCapitalCost;
        public Double computedCostPerTon;
    }

    @Override
    protected void doSetUp() throws Exception {
        // NOTE Auto-generated method stub
        
    }

    @Override
    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub
        
    }

}

