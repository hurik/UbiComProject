package de.andreasgiemza.ubicomproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

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

public class FtpServer {

	public class UbiCom_Pos {
		public String number;
		public String Latitude;
		public String Longtitude;
	}

	// Constants
	private final static String TAG = "FTPServer";
	private final static boolean DEBUG = true;
	protected static final String uploadPath = "/uploads/";

	// FTPClient
	private FTPClient mFtpclient = new FTPClient();

	private Context mContext = null;
	private static String mPhoneNumber;
	
	private int i = 0;

	public FtpServer(Context context) {
		mContext = context;

		// Telefonnummer auslesen und (erstmal) lokal speicher und ausgeben
		TelephonyManager mTManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneNumber = mTManager.getLine1Number();
	}

	public boolean isConnected() {
		return mFtpclient.isConnected();
	}

	public List<UbiCom_Pos> read() {

//		Thread thread = new Thread(new Runnable() {

//			@Override
//			public void run() {
				if (!connectingToFtpServer())
					return null;

				String filename = "chached";
				boolean status = false;

				// 1. Step create local chached File
				File downloadFile = new File(mContext.getCacheDir(), filename);
				OutputStream outputStream = null;

				try {
					status = downloadFile.createNewFile();

					if (!status)
						return null;

					outputStream = new FileOutputStream(downloadFile);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// 2. Copy file from Server
				try {
					FTPFile[] files = mFtpclient.listFiles();
					for (int i = 0; i < files.length; i++) {

						if (!files[i].isFile())
							continue;

						String remoteFileName = files[i].getName();

						outputStream.write(remoteFileName.getBytes());
						outputStream.write(':');

						status = mFtpclient.retrieveFile(remoteFileName,
								outputStream);

						if (!status) {
							outputStream.close();
							return null;
						}

						outputStream.write('\n');
					}
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// 3. read from input and write it to Array
				FileInputStream inputStream;
				List<UbiCom_Pos> list = new ArrayList<>();

				try {
					inputStream = new FileInputStream(downloadFile);
					InputStreamReader isr = new InputStreamReader(inputStream);
					BufferedReader reader = new BufferedReader(isr);

					String line;
					while ((line = reader.readLine()) != null) {

						String[] string = line.split(":");

						if (string.length != 3)
							continue;

						UbiCom_Pos l = new UbiCom_Pos();
						l.number = string[0];
						l.Latitude = string[1];
						l.Longtitude = string[2];

						list.add(l);
					}

					reader.close();
					isr.close();
					inputStream.close();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// clear data
				downloadFile.delete();

				disconnecting();

				return list;
//			}
//		});
//		thread.start();
	}

	String mLongitude = null;
	String mLatitude = null;

	public void write(String longitude, String latitude) {
		
		mLatitude = latitude;
		mLongitude = longitude;

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (DEBUG)
					Log.d(TAG, "write to, but first check connection...");

				// Check if connected
				if (!connectingToFtpServer())
					return;

				if (DEBUG)
					Log.d(TAG, "write to FTP");

				FileOutputStream outputStream;

				boolean status = false;

				// 1. Step create local chached File
				File tmpFile = null;

				try {
					tmpFile = File.createTempFile(mPhoneNumber, "loc");

					String outputString = new StringBuilder().append(mLatitude)
							.append(":").append(mLongitude).toString();

					outputStream = new FileOutputStream(tmpFile);
					outputStream.write(outputString.getBytes());
					outputStream.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

				// 2. Step copy file from internal storage to ftp
				FileInputStream inputStream;

				try {
					if (tmpFile == null)
						Log.e(TAG, "tmpFile is null");

					inputStream = new FileInputStream(tmpFile);
					status = mFtpclient.storeFile(mPhoneNumber, inputStream);
					inputStream.close();

					if (DEBUG)
						Log.d(TAG, "Status (sending to FTP): " + status);

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// clear tmpFile
				status = tmpFile.delete();

				if (DEBUG)
					Log.d(TAG, "Status (deleting File): " + status);

				disconnecting();
			}
		});
		thread.start();

	}

	public boolean checkFileExists(String file) throws IOException {

		InputStream inputStream = mFtpclient.retrieveFileStream(file);
		if (inputStream == null)
			return false;
		return true;
	}

	/*
	 * Verbindet sich zu einem FTP-Server. Dazu wird ein neuer Thread gestartet,
	 * der dann die verbindung aufbaut.
	 */
	private boolean connectingToFtpServer() {

		boolean status = false;
		try {

			if (DEBUG)
				Log.d(TAG, "connecting to FTP-Server");

			// TODO passwort "verstekcen"
			mFtpclient.setConnectTimeout(10 * 1000);
			mFtpclient.connect("ftp.g8j.de", 21);
			status = mFtpclient.login("187687-giemza.org", "UbiComProject");

			if (DEBUG)
				Log.d(TAG, "is connected");

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
		return status;
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
}
