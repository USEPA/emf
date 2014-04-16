package gov.epa.emissions.googleearth.kml.record;

public interface StreetRecord {

	int getColumnCount();

	String getValue(int index);

	void setValue(int index, String value);

	String getKey(int index);

	String getDisplayKey(int index);

	int getIndex(String key);
	
	String getPlaceMarkGeometryXml();
		
	String toStringKeys();
}
