package gov.epa.emissions.commons.io.reference;

import gov.epa.emissions.commons.db.DatabaseSetup;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
/*NOTES for updating a reference schema after updating a reference file
1. Drop your reference schema - DROP SCHEMA reference CASCADE;
2. Create a reference schema - CREATE SCHEMA reference AUTHORIZATION emf;
3. Run the ant target called  'setup-reference-schema'
4. Backup the reference schema using pgadmin
   a. Right click on the reference schema and select 'Backup'
   b. A dialog will applear 
       - specify a file name 
       - select check box next 'insert commands'
       - select the radio button next to 'PLAIN'
       click 'OK
         
5. Drop the reference schema at EPA
6. Use the back up file to create the reference schema and tables using pgadmin or psql 
*/

public class ReferenceDatasourceSetup {

    private ReferenceDatasourceTablesCreator creator;

    public ReferenceDatasourceTablesCreator getCreator() {
        return creator;
    }

    public ReferenceDatasourceSetup(String configFile, File base) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(configFile)));

        DatabaseSetup dbSetup = new DatabaseSetup(properties);
        creator = new ReferenceDatasourceTablesCreator(dbSetup.getDbServer(), base);
    }

    private void run() throws Exception {
        creator.create();
    }

    public static void main(String[] args) throws Exception {
        ReferenceDatasourceSetup setup = new ReferenceDatasourceSetup(args[0], new File(args[1]));
        setup.run();
    }
}
