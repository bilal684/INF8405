package ca.polymtl.INF8405;

public class Position {
	
	private Location location;
	@SuppressWarnings("unused")
	private double accuracy;

	public double getLatitude(){
		return location.getLatitude();
	}
	
	public double getLongitude(){
		return location.getLongitude();
	}
}
