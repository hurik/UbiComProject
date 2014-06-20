package de.andreasgiemza.ubicomproject.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.andreasgiemza.ubicomproject.helpers.PositionsStorage;

public class GcmIntentService extends IntentService {

	public static final String BROADCAST_ACTION = "PositionUpdated";

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
				String[] data = extras.get("message").toString().split(";");

				if (data.length == 3) {
					// Save the new position
					PositionsStorage.INSTANCE.updatedPosition(data, getApplicationContext());

					// Inform the the main activity that there is a new position
					Intent i = new Intent(BROADCAST_ACTION);
					LocalBroadcastManager.getInstance(getApplicationContext())
							.sendBroadcast(i);
				}
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
}
