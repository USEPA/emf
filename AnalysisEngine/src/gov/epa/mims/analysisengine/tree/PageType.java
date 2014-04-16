package gov.epa.mims.analysisengine.tree;

import java.io.Serializable;

/**
 * describes lines and point symbols for a plot
 * 
 * @author Tommy E. Cathey
 * @version $Id: PageType.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 * 
 */
public class PageType extends AnalysisOption implements Serializable, Cloneable, TextIfc, PageConstantsIfc {
	/*******************************************************************************************************************
	 * 
	 * fields
	 * 
	 ******************************************************************************************************************/
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** The file extension for JPEGs. */
	public static final String JPG_EXT = "jpg";

	/** The file extension for Latex picutres. */
	public static final String PTX_EXT = "ptx";

	/** The file extension for PNGs. */
	public static final String PNG_EXT = "png";

	/** The file extension for PDFs. */
	public static final String PDF_EXT = "pdf";

	/** The file extension for Poscript. */
	public static final String PS_EXT = "ps";

	/** layout object for this page */
	private Layout layout = null;

	/** destination file for output */
	private String filename = null;

	/** the type of output form */
	private String form = null;

	/** user selected pdf reader */
	private String pdfReader = null;

	/** delete temporary file on exit; default = true */
	private boolean deleteTemporaryFileOnExit = true;

	/*******************************************************************************************************************
	 * set the delete temporary file on exit
	 * 
	 * <pre>
	 *   o true - delete temporary file on exit (DEFAULT)
	 *   o false - do not delete temporary file on exit
	 * </pre>
	 * 
	 * @param arg
	 *            delete temporary file on exit flag
	 ******************************************************************************************************************/
	public void setDeleteTemporaryFileOnExit(boolean arg) {
		this.deleteTemporaryFileOnExit = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the delete temporary file on exit
	 * 
	 * <pre>
	 *   o true - delete temporary file on exit (DEFAULT)
	 *   o false - do not delete temporary file on exit
	 * </pre>
	 * 
	 * @return delete temporary file on exit flag
	 ******************************************************************************************************************/
	public boolean getDeleteTemporaryFileOnExit() {
		return deleteTemporaryFileOnExit;
	}

	/*******************************************************************************************************************
	 * set the output filename
	 * 
	 * @param arg
	 *            output filename
	 * @pre arg != null
	 ******************************************************************************************************************/
	public void setFilename(java.lang.String arg) {
		// for some reason, if there are backslashes, R is not happy on the PC
		if ((arg != null) && (arg.indexOf('\\') > 0)) {
			this.filename = arg.replace('\\', '/');
		} else {
			this.filename = arg;
		}
	}

	/*******************************************************************************************************************
	 * retrieve the filename set in setFilename(java.lang.String arg)
	 * 
	 * @return output filename
	 ******************************************************************************************************************/
	public java.lang.String getFilename() {
		return filename;
	}

	/*******************************************************************************************************************
	 * set the form of output for this page
	 * 
	 * @param arg
	 *            output form
	 * @pre arg != null
	 ******************************************************************************************************************/
	public void setForm(java.lang.String arg) {
		this.form = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the form set in setForm(java.lang.String arg)
	 * 
	 * @return output form
	 ******************************************************************************************************************/
	public java.lang.String getForm() {
		return form;
	}

	/*******************************************************************************************************************
	 * set the layout for this page
	 * 
	 * @param arg
	 *            layout
	 * @pre arg != null
	 ******************************************************************************************************************/
	public void setLayout(gov.epa.mims.analysisengine.tree.Layout arg) {
		this.layout = arg;
	}

	/*******************************************************************************************************************
	 * retrieve the layout
	 * 
	 * @return user generated layout
	 ******************************************************************************************************************/
	public Layout getLayout() {
		return layout;
	}

	/*******************************************************************************************************************
	 * set the user selected pdf reader
	 * 
	 * @param arg
	 *            user selected pdf reader
	 * @pre arg != null
	 ******************************************************************************************************************/
	public void setPDFreader(java.lang.String arg) {
		this.pdfReader = arg;
	}

	/*******************************************************************************************************************
	 * retrieve user selected pdf reader
	 * 
	 * @return user selected pdf reader
	 ******************************************************************************************************************/
	public java.lang.String getPDFreader() {
		return pdfReader;
	}

	/*******************************************************************************************************************
	 * Creates and returns a copy of this object
	 * 
	 * @return a copy of this object
	 ******************************************************************************************************************/
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/*******************************************************************************************************************
	 * Compares this object to the specified object.
	 * 
	 * @param o
	 *            the object to compare this object against
	 * 
	 * @return true if the objects are equal; false otherwise
	 ******************************************************************************************************************/
	public boolean equals(Object o) {
		boolean rtrn = true;

		if (o == null) {
			rtrn = false;
		} else if (o == this) {
			rtrn = true;
		} else if (!(o instanceof PageType)) {
			rtrn = false;
		} else {
			PageType other = (PageType) o;

			boolean tmp = other.deleteTemporaryFileOnExit;

			rtrn = ((form == null) ? (other.form == null) : (form.equals(other.form)))
					&& ((filename == null) ? (other.filename == null) : (filename.equals(other.filename)))
					&& ((layout == null) ? (other.layout == null) : (layout.equals(other.layout)))
					&& ((pdfReader == null) ? (other.pdfReader == null) : (pdfReader.equals(other.pdfReader)))
					&& (deleteTemporaryFileOnExit == tmp);
		}

		return rtrn;
	}

	/**
	 * Get the value for the filename.
	 */
	public String getTextString() {
		return this.filename;
	}

	/**
	 * Set the value for the filename.
	 * 
	 * @param textString
	 *            String that is the filename to save the plot to.
	 */
	public void setTextString(String textString) {
		setFilename(textString);

		int lastDot = textString.lastIndexOf('.');

		if (lastDot > -1) {
			if (isExtensionValid(textString)) {
				setForm(getFormatForExtension(textString.substring(lastDot + 1)));
			} else {
				throw new IllegalArgumentException("The filename must contain an extension "
						+ "that is either jpg, png, pdf or ps.");
			}
		} else {
			throw new IllegalArgumentException("The filename must contain an extension "
					+ "that is either jpg, png, pdf or ps.");
		}
	}

	/**
	 * print object to string
	 * 
	 * @return object as a String
	 */
	public String toString() {
		return Util.toString(this);
	}

	/**
	 * Check to see if the given filename ends with the given extension.
	 */
	public static String addFileExtensionIfNeeded(String filename, String extension) {
		if (!isExtensionValid(filename)) {
			String ending = filename.substring(filename.length() - extension.length());

			if (!ending.equalsIgnoreCase(extension)) {
				filename = filename + "." + extension;
			}
		}

		return filename;
	}

	/**
	 * 
	 * @param format
	 *            String that is one of Page.JPEG, Page.LATEX_PICTEX_GRAPHICS, Page.PDF, Page.PNG_BITMAP or
	 *            Page.POSTSCRIPT.
	 * @returna String with the output format. Could be null.
	 */
	public static String getExtensionForFormat(String format) {
		String extension = null;

		if (format.equalsIgnoreCase(Page.JPEG)) {
			extension = JPG_EXT;
		} else if (format.equalsIgnoreCase(Page.LATEX_PICTEX_GRAPHICS)) {
			extension = PTX_EXT;
		} else if (format.equalsIgnoreCase(Page.PDF)) {
			extension = PDF_EXT;
		} else if (format.equalsIgnoreCase(Page.PNG_BITMAP)) {
			extension = PNG_EXT;
		} else if (format.equalsIgnoreCase(Page.POSTSCRIPT)) {
			extension = PS_EXT;
		}

		return extension;
	}

	/**
	 * Given a file extension, return a format from the Page constants.
	 * 
	 * @param ext
	 *            String that is one of the valid file extensions
	 * @return String that is the format from PageConstantsIfc. Could be null if the extension was not found.
	 */
	public static String getFormatForExtension(String ext) {
		String retval = null;

		if (ext.equalsIgnoreCase(JPG_EXT)) {
			retval = JPEG;
		} else if (ext.equalsIgnoreCase(PTX_EXT)) {
			retval = LATEX_PICTEX_GRAPHICS;
		} else if (ext.equalsIgnoreCase(PDF_EXT)) {
			retval = PDF;
		} else if (ext.equalsIgnoreCase(PNG_EXT)) {
			retval = PNG_BITMAP;
		} else if (ext.equalsIgnoreCase(PS_EXT)) {
			retval = POSTSCRIPT;
		}

		return retval;
	}

	/**
	 * Is there a valid extension on the given filename? We accept jpg, pdf, ptx, ps and png.
	 * 
	 * @param filename
	 *            String whose extension should be checked.
	 * @return boolean that is true if the filename has a valid extension.
	 */
	public static boolean isExtensionValid(String filename) {
		int lastDot = filename.lastIndexOf('.');

		if (lastDot > 0) {
			String ext = filename.substring(lastDot + 1);

			return ((ext.equalsIgnoreCase(JPG_EXT)) || (ext.equalsIgnoreCase(PTX_EXT))
					|| (ext.equalsIgnoreCase(PDF_EXT)) || (ext.equalsIgnoreCase(PNG_EXT)) || (ext
					.equalsIgnoreCase(PS_EXT)));
		}
		return false;
	}
}