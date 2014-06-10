package de.andreasgiemza.ubicomproject.dataserver;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public enum DataServer {
	INSTANCE;

	private final JSONParser jsonParser = new JSONParser();

	public void updatePosition(Context applicationContext,
			final Location location) {
		final String number = getNumber(applicationContext);

		if (!"".equals(number) && checkInternetConnection(applicationContext)) {
			new Thread(new Runnable() {
				public void run() {
					List<NameValuePair> postParams = new ArrayList<NameValuePair>();
					postParams.add(new BasicNameValuePair("number", number));
					postParams.add(new BasicNameValuePair("latitude", String
							.valueOf(location.getLatitude())));
					postParams.add(new BasicNameValuePair("longitude", String
							.valueOf(location.getLongitude())));

					jsonParser.makeHttpRequest(
							"http://ucp.g8j.de/update_position.php", "GET",
							postParams);
				}
			}).start();
		}
	}

	// Helpers
	private String getNumber(Context applicationContext) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(applicationContext);
		return sharedPref.getString("settings_number", "");
	}

	private boolean checkInternetConnection(Context applicationContext) {
		ConnectivityManager cm = (ConnectivityManager) applicationContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}

		return false;
	}
}
