package de.andreasgiemza.ubicomproject;

import java.util.Map.Entry;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import de.andreasgiemza.ubicomproject.gcm.GcmIntentService;
import de.andreasgiemza.ubicomproject.helpers.PositionsStorage;
import de.andreasgiemza.ubicomproject.helpers.PositionsStorage.Position;
import de.andreasgiemza.ubicomproject.helpers.Preferences;
import de.andreasgiemza.ubicomproject.location.LocationService;

public class MainActivity extends Activity {

	private Preferences preferences;
	private GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		preferences = new Preferences(getApplicationContext());

		// startActivity(new Intent(getApplicationContext(),
		// NumberActivity.class));

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

		drawFriends();
	}

	@Override
	protected void onResume() {
		super.onResume();

		drawFriends();
	}

	private void drawFriends() {
		googleMap.clear();

		IconGenerator iconFactory = new IconGenerator(getApplicationContext());

		for (Entry<String, Position> entry : PositionsStorage.INSTANCE.positions
				.entrySet()) {

			int elapsedSeconds = (int) ((System.currentTimeMillis() - entry
					.getValue().time) / 1000);

			if (elapsedSeconds < 60 * 60) {
				MarkerOptions markerOptions = new MarkerOptions()
						.icon(BitmapDescriptorFactory.fromBitmap(iconFactory
								.makeIcon(entry.getKey() + "\n"
										+ elapsedSeconds + " seconds ago")))
						.position(entry.getValue().latLng)
						.anchor(iconFactory.getAnchorU(),
								iconFactory.getAnchorV());

				googleMap.addMarker(markerOptions);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.number:
			startActivity(new Intent(getApplicationContext(),
					AllowedNumbersActivity.class));
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
		super.onDestroy();

		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(mMessageReceiver);
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(android.content.Context context, Intent intent) {
			drawFriends();
		};
	};
}
