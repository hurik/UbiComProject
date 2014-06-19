package de.andreasgiemza.ubicomproject.helpers;

import java.util.HashMap;
import java.util.Map;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public enum PositionsStorage {
	INSTANCE;

	final public Map<String, Position> positions = new HashMap<>();
	public Position myPosition;

	public void updatedPosition(String[] data) {
		positions.put(data[0], new Position(data[1], data[2]));
	}

	public void updateMyPosition(Location location) {
		myPosition = new Position(location);
	}

	public class Position {
		public final LatLng latLng;
		public final long time = System.currentTimeMillis();

		public Position(String latitude, String longitude) {
			latLng = new LatLng(Double.parseDouble(latitude),
					Double.parseDouble(longitude));
		}

		public Position(Location location) {
			latLng = new LatLng(location.getLatitude(), location.getLongitude());
		}
		
		/*
		 * in meters
		 */
		public double getDistance(Position pos) {
			// Get numeric values out of form elements.
			double lat_1 = this.latLng.latitude; 
			double lon_1 = this.latLng.longitude;
			
			double lat_2 = pos.latLng.latitude;
			double lon_2 = pos.latLng.longitude;
			
			// Compute spherical coordinates
			double rho = 3958.75 * 1.609344; // earth diameter kilometers
			// convert latitude and longitude to spherical coordinates in radians
			// phi = 90 - latitude
			double phi_1 = (90.0 - lat_1)*Math.PI/180.0;
			double phi_2 = (90.0 - lat_2)*Math.PI/180.0;
			// theta = longitude
			double theta_1 = lon_1*Math.PI/180.0;
			double theta_2 = lon_2*Math.PI/180.0;
			// compute spherical distance from spherical coordinates
			// arc length = \arccos(\sin\phi\sin\phi'\cos(\theta-\theta') + \cos\phi\cos\phi')
			// distance = rho times arc length
			double d = rho*Math.acos( Math.sin(phi_1)*Math.sin(phi_2)*Math.cos(theta_1 - theta_2) + Math.cos(phi_1)*Math.cos(phi_2) );
			// Display result in miles and in kilometers
			return d*1000;
		}
	}
}
