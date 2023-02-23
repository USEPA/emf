package gov.epa.mims.analysisengine.table.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import java_cup.runtime.Symbol;

/*
 * FixedWidthReader.java
 * This class is the parser for files with fixed column width data
 * It derives from FileParser for compatibility with the rest of the model
 * Created on April 1, 2004, 12:31 PM
 * @author  Krithiga Thangavelu, CEP, UNC-CHAPEL HILL
 * @version $Id: FixedWidthReader.java,v 1.2 2006/10/30 21:43:50 parthee Exp $
 */

public class FixedWidthReader extends FileParser {
	/**
	 * inference while determining column splits whether the column data are right or left justified
	 */
	boolean right_justified = false;

	/** number of lines in the file header */
	int lineCount;

	/** Creates a new instance of FixedWidthFileReader */
	public FixedWidthReader(String fileName) throws Exception {
		super();
		try {
			this.fileName = fileName;
			Vector columnPositions = determineColumnWidths(fileName);
			readAndStoreFileData(columnPositions);
		} catch (Exception e) {
			throw new IOException("Error reading fixed column width file.\n" + "Possibly a wrong file type specified\n");
		}
	}

	/* placeholder for hookup to PreviewDialog */
	void previewColumnSplits() {
		// TODO
	}

	/* Read and store the file data */
	void readAndStoreFileData(Vector columnPositions) throws Exception {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			StringBuffer hdr = new StringBuffer();

			while (lineCount-- > 0 && (line = br.readLine()) != null) {
				hdr.append(line);
				hdr.append("\n");
			}

			fileHeader = hdr.toString();

			columnHeaderData = new ArrayList();

			fileData = new ArrayList();

			valuesPerLine = columnPositions.size() + 1;

			columnTypes = new Class[valuesPerLine];
			for (int i = 0; i < valuesPerLine; i++)
				columnTypes[i] = Object.class;

			ArrayList rowData;
			Vector needToBeDouble = new Vector();
			Vector needToBeString = new Vector();
			int count = 0;
			int maxErrorDoubleRow = -1;
			int maxErrorStringRow = -1;

			positions = new int[columnPositions.size() + 2];
			positions[0] = 0;
			for (int i = 1; i < valuesPerLine; i++)
				positions[i] = ((Integer) columnPositions.get(i - 1)).intValue();

			while ((line = br.readLine()) != null) {
				Symbol[] tokens = parseLine(line);
				if (tokens == null)
					continue;
				if (isAllStrings(tokens)) {
					String[] columnNamesPerRow = new String[valuesPerLine];
					for (int i = 0; i < valuesPerLine; i++)
						columnNamesPerRow[i] = (String) tokens[i].value;
					columnHeaderData.add(columnNamesPerRow);
				} else
					break;
			}

			do {
				Symbol[] tokens = parseLine(line);
				if (tokens == null)
					continue;
				count++;
				rowData = new ArrayList();
				Class type;
				for (int i = 0; i < valuesPerLine; i++) {
					type = (tokens[i].value).getClass();
					if (columnTypes[i] != String.class && type == String.class) {
						type = String.class;
						tokens[i].value = (tokens[i].value).toString();
						Integer I = Integer.valueOf(i);
						if (!needToBeString.contains(I)) {
							needToBeString.add(I);
							maxErrorStringRow = count;
						}
					} else if (columnTypes[i] == Integer.class && type == Double.class) {
						Integer I = Integer.valueOf(i);
						if (!needToBeDouble.contains(I)) {
							needToBeDouble.add(Integer.valueOf(i));
							maxErrorDoubleRow = count;
						}
					} else if (columnTypes[i] == Double.class && type == Integer.class) {
						type = Double.class;
						tokens[i].value = Double.valueOf(((Integer) tokens[i].value).intValue());
					}
					columnTypes[i] = type;
					rowData.add(tokens[i].value);
				}
				fileData.add(rowData);
			} while ((line = br.readLine()) != null);

			int maxErrorRow = (maxErrorDoubleRow > maxErrorStringRow) ? maxErrorDoubleRow : maxErrorStringRow;
			for (int i = 0; i < maxErrorRow; i++) {
				ArrayList rowDat = (ArrayList) fileData.get(i);
				if (maxErrorDoubleRow > i)
					for (int k = 0; k < needToBeDouble.size(); k++) {
						int l = ((Integer) needToBeDouble.get(k)).intValue();
						Object obj = rowDat.get(l);
						if (obj.getClass() != Double.class)
							rowDat.set(l, Double.valueOf(((Integer) obj).intValue()));
					}

				if (maxErrorStringRow > i)
					for (int k = 0; k < needToBeString.size(); k++) {
						int l = ((Integer) needToBeString.get(k)).intValue();
						Object obj = rowDat.get(l);
						if (obj.getClass() != String.class)
							rowDat.set(l, obj.toString());
					}
				fileData.set(i, rowDat);
			}
		} catch (IOException ie) {
			throw new Exception("Error reading " + fileName);
		}
	}

	private int[] positions;

	Symbol[] parseLine(String line) throws Exception {
		// split and convert values into objects
		positions[valuesPerLine] = line.length() - 1;
		Symbol[] tokens = new Symbol[positions.length - 1];
		if (line.trim().length() == 0)
			return null;
		for (int i = 0; i < positions.length - 1; i++) {
			tokens[i] = new Symbol(TokenConstants.STRING_LITERAL, 0, 0, line.substring(positions[i], positions[i + 1]));
			tokens[i] = modifyToken(tokens[i], columnTypes[i]);
		}
		return tokens;
	}

	boolean isAllStrings(Symbol[] tokens) {
		for (int i = 0; i < tokens.length; i++)
			if (tokens[i].sym != TokenConstants.STRING_LITERAL)
				return false;
		return true;
	}

	Symbol modifyToken(Symbol token, Class type) throws Exception {
		if (((String) token.value).trim().length() > 0)
			token.value = ((String) token.value).trim();
		else if (type == Object.class)
			throw new Exception("Possibly not a fixed width format file");
		else {
			token.value = FileParser.getNullToken(type);
			if (type == Double.class)
				token.sym = TokenConstants.DOUBLE_LITERAL;
			else if (type == String.class)
				token.sym = TokenConstants.STRING_LITERAL;
			else if (type == Integer.class)
				token.sym = TokenConstants.INTEGER_LITERAL;
			return token;
		}

		if (type != Object.class)
			try {
				if (type == Double.class) {
					token.value = Double.valueOf((String) token.value);
					token.sym = TokenConstants.DOUBLE_LITERAL;
				} else if (type == String.class) {
					token.value = ((String) token.value).trim();
					token.sym = TokenConstants.STRING_LITERAL;
				} else if (type == Integer.class) {
					token.value = Integer.valueOf((String) token.value);
					token.sym = TokenConstants.INTEGER_LITERAL;
				}
				return token;
			} catch (Exception e) {
				// TODO:
			}

		try {
			Integer i = Integer.valueOf((String) token.value);
			token.value = i;
			token.sym = TokenConstants.INTEGER_LITERAL;
		} catch (Exception e1) {
			try {
				Double d = Double.valueOf((String) token.value);
				token.value = d;
				token.sym = TokenConstants.DOUBLE_LITERAL;
			} catch (Exception e2) {
				token.value = ((String) token.value).trim();
				token.sym = TokenConstants.STRING_LITERAL;
			}
		}
		return token;
	}

	/**
	 * Determines the column widths
	 * 
	 * @param String
	 *            fileName
	 * @return Vector columnWidths
	 */
	Vector determineColumnWidths(String fileName) throws Exception {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			Vector possibleColumnEnds1, possibleColumnEnds2;
			int consistency = 0;
			boolean inHeader = true;
			lineCount = 0;
			possibleColumnEnds1 = new Vector();
			Vector result = new Vector();
			possibleColumnEnds2 = new Vector();
			while ((line = br.readLine()) != null && consistency != 5) {
				String[] tokens = line.split("[ \t]+");
				int prevIndex = 0;
				int k = 0;
				if (tokens != null)
					while (k < tokens.length && tokens[k].trim().length() == 0)
						k++;
				if (tokens == null || k >= tokens.length) {
					if (inHeader)
						lineCount++;
					continue;
				}
				prevIndex = line.indexOf(tokens[k]) + tokens[k].length();
				for (int i = k + 1; i < tokens.length && prevIndex < line.length(); i++) {
					if (tokens[i].trim().length() == 0)
						if (++i == tokens.length)
							break;

					int index = line.substring(prevIndex).indexOf(tokens[i]);
					index += prevIndex;
					if (index == prevIndex)
						continue;
					while (index > prevIndex) {
						possibleColumnEnds1.add(Integer.valueOf(prevIndex));
						prevIndex++;
					}
					prevIndex = index + tokens[i].length();
				}

				int ends2 = possibleColumnEnds2.size();
				int ends1 = possibleColumnEnds1.size();

				if (ends2 > 0) {
					result.clear();
					for (int i = 0; i < ends1; i++) {
						if (possibleColumnEnds2.contains(possibleColumnEnds1.get(i)))
							result.add(possibleColumnEnds1.get(i));
					}

					if (result.size() == 0 || result.size() < (ends1 / 3)) // if not data or hdr line
					{
						if (inHeader)
							lineCount++;
						possibleColumnEnds2.clear();
						possibleColumnEnds2.addAll(possibleColumnEnds1);
					} else {
						inHeader = false;
						if (result.size() != possibleColumnEnds2.size()) {
							possibleColumnEnds2.clear();
							possibleColumnEnds2.addAll(result);
						} else {
							consistency++;
							if (line.indexOf(tokens[0]) > 0)
								right_justified = true;
						}
					}
				} else {
					if (inHeader)
						lineCount++;
					possibleColumnEnds2.clear();
					possibleColumnEnds2.addAll(possibleColumnEnds1);
				}
				possibleColumnEnds1.clear();
			}
			br.close();
			--lineCount;
			Vector columnPositions = new Vector();
			if (consistency > 0) {
				int k;
				for (int i = 0; i < result.size(); i++) {
					k = 0;
					if (i == (result.size() - 1)) {
						columnPositions.add(possibleColumnEnds2.get(i));
						break;
					}
					while ((i + k + 1) < (result.size())
							&& ((Integer) possibleColumnEnds2.get(i + 1 + k)).intValue() == ((((Integer) possibleColumnEnds2
									.get(i + k)).intValue()) + 1))
						k++;
					if (right_justified)
						columnPositions.add(possibleColumnEnds2.get(i));
					else
						columnPositions.add(possibleColumnEnds2.get(i + k));
					i += k;
				}
			} else {
				int k;
				for (int i = 0; i < result.size(); i++) {
					k = 0;
					if (i == (result.size() - 1)) {
						columnPositions.add(possibleColumnEnds2.get(i));
						break;
					}
					while ((i + k) < result.size()
							&& ((Integer) possibleColumnEnds2.get(i + k)).intValue() == ((((Integer) possibleColumnEnds2
									.get(i + k + 1)).intValue()) - 1))
						k++;
					if (k == 0)
						columnPositions.add(possibleColumnEnds2.get(i));
					else
						columnPositions.add(possibleColumnEnds2.get(i + k / 2));
					i += k;
				}
			}
			if (columnPositions.size() <= 1)
				throw new Exception("Not able to determine" + " the column widths\n");
			return columnPositions;
		} catch (IOException ie) {
			throw new Exception("Error reading file " + fileName);
		}
	}

	/** Main method for Fixed Width File Reader */
	public static void main(String[] args) {
		try {
			FixedWidthReader parser = new FixedWidthReader(args[0]);
			System.out.println("File Header\n================\n" + parser.getFileHeader());
			System.out.println("\n\nColumn Name Rows \n ============\n");
			ArrayList result = parser.getColumnHeaderData();
			StringBuffer line_construct = new StringBuffer();

			if (result != null) {
				for (int i = 0; i < result.size(); i++) {
					String[] array = (String[]) result.get(i);

					line_construct.setLength(0);
					for (int j = 0; j < parser.getColumnCount(); j++) {
						line_construct.append(array[j] + " | ");
					}
					System.out.println(line_construct.toString());
				}
			}
			System.out.println("\n\nRow Header Data \n ============\n");
			result = parser.getRowHeaderData();
			if (result != null) {
				for (int i = 0; i < result.size(); i++) {
					ArrayList array = (ArrayList) result.get(i);

					line_construct.setLength(0);
					for (int j = 0; j < parser.getColumnCount(); j++) {
						line_construct.append(array.get(j) + " | ");
					}
					System.out.println(line_construct.toString());
				}
			}
			System.out.println("\n\nFile Data \n ============\n");
			result = parser.getFileData();
			if (result != null) {
				for (int i = 0; i < result.size(); i++) {
					ArrayList array = (ArrayList) result.get(i);

					line_construct.setLength(0);
					for (int j = 0; j < parser.valuesPerLine; j++) {
						line_construct.append(array.get(j) + " | ");
					}
					System.out.println(line_construct.toString());
				}
			}
			System.out.println("File Footer\n================\n" + parser.fileFooter);
			System.out.println("Log file\n=========\n" + parser.getLogMessages());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
