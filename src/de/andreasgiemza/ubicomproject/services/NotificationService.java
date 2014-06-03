package de.andreasgiemza.ubicomproject.services;

import java.util.Timer;
import java.util.TimerTask;

import de.andreasgiemza.ubicomproject.FtpServer;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationService extends Service{

	private static final int PERIOD_MIN = 5;
	private static final int PERIOD_SEC = 0;
	Timer timer = null;
	FtpServer mFtpServer = null;
	
	
	@Override
	public void onCreate() {
		
		mFtpServer = new FtpServer(getApplicationContext());
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				// TODO check mftp == null
				mFtpServer.read();				
			}
		}, (long) 0, (long)((PERIOD_MIN * 60) + PERIOD_SEC) * 1000);
		
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
		
		super.onDestroy();
	}

}
