package gov.epa.emissions.googleearth.kml.version;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class VersionBuilder {

	public static void main(String[] args) throws IOException {

		Date date = new Date(System.currentTimeMillis());
		System.out.println("Building version file with date " + date.toString()
				+ "...");
		File versionFile = new File(System.getProperty("user.dir") + "/src/gov/epa/emissions/googleearth/kml/version/Version.java");

		FileWriter fileWriter = new FileWriter(versionFile);
		fileWriter
				.write("package gov.epa.emissions.googleearth.kml.version;\n");
		fileWriter.write("\n");
		fileWriter.write("import java.util.Date;\n");
		fileWriter.write("\n");
		fileWriter
				.write("/**************************************************************   \n");
		fileWriter
				.write(" * Build version, created and date stamped at compile time.<p/>   \n");
		fileWriter
				.write(" *                                                                \n");
		fileWriter
				.write(" * <em><b>NOTE:</b> This file is created dynamically. Modifying it\n");
		fileWriter
				.write(" * directly will have no affect. See {@link VersionBuilder}</em>  \n");
		fileWriter
				.write(" **************************************************************/  \n");
		fileWriter.write("public class Version {\n");
		fileWriter.write("\n");
		fileWriter.write("    private Date buildDate = new Date("
				+ date.getTime() + "L);\n");
		fileWriter.write("\n");
		fileWriter.write("    public String getVersion() {\n");
		fileWriter.write("        return this.buildDate.toString();\n");
		fileWriter.write("    }\n");
		fileWriter.write("\n");
		fileWriter.write("    public static void main(String[] args) {\n");
		fileWriter
				.write("        System.out.println(\"Build Version: \" + new Version().getVersion());\n");
		fileWriter.write("    }\n");
		fileWriter.write("}\n");
		fileWriter.flush();
		fileWriter.close();
	}
}
