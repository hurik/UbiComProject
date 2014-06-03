package de.andreasgiemza.ubicomproject.services;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import de.andreasgiemza.ubicomproject.FtpServer;

public class NotificationService extends Service {

	private static final String TAG = "NotificationService";
	
	private static final int PERIOD_MIN = 2;
	private static final int PERIOD_SEC = 0;
	Timer timer = null;
	FtpServer mFtpServer = null;

	@Override
	public void onCreate() {

		Log.d(TAG,	"NotificationTask started");
		
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
				mFtpServer.read();
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
