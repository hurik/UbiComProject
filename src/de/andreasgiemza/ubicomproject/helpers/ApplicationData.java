package de.andreasgiemza.ubicomproject.helpers;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.location.Location;

public class ApplicationData extends Application {

	final public Map<String, Position> positions = new HashMap<>();
	private Location myLocation;

	public void updatedPosition(String[] data, Context context) {
		String number = data[0];
		Position pos = new Position(number, data[1], data[2],
				positions.get(number), context);
		positions.put(number, pos);
	}

	public void updateMyPosition(Location location) {
		myLocation = location;
	}

	public class Position {
		public final Location location;
		public final long time;
		public final int distance;
		// For notification
		public final long lastNotification;

		public Position(String number, String latitude, String longitude,
				Position oldPosition, Context context) {
			// Set location
			location = new Location(number);
			location.setLatitude(Double.parseDouble(latitude));
			location.setLongitude(Double.parseDouble(longitude));
			// Set time with current time
			time = System.currentTimeMillis();
			// Get distance, if my location was set ...
			if (myLocation != null)
				distance = (int) location.distanceTo(myLocation);
			else
				distance = Integer.MAX_VALUE;
			// Set lastNotification time
			if (oldPosition == null) {
				// Set last notification time to zero, because there was never a
				// notification
				lastNotification = 0;
			} else {
				// If a friend enters the 250 m circle and the last notification
				// was more than 20 minutes ago, send a notification
				if (distance <= Preferences.MAX_DISTANCE
						&& oldPosition.distance > Preferences.MAX_DISTANCE
						&& System.currentTimeMillis()
								- oldPosition.lastNotification > Preferences.MIN_TIME) {
					Notify.sendNotification(
							Phonebook.getContactName(context, number), this,
							context);
					lastNotification = System.currentTimeMillis();
				} else {
					// Nothing important happened, take on the old value
					lastNotification = oldPosition.lastNotification;
				}
			}
		}
	}
}
