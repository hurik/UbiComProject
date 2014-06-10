package de.andreasgiemza.ubicomproject.dataserver;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public enum DataServer {
	INSTANCE;

	public void updatePosition(Context applicationContext, Location location) {
		String number = getNumber(applicationContext);

		if (!"".equals(number) && checkInternetConnection(applicationContext)) {
			new UpdatePosition(number, location).execute();
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
