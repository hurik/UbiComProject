package de.andreasgiemza.ubicomproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.andreasgiemza.ubicomproject.gcm.GcmIntentService;
import de.andreasgiemza.ubicomproject.services.LocationService;

public class MainActivity extends Activity {

	private Preferences preferences;
	private GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		preferences = new Preferences(getApplicationContext());

		if (!preferences.isRegistered()) {
			startActivity(new Intent(getApplicationContext(),
					RegisterActivity.class));
			finish();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create GoogleMap
		googleMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.map)).getMap();

		// show own position
		googleMap.setMyLocationEnabled(true);

		// start Service
		startService(new Intent(getBaseContext(), LocationService.class));

		// BroadcastReceiver
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(mMessageReceiver,
						new IntentFilter(GcmIntentService.BROADCAST_ACTION));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(mMessageReceiver);
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(android.content.Context context, Intent intent) {
			final Double currentLatitude = intent.getDoubleExtra(
					GcmIntentService.BROADCAST_LATITUDE, 0);
			final Double currentLongitude = intent.getDoubleExtra(
					GcmIntentService.BROADCAST_LONGITUDE, 0);

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
