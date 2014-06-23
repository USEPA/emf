package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.hibernate.Session;

public abstract class AbstractDaoCommand<T> {

    private HibernateSessionFactory sessionFactory;

    private Log log;

    private T returnValue;

    public AbstractDaoCommand<T> execute() throws EmfException {

        if (this.sessionFactory == null) {
            throw new EmfException("Hibernate session factory cannot be null.");
        }

        Session session = this.sessionFactory.getSession();
        try {
            this.doExecute(session);
        } catch (EmfException e) {

            /*
             * Make sure we just throw any resulting EmfExceptions
             */
            throw e;
        } catch (Exception e) {

            /*test
             * Handle all other exceptions by logging the error message, then wrapping the message in an EmfException
             * and throwing that.
             */
            String errorMessage = this.getErrorMessage();
            this.log.error(errorMessage, e);
            throw new EmfException(errorMessage);
        } finally {

            if (session != null) {
                session.close();
            }
        }

        /*
         * Return _this_ so we can chain the getReturnValue() call to the execute() call. Not necessary, but it makes
         * the resulting code a little cleaner
         */
        return this;
    }

    public void setSessionFactory(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public T getReturnValue() {
        return this.returnValue;
    }

    protected void setReturnValue(T returnValue) {
        this.returnValue = returnValue;
    }

    protected String getErrorMessage() {
        return "";
    }

    protected abstract void doExecute(Session session) throws Exception;
}
