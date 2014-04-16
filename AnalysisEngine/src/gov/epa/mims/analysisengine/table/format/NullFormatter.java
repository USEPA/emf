package gov.epa.mims.analysisengine.table.format;

import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * <p>
 * Description: A formatter that does nothing but call toString(). This acts as a pass-through in the FilterCriteria
 * when filtering with String based criteria.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: UNC - CEP
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: NullFormatter.java,v 1.1 2006/11/01 15:33:38 parthee Exp $
 */
public class NullFormatter extends Format {

	public NullFormatter() {
		// Empty
	}

	/**
	 * Formats an object and appends the resulting text to a given string buffer.
	 * 
	 * @param obj
	 * @param toAppendTo
	 * @param pos
	 * @return
	 */
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (obj != null)
			toAppendTo.append(obj.toString());
		return toAppendTo;
	} // format()

	/**
	 * Formats an Object producing an AttributedCharacterIterator.
	 * 
	 * @param obj
	 * @return
	 */
	public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
		throw new IllegalArgumentException("Method formatToCharacterIterator() " + "has not been implemented in "
				+ getClass().toString());
	} // formatToCharacterIterator()

	/**
	 * Parses text from the beginning of the given string to produce an object.
	 * 
	 * @param source
	 * @return
	 */
	public Object parseObject(String source) {
		return source;
	} // parseObject()

	/**
	 * Parses text from a string to produce an object.
	 * 
	 * @param source
	 * @param pos
	 * @return
	 */
	public Object parseObject(String source, ParsePosition pos) {
		return source.substring(pos.getIndex());
	} // parseObject()
} // class NullFormatter

