package de.andreasgiemza.ubicomproject.helpers;

import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.maps.model.LatLng;

public enum PositionsStorage {
	INSTANCE;

	final public Map<String, Position> positions = new HashMap<>();

	public void updatedPosition(String[] data) {
		positions.put(data[0], new Position(data[1], data[2]));
	}

	public class Position {
		public final LatLng latLng;
		public final long time = System.currentTimeMillis();

		public Position(String latitude, String longitude) {
			latLng = new LatLng(Double.parseDouble(latitude),
					Double.parseDouble(longitude));
		}
	}
}
