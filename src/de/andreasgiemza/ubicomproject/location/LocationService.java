package de.andreasgiemza.ubicomproject.location;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import de.andreasgiemza.ubicomproject.gcm.GcmServer;
import de.andreasgiemza.ubicomproject.helpers.PositionsStorage;

public class LocationService extends Service implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
	public static final long UPDATE_INTERVAL = 2 * 60 * 1000;
	public static final long FASTEST_INTERVAL = 60 * 1000;

	IBinder mBinder = new LocalBinder();
	private LocationClient mLocationClient;
	private LocationRequest mLocationRequest;
	// Flag that indicates if a request is underway.
	private boolean mInProgress;
	private Boolean servicesAvailable = false;

	public class LocalBinder extends Binder {
		public LocationService getServerInstance() {
			return LocationService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mInProgress = false;
		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest
				.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		servicesAvailable = servicesConnected();
		/*
		 * Create a new location client, using the enclosing class to handle
		 * callbacks.
		 */
		mLocationClient = new LocationClient(this, this, this);
	}

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			return true;
		} else {
			return false;
		}
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (!servicesAvailable || mLocationClient.isConnected() || mInProgress)
			return START_STICKY;
		setUpLocationClientIfNeeded();
		if (!mLocationClient.isConnected() || !mLocationClient.isConnecting()
				&& !mInProgress) {
			mInProgress = true;
			mLocationClient.connect();
		}
		return START_STICKY;
	}

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null)
			mLocationClient = new LocationClient(this, this, this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Turn off the request flag
		mInProgress = false;
		if (servicesAvailable && mLocationClient != null) {
			mLocationClient.removeLocationUpdates(this);
			// Destroy the current location client
			mLocationClient = null;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onConnected(Bundle bundle) {
		// Request location updates using static settings
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		// Turn off the request flag
		mInProgress = false;
		// Destroy the current location client
		mLocationClient = null;
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		mInProgress = false;
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			// If no resolution is available, display an error dialog
		} else {

		}
	}

	@Override
	public void onLocationChanged(Location location) {
			GcmServer.INSTANCE
					.updatePosition(getApplicationContext(), location);
			PositionsStorage.INSTANCE.updateMyPosition(location);
	}
}
