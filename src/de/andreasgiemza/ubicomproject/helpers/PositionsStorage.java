package de.andreasgiemza.ubicomproject.helpers;

import java.util.HashMap;
import java.util.Map;

import android.location.Location;

public enum PositionsStorage {
	INSTANCE;

	public static final int MAX_DISTANCE = 100;

	final public Map<String, Position> positions = new HashMap<>();
	public Position myPosition;

	public void updatedPosition(String[] data) {
		positions.put(data[0], new Position(data[1], data[2]));
	}

	public void updateMyPosition(Location location) {
		myPosition = new Position(location);
	}

	public class Position {
		public final Location location;
		public final long time = System.currentTimeMillis();
		public final int distance;

		public Position(String latitude, String longitude) {
			location = new Location("");
			location.setLatitude(Double.parseDouble(latitude));
			location.setLongitude(Double.parseDouble(longitude));

			if (myPosition != null)
				distance = (int) location.distanceTo(myPosition.location);
			else
				distance = Integer.MAX_VALUE;
		}

		public Position(Location location) {
			this.location = location;

			if (myPosition != null)
				distance = (int) location.distanceTo(myPosition.location);
			else
				distance = Integer.MAX_VALUE;
		}
	}
}
