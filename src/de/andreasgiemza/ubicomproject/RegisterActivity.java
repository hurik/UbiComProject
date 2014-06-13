package de.andreasgiemza.ubicomproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.andreasgiemza.ubicomproject.dataserver.JSONParser;

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
				String number = registerNumber.getText().toString();

				// Check if user filled the form
				if (number.trim().length() > 0) {
					// Save number
					prefs.setNumber(number);
					// Check if regId is already saved or the app version
					// changed
					if (prefs.getRegId().isEmpty()
							|| prefs.getAppVersion() != prefs
									.getCurrentAppVersion()) {
						// Register the user in background
						registerInBackground();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.register_number_summary,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void registerInBackground() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// Register device with google cloud messaging
					gcm = GoogleCloudMessaging.getInstance(context);
					String regId = gcm.register("470689489809");

					prefs.setRegId(regId);
					prefs.setAppVersion(prefs.getCurrentAppVersion());

					// Register device with own server
					List<NameValuePair> getPrarams = new ArrayList<NameValuePair>();
					getPrarams.add(new BasicNameValuePair("number", prefs
							.getNumber()));
					getPrarams.add(new BasicNameValuePair("gcm", regId));

					new JSONParser()
							.makeHttpRequest("http://ucp.g8j.de/register.php",
									"GET", getPrarams);

					startActivity(new Intent(getApplicationContext(),
							MainActivity.class));
					finish();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}