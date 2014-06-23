/*
 * Created on Aug 10, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package gov.epa.emissions.framework.install.installer;

import java.io.*;
import java.lang.String;
import java.util.*;

/**
 * @author Qun He
 * @version 1.0
 * 
 * To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code
 * and Comments
 */
public class TextParser {
    private File file;

    private String delimiter;

    private File2Download[] f2d;

    private String[] fields;

    private int size;

    private static final String errormsg = "Download file list is not properly compiled.";

    public TextParser(File filename, String delim) {
        file = filename;
        delimiter = delim;
    }

    public void parse() {
        StringTokenizer st = null;
        Vector v = new Vector(500, 100);
        String[] temp;
        String line;
        String firstline;

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            // Push in the lines into vector v
            while ((line = br.readLine()) != null) {
                v.add(line);
            }
            br.close();

            // Move the lines into a temporary array
            firstline = (v.firstElement()).toString();
            v.removeElementAt(0);
            size = v.size();
            temp = new String[size];
            f2d = new File2Download[size];
            int i = 0;
            while (i < size) {
                f2d[i] = new File2Download();
                i++;
            }
            v.copyInto(temp);

            // Get the field names
            i = 0;
            st = new StringTokenizer(firstline, delimiter);
            fields = new String[st.countTokens()];
            while (st.hasMoreTokens()) {
                fields[i] = st.nextToken().trim();
                i++;
            }

            if (fields[0].equalsIgnoreCase("number") && fields[1].equalsIgnoreCase("path")
                    && fields[2].equalsIgnoreCase("groups") && fields[3].equalsIgnoreCase("date")
                    && fields[4].equalsIgnoreCase("version") && fields[5].equalsIgnoreCase("size")) {
                // TODO:
            } else {
                System.err.println(errormsg);
                System.exit(1);
            }

            // Get and save information for the download files
            for (int j = 0; j < size; j++) {
                st = new StringTokenizer(temp[j], delimiter);
                if (st.hasMoreTokens()) {
                    f2d[j].setName(st.nextToken().trim());
                } else {
                    System.err.println(errormsg);
                    System.err.println("setName");
                    System.exit(1);
                }
                if (st.hasMoreTokens()) {
                    f2d[j].setPath(st.nextToken().trim());
                } else {
                    System.err.println(errormsg);
                    System.err.println("setPath");
                    System.exit(1);
                }
                if (st.hasMoreTokens()) {
                    f2d[j].setGroups(st.nextToken().trim());
                } else {
                    System.err.println(errormsg);
                    System.err.println("setGroups");
                    System.exit(1);
                }
                if (st.hasMoreTokens()) {
                    f2d[j].setDate(st.nextToken().trim());
                } else {
                    System.err.println(errormsg);
                    System.err.println("setDate");
                    System.exit(1);
                }
                if (st.hasMoreTokens()) {
                    f2d[j].setVersion(st.nextToken().trim());
                } else {
                    System.err.println(errormsg);
                    System.err.println("setVersion");
                    System.exit(1);
                }
                if (st.hasMoreTokens()) {
                    f2d[j].setSize(st.nextToken().trim());
                } else {
                    System.err.println(errormsg);
                    System.err.println("setSize");
                    System.exit(1);
                }
                if (i > 5 && st.hasMoreTokens()) {
                    f2d[j].setParam1(st.nextToken().trim());
                }
                if (i > 6 && st.hasMoreTokens()) {
                    f2d[j].setParam2(st.nextToken().trim());
                }
                if (i > 7 && st.hasMoreTokens()) {
                    f2d[j].setParam3(st.nextToken().trim());
                }
                if (i > 8 && st.hasMoreTokens()) {
                    f2d[j].setParam4(st.nextToken().trim());
                }
                if (i > 9 && st.hasMoreTokens()) {
                    f2d[j].setParam5(st.nextToken().trim());
                }
                if (i > 10 && st.hasMoreTokens()) {
                    String x = "";
                    while (st.hasMoreTokens()) {
                        x += st.nextToken().trim() + delimiter;
                    }
                    x.substring(0, x.lastIndexOf(delimiter));
                    f2d[j].setRest(x);
                }
            }
        } catch (IOException e) {
            // TODO:
        }
    }

    public int getNumDownloadFiles() {
        return size;
    }

    public File2Download[] getDownloadFiles() {
        return f2d;
    }
}
