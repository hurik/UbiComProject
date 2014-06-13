package de.andreasgiemza.ubicomproject.dataserver;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import de.andreasgiemza.ubicomproject.Preferences;

public enum DataServer {
	INSTANCE;

	private final JSONParser jsonParser = new JSONParser();

	public void updatePosition(Context context, final Location location) {
		final Preferences prefs = new Preferences(context);

		if (prefs.isRegistered() && checkInternetConnection(context)) {
			new Thread(new Runnable() {
				public void run() {
					String message = prefs.getNumber() +";"+ String
							.valueOf(location.getLatitude())+";"+String
							.valueOf(location.getLongitude());
					
					List<NameValuePair> postParams = new ArrayList<NameValuePair>();
					postParams.add(new BasicNameValuePair("message", message));

					jsonParser.makeHttpRequest(
							"http://ucp.g8j.de/update_position.php", "GET",
							postParams);
				}
			}).start();
		}
	}

	// Helpers
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
