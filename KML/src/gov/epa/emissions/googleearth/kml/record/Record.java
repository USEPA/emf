package gov.epa.emissions.googleearth.kml.record;

public interface Record {

	int getColumnCount();

	String getValue(int index);

	void setValue(int index, String value);

	String getKey(int index);

	String getDisplayKey(int index);

	int getIndex(String key);

	String getHorizontal();

	String getVertical();
		
	String toStringKeys();
}
