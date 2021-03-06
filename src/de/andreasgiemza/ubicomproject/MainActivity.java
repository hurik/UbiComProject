package de.andreasgiemza.ubicomproject;

import java.util.Map.Entry;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import de.andreasgiemza.ubicomproject.gcm.GcmIntentService;
import de.andreasgiemza.ubicomproject.helpers.InternetConnection;
import de.andreasgiemza.ubicomproject.helpers.Phonebook;
import de.andreasgiemza.ubicomproject.helpers.Preferences;
import de.andreasgiemza.ubicomproject.helpers.ApplicationData;
import de.andreasgiemza.ubicomproject.helpers.ApplicationData.Position;
import de.andreasgiemza.ubicomproject.location.LocationService;

public class MainActivity extends Activity {

	private Preferences prefs;
	private GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		prefs = new Preferences(getApplicationContext());

		if (!prefs.isRegistered()) {
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

		Intent intent = getIntent();
		double lat = intent.getDoubleExtra("Latitude", 0);
		double lng = intent.getDoubleExtra("Longitude", 0);

		if (lat != 0 && lng != 0) {
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
					new LatLng(lat, lng), 15));
		}

		// start Service
		startService(new Intent(getBaseContext(), LocationService.class));

		// BroadcastReceiver
		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(mMessageReceiver,
						new IntentFilter(GcmIntentService.BROADCAST_ACTION));

		timerHandler.postDelayed(timerRunnable, 0);
	}

	@Override
	protected void onPause() {
		super.onPause();

		timerHandler.removeCallbacks(timerRunnable);
	}

	@Override
	protected void onResume() {
		super.onResume();

		timerHandler.postDelayed(timerRunnable, 0);
	}

	private void drawFriends() {
		googleMap.clear();

		IconGenerator iconFactory = new IconGenerator(getApplicationContext());

		for (Entry<String, Position> entry : ((ApplicationData) this
				.getApplication()).positions.entrySet()) {

			int elapsedMinutes = (int) ((System.currentTimeMillis() - entry
					.getValue().time) / 1000 / 60);

			if (elapsedMinutes < 20) {
				String markerText = Phonebook.getContactName(
						getApplicationContext(), entry.getKey())
						+ "\n"
						+ (entry.getValue().distance != Integer.MAX_VALUE ? (entry
								.getValue().distance
								+ getResources()
										.getString(R.string.main_m_away) + "\n")
								: "")
						+ (elapsedMinutes < 1 ? "< 1" : elapsedMinutes)
						+ " "
						+ (elapsedMinutes < 2 ? getResources().getString(
								R.string.main_minute_ago) : getResources()
								.getString(R.string.main_minutes_ago));

				MarkerOptions markerOptions = new MarkerOptions()
						.icon(BitmapDescriptorFactory.fromBitmap(iconFactory
								.makeIcon(markerText)))
						.position(
								new LatLng(entry.getValue().location
										.getLatitude(),
										entry.getValue().location
												.getLongitude()))
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
			if (InternetConnection.check(getApplicationContext())) {
				startActivity(new Intent(getApplicationContext(),
						AllowedNumbersActivity.class));
			} else {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.internet_connection_needed),
						Toast.LENGTH_SHORT).show();
			}
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

	Handler timerHandler = new Handler();
	Runnable timerRunnable = new Runnable() {

		@Override
		public void run() {
			drawFriends();

			timerHandler.postDelayed(this, 15 * 1000);
		}
	};
}
