package de.andreasgiemza.ubicomproject;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.andreasgiemza.ubicomproject.gcm.GcmServer;
import de.andreasgiemza.ubicomproject.helpers.Phonebook;
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
			// Get all Numbers from Telephone-Book
			List<String> allNumbers = Phonebook
					.getAllNumbers(getApplicationContext());

			// Get registered Numbers
			List<String> registeredNumbers = GcmServer.INSTANCE
					.getKnownNumbers(allNumbers);

			for (String number : registeredNumbers) {
				supportedNumbers.add(Phonebook.getContactName(
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
			List<String> allowedNumbers = prefs.getAllowedNumbersAsList();

			for (int i = 0; i < adapter.getCount(); i++) {
				String number = adapter.getItem(i).split("\n")[1];

				if (allowedNumbers.contains(number)) {
					getListView().setItemChecked(i, true);
				}
			}
		}
	}

}
