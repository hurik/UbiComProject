package de.andreasgiemza.ubicomproject.dataserver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

public enum DataServer {
	INSTANCE;

	private final JSONParser jsonParser = new JSONParser();

	public List<UbiCom_Pos> getPositions(Context applicationContext) {
		final List<UbiCom_Pos> positions = new LinkedList<>();
		final String number = getNumber(applicationContext);

		if (checkInternetConnection(applicationContext)) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// getting JSON string from URL
			JSONObject json = jsonParser.makeHttpRequest(
					"http://ucp.g8j.de/get_positions.php", "GET", params);

			try {
				if (json != null) {
					// Checking for SUCCESS TAG
					int success = json.getInt("success");

					if (success == 1) {
						// products found
						// Getting Array of Products
						JSONArray jsonPositions = json
								.getJSONArray("positions");

						// looping through All Products
						for (int i = 0; i < jsonPositions.length(); i++) {
							JSONObject c = jsonPositions.getJSONObject(i);

							if (!number.equals(c.getString("number"))) {
								positions.add(new UbiCom_Pos(c
										.getString("number"), c
										.getString("latitude"), c
										.getString("longitude"), c
										.getString("updated")));
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return positions;
	}

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

	public class UbiCom_Pos {
		public final String number;
		public final String latitude;
		public final String longitude;
		public final String updated;

		public UbiCom_Pos(String number, String latitude, String longitude,
				String updated) {
			this.number = number;
			this.latitude = latitude;
			this.longitude = longitude;
			this.updated = updated;
		}
	}
}
