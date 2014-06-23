package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.framework.services.EmfException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMAQLogFile implements EMFCaseFile {

    private String path;

    private File file;

    private BufferedReader fileReader;

    private CustomCharSetInputStreamReader inputStreamReader;

    private Map<String, String> parameters = new HashMap<String, String>();

    private Map<String, String[]> inputs = new HashMap<String, String[]>();

    private StringBuffer sb = null;

    private static final String lineSep = System.getProperty("line.separator");

    public CMAQLogFile(String path) {
        this.path = path;
        this.file = new File(path);
    }

    public CMAQLogFile(File file) {
        this.path = file.getAbsolutePath();
        this.file = file;
    }

    private void open() throws UnsupportedEncodingException, FileNotFoundException {
        inputStreamReader = new CustomCharSetInputStreamReader(new FileInputStream(file));
        fileReader = new BufferedReader(inputStreamReader);
    }

    private void close() throws IOException {
        fileReader.close();
    }

    public String getAttributeValue(String attribute) {
        return parameters.get(attribute);
    }

    public String[] getInputValue(String envVar) {
        return inputs.get(envVar);
    }

    public String getParameterValue(String envVar) {
        return parameters.get(envVar);
    }

    /*******************************************************************************************************************
     * Read specified attributes (inputs) only
     */
    public void readInputs(List<String> attributes, StringBuffer msg) throws EmfException {
        if (attributes == null || attributes.size() == 0)
            throw new EmfException("No attributes specified to read from log file.");

        String prevAttr = "";
        boolean first = true;
        List<String> files = new ArrayList<String>();

        if (!inputs.isEmpty())
            inputs.clear();

        try {
            open();
            String line = null;

            while ((line = fileReader.readLine()) != null) {
                line = line.trim();
                int eqIndex = line.indexOf("=");

                if (eqIndex < 0)
                    continue;

                String attrib = line.substring(0, eqIndex).trim();

                if (!first && !attrib.equals(prevAttr)) {
                    addAttribValue(prevAttr, files, inputs);
                    files.clear();
                    first = true;
                }

                if (!attributes.contains(attrib))
                    continue;

                int space = line.indexOf(" ");

                if (space < 0)
                    space = line.length();

                String value = line.substring(eqIndex + 1, space).trim();
                int slash = value.lastIndexOf('/');
                String dir = value.substring(0, slash+1);
                String file = value.substring(slash + 1);

                if (first) {
                    files.add(dir);
                    first = false;
                }
                
                files.add(file);
                prevAttr = attrib;
            }

            close();
        } catch (UnsupportedEncodingException e) {
            throw new EmfException("File " + path + " is not consistent with character encoding: "
                    + inputStreamReader.getEncoding() + ".");
        } catch (FileNotFoundException e) {
            throw new EmfException("File " + path + " doesn't exist.");
        } catch (IOException e) {
            throw new EmfException("Cannot read file " + path + ".");
        }
    }

    /*******************************************************************************************************************
     * Read specified attributes (parameters) only
     */
    public void readParameters(List<String> attributes, StringBuffer msg) throws EmfException {
        if (attributes == null || attributes.size() == 0)
            throw new EmfException("No attributes specified to read from log file.");

        String tempAttr = "";
        boolean recorded = false;

        if (!parameters.isEmpty())
            parameters.clear();

        try {
            open();
            String line = null;

            while ((line = fileReader.readLine()) != null) {
                int eqIndex = line.indexOf("=");

                if (eqIndex < 0)
                    continue;

                String attrib = line.substring(0, eqIndex).trim();

                if (!attributes.contains(attrib))
                    continue;

                String value = line.substring(eqIndex + 1).trim();

                if (attrib.equals(tempAttr)) {
                    if (!recorded) {
                        msg.append("WARNING: Variable \'" + tempAttr + "\' has duplicate values." + lineSep);
                        recorded = true;
                    }

                    continue;
                }

                addAttribValue(attrib, value, parameters);
                tempAttr = attrib;
                recorded = false;
            }

            close();
        } catch (UnsupportedEncodingException e) {
            throw new EmfException("File " + path + " is not consistent with character encoding: "
                    + inputStreamReader.getEncoding() + ".");
        } catch (FileNotFoundException e) {
            throw new EmfException("File " + path + " doesn't exist.");
        } catch (IOException e) {
            throw new EmfException("Cannot read file " + path + ".");
        }
    }

    private void addAttribValue(String key, String value, Map<String, String> map) {
        if (key == null || key.isEmpty())
            return;

        map.put(key, value);
    }

    private void addAttribValue(String key, List<String> files, Map<String, String[]> inputsMap) {
        if (key == null || key.isEmpty())
            return;

        inputsMap.put(key, files.toArray(new String[0]));
    }

    public String getMessages() {
        return (sb != null) ? sb.toString() : "";
    }

}
