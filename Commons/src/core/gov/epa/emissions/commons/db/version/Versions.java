package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.security.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

public class Versions {

    public Version[] getPath(int datasetId, int finalVersion, EntityManager entityManager) {
        Version version = get(datasetId, finalVersion, entityManager);
        if (version == null)
            return new Version[0];
        return doGetPath(version, entityManager);
    }

    private Version[] doGetPath(Version version, EntityManager entityManager) {
        int[] parentVersions = parseParentVersions(version.getPath());
        List<Version> versions = new ArrayList<Version>();
        for (int i = 0; i < parentVersions.length; i++) {
            Version parent = get(version.getDatasetId(), parentVersions[i], entityManager);
            versions.add(parent);
        }

        versions.add(version);

        return versions.toArray(new Version[0]);
    }

    public Version get(int datasetId, int version, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Version> criteriaQuery = builder.createQuery(Version.class);
        Root<Version> root = criteriaQuery.from(Version.class);

        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("datasetId"), Integer.valueOf(datasetId)), 
                builder.equal(root.get("version"), Integer.valueOf(version)));

        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    private int[] parseParentVersions(String versionsList) {
        IntList versions = new ArrayIntList();

        StringTokenizer tokenizer = new StringTokenizer(versionsList, ",");
        while (tokenizer.hasMoreTokens()) {
            int token = Integer.parseInt(tokenizer.nextToken());
            versions.add(token);
        }

        return versions.toArray();
    }

    public int getLastFinalVersion(int datasetId, EntityManager entityManager) {
        int versionNumber = 0;

        Version[] versions = get(datasetId, entityManager);

        for (int i = 0; i < versions.length; i++) {
            int versNum = versions[i].getVersion();

            if (versNum > versionNumber) {
                versionNumber = versions[i].getVersion();
            }
        }

        return versionNumber;
    }

    public Version[] get(int datasetId, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Version> criteriaQuery = builder.createQuery(Version.class);
        Root<Version> root = criteriaQuery.from(Version.class);

        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("datasetId"), Integer.valueOf(datasetId)));
        criteriaQuery.orderBy(builder.asc(root.get("version")));

        return entityManager.createQuery(criteriaQuery).getResultList().toArray(new Version[0]);
    }

    public Version derive(Version base, String name, User user, EntityManager entityManager) {
        if (!base.isFinalVersion())
            throw new RuntimeException("cannot derive a new version from a non-final version");

        Version version = new Version();
        int newVersionNum = getNextVersionNumber(base.getDatasetId(), entityManager);

        version.setName(name);
        version.setVersion(newVersionNum);
        version.setPath(path(base));
        version.setCreator(user);
        version.setDatasetId(base.getDatasetId());
        version.setLastModifiedDate(new Date());
        version.setNumberRecords(base.getNumberRecords());
        version.setDescription("");
        entityManager.clear();
        add(version, entityManager);

        return version;
    }

    public void save(Version version, EntityManager entityManager) {
        executeInsideTransaction(em -> entityManager.merge(version), entityManager);
    }
    
    public void delete(Version version, EntityManager entityManager) {
        executeInsideTransaction(em -> entityManager.remove(version), entityManager);
    }
    
    public void add(Object obj, EntityManager entityManager) {
        executeInsideTransaction(em -> entityManager.persist(obj), entityManager);
    }

    public Version markFinal(Version derived, EntityManager entityManager) {
        derived.markFinal();
        derived.setLastModifiedDate(new Date());

        executeInsideTransaction(em -> entityManager.merge(derived), entityManager);

        return derived;
    }

    private String path(Version base) {
        return base.createCompletePath();
    }

    private int getNextVersionNumber(int datasetId, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Version> criteriaQuery = builder.createQuery(Version.class);
        Root<Version> root = criteriaQuery.from(Version.class);

        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("datasetId"), Integer.valueOf(datasetId)));
        criteriaQuery.orderBy(builder.desc(root.get("version")));

        return entityManager.createQuery(criteriaQuery).getResultList().get(0).getVersion() + 1;
    }

    public Version[] getLaterVersions(int datasetId, Version version, EntityManager entityManager) throws Exception{
        List<Version> laterVers=new ArrayList<Version>();
        try{
            int last=getLastFinalVersion(datasetId, entityManager); 
            for (int i=version.getVersion()+1; i<=last; i++){
                Version ver=get(datasetId, i, entityManager);
                laterVers.add(ver);
            }
            return laterVers.toArray(new Version[0]);
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("Could not get later versions for dataset version: " + version.getName() + ".\n");
        }
    }

    public Version current(Version version, EntityManager entityManager) {
        return get(version.getDatasetId(), version.getVersion(), entityManager);
    }

    private void executeInsideTransaction(Consumer<EntityManager> action, EntityManager entityManager) {
        final EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            action.accept(entityManager);
            tx.commit(); 
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw e;
        } finally {
            //
        }
    }
}
