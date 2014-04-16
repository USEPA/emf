package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.analysis.common.SortBestMeasureEffRecordByApplyOrderAndLeastCost;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortBestMeasureEffRecordByApplyOrderAndLeastCostTest extends ServiceTestCase {

    private CostYearTable costYearTable;

    protected void doSetUp() throws Exception {
        costYearTable = new ControlMeasureServiceImpl(sessionFactory, dbServerFactory).getCostYearTable(2006);
    }

    protected void doTearDown() throws Exception {
        System.gc();
    }

    public void testShouldSortBestMeasureEffRecordsCorrectly() throws Exception {

        List<BestMeasureEffRecord> bestMeasureEffRecordList = new ArrayList<BestMeasureEffRecord>();

        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 1.0, 2006, 1000);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 1.1, 2006, 1100);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 1.0, 2006, 900);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 1.0, 2006, 1000);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 2.1, 2006, 2100);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 3.1, 2006, 1100);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 0.9, 2006, 0);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 4.1, 2006, 1100);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 0.1, 2006, 1);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 0.1, 2006, 0);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 0.1, 2006, 0);
        addBestMeasureEffRecordToList(bestMeasureEffRecordList, 0.1, 2006, .1);

        Collections.sort(bestMeasureEffRecordList, new SortBestMeasureEffRecordByApplyOrderAndLeastCost());
        for (int i=0; i < bestMeasureEffRecordList.size(); i++) {
            System.out.println("applyOrder: " + bestMeasureEffRecordList.get(i).measure().getApplyOrder() + ", costPerTon:" + bestMeasureEffRecordList.get(i).costPerTon());
        }
        assertEquals(bestMeasureEffRecordList.get(4).measure().getApplyOrder(), 0.9);
        assertEquals(bestMeasureEffRecordList.get(3).costPerTon(), 1.0);
        assertEquals(bestMeasureEffRecordList.get(9).measure().getApplyOrder(), 2.1);
        assertEquals(bestMeasureEffRecordList.get(10).costPerTon(), 1100.0);
    }

    private void addBestMeasureEffRecordToList(List<BestMeasureEffRecord> bestMeasureEffRecordList, double applyOrder, int costYear, double costPerTon) {
        ControlMeasure controlMeasure = new ControlMeasure();
        controlMeasure.setApplyOrder(applyOrder);
        EfficiencyRecord efficiencyRecord = new EfficiencyRecord();
        efficiencyRecord.setCostYear(costYear);
        efficiencyRecord.setCostPerTon(costPerTon);
        BestMeasureEffRecord bestMeasureEffRecord = new BestMeasureEffRecord(controlMeasure, efficiencyRecord, costYearTable);
        bestMeasureEffRecordList.add(bestMeasureEffRecord);
    }
}