package gov.epa.mims.analysisengine.table.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public class FileMethods {

	/*******************************************************************************************************************
	 * 
	 * static methods
	 * 
	 ******************************************************************************************************************/

	/**
	 * Delete a directory and its contents
	 * 
	 * @param file
	 *            java.io.File specifying the directory to delete
	 * @pre file != null
	 * 
	 * @return indicates if deletion was successful
	 * @review_concern This is a powerful method. The user should be made aware what would happen if file pointed to
	 *                 "c:\windows". Maybe asking the user if they are sure would be in order.
	 * @review_comment formatting does not follow coding standard
	 */
	public static boolean deleteTree(java.io.File file) {
		if (file.isDirectory()) {
			java.io.File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (!deleteTree(files[i]))
					return false;
			}
		}
		return file.delete();
	}

	/**
	 * method that replaces linefeed characters with line-separator characters
	 */
	static public String replaceLinefeeds(String buf) {
		if (buf != null) {
			int kpos = 0;
			int idx = 0;
			while (kpos < buf.length() && (idx = buf.indexOf("\n", kpos)) >= 0) {
				buf = buf.substring(0, idx) + System.getProperty("line.separator")
						+ ((idx < buf.length() - 1) ? buf.substring(idx + 1, buf.length()) : "");
				kpos = idx + 2;
			}
		}
		return buf;
	}

	/**
	 * method to convert a DOS ASCII file to UNIX
	 */
	static public File DOS2UNIX(File file) throws Exception {
		String record;
		File copy = new File(file.getAbsolutePath() + ".unix");

		if (file.exists() && file.isFile() && file.length() > 0) {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			BufferedWriter writer = new BufferedWriter(new FileWriter(copy));

			while ((record = reader.readLine()) != null) {
				writer.write(record + "\n");
			}

			reader.close();
			writer.close();
		}
		return copy;
	}

	/**
	 * method to convert a UNIX ASCII file to WINDOWS
	 */
	static public File UNIX2DOS(File file) throws Exception {
		String record;
		File copy = new File(file.getAbsolutePath() + ".dos");

		if (file.exists() && file.isFile() && file.length() > 0) {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			BufferedWriter writer = new BufferedWriter(new FileWriter(copy));

			while ((record = reader.readLine()) != null) {
				writer.write(record + "\r\n");
			}

			reader.close();
			writer.close();
		}
		return copy;
	}

	/**
	 * method to convert CRLF to System's line.separator
	 */
	static public File ConvertCRLF(File file) throws Exception {
		String record;
		File copy = new File(file.getAbsolutePath() + ".tmp");

		if (file.exists() && file.isFile() && file.length() > 0) {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			BufferedWriter writer = new BufferedWriter(new FileWriter(copy));

			while ((record = reader.readLine()) != null) {
				writer.write(record + System.getProperty("line.separator"));
			}

			reader.close();
			writer.close();
		}
		return copy;
	}

	/**
	 * Method to replace all special characters to underscores in filename string
	 */
	public static String toFileName(String fileName) {
		StringBuffer buf = new StringBuffer(fileName);
		for (int i = 0; i < buf.length(); i++) {
			char c = buf.charAt(i);
			if ((c <= '/' && c != '.') || (c >= ':' && c <= '@') || (c >= '[' && c <= '`') || (c >= '{'))
				buf.setCharAt(i, '_');
		}
		return buf.toString();
	}

	/**
	 * Method to replace path separators for os type
	 */
	public static String setFileSeparators(String fileName, char separator) {
		boolean inQuotes = false;
		StringBuffer buf = new StringBuffer(fileName);
		for (int i = 0; i < buf.length(); i++) {
			char c = buf.charAt(i);
			if (c == '"')
				inQuotes = !inQuotes;
			if (!inQuotes) {
				if (c == '/' || c == '\\')
					buf.setCharAt(i, separator);
			}
		}
		return buf.toString();
	}

	// /**
	// * Method to search system's Search PATH for file
	// *
	// * returns new file if found or original file if not found
	// **/
	// public static File searchPathForFile( String filename )
	// {
	// String searchPath = getEnvironmentVariable("PATH");
	//
	// if( searchPath!=null && searchPath.length()>0 )
	// {
	// String pathDelimiter = (System.getProperty("os.name").toUpperCase().startsWith("WIN"))
	// ? ";" : ":";
	//
	// // search each directory in searchPath for file
	// StringTokenizer tok = new StringTokenizer(searchPath,pathDelimiter);
	// while(tok.hasMoreTokens())
	// {
	// File searchFile = new File( tok.nextToken(), filename );
	// if( searchFile.exists() )
	// {
	// return searchFile;
	// }
	// }
	// }
	//
	// return new File(filename);
	// }

	// /* method to return the value of an environment variable */
	// public static String getEnvironmentVariable(String name)
	// {
	// // String to capture output from process
	// String result = "";
	// String commandLine;
	// String delimiter;
	//
	// if (System.getProperty("os.name").toUpperCase().startsWith("WIN"))
	// {
	// commandLine = "cmd /c echo %" + name + "%";
	// delimiter = "%";
	// }
	// else
	// {
	// commandLine = "/bin/sh -c echo ${" + name + "}";
	// delimiter = "$";
	// }
	//
	// // create OutputStreams for StandardOutput and StandardError
	// OutputStream outputStream = new ByteArrayOutputStream();
	// OutputStream errorStream = new ByteArrayOutputStream();
	//
	// // execute command line and save results to outputStream and errorStream
	// try
	// {
	// LocalExecutor.executeCommand(commandLine, outputStream, errorStream);
	// result = outputStream.toString();
	// }
	// catch(Exception ex)
	// {
	// result = "";
	// }
	//
	// // check for undefined variable
	// if( result.indexOf(delimiter)>=0 )
	// {
	// result = "";
	// }
	//
	// // remove crlf from result
	// int pos;
	// if( (pos=result.indexOf("\r"))>=0 )
	// {
	// result = result.substring(0,pos);
	// }
	//
	// return result;
	// }

	/**
	 * static method to return the next field from a file reader
	 */
	public static String readNextField(Reader reader) throws IOException {
		String text = "";

		boolean isText = false;
		boolean isNumber = false;
		boolean isSet = false;
		boolean isNull = false;
		boolean isInvalid = false;
		int badChar = 0;
		int levelOfSet = 0;

		int c;

		// read first non-blank character to determine type of field
		while ((c = reader.read()) <= 32 && c >= 0)
			;
		if (c < 0) // eof
		{
			throw new IOException("EOF Encountered");
		}
		if (c == 34) // quotation mark
		{
			isText = true;
			text += (char) c;
		} else if (c == 123) // { character used for sets
		{
			isSet = true;
			text += (char) c;
			levelOfSet = 1;
		} else if (c == 110) // n, check for null or nan
		{
			text += (char) c;
			isNull = true;
		} else if ((c >= 48 && c <= 57) || c == 43 || c == 45 || c == 46) // check for number
		{
			isNumber = true;
			text += (char) c;
		} else // invalid character, throw Exception
		{
			isInvalid = true;
			badChar = c;
		}

		// read remaining field
		while (!isInvalid && (c = reader.read()) >= 32) {
			if (isText) {
				text += (char) c;
				if (c == 34)
					break; // ending quotation mark
			} else if (isNumber) {
				if ((c >= 48 && c <= 57) || c == 43 || c == 45 || c == 46) {
					text += (char) c;
				} else {
					break;
				}
			} else if (isSet) {
				text += (char) c;
				if (c == 123)
					levelOfSet++;
				if (c == 125)
					levelOfSet--;
				if (levelOfSet == 0)
					break; // end of top level set
			} else if (isNull) {
				if (c != 44) {
					text += (char) c;
				} else {
					break;
				}
			} else {
				isInvalid = true;
				badChar = c;
			}
		}

		// read util comma, |, or eor or eof
		while (c != 44 && c != 124 && c > 32) {
			c = reader.read();
		}

		if (isNull) {
			text = text.trim();
			if (!text.equals("null") && !text.equals("nan")) {
				throw new IOException("Syntax Error, invalid field [" + text + "].");
			}
		}

		// throw Exception if invalid field
		if (isInvalid) {
			throw new IOException("Syntax Error, invalid character [" + (char) badChar + "].");
		}

		return text;
	}

	/**
	 * method that checks for invalid XML characters in String and replaces them with HTML code Characters
	 */
	public static String replaceSpecialChars(String text) {
		String crlf = System.getProperty("line.separator");
		char[] specialChar = { '\"', '<', '>', '&', '{', '|', '}', '\n' };
		String[] replaceChar = { "&#34;", "&#60;", "&#62;", "&#38;", "&#123;", "&#124;", "&#125;", crlf };
		String copy = text;

		if (text != null) {
			StringBuffer buff = new StringBuffer(text);
			copy = "";
			for (int i = 0; i < buff.length(); i++) {
				String append = text.substring(i, i + 1);
				// check for special character
				for (int j = 0; j < specialChar.length; j++) {
					if (buff.charAt(i) == specialChar[j]) {
						append = replaceChar[j];
						break;
					}
				}
				copy += append;
			}
		}
		return copy;
	}

	/**
	 * method that replaces HTML code characters back to special characters
	 */
	public static String restoreSpecialChars(String text) {
		String crlf = System.getProperty("line.separator");
		char[] specialChar = { '\"', '<', '>', '&', '{', '|', '}', '\n' };
		String[] replaceChar = { "&#34;", "&#60;", "&#62;", "&#38;", "&#123;", "&#124;", "&#125;", crlf };

		if (text != null) {
			for (int i = 0; i < replaceChar.length; i++) {
				int pos;
				while ((pos = text.indexOf(replaceChar[i])) >= 0) {
					String copy = "";
					copy += text.substring(0, pos);
					copy += specialChar[i];
					copy += text.substring(pos + replaceChar[i].length());
					text = copy;
				}
			}
		}
		return text;
	}

	/**
	 * method that removes outer most quotation mark from text string
	 */
	public static String removeQuotationMarks(String text) {
		text = text.trim();
		int pos1 = text.indexOf("\"");
		int pos2 = text.lastIndexOf("\"");

		if (pos1 < pos2 && pos1 == 0 && pos2 == (text.length() - 1)) {
			text = text.substring(pos1 + 1, pos2);
		}
		return text;
	}
}
