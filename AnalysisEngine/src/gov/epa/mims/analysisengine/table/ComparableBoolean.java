package gov.epa.mims.analysisengine.table;

public final class ComparableBoolean implements Comparable {
	
	private Boolean value;

	public ComparableBoolean(Boolean boolValue) {
		value = boolValue;
	}

	public boolean getBooleanValue() {
		return value.booleanValue();
	}

	public int compareTo(Object o) {
		if (o == null || !(o instanceof ComparableBoolean)) {
			throw new IllegalArgumentException("The object is null or not intance of Boolean");
		}
		boolean other = ((ComparableBoolean) o).getBooleanValue();
		boolean my = value.booleanValue();
		if ((my && other) || (!my && !other)) {
			return 0;
		} else if (my && !other) {
			return 1;
		} else // if(!my && other)
		{
			return -1;
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof ComparableBoolean) {
			boolean my = value.booleanValue();
			boolean other = ((ComparableBoolean) obj).getBooleanValue();

			return ((my && other) || (!my && !other)) ? true : false;
		}
		return false;
	}

	public int hashCode() {
		return value.hashCode();
	}

	public String toString() {
		return value.toString();
	}

}
