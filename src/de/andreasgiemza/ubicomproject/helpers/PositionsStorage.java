package de.andreasgiemza.ubicomproject.helpers;

import java.util.HashMap;
import java.util.Map;

import android.location.Location;

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
	}
}
