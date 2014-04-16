package gov.epa.emissions.googleearth.kml.record;

public interface StreetRecordHeader {
	String [] getHeaders();
	String toStringHeader();
	int getColCount();
	int getLinkIDIndex();
	int getLinkBufferIndex();
	int getLinkNameIndex();
	int getPM25TotalIndex();
	int getEC25TotalIndex();
	int getOC25TotalIndex();
	int getCOTotalIndex();
	int getNOXTotalIndex();
	int getBENZTotalIndex();
	int getPlacemarkXmlIndex();
	boolean isInt( int index);
	boolean isDouble( int index);
}
