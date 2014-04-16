package gov.epa.emissions.googleearth.kml.utils;

import java.awt.Color;

public class KMLUtils {

	public static final String DOCUMENT_TAG = "Document";
	public static final String FOLDER_TAG = "Folder";
	public static final String KML_TAG = "kml";
	public static final String NAME_TAG = "name";
	public static final String DESCRIPTION_TAG = "description";
	public static final String SNIPPET_TAG = "Snippet";
	public static final String VISIBILITY_TAG = "visibility";
	public static final String OPEN_TAG = "open";
	public static final String STYLE_TAG = "Style";
	public static final String STYLE_MAP_TAG = "StyleMap";
	public static final String PLACEMARK_TAG = "Placemark";
	public static final String STYLE_URL_TAG = "styleUrl";
	public static final String LOOK_AT_TAG = "LookAt";
	public static final String LONGITUDE_TAG = "longitude";
	public static final String LATITUDE_TAG = "latitude";
	public static final String ALTITUDE_TAG = "altitude";
	public static final String RANGE_TAG = "range";
	public static final String TILT_TAG = "tilt";
	public static final String HEADING_TAG = "heading";
	public static final String POINT_TAG = "Point";
	public static final String ALTITUDE_MODE_TAG = "altitudeMode";
	public static final String COORDINATES_TAG = "coordinates";
	public static final String LIST_STYLE_TAG = "ListStyle";
	public static final String ITEM_ICON_TAG = "ItemIcon";
	public static final String HREF_TAG = "href";
	public static final String LABEL_STYLE_TAG = "LabelStyle";
	public static final String SCALE_TAG = "scale";
	public static final String COLOR_TAG = "color";
	public static final String ICON_STYLE_TAG = "IconStyle";
	public static final String ICON_TAG = "Icon";
	public static final String PAIR_TAG = "Pair";
	public static final String KEY_TAG = "key";
	public static final String SCREEN_OVERLAY_TAG = "ScreenOverlay";
	public static final String OVERLAY_XY_TAG = "overlayXY";
	public static final String SCREEN_XY_TAG = "screenXY";
	public static final String ROTATION_XY_TAG = "rotationXY";
	public static final String SIZE_TAG = "size";
	public static final String POLYSTYLE_TAG = "PolyStyle";
	public static final String LINESTYLE = "LineStyle";

	public static final String NORMAL_VALUE = "normal";
	public static final String HIGHLIGHT_VALUE = "highlight";
	public static final String RELATIVE_TO_GROUND_VALUE = "relativeToGround";

	private static String redHexStr = Integer.toHexString(Color.RED.getRGB());
	private static String ornHexStr = Integer
			.toHexString(Color.ORANGE.getRGB());
	private static String yelHexStr = Integer
			.toHexString(Color.YELLOW.getRGB());
	private static String grnHexStr = Integer.toHexString(Color.GREEN.getRGB());
	private static String cynHexStr = Integer.toHexString(Color.CYAN.getRGB());
	private static String bluHexStr = Integer.toHexString(Color.BLUE.getRGB());
	private static String blkHexStr = Integer.toHexString(Color.BLACK.getRGB());
	private static String gryHexStr = Integer.toHexString(Color.LIGHT_GRAY
			.getRGB());
	private static String whtHexStr = Integer.toHexString(Color.WHITE.getRGB());

	// NOTE: KML use aabbggrr order for rgb values, and 'aa' stands for
	// transparency: 00 fully transparent, ff fully opaque
	public static final String RED = redHexStr.substring(6)
			+ redHexStr.substring(4, 6) + redHexStr.substring(2, 4);
	public static final String ORANGE = ornHexStr.substring(6)
			+ ornHexStr.substring(4, 6) + ornHexStr.substring(2, 4);
	public static final String YELLOW = yelHexStr.substring(6)
			+ yelHexStr.substring(4, 6) + yelHexStr.substring(2, 4);
	public static final String GREEN = grnHexStr.substring(6)
			+ grnHexStr.substring(4, 6) + grnHexStr.substring(2, 4);
	public static final String CYAN = cynHexStr.substring(6)
			+ cynHexStr.substring(4, 6) + cynHexStr.substring(2, 4);
	public static final String BLUE = bluHexStr.substring(6)
			+ bluHexStr.substring(4, 6) + bluHexStr.substring(2, 4);
	public static final String BLACK = blkHexStr.substring(6)
			+ blkHexStr.substring(4, 6) + blkHexStr.substring(2, 4);
	public static final String GRAY = gryHexStr.substring(6)
			+ gryHexStr.substring(4, 6) + gryHexStr.substring(2, 4);
	public static final String WHITE = whtHexStr.substring(6)
			+ whtHexStr.substring(4, 6) + whtHexStr.substring(2, 4);

	public static final String INDENTATION = "    ";

	private static void pushIndent(StringBuilder indent) {
		indent.append(INDENTATION);
	}

	private static void popIndent(StringBuilder indent) {
		indent.delete(0, INDENTATION.length());
	}

	public static String createElement(String name, String value,
			StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(name).append(">\n");
		pushIndent(indent);
		sb.append(indent).append(value).append("\n");
		popIndent(indent);
		sb.append(indent).append("</").append(name).append(">\n");

		return sb.toString();
	}

	public static String createElement(String string, StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(string).append("/>\n");
		return sb.toString();
	}

	public static String createNameElement(String value, StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(NAME_TAG).append(">\n");
		pushIndent(indent);
		sb.append(indent).append(value).append("\n");
		popIndent(indent);
		sb.append(indent).append("</").append(NAME_TAG).append(">\n");

		return sb.toString();
	}

	public static String createDescriptionElement(String value,
			StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(DESCRIPTION_TAG).append(">\n");
		pushIndent(indent);
		sb.append(indent).append(value).append("\n");
		popIndent(indent);
		sb.append(indent).append("</").append(DESCRIPTION_TAG).append(">\n");

		return sb.toString();
	}

	public static String createVisibilityElement(String value,
			StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(VISIBILITY_TAG).append(">\n");
		pushIndent(indent);
		sb.append(indent).append(value).append("\n");
		popIndent(indent);
		sb.append(indent).append("</").append(VISIBILITY_TAG).append(">\n");

		return sb.toString();
	}

	public static String createOpenElement(String value, StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(OPEN_TAG).append(">\n");
		pushIndent(indent);
		sb.append(indent).append(value).append("\n");
		popIndent(indent);
		sb.append(indent).append("</").append(OPEN_TAG).append(">\n");

		return sb.toString();
	}

	public static String createStyleElement(String id, String color,
			int lineWidth, StringBuilder indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(STYLE_TAG + " id=\"" + id + "\"")
				.append(">\n");
		pushIndent(indent);
		sb.append(createLineStyle("ff000000", lineWidth, indent));
		sb.append(indent).append("<").append(POLYSTYLE_TAG).append(">\n");
		pushIndent(indent);
		sb.append(createColorElement(color, indent));
		popIndent(indent);
		sb.append(indent).append("</").append(POLYSTYLE_TAG).append(">\n");
		popIndent(indent);
		sb.append(indent).append("</").append(STYLE_TAG).append(">\n");

		return sb.toString();

	}

	public static String createStyleMapElement(String id, String url,
			StringBuilder indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<StyleMap id=\"" + id + "\">\n");
		pushIndent(indent);
		sb.append(indent).append("<Pair>\n");
		pushIndent(indent);
		sb.append(indent).append("<key>normal</key>\n");
		sb.append(indent).append("<styleUrl>#" + url + "</styleUrl>\n");
		popIndent(indent);
		sb.append(indent).append("</Pair>\n");
		sb.append(indent).append("<Pair>\n");
		pushIndent(indent);
		sb.append(indent).append("<key>highlight</key>\n");
		sb.append(indent).append("<styleUrl>#" + url + "</styleUrl>\n");
		popIndent(indent);
		sb.append(indent).append("</Pair>\n");
		popIndent(indent);
		sb.append(indent).append("</StyleMap>\n");

		return sb.toString();

	}

	public static String createColorElement(String color, StringBuilder indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<color>" + color + "</color>\n");

		return sb.toString();
	}

	public static String createWidthElement(int width, StringBuilder indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<width>" + width + "</width>\n");

		return sb.toString();
	}

	public static String createStyleUrlElement(String value,
			StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(STYLE_URL_TAG).append(">\n");
		pushIndent(indent);
		sb.append(indent).append("#").append(value).append("\n");
		popIndent(indent);
		sb.append(indent).append("</").append(STYLE_URL_TAG).append(">\n");

		return sb.toString();
	}

	public static String createLineStyle(String color, int width,
			StringBuilder indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<LineStyle>\n");
		pushIndent(indent);
		sb.append(createColorElement(color, indent));
		sb.append(createWidthElement(width, indent));
		popIndent(indent);
		sb.append(indent).append("</LineStyle>\n");

		return sb.toString();

	}

	public static String openElement(String name, StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(name).append(">\n");
		pushIndent(indent);

		return sb.toString();
	}

	public static String openDocumentElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(DOCUMENT_TAG).append(">\n");
		pushIndent(indent);

		return sb.toString();
	}

	public static String openFolderElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(FOLDER_TAG).append(">\n");
		pushIndent(indent);

		return sb.toString();
	}

	public static String opendFolderWithName(String name, String openNum,
			StringBuilder indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(FOLDER_TAG).append(">\n");
		pushIndent(indent);
		sb.append(indent).append("<name>" + name + "</name>\n");
		sb.append(indent).append("<open>" + openNum + "</open>\n");
		pushIndent(indent);

		return sb.toString();
	}

	public static String openPlacemarkElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(PLACEMARK_TAG).append(">\n");
		pushIndent(indent);

		return sb.toString();
	}

	public static String openDescriptionElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(DESCRIPTION_TAG).append(">\n");
		pushIndent(indent);

		return sb.toString();
	}

	public static String openScreenOverlayElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("<").append(SCREEN_OVERLAY_TAG).append(">\n");
		pushIndent(indent);

		return sb.toString();
	}

	public static String addValue(String value, StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(indent).append(value).append("\n");

		return sb.toString();
	}

	public static String closeElement(String name, StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		sb.append(indent).append("</").append(name).append(">\n");

		return sb.toString();
	}

	public static String closePlacemarkElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		sb.append(indent).append("</").append(PLACEMARK_TAG).append(">\n");

		return sb.toString();
	}

	public static String closeSnippetElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		sb.append(indent).append("</").append(SNIPPET_TAG).append(">\n");

		return sb.toString();
	}

	public static String closeFolderElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		sb.append(indent).append("</").append(FOLDER_TAG).append(">\n");

		return sb.toString();
	}

	public static String closeFolderWithName(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		popIndent(indent);
		sb.append(indent).append("</").append(FOLDER_TAG).append(">\n");

		return sb.toString();
	}

	public static String closeDocumentElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		sb.append(indent).append("</").append(DOCUMENT_TAG).append(">\n");

		return sb.toString();
	}

	public static String closeKMLElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		sb.append(indent).append("</").append(KML_TAG).append(">\n");

		return sb.toString();
	}

	public static String closeStyleElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		sb.append(indent).append("</").append(STYLE_TAG).append(">\n");

		return sb.toString();
	}

	public static String closeStyleMapElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		sb.append(indent).append("</").append(STYLE_MAP_TAG).append(">\n");

		return sb.toString();
	}

	public static String closeDescriptionElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		sb.append(indent).append("</").append(DESCRIPTION_TAG).append(">\n");

		return sb.toString();
	}

	public static String closeScreenOverlayElement(StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		popIndent(indent);
		sb.append(indent).append("</").append(SCREEN_OVERLAY_TAG).append(">\n");

		return sb.toString();
	}

	public static String createXMLElement() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	}

	public static String openKMLElement(StringBuilder indent) {
		return KMLUtils.openElement(
				"kml xmlns='http://earth.google.com/kml/2.1'", indent);
	}

	public static String openSnippetElement(int maxLines, StringBuilder indent) {
		return KMLUtils.openElement(SNIPPET_TAG + " maxLines='" + maxLines
				+ "'", indent);
	}

	public static String openStyleElement(String styleID, StringBuilder indent) {
		return KMLUtils
				.openElement(STYLE_TAG + " id='" + styleID + "'", indent);
	}

	public static String openStyleMapElement(String styleMapID,
			StringBuilder indent) {
		return KMLUtils.openElement(STYLE_MAP_TAG + " id='" + styleMapID + "'",
				indent);
	}

	public static String createOverlayXYElement(String x, String y,
			StringBuilder indent) {
		return KMLUtils.createElement(OVERLAY_XY_TAG + " x=\"" + x + "\" y=\""
				+ y + "\" xunits=\"fraction\" yunits=\"fraction\"", indent);
	}

	public static String createScreenXYElement(String x, String y,
			StringBuilder indent) {
		return KMLUtils.createElement(SCREEN_XY_TAG + " x=\"" + x + "\" y=\""
				+ y + "\" xunits=\"fraction\" yunits=\"fraction\"", indent);
	}

	public static String createRotationXYElement(String x, String y,
			StringBuilder indent) {
		return KMLUtils.createElement(ROTATION_XY_TAG + " x=\"" + x + "\" y=\""
				+ y + "\" xunits=\"fraction\" yunits=\"fraction\"", indent);
	}

	public static String createSizeElement(String x, String y,
			StringBuilder indent) {
		return KMLUtils.createElement(SIZE_TAG + " x=\"" + x + "\" y=\"" + y
				+ "\" xunits=\"fraction\" yunits=\"fraction\"", indent);
	}

	public static String createLookAt(String lonStr, String latStr, String alt,
			String range, String tilt, String heading, StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.openElement(LOOK_AT_TAG, indent));
		sb.append(KMLUtils.createElement(LONGITUDE_TAG, lonStr, indent));
		sb.append(KMLUtils.createElement(LATITUDE_TAG, latStr, indent));
		sb.append(KMLUtils.createElement(ALTITUDE_TAG, alt, indent));
		sb.append(KMLUtils.createElement(RANGE_TAG, range, indent));
		sb.append(KMLUtils.createElement(TILT_TAG, tilt, indent));
		sb.append(KMLUtils.createElement(HEADING_TAG, heading, indent));
		sb.append(KMLUtils.closeElement(LOOK_AT_TAG, indent));

		return sb.toString();
	}

	public static String createPoint(String lonStr, String latStr, String alt,
			StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.openElement(POINT_TAG, indent));
		sb.append(KMLUtils.createElement(ALTITUDE_MODE_TAG,
				RELATIVE_TO_GROUND_VALUE, indent));
		sb.append(KMLUtils.createElement(COORDINATES_TAG, lonStr + "," + latStr
				+ "," + alt, indent));
		sb.append(KMLUtils.closeElement(POINT_TAG, indent));

		return sb.toString();
	}

	public static String createListStyleElement(String imagePath,
			StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.openElement(LIST_STYLE_TAG, indent));
		sb.append(KMLUtils.openElement(ITEM_ICON_TAG, indent));
		sb.append(KMLUtils.createElement(HREF_TAG, imagePath, indent));
		sb.append(KMLUtils.closeElement(ITEM_ICON_TAG, indent));
		sb.append(KMLUtils.closeElement(LIST_STYLE_TAG, indent));

		return sb.toString();
	}

	public static String createLabelStyleElement(String scale, String color,
			StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.openElement(LABEL_STYLE_TAG, indent));
		sb.append(KMLUtils.createElement(SCALE_TAG, scale, indent));
		sb.append(KMLUtils.createElement(COLOR_TAG, color, indent));
		sb.append(KMLUtils.closeElement(LABEL_STYLE_TAG, indent));

		return sb.toString();
	}

	public static String createIconStyleElement(String scale, String imagePath,
			StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.openElement(ICON_STYLE_TAG, indent));
		sb.append(KMLUtils.createElement(SCALE_TAG, scale, indent));
		sb.append(KMLUtils.createIconElement(imagePath, indent));
		sb.append(KMLUtils.closeElement(ICON_STYLE_TAG, indent));

		return sb.toString();
	}

	public static String createIconElement(String imagePath,
			StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.openElement(ICON_TAG, indent));
		sb.append(KMLUtils.createElement(HREF_TAG, imagePath, indent));
		sb.append(KMLUtils.closeElement(ICON_TAG, indent));

		return sb.toString();
	}

	private static String createPairElement(String key, String styleURL,
			StringBuilder indent) {

		StringBuilder sb = new StringBuilder();
		sb.append(KMLUtils.openElement(PAIR_TAG, indent));
		sb.append(KMLUtils.createElement(KEY_TAG, key, indent));
		sb
				.append(KMLUtils.createElement(STYLE_URL_TAG, "#" + styleURL,
						indent));
		sb.append(KMLUtils.closeElement(PAIR_TAG, indent));

		return sb.toString();
	}

	public static String createNormalPairElement(String styleURL,
			StringBuilder indent) {
		return createPairElement(NORMAL_VALUE, styleURL, indent);
	}

	public static String createHighlightPairElement(String styleURL,
			StringBuilder indent) {
		return createPairElement(HIGHLIGHT_VALUE, styleURL, indent);
	}

	public static String createPlaceMark(String colorMap, String grid,
			String date, String time, boolean clampToGround,
			StringBuilder indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append(
				"<Placemark><styleUrl>" + colorMap + "</styleUrl>\n");
		pushIndent(indent);
		sb.append(indent).append("<Polygon>\n");
		pushIndent(indent);
		sb.append(indent).append("<outerBoundaryIs>\n");
		pushIndent(indent);
		sb.append(indent).append("<LinearRing>\n");
		pushIndent(indent);
		sb.append(indent).append("<coordinates>" + grid + "</coordinates>\n");
		popIndent(indent);
		sb.append(indent).append("</LinearRing>\n");
		popIndent(indent);
		sb.append(indent).append("</outerBoundaryIs>\n");

		if (clampToGround)
			sb.append(indent).append(
					"<altitudeMode>clampToGround</altitudeMode>\n");

		popIndent(indent);
		sb.append(indent).append("</Polygon>\n");
		sb.append(indent).append(
				"<TimeStamp><when>" + date + "T" + time
						+ "Z</when></TimeStamp>\n");
		popIndent(indent);
		sb.append(indent).append("</Placemark>\n");

		return sb.toString();
	}

	public static String createPlaceMark(String colorMap, String grid,
			StringBuilder indent) {
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append(
				"<Placemark><styleUrl>" + colorMap + "</styleUrl>\n");
		pushIndent(indent);
		sb.append(indent).append("<Polygon>\n");
		pushIndent(indent);
		sb.append(indent).append("<outerBoundaryIs>\n");
		pushIndent(indent);
		sb.append(indent).append("<LinearRing>\n");
		pushIndent(indent);
		sb.append(indent).append("<coordinates>" + grid + "</coordinates>\n");
		popIndent(indent);
		sb.append(indent).append("</LinearRing>\n");
		popIndent(indent);
		sb.append(indent).append("</outerBoundaryIs>\n");
		sb.append(indent)
				.append("<altitudeMode>clampToGround</altitudeMode>\n");
		popIndent(indent);
		sb.append(indent).append("</Polygon>\n");
		popIndent(indent);
		sb.append(indent).append("</Placemark>\n");

		return sb.toString();
	}
}
