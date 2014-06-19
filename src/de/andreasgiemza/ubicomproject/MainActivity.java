package de.andreasgiemza.ubicomproject;

import java.util.Map.Entry;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
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

		for (Entry<String, Position> entry : PositionsStorage.INSTANCE.positions
				.entrySet()) {
			
			int elapsedMinutes = (int) ((System.currentTimeMillis() - entry
					.getValue().time) / 1000 / 60);

			if (elapsedMinutes < 20) {
				String markerText = getContactName(getApplicationContext(),
						entry.getKey())
						+ "\n"
						+ (elapsedMinutes < 1 ? "< 1" : elapsedMinutes)
						+ " "
						+ (elapsedMinutes < 2 ? getResources().getString(
								R.string.main_minute_ago) : getResources()
								.getString(R.string.main_minutes_ago));

				MarkerOptions markerOptions = new MarkerOptions()
						.icon(BitmapDescriptorFactory.fromBitmap(iconFactory
								.makeIcon(markerText)))
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

	public static String getContactName(Context context, String phoneNumber) {
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri,
				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) {
			return phoneNumber;
		}
		String contactName = phoneNumber;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor
					.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}

		return contactName;
	}

	Handler timerHandler = new Handler();
	Runnable timerRunnable = new Runnable() {

		@Override
		public void run() {
			drawFriends();

			timerHandler.postDelayed(this, 15 * 1000);
		}
	};

}
