package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.security.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class Versions {

    public Version[] getPath(int datasetId, int finalVersion, Session session) {
        Version version = get(datasetId, finalVersion, session);
        if (version == null)
            return new Version[0];
        return doGetPath(version, session);
    }

    private Version[] doGetPath(Version version, Session session) {
        int[] parentVersions = parseParentVersions(version.getPath());
        List versions = new ArrayList();
        for (int i = 0; i < parentVersions.length; i++) {
            Version parent = get(version.getDatasetId(), parentVersions[i], session);
            versions.add(parent);
        }

        versions.add(version);

        return (Version[]) versions.toArray(new Version[0]);
    }

    public Version get(int datasetId, int version, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Version.class);
            Criteria fullCrit = crit.add(Restrictions.eq("datasetId", new Integer(datasetId))).add(
                    Restrictions.eq("version", new Integer(version)));
            tx.commit();

            return (Version) fullCrit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
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

    public int getLastFinalVersion(int datasetId, Session session) {
        int versionNumber = 0;

        Version[] versions = get(datasetId, session);

        for (int i = 0; i < versions.length; i++) {
            int versNum = versions[i].getVersion();

            if (versNum > versionNumber) {
                versionNumber = versions[i].getVersion();
            }
        }

        return versionNumber;
    }

    public Version[] get(int datasetId, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Version.class).add(
                    Restrictions.eq("datasetId", new Integer(datasetId))).addOrder(Order.asc("version"));
            List versions = crit.list();
            tx.commit();

            return (Version[]) versions.toArray(new Version[0]);
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Version derive(Version base, String name, User user, Session session) {
        if (!base.isFinalVersion())
            throw new RuntimeException("cannot derive a new version from a non-final version");

        Version version = new Version();
        int newVersionNum = getNextVersionNumber(base.getDatasetId(), session);

        version.setName(name);
        version.setVersion(newVersionNum);
        version.setPath(path(base));
        version.setCreator(user);
        version.setDatasetId(base.getDatasetId());
        version.setLastModifiedDate(new Date());
        version.setNumberRecords(base.getNumberRecords());
        version.setDescription("");
        session.clear();
        add(version, session);

        return version;
    }

    public void save(Version version, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(version);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
    
    public void add(Object obj, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(obj);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Version markFinal(Version derived, Session session) {
        derived.markFinal();
        derived.setLastModifiedDate(new Date());

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(derived);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return derived;
    }

    private String path(Version base) {
        return base.createCompletePath();
    }

    private int getNextVersionNumber(int datasetId, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria base = session.createCriteria(Version.class);
            Criteria fullCrit = base.add(Restrictions.eq("datasetId", new Integer(datasetId))).addOrder(
                    Order.desc("version"));
            List versions = fullCrit.list();
            tx.commit();

            Version latest = (Version) versions.get(0);
            return (latest).getVersion() + 1;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Version[] getLaterVersions(int datasetId, Version version, Session session) throws Exception{
        List<Version> laterVers=new ArrayList<Version>();
        try{
            int last=getLastFinalVersion(datasetId, session); 
            for (int i=version.getVersion()+1; i<=last; i++){
                Version ver=get(datasetId, i, session);
                laterVers.add(ver);
            }
            return laterVers.toArray(new Version[0]);
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("Could not get later versions for dataset version: " + version.getName() + ".\n");
        }
    }
    
    
    public Version current(Version version, Session session) {
        return get(version.getDatasetId(), version.getVersion(), session);
    }
}
