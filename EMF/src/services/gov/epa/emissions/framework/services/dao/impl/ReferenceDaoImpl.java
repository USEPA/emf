package gov.epa.emissions.framework.services.dao.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.framework.services.dao.ReferenceDao;
import gov.epa.emissions.framework.services.persistence.AbstractJpaDao;

@Repository(value = "referenceDao")
public class ReferenceDaoImpl extends AbstractJpaDao<Reference> implements ReferenceDao {

    public ReferenceDaoImpl() {
        super();
        setClazz(Reference.class);
    }

    @Transactional("transactionManager")
    @Override
    public void addReference(Reference reference) {
        create(reference);
    }

    @Override
    public List<Reference> getReferences() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Reference> criteriaQuery = criteriaBuilder.createQuery(Reference.class);
        Root<Reference> from = criteriaQuery.from(Reference.class);
        criteriaQuery.select(from);
        criteriaQuery.orderBy(criteriaBuilder.asc(from.get("description")));
        
        return entityManager
                .createQuery(criteriaQuery)
                .getResultList();
    }

    @Override
    public List<Reference> getReferences(String textContains) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Reference> criteriaQuery = criteriaBuilder.createQuery(Reference.class);
        Root<Reference> from = criteriaQuery.from(Reference.class);
        criteriaQuery.select(from);

        if (StringUtils.isNotBlank(textContains)) {
            criteriaQuery.where(
                    criteriaBuilder.like(criteriaBuilder.lower(from.get("description")), "%" + textContains.toLowerCase() + "%")
            );
        }
        
        criteriaQuery.orderBy(criteriaBuilder.asc(from.get("description")));
        
        return entityManager
                .createQuery(criteriaQuery)
                .getResultList();
        // return session.createQuery(
        // "select new Reference(ref.id, ref.description) from Reference as ref where lower(ref.description) "
        // + "like '%%" + textContains.toLowerCase().trim() + "%%' order by ref.description").list();
    }

    @Override
    public boolean canUpdate(Reference reference) {
        Reference current = findOne(reference.getId());
        if (current == null) {
            return false;
        }

        if (current.getDescription().equals(reference.getDescription()))
            return true;

        return !descriptionUsed(reference.getDescription());
    }

    @Override
    public Long getReferenceCount() {
        return entityManager
                .createQuery("SELECT COUNT(ref.id) from Reference as ref", Long.class)
                .getSingleResult();
    }

    @Override
    public Long getReferenceCount(String text) {
        return entityManager
                .createQuery("SELECT COUNT(ref.id) from Reference as ref where lower(ref.description) like '%%"
                        + text.toLowerCase().trim() + "%%'", Long.class)
                .getSingleResult();
    }

    @Override
    public boolean descriptionUsed(String description) {
        return entityManager
                .createQuery("SELECT COUNT(ref.id) from Reference as ref where lower(ref.description) like '%%"
                        + description.toLowerCase().trim() + "%%'", Integer.class)
                .getSingleResult() > 0;
    }
}
