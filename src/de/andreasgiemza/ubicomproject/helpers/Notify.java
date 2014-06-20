package de.andreasgiemza.ubicomproject.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import de.andreasgiemza.ubicomproject.MainActivity;
import de.andreasgiemza.ubicomproject.R;
import de.andreasgiemza.ubicomproject.helpers.ApplicationData.Position;

public final class Notify {
	private Notify() {
	}

	public static void sendNotification(String msg, Position pos,
			Context context) {
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
}
