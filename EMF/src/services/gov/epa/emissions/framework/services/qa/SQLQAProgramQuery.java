package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableMetaData;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class SQLQAProgramQuery {
    
    // Input is currently a set of 12 or 24 or 12*n monthly files
    // The lists are filled using command-line and/or GUI input.
    
    protected QAStep qaStep;

    protected String tableName;

    protected EntityManagerFactory entityManagerFactory;
    
    protected String emissionDatasourceName;
       
    protected boolean hasInvTableDataset;
    
    protected ArrayList<String> datasetNames = new ArrayList<String>();
    
    protected DatasetDAO dao = new DatasetDAO();
    

    public SQLQAProgramQuery(EntityManagerFactory entityManagerFactory, String emissioDatasourceName, String tableName, QAStep qaStep) {
        
        this.qaStep = qaStep;
        this.tableName = tableName;
        this.entityManagerFactory = entityManagerFactory;
        this.emissionDatasourceName = emissioDatasourceName;         
    }            

    protected void checkDataset() throws EmfException {
        String errors ="";
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            if ( datasetNames.size() > 0){
                for (String dsName : datasetNames){
                    //System.out.println(dsName);
                    if ( !dao.exists(dsName, entityManager))
                        errors += "The dataset name \"" + dsName + "\" does not exist. ";
                }
                if ( errors.length() > 0)
                    throw new EmfException(errors);
            }else
                throw new EmfException("Inventories are needed. ");
        } catch (Exception ex) {           
            throw new EmfException(ex.getMessage());
        } finally {
            entityManager.close();
        }
    }
    
    protected EmfDataset getDataset(String dsName) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getDataset(entityManager, dsName);
        } catch (Exception ex) {
            //ex.printStackTrace();
            throw new EmfException("The dataset named" + dsName + " does not exist");
        } finally {
            entityManager.close();
        }

    }
    
    protected EmfDataset getDataset(int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getDataset(entityManager, id);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("The dataset id " + id + " is not valid");
        } finally {
            entityManager.close();
        }
    }

    protected Version version(int datasetId, int version) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, entityManager);
        } finally {
            entityManager.close();
        }
    }
    
    protected String aliasExpression(String expression, Map<String,Column> tableColumns, String tableAlias) {
        Set<String> columnsKeySet = tableColumns.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        String aliasedExpression = expression;
        
        //find all column names in the expression and alias each one
        
        //first check the expressions contains columns from the base dataset
        while (iterator.hasNext()) {
            String columnName = iterator.next();

            Pattern pattern = getPattern(columnName);
            Matcher matcher = pattern.matcher(aliasedExpression);

            //check to see if anything is found...
            if (matcher.find()) {
                aliasedExpression = matcher.replaceAll(tableAlias + ".\"" + tableColumns.get(columnName).getName() + "\"");
            }
        }
        return aliasedExpression;
    }
    
    //Patterns are expensive, so lets use a map to cache them...
    private Map<String,Pattern> patternMap = new HashMap<String,Pattern> ();
    
    private Pattern getPattern(String columnName) {
        if (!patternMap.containsValue(columnName))
            patternMap.put(columnName, Pattern.compile("\\b(?i)" + columnName + "\\b"));
        return patternMap.get(columnName);
    }
    
    protected Map<String,Column> getDatasetColumnMap(Datasource datasource, EmfDataset dataset) throws SQLException {
        if (dataset.getInternalSources() == null )
            throw new SQLException("Dataset, " + dataset.getName() + ", does't have internalsources. " );
        Map<String,Column> datasetColumnMap = new TableMetaData(datasource).getColumnMap(dataset.getInternalSources()[0].getTable());
        Map<String,Column> lowerCaseDatasetColumnMap = new HashMap<String,Column>();
        
        //let's make sure and add ONLY user lower case versions so there is a consistent map regardless of case...
        Set<String> columnsKeySet = datasetColumnMap.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            lowerCaseDatasetColumnMap.put(columnName.toLowerCase(), datasetColumnMap.get(columnName));
        }
        
        return lowerCaseDatasetColumnMap;
    }

    protected String qualifiedEmissionTableName(Dataset dataset) {
        return qualifiedName(emissionTableName(dataset));
    }

    protected String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable();
    }

    private String qualifiedName(String table) {
        if ("versions".equalsIgnoreCase(table.toLowerCase()) && "emissions".equalsIgnoreCase(emissionDatasourceName.toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        return emissionDatasourceName + "." + table;
    }

}
