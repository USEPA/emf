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
import org.hibernate.Session;

public class DatasetTypesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public DatasetTypesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public List<DatasetType> getAll(Session session) {
        CriteriaBuilderQueryRoot<DatasetType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(DatasetType.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<DatasetType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<DatasetType> getDatasetTypes(Session session, BasicSearchFilter searchFilter) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<DatasetType> criteriaQuery = builder.createQuery(DatasetType.class);
        Root<DatasetType> root = criteriaQuery.from(DatasetType.class);

        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            Subquery<DatasetType> aSubquery = criteriaQuery.subquery(DatasetType.class);
            Root<DatasetType> aSubRoot = aSubquery.from(DatasetType.class);

            aSubquery.select(aSubRoot.get("id"));
            
            SearchDAOUtility.buildSearchCriterion(aSubquery, builder, aSubRoot, new DatasetTypeFilter(), searchFilter);

            criteriaQuery.where(builder.in(aSubquery));
        }

        return session.createQuery(criteriaQuery).getResultList();
    }

    public List<DatasetType> getLightAll(Session session) {
        return session.createQuery("select new DatasetType(dT.id, dT.name) " +
                "from DatasetType dT order by dT.name").list();
//        return hibernateFacade.getAll(DatasetType.class, Order.asc("name").ignoreCase(), session);
    }

    public DatasetType obtainLocked(User user, DatasetType type, Session session) {
        return (DatasetType) lockingScheme.getLocked(user, current(type, session), session);
    }

    public DatasetType releaseLocked(User user, DatasetType locked, Session session) {
        return (DatasetType) lockingScheme.releaseLock(user, current(locked, session), session);
    }

    public DatasetType update(DatasetType type, Session session) throws EmfException {
        return (DatasetType) lockingScheme.releaseLockOnUpdate(type, current(type, session), session);
    }

    public DatasetType get(String name, Session session) {
        List<DatasetType> list = hibernateFacade.get(DatasetType.class, "name", name, session);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public void add(DatasetType datasetType, Session session) {
        hibernateFacade.add(datasetType, session);
    }

    public boolean canUpdate(DatasetType datasetType, Session session) {
        if (!exists(datasetType.getId(), session)) {
            return false;
        }

        DatasetType current = current(datasetType.getId(), session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(datasetType.getName()))
            return true;

        return !nameUsed(datasetType.getName(), session);
    }

    private boolean exists(int id, Session session) {
        return hibernateFacade.exists(id, DatasetType.class, session);
    }

    private boolean nameUsed(String name, Session session) {
        return hibernateFacade.nameUsed(name, DatasetType.class, session);
    }

    public DatasetType current(int id, Session session) {
        return hibernateFacade.current(id, DatasetType.class, session);
    }

    private DatasetType current(DatasetType datasetType, Session session) {
        return current(datasetType.getId(), session);
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
