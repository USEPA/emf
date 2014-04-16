/*
 * $Source: /cvsroot/emisview/EMF/src/common/gov/epa/emissions/common/version/EMFVersionPropertiesUpdater.java,v $
 * $Revision: 1.1 $
 * $Author: rross67 $
 * $Date: 2010/05/20 14:38:27 $
 */
package gov.epa.emissions.common.version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class EMFVersionPropertiesUpdater {

    public static void main(String[] args) throws IOException {

        String userDir = System.getProperty("user.dir");
        String relativeFilePath = "/res/properties/version_for_deploy.properties";
        String absoluteFilePath = "";
        if (args.length == 0) {

            absoluteFilePath = userDir + relativeFilePath;
            System.out.println("No argument given for the updated properties file path. Using default: "
                    + absoluteFilePath);
        } else {

            absoluteFilePath = userDir + args[0];
            System.out.println("Updated properties file path found. Using: " + absoluteFilePath);
        }

        File versionUpdatedFile = new File(absoluteFilePath);

        BufferedReader fileReader = new BufferedReader(new FileReader(System.getProperty("user.dir")
                + "/res/properties/version.properties"));
        FileWriter fileWriter = new FileWriter(versionUpdatedFile);

        String line;
        boolean changed = false;
        while ((line = fileReader.readLine()) != null) {

            if (line.startsWith("#") || line.trim().isEmpty()) {
                fileWriter.write(line);
            } else if (line.trim().endsWith("true")) {

                changed = true;
                line = line.replace("true", "false");
                fileWriter.write(line);
            } else if (changed) {

                changed = false;
                String[] tokens = line.split("=");
                if (tokens.length != 2) {
                    throw new RuntimeException("Malformed property line: " + line);
                }

                int versionNumber = Integer.parseInt(tokens[1].trim());
                line = tokens[0] + "=" + Integer.toString(++versionNumber);
                fileWriter.write(line);
            } else {
                fileWriter.write(line);
            }

            fileWriter.write("\n");
            System.out.println(line);
        }

        fileWriter.flush();
        fileWriter.close();
    }
}
