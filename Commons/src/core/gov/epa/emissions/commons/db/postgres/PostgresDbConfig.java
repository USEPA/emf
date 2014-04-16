package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.FileConfig;

import org.apache.commons.configuration.ConfigurationException;

public class PostgresDbConfig extends FileConfig {

    public PostgresDbConfig(String file) throws ConfigurationException {
        super(file);
    }

    public String driver() {
        return "org.postgresql.Driver";
    }

    public String url() {
        return "jdbc:postgresql://" + value("database.host") + ":" + value("database.port") + "/"
                + value("database.name");
    }

    public String username() {
        return value("database.username");
    }

    public String password() {
        return value("database.password");
    }
}
