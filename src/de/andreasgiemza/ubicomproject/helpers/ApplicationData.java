package de.andreasgiemza.ubicomproject.helpers;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.location.Location;

public class ApplicationData extends Application {

	final public Map<String, Position> positions = new HashMap<>();
	public Position myPosition;

	final public Map<String, NotificationData> notificationData = new HashMap<>();

	public void updatedPosition(String[] data, Context context) {
		String number = data[0];
		Position pos = new Position(data[1], data[2]);
		positions.put(number, pos);

		// Check if distance was calculated
		if (pos.distance != Integer.MAX_VALUE) {
			// Check if notification data was saved
			NotificationData last = notificationData.get(number);

			// No notify data is saved
			if (last == null) {
				// Save current status and return
				notificationData.put(number, new NotificationData(
						pos.distance < Preferences.MAX_DISTANCE, 0));
				return;
			}

			// Check distance
			if (pos.distance < Preferences.MAX_DISTANCE) {
				// Check if old near was false, else do nothing
				if (last.near == false) {
					// Check if last notification is MIN_TIME ago
					if (System.currentTimeMillis() - last.lastNotification > Preferences.MIN_TIME) {
						Notify.sendNotification(
								Phonebook.getContactName(context, number), pos,
								context);

						notificationData.put(number, new NotificationData(true,
								System.currentTimeMillis()));
					} else {
						notificationData.put(number, new NotificationData(true,
								last.lastNotification));
					}
				}
			} else {
				if (last.near == true) {
					notificationData.put(number, new NotificationData(false,
							last.lastNotification));
				}
			}
		}
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

	public class NotificationData {
		public final boolean near;
		public final long lastNotification;

		public NotificationData(boolean near, long lastNotification) {
			this.near = near;
			this.lastNotification = lastNotification;
		}
	}
}
