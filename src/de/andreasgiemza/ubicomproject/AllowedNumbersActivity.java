package de.andreasgiemza.ubicomproject;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.andreasgiemza.ubicomproject.gcm.GcmServer;
import de.andreasgiemza.ubicomproject.helpers.Preferences;

public class AllowedNumbersActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new LoadViewTask().execute();
	}

	@Override
	public void onBackPressed() {
		String allowedNumbers = "";

		SparseBooleanArray checked = getListView().getCheckedItemPositions();
		for (int i = 0; i < checked.size(); i++) {
			if (checked.valueAt(i) == true) {
				String[] number = ((String) getListView().getItemAtPosition(
						checked.keyAt(i))).split("\n");
				allowedNumbers += number[1];
				if (i != checked.size() - 1) {
					allowedNumbers += ";";
				}
			}
		}

		Preferences prefs = new Preferences(getApplicationContext());
		prefs.setAllowedNumbers(allowedNumbers);

		super.onBackPressed();
	}

	private class LoadViewTask extends AsyncTask<String, String, String> {

		ProgressDialog loadingDialog;
		List<String> supportedNumbers = new ArrayList<>();

		// Before running code in separate thread
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			loadingDialog = new ProgressDialog(AllowedNumbersActivity.this);
			loadingDialog.setMessage(getResources().getString(R.string.loading));
			loadingDialog.setIndeterminate(false);
			loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			loadingDialog.setCancelable(false);
			loadingDialog.show();
		}

		// The code to be executed in a background thread.
		@Override
		protected String doInBackground(String... params) {
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
			List<String> registeredNumbers = GcmServer.INSTANCE
					.getKnownNumbers(allNumbers);

			for (String number : registeredNumbers) {
				supportedNumbers.add(MainActivity.getContactName(
						getApplicationContext(), number) + "\n" + number);
			}

			return null;
		}

		// after executing the code in the thread
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			loadingDialog.dismiss();

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					AllowedNumbersActivity.this,
					android.R.layout.simple_list_item_multiple_choice,
					supportedNumbers);
			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			setListAdapter(adapter);

			Preferences prefs = new Preferences(getApplicationContext());
			List<String> allowedNumbers = prefs.getAllowedNumbers();

			for (int i = 0; i < adapter.getCount(); i++) {
				String number = adapter.getItem(i).split("\n")[1];

				if (allowedNumbers.contains(number)) {
					getListView().setItemChecked(i, true);
				}
			}
		}
	}

	private String parseNumber(String string) {
		string = string.replaceAll("\\s", "");

		if (string.startsWith("+"))
			return string;
		else if (string.startsWith("00"))
			return string.replaceFirst("00", "+");
		else
			return string.replaceFirst("0", "+49");
	}
}
