package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.framework.services.EmfException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;

public abstract class AbstractDaoCommand<T> {

    private EntityManagerFactory entityManagerFactory;

    private Log log;

    private T returnValue;

    public AbstractDaoCommand<T> execute() throws EmfException {

        if (this.entityManagerFactory == null) {
            throw new EmfException("Hibernate entityManager factory cannot be null.");
        }

        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        try {
            this.doExecute(entityManager);
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

            if (entityManager != null) {
                entityManager.close();
            }
        }

        /*
         * Return _this_ so we can chain the getReturnValue() call to the execute() call. Not necessary, but it makes
         * the resulting code a little cleaner
         */
        return this;
    }

    public void setSessionFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
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

    protected abstract void doExecute(EntityManager entityManager) throws Exception;
}
