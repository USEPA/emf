package gov.epa.emissions.googleearth.kml.record;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;

public class StreetRecordHeaderImpl implements StreetRecordHeader{

	private static final String COL_ROAD_LINK_ID = "road_link_id";
	private static final String COL_ROAD_LINK_BUFFER = "buffer";
	private static final String COL_ROAD_LINK_NAME = "name";
	private static final String COL_PM25_TOTAL = "pm25_total";
	private static final String COL_EC25_TOTAL = "ec25_total";
	private static final String COL_OC25_TOTAL = "oc25_total";
	private static final String COL_CO_TOTAL = "co_total";
	private static final String COL_NOX_TOTAL = "nox_total";
	private static final String COL_BENZ_TOTAL = "benz_total";
	private static final String COL_PLACEMARK_XML = "placemark_geometry_kml";
	
	private int INX_ROAD_LINK_ID = -1;
	private int INX_ROAD_LINK_BUFFER = -1;
	private int INX_ROAD_LINK_NAME = -1;
	private int INX_PM25_TOTAL = -1;
	private int INX_EC25_TOTAL = -1;
	private int INX_OC25_TOTAL = -1;
	private int INX_CO_TOTAL = -1;
	private int INX_NOX_TOTAL = -1;
	private int INX_BENZ_TOTAL = -1;
	private int INX_PLACEMARK_XML = -1;
	
	private String[] headers;
	
	public StreetRecordHeaderImpl(String[] headers) throws KMZGeneratorException {
		this.headers = headers;
		this.calcColumnIndices(this.headers);
	}
	
	private void calcColumnIndices(String[] headers) throws KMZGeneratorException{
		if ( headers == null || headers.length == 0) {
			throw new KMZGeneratorException(KMZGeneratorException.ERROR_CODE_NULL_INPUT,"Street record column headers are empty!");
		}
		for (int i = 0; i<headers.length; i++) {
			String col = headers[i];
			if (COL_ROAD_LINK_ID.equalsIgnoreCase(col.trim())) {
				INX_ROAD_LINK_ID = i;
			} else if (COL_ROAD_LINK_BUFFER.equalsIgnoreCase(col.trim())) {
				INX_ROAD_LINK_BUFFER = i;
			} else if (COL_ROAD_LINK_NAME.equalsIgnoreCase(col.trim())) {
				INX_ROAD_LINK_NAME = i;
			} else if (COL_PM25_TOTAL.equalsIgnoreCase(col.trim())) {
				INX_PM25_TOTAL = i;
			} else if (COL_EC25_TOTAL.equalsIgnoreCase(col.trim())) {
				INX_EC25_TOTAL = i;
			} else if (COL_OC25_TOTAL.equalsIgnoreCase(col.trim())) {
				INX_OC25_TOTAL = i;
			} else if (COL_NOX_TOTAL.equalsIgnoreCase(col.trim())) {
				INX_NOX_TOTAL = i;
			} else if (COL_CO_TOTAL.equalsIgnoreCase(col.trim())) {
				INX_CO_TOTAL = i;
			} else if (COL_BENZ_TOTAL.equalsIgnoreCase(col.trim())) {
				INX_BENZ_TOTAL = i;
			} else if (COL_PLACEMARK_XML.equalsIgnoreCase(col.trim())) {
				INX_PLACEMARK_XML = i;
			}
		}
	}

	@Override
	public int getLinkIDIndex() {
		return this.INX_ROAD_LINK_ID;
	}

	@Override
	public int getLinkBufferIndex() {
		return this.INX_ROAD_LINK_BUFFER;
	}

	@Override
	public int getLinkNameIndex() {
		return this.INX_ROAD_LINK_NAME;
	}

	@Override
	public int getPM25TotalIndex() {
		return this.INX_PM25_TOTAL;
	}

	@Override
	public int getEC25TotalIndex() {
		return this.INX_EC25_TOTAL;
	}

	@Override
	public int getOC25TotalIndex() {
		return this.INX_OC25_TOTAL;
	}

	@Override
	public int getCOTotalIndex() {
		return this.INX_CO_TOTAL;
	}

	@Override
	public int getNOXTotalIndex() {
		return this.INX_NOX_TOTAL;
	}

	@Override
	public int getBENZTotalIndex() {
		return this.INX_BENZ_TOTAL;
	}

	@Override
	public int getPlacemarkXmlIndex() {
		return this.INX_PLACEMARK_XML;
	}

	@Override
	public String[] getHeaders() {
		// TODO Auto-generated method stub
		return this.headers;
	}

	@Override
	public String toStringHeader() {
		String str = "";
		for (String col : headers) {
			str += col + ", ";
		}
		str = str.substring(0,str.length()-1);
		return str;
	}

	@Override
	public int getColCount() {
		return this.headers.length;
	}

	@Override
	public boolean isInt(int index) {
		return index >= 0 && index < this.getColCount() && ( index == this.INX_ROAD_LINK_ID || index == this.INX_ROAD_LINK_BUFFER); 
	}

	@Override
	public boolean isDouble(int index) {
		return index >= 0 && index < this.getColCount() && ( index == this.INX_BENZ_TOTAL || index == this.INX_CO_TOTAL || index == this.INX_EC25_TOTAL || index == this.INX_NOX_TOTAL || index == this.INX_OC25_TOTAL || index == this.INX_PM25_TOTAL); 
	}
	
}
