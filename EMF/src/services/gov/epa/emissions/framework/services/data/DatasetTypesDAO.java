package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.basic.FilterField;
import gov.epa.emissions.framework.services.basic.SearchDAOUtility;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

public class DatasetTypesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public DatasetTypesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return hibernateFacade.getAll(DatasetType.class, Order.asc("name").ignoreCase(), session);
    }

    public List<DatasetType> getDatasetTypes(Session session, BasicSearchFilter searchFilter) {
        Criteria criteria = session.createCriteria(DatasetType.class, "dt");

        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            Criteria inCriteria = session.createCriteria(DatasetType.class, "dt")
                    .setProjection(Property.forName("id"));

            SearchDAOUtility.buildSearchCriterion(inCriteria, new DatasetTypeFilter(), searchFilter);

            List<Integer> moduleIds = inCriteria.list();

            if (moduleIds.size() > 0)
                criteria
                        .add(Property.forName("id").in(moduleIds));
            else
                criteria
                        .add(Property.forName("id").eq((Object)null));
        }

        return criteria.list();
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
        Criterion criterion = Restrictions.eq("name", name);
        List list = hibernateFacade.get(DatasetType.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (DatasetType) list.get(0);
    }

    public void add(DatasetType datasetType, Session session) {
        hibernateFacade.add(datasetType, session);
    }

    public boolean canUpdate(DatasetType datasetType, Session session) {
        if (!exists(datasetType.getId(), DatasetType.class, session)) {
            return false;
        }

        DatasetType current = current(datasetType.getId(), DatasetType.class, session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(datasetType.getName()))
            return true;

        return !nameUsed(datasetType.getName(), DatasetType.class, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public DatasetType current(int id, Class clazz, Session session) {
        return (DatasetType) hibernateFacade.current(id, clazz, session);
    }

    private DatasetType current(DatasetType datasetType, Session session) {
        return current(datasetType.getId(), DatasetType.class, session);
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
