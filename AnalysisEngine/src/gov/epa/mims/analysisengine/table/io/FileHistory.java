package gov.epa.mims.analysisengine.table.io;

import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import java_cup.runtime.Symbol;

import javax.swing.table.DefaultTableModel;

public class FileHistory extends DefaultTableModel {

	private File file;

	private Vector columnNames;

	private String fileName;

	public FileHistory(String fileName) {
		this.fileName = fileName;
		columnNames = new Vector(Arrays.asList(new String[] { "File Type", "Directory", "File Name", "Delimiter",
				"Column Header Rows" }));
		initialize();
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}

	private void initialize() {
		String filename = System.getProperty("RecentFiles");
		if (filename == null)
			filename = System.getProperty("user.dir") + File.separator + this.fileName;
		setColumnIdentifiers(columnNames);
		file = new File(filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			loadHistory();
	}

	private void loadHistory() {
		Symbol[] lineTokens;
		Vector fileData = new Vector();

		try {
			FileScanner scanner = new FileScanner(new FileReader(file));
			while ((lineTokens = scanner.getTokensPerLine(';', false)) != null) {
				FileInfo rowValues = getObjects(lineTokens);
				fileData.add(rowValues);
			}
			setDataVector(fileData, columnNames);
		} catch (Exception e) {
			new GUIUserInteractor().notify(null, "Error with Recent Files List", e.getMessage(), UserInteractor.ERROR);
		}
	}

	private FileInfo getObjects(Symbol[] lineTokens) throws Exception {
		Object[] values = new Object[5];
		if (lineTokens.length == 1)
			return null;
		if (lineTokens.length != 5)
			throw new Exception("Unexpected record type");
		for (int i = 0; i < lineTokens.length; i++)
			if (lineTokens[i].sym != TokenConstants.NULL_LITERAL)
				values[i] = (lineTokens[i].value);
		return new FileInfo((String) values[2], (String) values[1], (String) values[3], ((Integer) values[4])
				.intValue(), (String) values[0]);
	}

	public void addToHistory(String filetype, String fullname, String delim, int numColHdrRows) {
		Vector data = getDataVector();
		String filename = fullname.substring(fullname.lastIndexOf(File.separatorChar) + 1, fullname.length());
		String path = fullname.substring(0, fullname.lastIndexOf(File.separatorChar));
		FileInfo info = new FileInfo(filename, path, delim, numColHdrRows, filetype);
		if (data.contains(info))
			return;
		addRow(info);
	}

	public void saveHistory() throws IOException {
		FileOutputStream outputstream = new FileOutputStream(file);
		Vector data = getDataVector();
		for (int i = 0; i < data.size(); i++) {
			FileInfo info = (FileInfo) data.get(i);
			info.print(outputstream);
		}
		outputstream.close();
	}

}
