package de.andreasgiemza.ubicomproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity {

	private GoogleMap googleMap;
	private MarkerOptions myMarker = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(mMessageReceiver,
						new IntentFilter("LocationService"));

		// Create GoogleMap
		googleMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.map)).getMap();

		// start Service
		startService(new Intent(getBaseContext(), LocationService.class));
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Double currentLatitude = intent.getDoubleExtra("LocationLatitude",
					0);
			Double currentLongitude = intent.getDoubleExtra(
					"LocationLongitude", 0);

			if (myMarker == null) {
				myMarker = new MarkerOptions().position(new LatLng(
						currentLatitude, currentLongitude));

				// adding marker
				googleMap.addMarker(myMarker);
			} else
				myMarker.position(new LatLng(currentLatitude, currentLongitude));
		}
	};
}
