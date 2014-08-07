package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class DatasetCreator {

    // private String tablePrefix;

    private User user;

    // private String outputDatasetName;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    // private String datasetNamePrefix;

    private TemporalAllocation temporalAllocation;

    private Keywords keywordMasterList;

    private Datasource datasource;

    public DatasetCreator() {
        //
    }

    public DatasetCreator(TemporalAllocation temporalAllocation, User user, HibernateSessionFactory sessionFactory,
            DbServerFactory dbServerFactory, Datasource datasource, Keywords keywordMasterList) {
        // this.datasetNamePrefix = datasetNamePrefix;
        // this.tablePrefix = tablePrefix;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        // this.outputDatasetName = getResultDatasetName(strategy.getName());
        this.temporalAllocation = temporalAllocation;
        this.datasource = datasource;
        this.keywordMasterList = keywordMasterList;// new Keywords(new
                                                   // DataCommonsServiceImpl(sessionFactory).getKeywords());
    }

    public EmfDataset addDataset(String tablePrefix, String datasetName, DatasetType type, TableFormat tableFormat,
            String description) throws EmfException {
        String outputTableName = createTableName(tablePrefix, datasetName);

        // create dataset
        EmfDataset dataset = createDataset(datasetName, description, type);

        setDatasetInternalSource(dataset, outputTableName, tableFormat, datasetName);

        // persist dataset to db
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        createTable(outputTableName, tableFormat);

        return dataset;
    }

    private EmfDataset createDataset(String name, String description, DatasetType type) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        Date start = new Date();

        String newName = name;
        if (newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);

        dataset.setCreator(user.getUsername());
        dataset.setCreatorFullName(user.getName());
        dataset.setDatasetType(type);
        dataset.setDescription(description);
        dataset.setCreatedDateTime(start);
        dataset.setModifiedDateTime(start);
        dataset.setAccessedDateTime(start);
        dataset.setStatus("Created by temporal allocation");

        // Add keywords to the dataset
        addKeyVals(dataset);

        return dataset;
    }

    protected void addKeyVals(EmfDataset dataset) {
        if (temporalAllocation == null)
            return;
        // Add keywords to the dataset
        addKeyVal(dataset, "TEMPORAL_ALLOCATION_NAME", temporalAllocation.getName());
        addKeyVal(dataset, "TEMPORAL_ALLOCATION_ID", temporalAllocation.getId() + "");
        addKeyVal(dataset, "RESOLUTION_TYPE", temporalAllocation.getResolution().getName());
    }

    public void addKeyVal(EmfDataset dataset, String keywordName, String value) {
        Keyword keyword = keywordMasterList.get(keywordName);
        KeyVal keyval = new KeyVal(keyword, value);
        dataset.addKeyVal(keyval);
    }

    private void addVersionZeroEntryToVersionsTable(Dataset dataset) throws Exception {
        gov.epa.emissions.framework.utils.Utils.addVersionEntryToVersionsTable(this.sessionFactory, this.user,
                dataset.getId(), 0, "Initial Version", "", true, "");
    }

    private void setDatasetInternalSource(EmfDataset dataset, String tableName, TableFormat tableFormat, String source) {
        InternalSource internalSource = new InternalSource();
        internalSource.setTable(tableName);
        internalSource.setType(tableFormat.identify());
        internalSource.setCols(colNames(tableFormat.cols()));
        internalSource.setSource(source);
        dataset.setInternalSources(new InternalSource[] { internalSource });
    }

    private String[] colNames(Column[] cols) {
        List names = new ArrayList();
        for (int i = 0; i < cols.length; i++)
            names.add(cols[i].name());

        return (String[]) names.toArray(new String[0]);
    }

    public static String createDatasetName(String name) {
        // name += "_" + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
        // if (name.length() > 46) { //postgresql table name max length is 64
        // name = name.substring(0, 45);
        // }//16+1
        if (name.length() > 54) { // postgresql table name max length is 64
            name = name.substring(0, 53);
        }// 8+1

        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i))) {
                name = name.replace(name.charAt(i), '_');
            }
        }
        // format_HHMMSSSS
        // format_YYYYMMDDHHMMSSSS
        return name.trim().replaceAll(" ", "_") + "_" + CustomDateFormat.format_HHMMSSSS(new Date());
    }

    private String createTableName(String tablePrefix, String name) {
        return createTableName(tablePrefix + "_" + name);
    }

    private String createTableName(String name) {
        String table = name;
        // truncate if necessary so a unique timestamp can be added to ensure uniqueness
        if (table.length() > 46) { // postgresql table name max length is 64
            table = table.substring(0, 45);
        }

        for (int i = 0; i < table.length(); i++) {
            if (!Character.isLetterOrDigit(table.charAt(i))) {
                table = table.replace(table.charAt(i), '_');
            }
        }

        // add unique timestamp to ensure uniqueness
        return table.trim().replaceAll(" ", "_") + "_" + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
    }

    private void add(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO(dbServerFactory);
            if (dao.datasetNameUsed(dataset.getName(), session))
                throw new EmfException("The selected dataset name is already in use.");

            dao.add(dataset, session);
        } catch (Exception e) {
            throw new EmfException("Could not add dataset: " + dataset.getName());
        } finally {
            session.close();
        }
    }

    private void createTable(String tableName, TableFormat tableFormat) throws EmfException {
        TableCreator creator = new TableCreator(datasource);
        try {
            if (creator.exists(tableName))
                creator.drop(tableName);

            creator.create(tableName, tableFormat);
        } catch (Exception e) {
            throw new EmfException("Could not create table '" + tableName + "'+\n" + e.getMessage());
        }
    }

}
