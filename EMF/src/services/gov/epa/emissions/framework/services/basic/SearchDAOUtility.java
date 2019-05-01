package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.module.SearchFilterFields;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by DDelVecc on 3/14/2019.
 */
public class SearchDAOUtility {

    public static void buildSearchCriterion(Criteria criteria, SearchFilterFields searchFilterFields,
                                            BasicSearchFilter searchFilter) {
        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            FilterField filterField = searchFilterFields.getFilterFields().get(searchFilter.getFieldName());
            if (filterField.getFieldDataType().equals(String.class)) {
                criteria.add(Restrictions.ilike(filterField.getAssociationPath(), searchFilter.getFieldValue()));
            } else if (filterField.getFieldDataType().equals(Integer.class)) {
                try {
                    criteria.add(Restrictions.eq(filterField.getAssociationPath(), Integer.parseInt(searchFilter.getFieldValue())));
                } catch (NumberFormatException e) {
                    criteria.add(Restrictions.eq(filterField.getAssociationPath(), null));
                }
            } else if (filterField.getFieldDataType().equals(Date.class)) { //this is ignoring the time part of the date!!!
                try {
                    final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
                    Date date = df.parse(searchFilter.getFieldValue());
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);
                    c.add(Calendar.DATE, 1);
                    criteria.add(Restrictions.between(filterField.getAssociationPath(), date, c.getTime()));
                } catch (ParseException e) {
                    criteria.add(Restrictions.eq(filterField.getAssociationPath(), null));
                }
            } else if (filterField.getFieldDataType().equals(Boolean.class)) {
                if (searchFilter.getFieldValue().equalsIgnoreCase("y") || searchFilter.getFieldValue().equalsIgnoreCase("yes")) {
                    criteria.add(Restrictions.eq(filterField.getAssociationPath(), true));
                } else if (searchFilter.getFieldValue().equalsIgnoreCase("n") || searchFilter.getFieldValue().equalsIgnoreCase("no")) {
                    criteria.add(Restrictions.eq(filterField.getAssociationPath(), false));
                } else {
                    criteria.add(Restrictions.eq(filterField.getAssociationPath(), null));
                }
            }
        }
    }

}
