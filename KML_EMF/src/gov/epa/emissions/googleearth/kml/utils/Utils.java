package gov.epa.emissions.googleearth.kml.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	public static final int NUM_DIGITS = 4;
	public static final double BIG_VALUE = 1000000.0;
	public static final double SMALL_VALUE = 0.0001;
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(
			"0.0E0");
	public static final DecimalFormat ONE_TO_TEN_DECIMAL_FORMAT = new DecimalFormat(
			"0.0E0");
	public static final DecimalFormat POINT_ONE_TO_ONE_DECIMAL_FORMAT = new DecimalFormat(
	        "0.0E0");
	public static final DecimalFormat POINT_ZEROONE_TO_POINT_ONE_DECIMAL_FORMAT = new DecimalFormat(
	        "0.00E0");
	public static final DecimalFormat POINT_ZEROZEROONE_TO_ZEROONE_DECIMAL_FORMAT = new DecimalFormat(
	        "0.000E0");
	public static final DecimalFormat ZERO_TO_POINT_ZEROZEROONE_DECIMAL_FORMAT = new DecimalFormat(
	        "0.0000E0");

	public static String capitalize(String text) {

		StringBuilder retVal = new StringBuilder();

		String[] words = text.split(" ");

		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].substring(0, 1).toUpperCase()
					+ words[i].substring(1);
		}

		for (String string : words) {
			retVal.append(string).append(" ");
		}

		return retVal.toString().trim();
	}

	public static String stripExtension(String fileName) {

		String retVal = fileName;

		int indexOfDot = retVal.lastIndexOf(".");
		if (indexOfDot >= 0) {
			retVal = retVal.substring(0, indexOfDot);
		}

		return retVal;
	}

	public static double roundDigits(double d, int digits) {

		double retVal = 0;

		if (d != 0) {

			double temp = d;

			int count = 0;

			if (Math.abs(temp) < 1) {
				digits--;
			}

			while (Math.abs(temp) < 1) {

				temp *= 10;
				count++;
			}

			for (int i = 0; i < digits; i++) {
				temp *= 10;
			}

			temp = Math.round(temp);

			retVal = temp / Math.pow(10, count + digits);
		}

		return retVal;
	}

	public static double roundDecimalPaces(double d, int digits) {
		double pow = Math.pow(10, digits);
		return ((double) Math.round(d * pow)) / pow;
	}

	public static double ceilDecimalPaces(double d, int digits) {
		double pow = Math.pow(10, digits);
		return ((double) Math.ceil(d * pow)) / pow;
	}

	public static String generateStackTrace(Throwable throwable) {

		StringBuilder sb = new StringBuilder();
		sb.append(throwable).append("\n");
		StackTraceElement[] trace = throwable.getStackTrace();
		for (int i = 0; i < trace.length; i++)
			sb.append("\tat ").append(trace[i]).append("\n");

		Throwable ourCause = throwable.getCause();
		if (ourCause != null) {

			sb.append("Caused by: " + ourCause).append("\n");
			sb.append(generateStackTrace(ourCause));
		}

		return sb.toString();
	}

	public static DecimalFormat getFormat(double value) {
        // TODO: need to change this to finer grade
		DecimalFormat format = null;
		if (value >= 0 && value < 0.001) {
			format = ZERO_TO_POINT_ZEROZEROONE_DECIMAL_FORMAT;
		} else if (value >= 0.001 && value < 0.01){
			format = POINT_ZEROZEROONE_TO_ZEROONE_DECIMAL_FORMAT;
		} else if (value >= 0.01 && value < 0.1){
			format = POINT_ZEROONE_TO_POINT_ONE_DECIMAL_FORMAT;
		} else if (value >= 0.1 && value < 1) {
			format = POINT_ONE_TO_ONE_DECIMAL_FORMAT;
		} else if (value >= 1 && value < 10) {
			format = ONE_TO_TEN_DECIMAL_FORMAT;
		}else {
			format = DECIMAL_FORMAT;
		}

		return format;
	}

	public static String format(double d, DecimalFormat decimalFormat) {

		String retVal = "";
		
		//if (d == (int) d || d >= 10) {
		//	retVal = Long.toString((int) d);
		//} else if (d >= 10) {
		//	retVal = Long.toString(Math.round(d));
		//} else {
			retVal = decimalFormat.format(d);
		//}

		return retVal;
	}
	
	public static String format(double d, double range) {

		String retVal = "";
		
		DecimalFormat format = null;
		
		double r = range;
		if ( r<0){
			r  = 0-r;
		}
		int n = 0;
		while ( r<1 && n<NUM_DIGITS)
		{
			r *= 10;
			n++;
		}
		switch (n) {
			case 1:
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0.0");
				} else {
					format = new DecimalFormat("0.0E0");
				}
				break;
			case 2:
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0.00");
				} else {
					format = new DecimalFormat("0.00E0");
				}	
				break;
			case 3:
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0.000");
				} else {
					format = new DecimalFormat("0.000E0");
				}
				break;
			case 4:
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0.0000");
				} else {
					format = new DecimalFormat("0.0000E0");
				}
				break;
			case 5:
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0.00000");
				} else {
					format = new DecimalFormat("0.00000E0");
				}
				break;
			case 6:
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0.000000");
				} else {
					format = new DecimalFormat("0.000000E0");
				}
				break;
			case 7:
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0.0000000");
				} else {
					format = new DecimalFormat("0.0000000E0");
				}
				break;
			case 8:
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0.00000000");
				} else {
					format = new DecimalFormat("0.00000000E0");
				}
				break;
			case 9:
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0.000000000");
				} else {
					format = new DecimalFormat("0.000000000E0");
				}
				break;
			case 10: 
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0.0000000000");
				} else {
					format = new DecimalFormat("0.0000000000E0");
				}
				break;
			default:
				if ( (d<BIG_VALUE && d>SMALL_VALUE) || (d<0-SMALL_VALUE && d>0-BIG_VALUE)) {
					format = new DecimalFormat("######0");
				} else {
					format = new DecimalFormat("0E0");
				}
				break;
		}
			
		retVal = format.format(d);

		return retVal;
	}	

	public static String wrapLine(String line, int length) {

		StringBuilder sb = new StringBuilder();

		String[] words = line.split(" ");

		int currentLength = 0;
		for (String word : words) {

			if (currentLength >= length) {

				sb.append("\n");
				currentLength = 0;
			}

			sb.append(word).append(" ");
			currentLength += word.length() + 1;
		}

		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	public static boolean hasEvenNumberOfQuotes(String text) {

		int quoteCount = 0;
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '"') {
				quoteCount++;
			}
		}

		return quoteCount % 2 == 0;
	}

	public static String[] parseLine(String line, String delimiter) {

		if (line.startsWith(delimiter)) {
			line = " "+line;
		}
		if (line.endsWith(delimiter)) {
			line += " ";
		}
		
		String[] temp = line.split(delimiter);

		List<String> tokens = new ArrayList<String>();
		for (int i = 0; i < temp.length; i++) {

			if (temp[i].startsWith("\"") && !temp[i].endsWith("\"")) {

				StringBuilder sb = new StringBuilder();
				for (; i < temp.length; i++) {

					sb.append(temp[i].trim());

					if (temp[i].endsWith("\"")) {

						tokens.add(sb.toString());
						break;
					}

					sb.append(delimiter);
				}
			} else {
				tokens.add(temp[i].trim());
			}
		}

		return tokens.toArray(new String[0]);
	}

	public static void main(String[] args) {

		String capitalize = Utils.capitalize("this is a test");
		System.out.println(capitalize);

		try {
			new Double("this is a test");
		} catch (Exception e) {
			System.out.println(Utils.generateStackTrace(e));
		}
	}
}
