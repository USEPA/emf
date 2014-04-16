package gov.epa.emissions.googleearth.kml.generator;

public enum OverlayPosition {

	TOP_LEFT("Top-left", 0, 1, 0, 1), //
	TOP_RIGHT("Top-right", 1, 1, 1, 1), //
	BOTTOM_LEFT("Bottom-left", 0, -1, 0, 0), //
	BOTTOM_RIGHT("Bottom-right", 1, -1, 1, 0);//

	private String displayName;
	private double overlayX;
	private double overlayY;
	private double screenX;
	private double screenY;

	private OverlayPosition(String displayName, double ox, double oy,
			double sx, double sy) {

		this.displayName = displayName;
		this.overlayX = ox;
		this.overlayY = oy;
		this.screenX = sx;
		this.screenY = sy;
	}

	public double getOverlayX() {
		return overlayX;
	}

	public void setOverlayX(double overlayX) {
		this.overlayX = overlayX;
	}

	public double getOverlayY() {
		return overlayY;
	}

	public void setOverlayY(double overlayY) {
		this.overlayY = overlayY;
	}

	public double getScreenX() {
		return screenX;
	}

	public void setScreenX(double screenX) {
		this.screenX = screenX;
	}

	public double getScreenY() {
		return screenY;
	}

	public void setScreenY(double screenY) {
		this.screenY = screenY;
	}

	@Override
	public String toString() {
		return this.displayName;
	}

	public static OverlayPosition getByDisplayName(String displayName) {

		OverlayPosition retVal = null;

		if (TOP_LEFT.displayName.equals(displayName)) {
			retVal = TOP_LEFT;
		} else if (TOP_RIGHT.displayName.equals(displayName)) {
			retVal = TOP_RIGHT;
		} else if (BOTTOM_LEFT.displayName.equals(displayName)) {
			retVal = BOTTOM_LEFT;
		} else if (BOTTOM_RIGHT.displayName.equals(displayName)) {
			retVal = BOTTOM_RIGHT;
		}

		return retVal;
	}
}
