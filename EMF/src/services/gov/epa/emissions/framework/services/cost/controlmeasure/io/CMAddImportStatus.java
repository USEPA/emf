package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;

import java.util.Date;

import javax.persistence.EntityManagerFactory;

public class CMAddImportStatus {

    private StatusDAO statusDao;

    private User user;

    public CMAddImportStatus(User user, EntityManagerFactory entityManagerFactory) {
        this.user = user;
        this.statusDao = new StatusDAO(entityManagerFactory);
    }

    public void addStatus(int lineNo, StringBuffer sb) {
        String message = sb.toString();
        if (message.length() > 0)
            setStatus("Line "+lineNo + ": " + message);
    }

    public void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImportDetailMsg");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

    public String format(String text) {
        return text + "\n";
    }

}
