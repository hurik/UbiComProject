package de.andreasgiemza.ubicomproject.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

	public static final String BROADCAST_ACTION = "NotificationService";
	public static final String BROADCAST_NUMBER = "LocationNumber";
	public static final String BROADCAST_LATITUDE = "LocationLatitude";
	public static final String BROADCAST_LONGITUDE = "LocationLongitude";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				String[] dfsdf = extras.get("message").toString().split(";");

				Intent i = new Intent(BROADCAST_ACTION);
				i.putExtra(BROADCAST_NUMBER, dfsdf[0]);
				i.putExtra(BROADCAST_LATITUDE, Double.parseDouble(dfsdf[1]));
				i.putExtra(BROADCAST_LONGITUDE, Double.parseDouble(dfsdf[2]));
				LocalBroadcastManager.getInstance(getApplicationContext())
						.sendBroadcast(i);
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
}
