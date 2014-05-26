package de.andreasgiemza.ubicomproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

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

	private static String mPhoneNumber;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(mMessageReceiver,
						new IntentFilter(LocationService.BROADCAST_ACTION));

		// Telefonnummer auslesen und (erstmal) lokal speicher und ausgeben
		TelephonyManager mTManager = (TelephonyManager) getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneNumber = mTManager.getLine1Number();

		// Nur zum testen
		Log.d(TAG, mPhoneNumber);
		
		
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

	private void read() {
		String filename = "01715471692";

		int ch;
		StringBuffer fileContent = new StringBuffer("");
		Byte[] testString = null;

		FileInputStream inputStream;

		boolean status = false;

		// 1. Step create local chached File

		try {
			inputStream = getApplicationContext().openFileInput(filename);
			try {
				while ((ch = inputStream.read()) != -1)
					fileContent.append((char) ch);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {

			e1.printStackTrace();
		}

		String data = new String(fileContent);
		Log.d("READED STRING", data);

	}

	private void write(String longitude, String latitude) {

		// Check if connected
		if (!isConnected())
			return;

		Log.d(TAG, "write()");

		String outputString = new StringBuilder().append(latitude).append(":").append(longitude).toString();

		FileOutputStream outputStream;

		boolean status = false;

		// 1. Step create local chached File
		File tmpFile = new File(getApplicationContext().getCacheDir(), mPhoneNumber);

		try {
			status = tmpFile.createNewFile();
			
			if(!status) {
				Log.e(TAG, "Can't write to file");
				return;
			}
			
			outputStream = new FileOutputStream(tmpFile);
			outputStream.write(outputString.getBytes());
			outputStream.close();
		} catch (IOException e1) {

			e1.printStackTrace();
		}

		// 2. Step copy file from internal storage to ftp
		FileInputStream inputStream;

		try {
			inputStream = new FileInputStream(tmpFile);
			status = mFtpclient.storeFile(mPhoneNumber, inputStream);
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (DEBUG)
			Log.d(TAG, "Status (sending to FTP): " + status);

		// clear tmpFile
		status = tmpFile.delete();

		if (DEBUG)
			Log.d(TAG, "Status (deleting File): " + status);
	}

	public boolean checkFileExists(String file) throws IOException {

		InputStream inputStream = mFtpclient.retrieveFileStream(file);
		if (inputStream == null)
			return false;
		return true;
	}

	/*
	 * Starts with the Service (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {



		// starten der Verbindung
		connectingToFtpServer();

		return super.onStartCommand(intent, flags, startId);
	}

	/*
	 * Verbindet sich zu einem FTP-Server. Dazu wird ein neuer Thread gestartet,
	 * der dann die verbindung aufbaut.
	 */
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

					Log.d(TAG, mFtpclient.printWorkingDirectory());

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
			final Double currentLatitude = intent.getDoubleExtra(
					"LocationLatitude", 0);
			final Double currentLongitude = intent.getDoubleExtra(
					"LocationLongitude", 0);

			// TODO nur zum testen
			Toast.makeText(getApplicationContext(),
					currentLatitude + " : " + currentLongitude,
					Toast.LENGTH_LONG).show();
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					write(String.valueOf(currentLongitude), String.valueOf(currentLatitude));
				}
			});
			thread.start();
		}
	};

}
