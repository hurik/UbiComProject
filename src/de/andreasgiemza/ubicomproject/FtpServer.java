package de.andreasgiemza.ubicomproject;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * TODO download commons-net-3.3-bin.zip at
 * (http://commons.apache.org/proper/commons-net/download_net.cgi)
 * 
 * and import jar file to Build Path
 * http://stackoverflow.com/questions/8280594/how
 * -to-import-org-apache-commons-net-ftp-ftpclient
 * 
 * @author Rauser
 * 
 */

public class FtpServer extends Service {

	// Constants
	private final static String TAG = "FTPServer";
	private final static boolean DEBUG = true;
	protected static final String uploadPath = "/uploads/";

	// FTPClient
	private FTPClient mFtpclient = new FTPClient();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(mMessageReceiver,
						new IntentFilter(LocationService.BROADCAST_ACTION));

		super.onCreate();
	}

	@Override
	public void onDestroy() {
		disconnecting();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mMessageReceiver);
		super.onDestroy();
	}

	public boolean isConnected() {
		return mFtpclient.isConnected();
	}

	private void write() {
		// TODO Auto-generated method stub

		try {
			Log.d(TAG, "Current Path: " + mFtpclient.printWorkingDirectory());
			// FTPFile file = mFtpclient.mlistFile("test.txt");
			//
			//
			// if (!file.isFile() /* && !file.isDirectory() */) {
			String fileDir = "testfile.txt";
			FileInputStream in = null;
			in = new FileInputStream(fileDir);
			mFtpclient.storeFile(fileDir, in);
			// }

			// buffout.write(0x01);
			// buffout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * Starts with the Service (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		TelephonyManager mTManager = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		String mPhoneNumber = mTManager.getLine1Number();

		Log.d(TAG, mPhoneNumber);
		return super.onStartCommand(intent, flags, startId);
	}

	private void connectingToFtpServer() {

		Thread thread = new Thread(new Runnable() {
			boolean status = false;

			@Override
			public void run() {
				try {

					if (DEBUG)
						Log.d(TAG, "connecting to FTP-Server");

					// TODO passwort "verstekcen"
					mFtpclient.setConnectTimeout(10 * 1000);
					mFtpclient.connect("ftp.g8j.de", 21);
					status = mFtpclient.login("187687-giemza.org",
							"UbiComProject");

					mFtpclient.setFileType(FTP.ASCII_FILE_TYPE);
					mFtpclient.changeWorkingDirectory(uploadPath);

					// Test
					write();

					if (DEBUG)
						Log.d(TAG, "isConnected:" + String.valueOf(status));

				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.start();
	}

	private boolean disconnecting() {
		boolean status = false;
		try {
			status = mFtpclient.logout();
			mFtpclient.disconnect();

			if (DEBUG)
				Log.d(TAG, "disconnecting");
		} catch (IOException e) {

		}
		return status;
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		// TODO Deaktieveren? Filter? Loacal?
		@Override
		public void onReceive(Context context, Intent intent) {
			Double currentLatitude = intent.getDoubleExtra("LocationLatitude",
					0);
			Double currentLongitude = intent.getDoubleExtra(
					"LocationLongitude", 0);

			Toast.makeText(getApplicationContext(),
					currentLatitude + " : " + currentLongitude,
					Toast.LENGTH_LONG).show();
		}
	};

}
