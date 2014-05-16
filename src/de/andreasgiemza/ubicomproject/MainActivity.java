package de.andreasgiemza.ubicomproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class MainActivity extends Activity {

	private GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create GoogleMap
		googleMap = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.map)).getMap();

		// Show my location
		googleMap.setMyLocationEnabled(true);

		// start Service

	}

	// Method to start the service
	public void startService(View view) {
		startService(new Intent(getBaseContext(), LocationService.class));
	}

	// Method to stop the service
	public void stopService(View view) {
		stopService(new Intent(getBaseContext(), LocationService.class));
	}
}
