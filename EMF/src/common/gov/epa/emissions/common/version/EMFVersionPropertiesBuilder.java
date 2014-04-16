/*
 * $Source: /cvsroot/emisview/EMF/src/common/gov/epa/emissions/common/version/EMFVersionPropertiesBuilder.java,v $
 * $Revision: 1.9 $
 * $Author: rross67 $
 * $Date: 2009/12/08 19:09:58 $
 */
package gov.epa.emissions.common.version;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

public class EMFVersionPropertiesBuilder {

    public static void main(String[] args) throws IOException {

        String userDir = System.getProperty("user.dir");
        String relativeFilePath = "/res/properties/version_generated.properties";
        String absoluteFilePath = "";
        if (args.length == 0) {

            absoluteFilePath = userDir + relativeFilePath;
            System.out.println("No argument given for the generated properties file path. Using default: " + absoluteFilePath);
        } else {
            
            absoluteFilePath = userDir + args[0];
            System.out.println("Generated properties file path found. Using: " + absoluteFilePath);
        }

        Date date = new Date(System.currentTimeMillis());
        File versionGeneratedFile = new File(absoluteFilePath);
        System.out.println("Building version file:" + versionGeneratedFile.getAbsolutePath() + " with date: "
                + date.toString() + "...");

        PropertiesManager propertiesManager = PropertiesManager.getInstance();
        propertiesManager.initProperties(System.getProperty("user.dir") + "/res/properties/version.properties");

        FileWriter fileWriter = new FileWriter(versionGeneratedFile);

        fileWriter
                .write("#WARNING: This is an automatically generated file. Any manual changes to it will be lost during build.");
        fileWriter.write("\n");
        fileWriter.write("\n");
        fileWriter.write("#build.version.timestamp in readable form: ");
        fileWriter.write(date.toString());
        fileWriter.write("\n");
        fileWriter.write("build.version.timestamp=");
        fileWriter.write(Long.toString(date.getTime()));
        fileWriter.write("\n");

        Set<String> keys = propertiesManager.getKeys();
        for (String key : keys) {

            fileWriter.write(key);
            fileWriter.write("=");

            Object value = propertiesManager.getValue(key);
            fileWriter.write(value.toString());
            fileWriter.write("\n");
        }

        fileWriter.flush();
        fileWriter.close();
    }
}
