package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.module.SearchFilterFields;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/**
 * Created by DDelVecc on 3/14/2019.
 */
public class SearchDAOUtility {

    public static void buildSearchCriterion(Criteria criteria, SearchFilterFields searchFilterFields,
                                            BasicSearchFilter searchFilter) {
        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            FilterField filterField = searchFilterFields.getFilterFields().get(searchFilter.getFieldName());

            //agregate fields are dealt with differently
            if (!filterField.isAggregrateField()) {
                if (filterField.getFieldDataType().equals(String.class)) {
                    criteria.add(Restrictions.ilike(filterField.getAssociationPath(), "%" + searchFilter.getFieldValue() + "%"));
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
            } else {
                if (filterField.getFieldDataType().equals(Integer.class)) {
                    try {
                        criteria.add(Restrictions.sizeEq(filterField.getAssociationPath(), Integer.parseInt(searchFilter.getFieldValue())));
                    } catch (NumberFormatException e) {
                        criteria.add(Restrictions.sizeEq(filterField.getAssociationPath(), -1));
                    }
                }
            }
        }
    }

    public static void buildSearchCriterion(Subquery<DatasetType> subQuery, CriteriaBuilder builder, Root<DatasetType> subRoot, SearchFilterFields searchFilterFields,
            BasicSearchFilter searchFilter) {
        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            FilterField filterField = searchFilterFields.getFilterFields().get(searchFilter.getFieldName());

            List<Predicate> predicates = new ArrayList<Predicate>();
            
//agregate fields are dealt with differently
            if (!filterField.isAggregrateField()) {
                if (filterField.getFieldDataType().equals(String.class)) {
                    predicates.add(builder.like(builder.lower(subRoot.get(filterField.getAssociationPath())),
                            "%" + searchFilter.getFieldValue().toLowerCase() + "%"));
                } else if (filterField.getFieldDataType().equals(Integer.class)) {
                    try {
                        predicates.add(builder.equal(subRoot.get(filterField.getAssociationPath()),
                                Integer.parseInt(searchFilter.getFieldValue())));
                    } catch (NumberFormatException e) {
                        predicates.add(builder.equal(subRoot.get(filterField.getAssociationPath()), null));
                    }
                } else if (filterField.getFieldDataType().equals(Date.class)) { // this is ignoring the time part of the
                                                                                // date!!!
                    try {
                        final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
                        Date date = df.parse(searchFilter.getFieldValue());
                        Calendar c = Calendar.getInstance();
                        c.setTime(date);
                        c.add(Calendar.DATE, 1);
                        predicates.add(builder.between(subRoot.get(filterField.getAssociationPath()), date, c.getTime()));
                    } catch (ParseException e) {
                        predicates.add(builder.equal(subRoot.get(filterField.getAssociationPath()), null));
                    }
                } else if (filterField.getFieldDataType().equals(Boolean.class)) {
                    if (searchFilter.getFieldValue().equalsIgnoreCase("y")
                            || searchFilter.getFieldValue().equalsIgnoreCase("yes")) {
                        predicates.add(builder.equal(subRoot.get(filterField.getAssociationPath()), true));
                    } else if (searchFilter.getFieldValue().equalsIgnoreCase("n")
                            || searchFilter.getFieldValue().equalsIgnoreCase("no")) {
                        predicates.add(builder.equal(subRoot.get(filterField.getAssociationPath()), false));
                    } else {
                        predicates.add(builder.equal(subRoot.get(filterField.getAssociationPath()), null));
                    }
                }
            } else {
                if (filterField.getFieldDataType().equals(Integer.class)) {
                    try {
                        predicates.add(builder.equal(builder.size(subRoot.get(filterField.getAssociationPath())),
                                Integer.parseInt(searchFilter.getFieldValue())));
                    } catch (NumberFormatException e) {
                        predicates.add(builder.equal(builder.size(subRoot.get(filterField.getAssociationPath())),
                                -1));
                    }
                }
            }
            subQuery.where(predicates.toArray(new Predicate[0]));
        }
    }

    public static String buildSearchCriterion(SearchFilterFields searchFilterFields,
                                            BasicSearchFilter searchFilter) {
        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            FilterField filterField = searchFilterFields.getFilterFields().get(searchFilter.getFieldName());
            StringBuilder whereClause = new StringBuilder();

            //agregate fields are dealt with differently
            if (!filterField.isAggregrateField()) {
                if (filterField.getFieldDataType().equals(String.class)) {
                    whereClause.append("lower(" + filterField.getAssociationPath() + ") like " + "'%" + searchFilter.getFieldValue().toLowerCase() + "%'");
//                    criteria.add(Restrictions.ilike(filterField.getAssociationPath(), "%" + searchFilter.getFieldValue() + "%"));
                } else if (filterField.getFieldDataType().equals(Integer.class)) {
                    try {
                        whereClause.append(filterField.getAssociationPath() + " = " + Integer.parseInt(searchFilter.getFieldValue()));
//                        criteria.add(Restrictions.eq(filterField.getAssociationPath(), Integer.parseInt(searchFilter.getFieldValue())));
                    } catch (NumberFormatException e) {
                        whereClause.append(" 1 = 0 ");
//                        criteria.add(Restrictions.eq(filterField.getAssociationPath(), null));
                    }
                } else if (filterField.getFieldDataType().equals(Date.class)) { //this is ignoring the time part of the date!!!
                    try {
                        final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
                        Date date = df.parse(searchFilter.getFieldValue());
                        Calendar c = Calendar.getInstance();
                        c.setTime(date);
                        c.add(Calendar.DATE, 1);
                        whereClause.append(filterField.getAssociationPath() + " between '" + date + "' and '" + c.getTime() + "'");
//                        criteria.add(Restrictions.between(filterField.getAssociationPath(), date, c.getTime()));
                    } catch (ParseException e) {
                        whereClause.append(" 1 = 0 ");
//                        criteria.add(Restrictions.eq(filterField.getAssociationPath(), null));
                    }
                } else if (filterField.getFieldDataType().equals(Boolean.class)) {
                    if (searchFilter.getFieldValue().equalsIgnoreCase("y") || searchFilter.getFieldValue().equalsIgnoreCase("yes")) {
                        whereClause.append(filterField.getAssociationPath() + " = true");
//                        criteria.add(Restrictions.eq(filterField.getAssociationPath(), true));
                    } else if (searchFilter.getFieldValue().equalsIgnoreCase("n") || searchFilter.getFieldValue().equalsIgnoreCase("no")) {
                        whereClause.append(filterField.getAssociationPath() + " = false");
//                        criteria.add(Restrictions.eq(filterField.getAssociationPath(), false));
                    } else {
                        whereClause.append(" 1 = 0 ");
//                        criteria.add(Restrictions.eq(filterField.getAssociationPath(), null));
                    }
                }
            } else {
                if (filterField.getFieldDataType().equals(Integer.class)) {
//                    try {
//                        criteria.add(Restrictions.sizeEq(filterField.getAssociationPath(), Integer.parseInt(searchFilter.getFieldValue())));
//                    } catch (NumberFormatException e) {
//                        criteria.add(Restrictions.sizeEq(filterField.getAssociationPath(), -1));
//                    }
                }
            }
            return whereClause.toString();
        }
        return null;
    }

}
