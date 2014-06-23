package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

public class GenerateRegionMap {

//    private static Log log = LogFactory.getLog(GenerateSccControlMeasuresMap.class);

    private Datasource emissionDatasource;
    private RegionMap map;
    private HibernateSessionFactory sessionFactory;

    public GenerateRegionMap(DbServer dbServer, HibernateSessionFactory sessionFactory) {
        this.emissionDatasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.map = new RegionMap();
    }

    public RegionMap getRegionMap(EmfDataset regionDataset, int version) throws EmfException {
        int regiodDatasetId = regionDataset.getId();
        //see if this has already been added to the region map
        if (map.exists(regiodDatasetId + "_" + version)) 
                return map;
        String query = query(regionDataset, version);
        ResultSet rs = null;
        try {
            System.out.println("GenerateRegionMap.getRegionMap -- " + regionDataset.getName());
            rs = emissionDatasource.query().executeQuery(query);
            return buildMap(regiodDatasetId + "_" + version, rs);
        } catch (SQLException e) {
            throw new EmfException("Could not create a RegionMap: " + e.getMessage());
        } finally {
            if (rs != null)
                closeResultSet(rs);
        }
    }

    private void closeResultSet(ResultSet rs) throws EmfException {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new EmfException("Could not close result set after creating a RegionMap: \n"
                    + e.getMessage());
        }

    }

    private RegionMap buildMap(String datasetIdVersion, ResultSet rs) throws SQLException {
        List<String> fipsList = new ArrayList<String>();
        while (rs.next()) {
            fipsList.add(rs.getString(1));
        }
        map.add(datasetIdVersion, fipsList);
        return map;
    }

    private String query(EmfDataset regionDataset, int version) throws EmfException {
        String versionedQuery = new VersionedQuery(version(regionDataset, version)).query();
        return "SELECT DISTINCT fips "
            + " FROM " + qualifiedName(regionDataset.getInternalSources()[0].getTable(), emissionDatasource)
            + " where " + versionedQuery;
    }

    private String qualifiedName(String tableName, Datasource datasource) throws EmfException {
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(datasource.getName()) && "versions".equalsIgnoreCase(tableName.toLowerCase())) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return datasource.getName() + "." + tableName;
    }
    
    private Version version(EmfDataset regionDataset, int version) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(regionDataset.getId(), version, session);
        } finally {
            session.close();
        }
    }

}