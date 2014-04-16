package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.db.DataQuery;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CostYearTableReader {

    private Datasource datasource;

    private CostYearTable table;

    public CostYearTableReader(DbServer server, int targetYear) throws EmfException {
        datasource = server.getReferenceDatasource();
        table = new CostYearTable(targetYear);
        query();
    }

    private void query() throws EmfException {
        ResultSet rs = null;
        try {
            DataQuery query = datasource.query();
            String queryString = "SELECT * FROM " + qualifiedTableName() + " order by annual";
            rs = query.executeQuery(queryString);
            if (rs.next())
                table.addFirst(rs.getInt(1), rs.getDouble(3));
            while (rs.next()) {
                double gdp = rs.getDouble(3);
                int year = rs.getInt(1);
                table.add(year, gdp);
            }
        } catch (Exception e) {
            throw new EmfException("Could not get records from " + qualifiedTableName());
        } finally {
            if (rs != null)
                close(rs);
        }
    }

    private String qualifiedTableName() {
        return datasource.getName() + "." + "gdplev";
    }

    private void close(ResultSet rs) throws EmfException {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new EmfException("Could not close result set after reading " + qualifiedTableName());
        }
    }

    public CostYearTable costYearTable() {
        return table;
    }

}
