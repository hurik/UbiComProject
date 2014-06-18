package de.andreasgiemza.ubicomproject.gcm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import de.andreasgiemza.ubicomproject.helpers.Preferences;

public enum GcmServer {
	INSTANCE;

	private InputStream is = null;
	private JSONObject jObj = null;
	private String json = "";

	public void register(Context context, String regId) {
		final Preferences prefs = new Preferences(context);

		List<NameValuePair> getPrarams = new ArrayList<NameValuePair>();
		getPrarams.add(new BasicNameValuePair("number", prefs.getNumber()));
		getPrarams.add(new BasicNameValuePair("gcm", regId));

		makeHttpRequest("http://ucp.g8j.de/register.php", "GET", getPrarams);
	}

	public void updatePosition(Context context, final Location location) {
		final Preferences prefs = new Preferences(context);

		if (prefs.isRegistered() && checkInternetConnection(context)) {
			new Thread(new Runnable() {
				public void run() {
					String message = prefs.getNumber() + ";"
							+ String.valueOf(location.getLatitude()) + ";"
							+ String.valueOf(location.getLongitude());

					List<NameValuePair> postParams = new ArrayList<NameValuePair>();
					postParams.add(new BasicNameValuePair("message", message));

					makeHttpRequest("http://ucp.g8j.de/update_position.php",
							"GET", postParams);
				}
			}).start();
		}
	}

	public List<String> getKnownNumbers(final List<String> numbers) {
		String numbersString = "";

		for (String number : numbers) {
			if (number != null)
				numbersString += number.replaceAll("\\s", "") + ";";
		}

		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		postParams.add(new BasicNameValuePair("numbers", numbersString));

		JSONObject answer = makeHttpRequest(
				"http://ucp.g8j.de/known_numbers.php", "GET", postParams);

		List<String> knownNumbers = new LinkedList<>();

		try {
			if (answer != null) {
				// Checking for SUCCESS TAG
				int success = answer.getInt("success");

				if (success == 1) {
					// Getting Array of Products
					JSONArray jsonKnownNumbers = answer
							.getJSONArray("knownNumbers");

					// looping through All Products
					for (int i = 0; i < jsonKnownNumbers.length(); i++) {
						JSONObject c = jsonKnownNumbers.getJSONObject(i);

						knownNumbers.add(c.getString("number"));
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		for (String number : knownNumbers) {
			Log.d("AllowedNumbers", number);
		}
		return knownNumbers;

	}

	// function get json from url
	// by making HTTP POST or GET mehtod
	public JSONObject makeHttpRequest(String url, String method,
			List<NameValuePair> params) {

		// Making HTTP request
		try {
			// check for request method
			if (method == "POST") {
				// request method is POST
				// defaultHttpClient
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);
				httpPost.setEntity(new UrlEncodedFormEntity(params));

				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();

			} else if (method == "GET") {
				// request method is GET
				DefaultHttpClient httpClient = new DefaultHttpClient();
				String paramString = URLEncodedUtils.format(params, "utf-8");
				url += "?" + paramString;
				HttpGet httpGet = new HttpGet(url);

				HttpResponse httpResponse = httpClient.execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jObj;
	}

	// Helpers
	private boolean checkInternetConnection(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}

		return false;
	}
}
