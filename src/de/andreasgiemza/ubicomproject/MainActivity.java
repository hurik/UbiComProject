package de.andreasgiemza.ubicomproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import de.andreasgiemza.ubicomproject.services.LocationService;

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
	}
}
