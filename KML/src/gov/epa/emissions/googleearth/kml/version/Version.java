package gov.epa.emissions.googleearth.kml.version;

import java.util.Date;

/**************************************************************   
 * Build version, created and date stamped at compile time.<p/>   
 *                                                                
 * <em><b>NOTE:</b> This file is created dynamically. Modifying it
 * directly will have no affect. See {@link VersionBuilder}</em>  
 **************************************************************/  
public class Version {

    private Date buildDate = new Date(1273765395656L);

    public String getVersion() {
        return this.buildDate.toString();
    }

    public static void main(String[] args) {
        System.out.println("Build Version: " + new Version().getVersion());
    }
}
