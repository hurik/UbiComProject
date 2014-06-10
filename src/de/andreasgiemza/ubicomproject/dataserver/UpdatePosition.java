package de.andreasgiemza.ubicomproject.dataserver;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.AsyncTask;

public class UpdatePosition extends AsyncTask<Object, Object, Object> {

	private final String number;
	private final String latitude;
	private final String longitude;
	private final JSONParser jsonParser = new JSONParser();

	public UpdatePosition(String number, Location location) {
		this.number = number;
		latitude = String.valueOf(location.getLatitude());
		longitude = String.valueOf(location.getLongitude());
	}

	@Override
	protected Object doInBackground(Object... params) {
		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		postParams.add(new BasicNameValuePair("number", number));
		postParams.add(new BasicNameValuePair("latitude", latitude));
		postParams.add(new BasicNameValuePair("longitude", longitude));

		JSONObject json = jsonParser.makeHttpRequest(
				"http://ucp.g8j.de/update_position.php", "GET", postParams);

		try {
			int success = json.getInt("success");

			if (success == 1) {

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}
}
