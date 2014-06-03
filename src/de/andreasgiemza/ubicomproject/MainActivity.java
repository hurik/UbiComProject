package de.andreasgiemza.ubicomproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

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

			Toast.makeText(getApplicationContext(), currentLatitude + "," + currentLongitude, Toast.LENGTH_SHORT).show();
			Log.d("MAIN", currentLatitude + "," + currentLongitude);
			
		};

	};
}
