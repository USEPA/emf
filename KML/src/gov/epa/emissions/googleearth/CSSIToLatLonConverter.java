package gov.epa.emissions.googleearth;

import java.awt.geom.Point2D;

/**
 * module mod_cssi
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * ! This module contains routines to convert between lat - lon & CSSI's !
 * cartesian projection used in the FAA AERMOD simulations
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * implicit none
 * 
 * contains subroutine cssi2latlon(x, y, lat, lon, lat0d, lon0d)
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! !
 * Converts X & Y values from CSSI cartesian grid to lat lon !
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * implicit none ! Arguments REAL, intent(IN) :: lat0d, lon0d REAL, intent(IN)
 * :: x, y REAL, intent(OUT) :: lat, lon
 * 
 * ! Constants REAL, parameter :: rad2deg = 180/3.14159 REAL, parameter ::
 * deg2rad = 3.14159/180 REAL, parameter :: A = 6378137.0 REAL, parameter :: B =
 * 6356752.314
 * 
 * ! Cartesian conversion variables REAL :: r REAL :: e REAL :: lat0, lon0 REAL
 * :: latr, lonr INTEGER :: i
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! !
 * BEGIN CODE
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * ! Convert lat0 & lon0 to radians lat0 = lat0d * deg2rad lon0 = lon0d *
 * deg2rad
 * 
 * !********************************************************* ! Convert AERMOD
 * Local Cartesian Coordinates to Lat - Lon ! Lat & lon values in radians
 * !********************************************************* r = A**2 /
 * sqrt(A**2 * cos(lat0)**2 + B**2 * sin(lat0)**2) e = tan(lat0) / (2*r)
 * 
 * lonr = (r * lon0 * cos(lat0) + lon0 * ( e * x**2 - y) * sin(lat0) + x) / (r *
 * cos(lat0) + (e * x**2 - y) * sin(lat0)) latr = - (A**4 * e * x**2 - A**4 * y
 * - b**2 * r**3 * lat0) / (b**2 * r**3)
 * 
 * ! Convert back to degrees lat = latr * rad2deg lon = lonr * rad2deg
 * 
 * end subroutine cssi2latlon subroutine latlon2cssi end subroutine latlon2cssi
 * end module mod_cssi
 */
public class CSSIToLatLonConverter {

	private double centerLatRad;
	private double centerLonRad;
	private double cosCenterLat;
	private double cosCenterLatSqr;
	private double sinCenterLat;
	private double sinCenterLatSqr;
	private double radius;
	private double radiusCube;
	private double eccentricity;

	public static final double RAD2DEG = 180.0 / 3.14159;
	public static final double DEG2RAD = 3.14159 / 180.0;
	public static final double A_AXIS = 6378137.0;
	public static final double B_AXIS = 6356752.314;
	public static final double A_AXIS_SQR = A_AXIS * A_AXIS;
	public static final double A_AXIS_SQR_SQR = A_AXIS_SQR * A_AXIS_SQR;
	public static final double B_AXIS_SQR = B_AXIS * B_AXIS;

	public CSSIToLatLonConverter(double centerLatDeg, double centerLonDeg) {

		this.centerLatRad = centerLatDeg * DEG2RAD;
		this.centerLonRad = centerLonDeg * DEG2RAD;

		this.cosCenterLat = Math.cos(this.centerLatRad);
		this.cosCenterLatSqr = this.cosCenterLat * this.cosCenterLat;
		this.sinCenterLat = Math.sin(this.centerLatRad);
		this.sinCenterLatSqr = this.sinCenterLat * this.sinCenterLat;

		this.radius = A_AXIS_SQR
				/ Math.sqrt(A_AXIS_SQR * this.cosCenterLatSqr + B_AXIS_SQR
						* this.sinCenterLatSqr);
		this.radiusCube = this.radius * this.radius * this.radius;

		this.eccentricity = Math.tan(this.centerLatRad) / (2 * this.radius);
	}

	public Point2D convert(double x, double y) {

		double xSqr = x * x;

		double eXSqrY = this.eccentricity * xSqr - y;

		/*
		 * lonr = (r * lon0 * cos(lat0) + lon0 * ( e * x**2 - y) * sin(lat0) +
		 * x) / (r * cos(lat0) + (e * x**2 - y) * sin(lat0))
		 */
		double lonRad = (this.radius * this.centerLonRad * this.cosCenterLat
				+ this.centerLonRad * eXSqrY * this.sinCenterLat + x)
				/ (this.radius * this.cosCenterLat + eXSqrY * this.sinCenterLat);

		double bSqrRCube = B_AXIS_SQR * this.radiusCube;

		/*
		 * latr = - (A**4 * e * x**2 - A**4 * y - b**2 * r**3 * lat0) / (b**2 *
		 * r**3)
		 */
		double latRad = -(A_AXIS_SQR_SQR * eccentricity * xSqr - A_AXIS_SQR_SQR
				* y - bSqrRCube * this.centerLatRad)
				/ bSqrRCube;

		return new Point2D.Double(latRad * RAD2DEG, lonRad * RAD2DEG);
	}
}
