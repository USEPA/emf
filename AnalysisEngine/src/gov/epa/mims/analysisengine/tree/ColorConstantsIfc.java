package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

/***********************************************************************************************************************
 * a Constants interface
 * 
 * @author Tommy E. Cathey
 * @version $Id: ColorConstantsIfc.java,v 1.3 2007/01/09 23:06:15 parthee Exp $
 * 
 **********************************************************************************************************************/
public interface ColorConstantsIfc {

	/** Array of Predefined font */
	public static final Color[] DEFAULT_COLORS = {

	Color.black, Color.red, Color.blue, Color.green, Color.orange, Color.magenta, new Color(50, 150, 255),
			new Color(50, 150, 50)
	// Color.red,
	// Color.yellow,
	// Color.blue,
	// Color.cyan,
	// Color.green,
	// Color.magenta,
	// Color.orange,
	// Color.pink,
	// Color.lightGray,
	// Color.gray,
	// Color.black
	};
}