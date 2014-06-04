package de.andreasgiemza.ubicomproject.services;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import de.andreasgiemza.ubicomproject.FtpServer;
import de.andreasgiemza.ubicomproject.FtpServer.UbiCom_Pos;

public class NotificationService extends Service {

	private static final String TAG = "NotificationService";

	public static final String BROADCAST_ACTION = "FTP_POSITION_RECEIVED";
	public static final String BROADCAST_LATITUDE = "LocationLatitude";
	public static final String BROADCAST_LONGITUDE = "LocationLongitude";

	private static final int PERIOD_MIN = 0;
	private static final int PERIOD_SEC = 20;
	Timer timer = null;
	FtpServer mFtpServer = null;

	@Override
	public void onCreate() {

		Log.d(TAG, "NotificationTask started");

		mFtpServer = new FtpServer(getApplicationContext());

		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				// TODO check mftp == null
				if (mFtpServer == null) {
					Log.e(TAG, "mFtpServer = null");
					return;
				}

				Log.d(TAG, "mFtpServer.read()");
				List<UbiCom_Pos> list = mFtpServer.read();

				if (list != null)
					for (UbiCom_Pos l : list) {
						Intent i = new Intent(BROADCAST_ACTION);
						i.putExtra(BROADCAST_LATITUDE, l.Latitude);
						i.putExtra(BROADCAST_LONGITUDE, l.Longtitude);
						LocalBroadcastManager.getInstance(
								getApplicationContext()).sendBroadcast(i);
					}
			}
		}, (long) 0, (long) ((PERIOD_MIN * 60) + PERIOD_SEC) * 1000);

		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {

		timer.cancel();
		mFtpServer = null;

		Log.d(TAG, "NoftificationService deleted");

		super.onDestroy();
	}

}
