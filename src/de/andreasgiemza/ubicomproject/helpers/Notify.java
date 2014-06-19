package de.andreasgiemza.ubicomproject.helpers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import de.andreasgiemza.ubicomproject.MainActivity;
import de.andreasgiemza.ubicomproject.R;

public enum Notify {
	INSTANCE;

	public static final int DELAY_MIN = 30; // Delay between notifications

	final public Map<String, Date> repeat = new HashMap<>();

	public void notify(Context context, String name) {

		if (repeat.containsKey(name)) {
			if (difference(repeat.get(name), new Date()) >= DELAY_MIN)
				return;
		}
		repeat.put(name, new Date());

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(
						context.getResources().getText(
								R.string.notification_title))
				.setContentText(MainActivity.getContactName(context, name) + " is here.");

		Intent resultIntent = new Intent(context, MainActivity.class);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
				0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(resultPendingIntent);

		// Sets an ID for the notification
		int mNotificationId = 001;
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(mNotificationId, mBuilder.build());
	}

	/*
	 * returns difference in minutes
	 */
	private long difference(Date date, Date currentDate) {

		if (date.after(currentDate))
			return 0;

		long diff = currentDate.getTime() - date.getTime();

		return (diff * 1000 * 60);
	}

}
