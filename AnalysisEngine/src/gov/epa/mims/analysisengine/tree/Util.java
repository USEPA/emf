package gov.epa.mims.analysisengine.tree;

import java.awt.Color;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/***********************************************************************************************************************
 * utilities class
 * 
 * @version $Revision: 1.3 $
 * @author $author$
 **********************************************************************************************************************/
public class Util implements java.io.Serializable {
	/** Serial Version UID */
	static final long serialVersionUID = 1;

	/*******************************************************************************************************************
	 * compare two Lists
	 * 
	 * @param a
	 *            first list
	 * @param b
	 *            second list
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(List a, List b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.size() != b.size()) {
			rtrn = false;
		} else {
			for (int i = 0; i < a.size(); ++i) {
				if (!(a.get(i).equals(b.get(i)))) {
					rtrn = false;
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two HashMaps
	 * 
	 * @param a
	 *            first HashMap
	 * @param b
	 *            second HashMap
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(HashMap a, HashMap b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.size() != b.size()) {
			rtrn = false;
		} else {
			Set keys = a.keySet();
			Iterator iter = keys.iterator();

			while (iter.hasNext()) {
				Object key = iter.next();
				Object objA = a.get(key);

				if (!(b.containsKey(key))) {
					rtrn = false;
				} else {
					Object objB = b.get(key);

					if (!(objA.equals(objB))) {
						rtrn = false;
					}
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two DataSetInfo arrays
	 * 
	 * @param a
	 *            first DataSetInfo array
	 * @param b
	 *            second DataSetInfo array
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(DataSetInfo[] a, DataSetInfo[] b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.length != b.length) {
			rtrn = false;
		} else {
			for (int i = 0; i < a.length; ++i) {
				if (!a[i].equals(b[i])) {
					rtrn = false;
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two arrays of Colors
	 * 
	 * @param a
	 *            Color array
	 * @param b
	 *            Color array
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(Color[] a, Color[] b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.length != b.length) {
			rtrn = false;
		} else {
			for (int i = 0; i < a.length; ++i) {
				if (!a[i].equals(b[i])) {
					rtrn = false;
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two boolean arrays
	 * 
	 * @param a
	 *            boolean array
	 * @param b
	 *            boolean array
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(boolean[] a, boolean[] b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.length != b.length) {
			rtrn = false;
		} else {
			for (int i = 0; i < a.length; ++i) {
				if (!(a[i] == b[i])) {
					rtrn = false;
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two String arrays
	 * 
	 * @param a
	 *            String array
	 * @param b
	 *            String array
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(String[] a, String[] b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.length != b.length) {
			rtrn = false;
		} else {
			for (int i = 0; i < a.length; ++i) {
				if (!a[i].equals(b[i])) {
					rtrn = false;
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two 2-D int arrays
	 * 
	 * @param a
	 *            2-D int array
	 * @param b
	 *            2-D int array
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(int[][] a, int[][] b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.length != b.length) {
			rtrn = false;
		} else {
			for (int i = 0; i < a.length; ++i) {
				if (!(a[i].equals(b[i]))) {
					rtrn = false;
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two int arrays
	 * 
	 * @param a
	 *            int array
	 * @param b
	 *            int array
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(int[] a, int[] b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.length != b.length) {
			rtrn = false;
		} else {
			for (int i = 0; i < a.length; ++i) {
				if (a[i] != b[i]) {
					rtrn = false;
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two double arrays
	 * 
	 * @param a
	 *            double array
	 * @param b
	 *            double array
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(double[] a, double[] b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.length != b.length) {
			rtrn = false;
		} else {
			for (int i = 0; i < a.length; ++i) {
				if (a[i] != b[i]) {
					rtrn = false;
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two doubles
	 * 
	 * @param a
	 *            double
	 * @param b
	 *            double
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(double a, double b) {
		boolean rtrn = true;

		if (Double.isNaN(a) && Double.isNaN(b)) {
			rtrn = true;
		} else if (a == b) {
			rtrn = true;
		} else {
			rtrn = false;
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two Double arrays
	 * 
	 * @param a
	 *            Double array
	 * @param b
	 *            Double array
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(Double[] a, Double[] b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.length != b.length) {
			rtrn = false;
		} else {
			for (int i = 0; i < a.length; ++i) {
				if (!(a[i].equals(b[i]))) {
					rtrn = false;
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two Date arrays
	 * 
	 * @param a
	 *            Date array
	 * @param b
	 *            Date array
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(Date[] a, Date[] b) {
		boolean rtrn = true;

		if (a == b) {
			rtrn = true;
		} else if ((a == null) || (b == null)) {
			rtrn = false;
		} else if (a.length != b.length) {
			rtrn = false;
		} else {
			for (int i = 0; i < a.length; ++i) {
				if (!(a[i].equals(b[i]))) {
					rtrn = false;
				}
			}
		}

		return rtrn;
	}

	/*******************************************************************************************************************
	 * compare two Strings
	 * 
	 * @param a
	 *            String 1
	 * @param b
	 *            String 2
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(String a, String b) {
		return (a == null) ? (b == null) : (a.equals(b));
	}

	/*******************************************************************************************************************
	 * compare two Doubles
	 * 
	 * @param a
	 *            Double 1
	 * @param b
	 *            Double 2
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(Double a, Double b) {
		return (a == null) ? (b == null) : (a.equals(b));
	}

	/*******************************************************************************************************************
	 * compare two booleans
	 * 
	 * @param a
	 *            boolean 1
	 * @param b
	 *            boolean 2
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(boolean a, boolean b) {
		return (a == b);
	}

	/*******************************************************************************************************************
	 * compare two Colors
	 * 
	 * @param a
	 *            Color 1
	 * @param b
	 *            Color 2
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(Color a, Color b) {
		return (a == null) ? (b == null) : (a.equals(b));
	}

	/*******************************************************************************************************************
	 * compare two Dates
	 * 
	 * @param a
	 *            Date 1
	 * @param b
	 *            Date 2
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(Date a, Date b) {
		return (a == null) ? (b == null) : (a.equals(b));
	}

	/*******************************************************************************************************************
	 * compare two Longs
	 * 
	 * @param a
	 *            Long 1
	 * @param b
	 *            Long 2
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(Long a, Long b) {
		return (a == null) ? (b == null) : (a.equals(b));
	}

	/*******************************************************************************************************************
	 * compare two Integers
	 * 
	 * @param a
	 *            Integer 1
	 * @param b
	 *            Integer 2
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(Integer a, Integer b) {
		return (a == null) ? (b == null) : (a.equals(b));
	}

	/*******************************************************************************************************************
	 * compare two TimeZones
	 * 
	 * @param a
	 *            TimeZone 1
	 * @param b
	 *            TimeZone 2
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(TimeZone a, TimeZone b) {
		return (a == null) ? (b == null) : (a.equals(b));
	}

	/*******************************************************************************************************************
	 * compare two Texts
	 * 
	 * @param a
	 *            Text 1
	 * @param b
	 *            Text 2
	 * 
	 * @return true if a==b; else false
	 ******************************************************************************************************************/
	public static boolean equals(Text a, Text b) {
		return (a == null) ? (b == null) : (a.equals(b));
	}

	/*******************************************************************************************************************
	 * a generic toString Function
	 * 
	 * @param obj
	 *            Object to write to the String
	 * 
	 * @return String representation of obj
	 ******************************************************************************************************************/
	public static String toString(Object obj) {
		Class c1 = obj.getClass();
		String r = c1.getName();

		do {
			r += "[";

			Field[] fields = c1.getDeclaredFields();
			AccessibleObject.setAccessible(fields, true);

			for (int i = 0; i < fields.length; ++i) {
				Field f = fields[i];
				r += (f.getName() + "=");

				try {
					Object val = f.get(obj);
					r += ((val == null) ? "value is null" : val.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (i < (fields.length - 1)) {
					r += ", ";
				}
			}

			r += "]";
			c1 = c1.getSuperclass();
		} while (c1 != Object.class);

		return r;
	}

	/*******************************************************************************************************************
	 * DOCUMENT_ME
	 * 
	 * @param str
	 *            DOCUMENT_ME
	 * 
	 * @return DOCUMENT_ME
	 ******************************************************************************************************************/
	public static String protectNewLineChars(String str) {
		StringBuffer b = new StringBuffer();
		String[] tmp = str.split("\\n");

		for (int j = 0; j < tmp.length; j++) {
			b.append(tmp[j]);

			if (j < (tmp.length - 1)) {
				b.append("\\n");
			}
		}

		return b.toString();
	}
}