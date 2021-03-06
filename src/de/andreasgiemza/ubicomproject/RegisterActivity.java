package de.andreasgiemza.ubicomproject;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.andreasgiemza.ubicomproject.gcm.UbiComProjectServer;
import de.andreasgiemza.ubicomproject.helpers.InternetConnection;
import de.andreasgiemza.ubicomproject.helpers.Phonebook;
import de.andreasgiemza.ubicomproject.helpers.Preferences;

public class RegisterActivity extends Activity {

	private Context context;
	private Preferences prefs;
	private EditText registerNumber;
	private Button registerButton;
	private GoogleCloudMessaging gcm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		// Get the context
		context = getApplicationContext();

		prefs = new Preferences(context);

		// Get ui elements
		registerNumber = (EditText) findViewById(R.id.register_number);
		registerButton = (Button) findViewById(R.id.register_button);

		// Try to get saved number or try to get it from the sim card
		if (!prefs.getNumber().isEmpty()) {
			registerNumber.setText(prefs.getNumber());
		} else {
			registerNumber.setText(((TelephonyManager) getApplicationContext()
					.getSystemService(Context.TELEPHONY_SERVICE))
					.getLine1Number());
		}

		// Setup button
		registerButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// check if there is a internet connection
				if (!InternetConnection.check(getApplicationContext())) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.internet_connection_needed),
							Toast.LENGTH_SHORT).show();
					return;
				}

				String number = registerNumber.getText().toString();

				// Check if user filled the form
				if (number.trim().length() > 0) {
					// Parse the number
					number = Phonebook.parseNumber(number);

					// Save number
					prefs.setNumber(number);
					// Check if regId is already saved or the app version
					// changed
					if (prefs.getRegId().isEmpty()
							|| prefs.getAppVersion() != prefs
									.getCurrentAppVersion()) {
						// Register the user in background
						new RegisterTask().execute();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.register_number_summary,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private class RegisterTask extends AsyncTask<String, String, String> {

		ProgressDialog loadingDialog;

		// Before running code in separate thread
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			loadingDialog = new ProgressDialog(RegisterActivity.this);
			loadingDialog
					.setMessage(getResources().getString(R.string.loading));
			loadingDialog.setIndeterminate(false);
			loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			loadingDialog.setCancelable(false);
			loadingDialog.show();
		}

		// The code to be executed in a background thread.
		@Override
		protected String doInBackground(String... params) {
			try {
				// Register device with google cloud messaging
				gcm = GoogleCloudMessaging.getInstance(context);
				String regId = gcm.register(Preferences.GCMPoject);

				prefs.setRegId(regId);
				prefs.setAppVersion(prefs.getCurrentAppVersion());

				// Register device with own server
				UbiComProjectServer.register(getApplicationContext(), regId);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		// after executing the code in the thread
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			loadingDialog.dismiss();

			// All finished start main activity
			startActivity(new Intent(getApplicationContext(),
					MainActivity.class));
			finish();
		}
	}
}
