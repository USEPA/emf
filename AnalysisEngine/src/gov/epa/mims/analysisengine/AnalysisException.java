package gov.epa.mims.analysisengine;

import java.lang.RuntimeException;
import java.lang.String;

/**
 * exception to be thrown by Analysis Engine objects
 * 
 * @author Tommy E. Cathey
 * @version $Id: AnalysisException.java,v 1.3 2006/10/30 21:43:51 parthee Exp $
 * 
 */
public class AnalysisException extends RuntimeException {
	/*******************************************************************************************************************
	 * 
	 * methods
	 * 
	 ******************************************************************************************************************/
	public AnalysisException() {
		// Empty
	}

	/*******************************************************************************************************************
	 * Creates a new AnalysisException object.
	 * 
	 * @param gripe
	 *            DOCUMENT_ME
	 ******************************************************************************************************************/
	public AnalysisException(String gripe) {
		super(gripe);
	}
}