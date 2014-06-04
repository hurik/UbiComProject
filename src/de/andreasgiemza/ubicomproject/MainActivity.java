package de.andreasgiemza.ubicomproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.andreasgiemza.ubicomproject.services.LocationService;
import de.andreasgiemza.ubicomproject.services.NotificationService;

public class MainActivity extends Activity {

	private GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create GoogleMap
		googleMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.map)).getMap();

		// show own position
		googleMap.setMyLocationEnabled(true);

		// start Service
		startService(new Intent(getBaseContext(), LocationService.class));
		startService(new Intent(getBaseContext(), NotificationService.class));

		// BroadcastReceiver
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(mMessageReceiver,
						new IntentFilter(NotificationService.BROADCAST_ACTION));

		// Check phone number
		checkPhoneNumber();
	}

	private void checkPhoneNumber() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String number = sharedPref.getString("settings_number", "");

		// Number not set
		if ("".equals(number)) {
			// Get number from sim card
			String phoneSavedNumber = ((TelephonyManager) getApplicationContext()
					.getSystemService(Context.TELEPHONY_SERVICE))
					.getLine1Number();

			if ("".equals(phoneSavedNumber)) {
				// Number not on sim card
				startActivity(new Intent(getApplicationContext(),
						SettingsActivity.class));
				Toast.makeText(getApplicationContext(),
						R.string.settings_number_summary, Toast.LENGTH_LONG)
						.show();
			} else {
				// Number was obtained
				sharedPref.edit()
						.putString("settings_number", phoneSavedNumber)
						.commit();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(getApplicationContext(),
					SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	protected void onDestroy() {

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(mMessageReceiver);

		stopService(new Intent(getBaseContext(), NotificationService.class));

		super.onDestroy();
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(android.content.Context context, Intent intent) {

			final Double currentLatitude = intent.getDoubleExtra(
					NotificationService.BROADCAST_LATITUDE, 0);
			final Double currentLongitude = intent.getDoubleExtra(
					NotificationService.BROADCAST_LONGITUDE, 0);

			if (currentLongitude == 0 | currentLatitude == 0)
				return;

			Log.d("MAIN", currentLatitude + "," + currentLongitude);

			MarkerOptions mMarker = new MarkerOptions().icon(
					BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
					.position(new LatLng(currentLatitude, currentLongitude));
			googleMap.addMarker(mMarker);
		};
	};
}
