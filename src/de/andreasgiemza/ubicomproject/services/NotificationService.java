package de.andreasgiemza.ubicomproject.services;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import de.andreasgiemza.ubicomproject.dataserver.DataServer;
import de.andreasgiemza.ubicomproject.dataserver.DataServer.UbiCom_Pos;

public class NotificationService extends Service {
	public static final String BROADCAST_ACTION = "NotificationService";
	public static final String BROADCAST_NUMBER = "LocationNumber";
	public static final String BROADCAST_LATITUDE = "LocationLatitude";
	public static final String BROADCAST_LONGITUDE = "LocationLongitude";
	public static final String BROADCAST_UPDATED = "LocationUpdated";

	private static final int PERIOD_MIN = 0;
	private static final int PERIOD_SEC = 20;

	Timer timer = null;

	// FtpServer mFtpServer = null;

	@Override
	public void onCreate() {
		super.onCreate();

		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				List<UbiCom_Pos> list = DataServer.INSTANCE
						.getPositions(getApplicationContext());

				for (UbiCom_Pos l : list) {
					Intent i = new Intent(BROADCAST_ACTION);
					i.putExtra(BROADCAST_NUMBER, l.number);
					i.putExtra(BROADCAST_LATITUDE,
							Double.parseDouble(l.latitude));
					i.putExtra(BROADCAST_LONGITUDE,
							Double.parseDouble(l.longitude));
					i.putExtra(BROADCAST_UPDATED, l.updated);
					LocalBroadcastManager.getInstance(getApplicationContext())
							.sendBroadcast(i);
				}
			}
		}, (long) 0, (long) ((PERIOD_MIN * 60) + PERIOD_SEC) * 1000);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
	}
}
