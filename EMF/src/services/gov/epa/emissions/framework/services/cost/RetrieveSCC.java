package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RetrieveSCC {

    private int controlMeasureId;

    private DbServer dbServer;

    public RetrieveSCC(int measureId, DbServer dbServer) throws Exception {
        this.controlMeasureId = measureId;
        this.dbServer = dbServer;
    }

    public Scc[] sccs() throws SQLException {
        Scc[] sccs;
        try {
            ResultSet set = dbServer.getReferenceDatasource().query().executeQuery(query(controlMeasureId));
            sccs = values(set);
        } catch (SQLException e) {
            throw e;
        }

        return sccs;
    }

    public String[] cmAbbrevAndSccs() throws SQLException {
        String[] cmAbbrevAndSccs;
        try {
            ResultSet set = dbServer.getReferenceDatasource().query().executeQuery(
                    cmAbbrevAndSccQuery(controlMeasureId));
            cmAbbrevAndSccs = getAbbrevAndSccStrings(set);
        } catch (SQLException e) {
            throw e;
        }

        return cmAbbrevAndSccs;
    }

    private String[] getAbbrevAndSccStrings(ResultSet rs) throws SQLException {
        List abbrevSccs = new ArrayList();
        try {
            while (rs.next()) {
                abbrevSccs.add("\"" + rs.getString(1)+"\"" + "," + rs.getString(2));
            }
        } finally {
            rs.close();
        }
        return (String[]) abbrevSccs.toArray(new String[0]);
    }

    private Scc[] values(ResultSet rs) throws SQLException {
        List sccs = new ArrayList();
        try {
            while (rs.next()) {
                String desc = rs.getString(2);
                if (desc == null)
                    desc = "The SCC entry is not found in the reference.scc table";
                Scc scc = new Scc(rs.getString(1), desc);
                scc.setCombustionEfficiency(rs.getFloat(3));
                sccs.add(scc);
            }
        } finally {
            rs.close();
        }
        return (Scc[]) sccs.toArray(new Scc[0]);
    }

    private String query(int id) {
        String query = "SELECT e.name,r.scc_description,COALESCE(e.combustion_efficiency,100) FROM emf.control_measure_sccs AS e LEFT OUTER JOIN reference.scc AS r "
                + "ON (e.name=r.scc) WHERE e.control_measures_id=" + id;
        return query;
    }

    private String cmAbbrevAndSccQuery(int id) {
        String query = "SELECT cm.abbreviation, e.name FROM emf.control_measures AS cm, emf.control_measure_sccs AS e "
                + "WHERE cm.id=" + id + " AND e.control_measures_id=" + id;
        return query;
    }

}
