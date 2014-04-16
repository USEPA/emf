package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.UserPreferences;

import java.io.File;

public class CurrentDirectory {

	private File currentDirectory;

	private static CurrentDirectory usedDirectory;

	private CurrentDirectory(UserPreferences preferences) {
		currentDirectory = current(preferences);
	}

	//FIXME: REMOVE preference 
	private File current(UserPreferences preferences) {
		String directory = System.getProperty("HOME_DIR");
		if (directory != null) {
			File dir = new File(directory);
			if (dir.exists())
				return dir;
		}

		if (currentDirectory == null)
			return new File(System.getProperty("user.dir"));

		return null;
	}

	public static CurrentDirectory get(UserPreferences preferences) {
		if (usedDirectory == null)
			usedDirectory = new CurrentDirectory(preferences);

		return usedDirectory;
	}

	public void setCurrentDirectory(File directory) {
		this.currentDirectory = directory;
	}

	public File getCurrentDirectory() {
		return currentDirectory;
	}

}
