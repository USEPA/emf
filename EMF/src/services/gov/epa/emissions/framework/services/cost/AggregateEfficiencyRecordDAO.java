package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.data.SumEffRec;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.itextpdf.text.List;

public class AggregateEfficiencyRecordDAO {

    public AggregateEfficiencyRecordDAO() {
        //
    }

    public void updateAggregateEfficiencyRecords(int controlMeasureId, DbServer dbServer) throws EmfException {
        try {
             dbServer.getEmfDatasource().query().execute(updateQuery(controlMeasureId));
         } catch (SQLException e) {
             throw new EmfException(e.getMessage());
         }
     }

    public void updateAggregateEfficiencyRecords(ControlMeasure[] measures, DbServer dbServer) throws EmfException {
        try {
            for (int i = 0; i < measures.length; i++) {
                dbServer.getEmfDatasource().query().execute(updateQuery(measures[i].getId()));
            }
         } catch (SQLException e) {
             throw new EmfException(e.getMessage());
         }
     }

    public void removeAggregateEfficiencyRecords(int controlMeasureId, DbServer dbServer) throws EmfException {
        try {
            dbServer.getEmfDatasource().query().execute(removeQuery(controlMeasureId));
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void removeAggregateEfficiencyRecords(int[] sectorIds, DbServer dbServer) throws EmfException {
        try {
            dbServer.getEmfDatasource().query().execute(removeQuery(sectorIds));
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void removeAggregateEfficiencyRecords(ControlMeasure[] measures, DbServer dbServer) throws EmfException {
        try {
            for (int i = 0; i < measures.length; i++) {
                dbServer.getEmfDatasource().query().execute(removeQuery(measures[i].getId()));
            }
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

    public SumEffRec[] getAggregrateEfficiencyRecords(int controlMeasureId, DbServer dbServer) throws EmfException {
        java.util.List<SumEffRec> list = new ArrayList<SumEffRec>();
        String query = "select aer.pollutant_id, " +
        "p.name, " +
        "aer.max_efficiency, aer.min_efficiency, " +
        "aer.avg_efficiency, aer.max_cost_per_ton, " +
        "aer.min_cost_per_ton, aer.avg_cost_per_ton, " +
        "aer.avg_rule_effectiveness, aer.avg_rule_penetration " +
        "from emf.aggregrated_efficiencyrecords aer " +
        "left outer join emf.pollutants p " +
        "on p.id = aer.pollutant_id " + 
        "where aer.control_measures_id = " + controlMeasureId;
        
        try {
            ResultSet rs = dbServer.getEmfDatasource().query().executeQuery(query);
            while (rs.next()) {
                Float maxCE;
                Float minCE;
                Float avgCE;
                Float maxCPT;
                Float minCPT;
                Float avgCPT;
                Float avgRE;
                Float avgRP;
                
                maxCE = rs.getFloat(3);
                maxCE = (!rs.wasNull() ? maxCE : null);
                minCE = rs.getFloat(4);
                minCE = (!rs.wasNull() ? minCE : null);
                avgCE = rs.getFloat(5);
                avgCE = (!rs.wasNull() ? avgCE : null);
                maxCPT = rs.getFloat(6);
                maxCPT = (!rs.wasNull() ? maxCPT : null);
                minCPT = rs.getFloat(7);
                minCPT = (!rs.wasNull() ? minCPT : null);
                avgCPT = rs.getFloat(8);
                avgCPT = (!rs.wasNull() ? avgCPT : null);
                avgRE = rs.getFloat(9);
                avgRE = (!rs.wasNull() ? avgRE : null);
                avgRP = rs.getFloat(10);
                avgRP = (!rs.wasNull() ? avgRP : null);

                list.add(new SumEffRec(
                        new Pollutant(rs.getInt(1), rs.getString(2)), maxCE, 
                        minCE, avgCE,
                        maxCPT, minCPT,
                        avgCPT, avgRE,
                        avgRP));
            }
            
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
        return list.toArray(new SumEffRec[0]);
        
    }
     
    private String updateQuery(int controlMeasureId) {

        String query = removeQuery(controlMeasureId) + " " +
            "insert into emf.aggregrated_efficiencyrecords " +
            "select er.control_measures_id, er.pollutant_id, max(er.efficiency) as maxefficiency, min(er.efficiency) as minefficiency, " + 
            "avg(er.efficiency) as avgefficiency, max(er.cost_per_ton) as maxcpt, " +
            "min(er.cost_per_ton) as mincpt, avg(er.cost_per_ton) as avgcpt, " +
            "avg(er.rule_effectiveness) as avgruleff, avg(er.rule_penetration) as avgrulpen " + 
            "from emf.control_measure_efficiencyrecords er " +
            "left outer join emf.aggregrated_efficiencyrecords aer " +
            "on aer.control_measures_id = er.control_measures_id " +
            "and aer.pollutant_id = er.pollutant_id " +
            "where aer.control_measures_id is null " +
            "and er.control_measures_id = " + controlMeasureId + " " +
            "group by er.control_measures_id, er.pollutant_id;";

        return query;
    }

    private String removeQuery(int controlMeasureId) {

        String query = "delete from emf.aggregrated_efficiencyrecords " +
            "where control_measures_id = " + controlMeasureId + ";";

        return query;
    }

    private String removeQuery(int[] sectorIds) {

        String idList = "";
        for (int i = 0; i < sectorIds.length; ++i) {
            idList += (i > 0 ? ","  : "") + sectorIds[i];
        }
        
        String query = "delete from emf.aggregrated_efficiencyrecords where control_measures_id IN (select cm.id "
                + "FROM emf.control_measures AS cm "
                + (sectorIds != null && sectorIds.length > 0 
                        ? "where cm.id in (select control_measure_id from emf.control_measure_sectors "
                          + "WHERE sector_id in (" + idList + ") )" 
                        : "") + ")";

        return query;
    }
}
