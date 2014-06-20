package de.andreasgiemza.ubicomproject.helpers;

import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import de.andreasgiemza.ubicomproject.MainActivity;
import de.andreasgiemza.ubicomproject.R;

public enum PositionsStorage {
	INSTANCE;

	final public Map<String, Position> positions = new HashMap<>();
	public Position myPosition;

	final public Map<String, NotificationData> notify = new HashMap<>();

	public void updatedPosition(String[] data, Context context) {
		String number = data[0];
		Position pos = new Position(data[1], data[2]);
		positions.put(number, pos);

		// Check if distance was calculated
		if (pos.distance != Integer.MAX_VALUE) {
			// Check if notification data was saved
			NotificationData last = notify.get(number);

			// No notify data is saved
			if (last == null) {
				// Save current status and return
				notify.put(number, new NotificationData(
						pos.distance < Preferences.MAX_DISTANCE, 0));
				return;
			}

			// Check distance
			if (pos.distance < Preferences.MAX_DISTANCE) {
				// Check if old near was false, else do nothing
				if (last.near == false) {
					// Check if last notification is MIN_TIME ago
					if (System.currentTimeMillis() - last.lastNotification > Preferences.MIN_TIME) {
						sendNotification(
								Phonebook.INSTANCE.getContactName(context, number),
								pos, context);

						notify.put(
								number,
								new NotificationData(true, System
										.currentTimeMillis()));
					} else {
						notify.put(number, new NotificationData(true,
								last.lastNotification));
					}
				}
			} else {
				if (last.near == true) {
					notify.put(number, new NotificationData(false,
							last.lastNotification));
				}
			}
		}
	}

	public void updateMyPosition(Location location) {
		myPosition = new Position(location);
	}

	private void sendNotification(String msg, Position pos, Context context) {
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent zoom = new Intent(context, MainActivity.class);
		zoom.putExtra("Latitude", pos.location.getLatitude());
		zoom.putExtra("Longitude", pos.location.getLongitude());

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				zoom, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(
						context.getResources().getString(
								R.string.notification_title))
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true)
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(1, mBuilder.build());
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
