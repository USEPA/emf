package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.basic.SearchDAOUtility;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import javax.persistence.EntityManager;

public class DatasetTypesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public DatasetTypesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public List<DatasetType> getAll(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<DatasetType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(DatasetType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<DatasetType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<DatasetType> getDatasetTypes(EntityManager entityManager, BasicSearchFilter searchFilter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DatasetType> criteriaQuery = builder.createQuery(DatasetType.class);
        Root<DatasetType> root = criteriaQuery.from(DatasetType.class);

        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            Subquery<DatasetType> aSubquery = criteriaQuery.subquery(DatasetType.class);
            Root<DatasetType> aSubRoot = aSubquery.from(DatasetType.class);

            aSubquery.select(aSubRoot.get("id"));
            
            SearchDAOUtility.buildSearchCriterion(aSubquery, builder, aSubRoot, new DatasetTypeFilter(), searchFilter);

            criteriaQuery.where(builder.in(root.get("id")).value(aSubquery));
        }

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public List<DatasetType> getLightAll(EntityManager entityManager) {
        return entityManager.createQuery("select new DatasetType(dT.id, dT.name) " +
                "from DatasetType dT order by dT.name").getResultList();
//        return hibernateFacade.getAll(DatasetType.class, Order.asc("name").ignoreCase(), entityManager);
    }

    public DatasetType obtainLocked(User user, DatasetType type, EntityManager entityManager) {
        return (DatasetType) lockingScheme.getLocked(user, current(type, entityManager), entityManager);
    }

    public DatasetType releaseLocked(User user, DatasetType locked, EntityManager entityManager) {
        return (DatasetType) lockingScheme.releaseLock(user, current(locked, entityManager), entityManager);
    }

    public DatasetType update(DatasetType type, EntityManager entityManager) throws EmfException {
        return (DatasetType) lockingScheme.releaseLockOnUpdate(type, current(type, entityManager), entityManager);
    }

    public DatasetType get(String name, EntityManager entityManager) {
        List<DatasetType> list = hibernateFacade.get(DatasetType.class, "name", name, entityManager);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public void add(DatasetType datasetType, EntityManager entityManager) {
        hibernateFacade.add(datasetType, entityManager);
    }

    public boolean canUpdate(DatasetType datasetType, EntityManager entityManager) {
        if (!exists(datasetType.getId(), entityManager)) {
            return false;
        }

        DatasetType current = current(datasetType.getId(), entityManager);
        // The current object is saved in the entityManager. Hibernate cannot persist our
        // object with the same id.
        entityManager.clear();
        if (current.getName().equals(datasetType.getName()))
            return true;

        return !nameUsed(datasetType.getName(), entityManager);
    }

    private boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, DatasetType.class, entityManager);
    }

    private boolean nameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, DatasetType.class, entityManager);
    }

    public DatasetType current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, DatasetType.class, entityManager);
    }

    private DatasetType current(DatasetType datasetType, EntityManager entityManager) {
        return current(datasetType.getId(), entityManager);
    }

    public void validateDatasetTypeIndicesKeyword(DatasetType datasetType, Column[] cols) throws EmfException {
        //validate INDICES keyword...
        //first validate columns to index actually exist! 
        KeyVal[] keyVal = keyValFound(datasetType, Keyword.INDICES);
        if (cols != null && keyVal != null && keyVal.length > 0) {
            for (String columnList : keyVal[0].getValue().split("\\|")) {
                for (String columnName : columnList.split("\\,")) {
                    if (!hasColName(cols, columnName))
                        throw new EmfException("DatasetType keyword, INDICES, contains an missing column name, " + columnName + ".");
                }
            }
        }
    }

    private  KeyVal[] keyValFound(DatasetType datasetType, String keyword) {
        KeyVal[] keys = datasetType.getKeyVals();
        List<KeyVal> list = new ArrayList<KeyVal>();
        
        for (KeyVal key : keys)
            if (key.getName().equalsIgnoreCase(keyword)) 
                list.add(key);
        
        return list.toArray(new KeyVal[0]);
    }
    
    private boolean hasColName(Column[] cols, String colName) {
        boolean hasIt = false;
        for (int i = 0; i < cols.length; i++)
            if (colName.equalsIgnoreCase(cols[i].name())) hasIt = true;

        return hasIt;
    }

}
