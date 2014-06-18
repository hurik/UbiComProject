package de.andreasgiemza.ubicomproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.andreasgiemza.ubicomproject.gcm.GcmServer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AllowedNumbersActivity extends Activity {

	ListView mListView = null;

	String[] countries = new String[] { "India", "Pakistan", "Sri Lanka",
			"China", "Bangladesh", "Nepal", "Afghanistan", "North Korea",
			"South Korea", "Japan", "Deutschland", "Halo", "Hallo",
			"hallohallo" };

	// Array of booleans to store toggle button status
	public boolean[] status = { true, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false };

	// A ProgressDialog object
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_allowed_numbers);

		if (savedInstanceState != null) {
			status = savedInstanceState.getBooleanArray("status");
		}

		new LoadViewTask().execute();
	}

	@Override
	protected void onStart() {

		super.onStart();

		mListView = (ListView) findViewById(R.id.list_number);
		if (mListView == null)
			Log.e("tag", "ListView is null");

		mListView.setOnItemClickListener(itemClickListener);
	}
	
	@Override
	protected void onStop() {
		
		//TODO save all allowed Number in Preferences
		
		super.onStop();
		
		
	}

	private String parseNumber(String string) {
		if (string.charAt(0) == '+') {
			return string;
		}
		string = string.replaceFirst("0", "+49");

		return string;
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View item, int position,
				long id) {

			ListView listView = (ListView) parent;

			SimpleAdapter adapter = (SimpleAdapter) listView.getAdapter();

			HashMap<String, Object> hm = (HashMap<String, Object>) adapter
					.getItem(position);

			/** The clicked Item in the ListView */
			RelativeLayout rLayout = (RelativeLayout) item;

			/** Getting the toggle button corresponding to the clicked item */
			ToggleButton tgl = (ToggleButton) rLayout.getChildAt(2);

			String strStatus = "";
			if (tgl.isChecked()) {
				tgl.setChecked(false);
				strStatus = "Off";
				status[position] = false;
			} else {
				tgl.setChecked(true);
				strStatus = "On";
				status[position] = true;
			}
			Toast.makeText(getBaseContext(),
					(String) hm.get("txt") + " : " + strStatus,
					Toast.LENGTH_SHORT).show();
		}
	};

	private class LoadViewTask extends AsyncTask<Void, Integer, Void> {
		List<HashMap<String, Object>> aList;

		// Before running code in separate thread
		@Override
		protected void onPreExecute() {
			// Create a new progress dialog
			progressDialog = new ProgressDialog(AllowedNumbersActivity.this);
			// Set the progress dialog to display a horizontal progress bar
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// Set the dialog title to 'Loading...'
			progressDialog.setTitle("Loading...");
			// Set the dialog message to 'Loading application View, please
			// wait...'
			progressDialog
					.setMessage("Loading application View, please wait...");
			// This dialog can't be canceled by pressing the back key
			progressDialog.setCancelable(false);
			// This dialog isn't indeterminate
			progressDialog.setIndeterminate(false);
			// //The maximum number of items is 100
			// progressDialog.setMax(100);
			// //Set the current progress to zero
			// progressDialog.setProgress(0);
			// Display the progress dialog
			progressDialog.show();
		}

		// The code to be executed in a background thread.
		@Override
		protected Void doInBackground(Void... params) {
			// Get the current thread's token
//			synchronized (this) {
				
				//TODO check if connected to Internet
			
				// Get all Numbers from Telephone-Book
				List<String> allNumbers = new ArrayList<>();
				
				String projection[] = { Phone.NUMBER };
				String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP
						+ " = '" + ("1") + "'" + " AND "
						+ ContactsContract.Contacts.HAS_PHONE_NUMBER;
				String selectionArgs[] = null;
				String sortOrder = Phone.DISPLAY_NAME + " ASC";

				Cursor contacts = getContentResolver().query(Phone.CONTENT_URI,
						projection, selection, selectionArgs, sortOrder);

				// Save all Numbers in a List
				while (contacts.moveToNext()) {

					String contactNumber = contacts
							.getString(contacts
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					allNumbers.add(parseNumber(contactNumber));
				}
				
				contacts.close();

				// Get registered Numbers
				List<String> registeredNumbers;
				registeredNumbers = GcmServer.INSTANCE.getKnownNumbers(allNumbers);
				
				// Get allowedNumbers
				List<String> allowedNumbers;
				
				// Each row in the list stores country name and its status
				aList = new ArrayList<HashMap<String, Object>>();
			
				
//				for (String number : knownNumbers) {
//					HashMap<String, Object> hm = new HashMap<String, Object>();
//					hm.put("txt", MainActivity.getContactName(getApplicationContext(), number));
//					hm.put("numb", number);
//					hm.put("stat", false);
//					aList.add(hm);
//				}

//			}
			return null;
		}

		// Update the progress
		@Override
		protected void onProgressUpdate(Integer... values) {
			// set the current progress of the progress dialog
			progressDialog.setProgress(values[0]);
		}

		// after executing the code in the thread
		@Override
		protected void onPostExecute(Void result) {
			// close the progress dialog
			progressDialog.dismiss();
			// initialize the View
			setContentView(R.layout.activity_allowed_numbers);

			// Keys used in Hashmap
			String[] from = { "txt", "numb", "stat" };

			// Ids of views in listview_layout
			int[] to = { R.id.togglelist_name, R.id.togglelist_number,
					R.id.togglelist_status };

			// Instantiating an adapter to store each items
			// R.layout.listview_layout defines the layout of each item
			SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList,
					R.layout.togglelist, from, to);

			// TODO check if null
			mListView.setAdapter(adapter);
		}
	}
}
