package de.andreasgiemza.ubicomproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
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

public class NumberActivity extends Activity {

	String[] countries = new String[] { "India", "Pakistan", "Sri Lanka",
			"China", "Bangladesh", "Nepal", "Afghanistan", "North Korea",
			"South Korea", "Japan", "Deutschland", "Halo", "Hallo",
			"hallohallo" };

	// Array of booleans to store toggle button status
	public boolean[] status = { true, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_number);

		if (savedInstanceState != null) {
			status = savedInstanceState.getBooleanArray("status");
		}
	}

	@Override
	protected void onStart() {

		super.onStart();

		Cursor contacts = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null, null);
		String aNameFromContacts[] = new String[contacts.getCount() + 1];
		String aNumberFromContacts[] = new String[contacts.getCount() + 1];
		
		
		Log.d("Lenght getCount", String.valueOf(contacts.getCount()));
		int i = 0;

		// int nameFieldColumnIndex =
		// contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
		// int numberFieldColumnIndex =
		// contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

		while (contacts.moveToNext()) {

			String contactName = contacts
					.getString(contacts
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			aNameFromContacts[i] = contactName;

			String contactNumber = contacts
					.getString(contacts
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			aNumberFromContacts[i] = contactNumber;
			i++;
		}

		for (int j = 0; j < i; j++) {
			Log.d(aNameFromContacts[j], aNumberFromContacts[j]);
		}

		contacts.close();

		ListView mListView = (ListView) findViewById(R.id.list_number);
		if (mListView == null)
			Log.e("tag", "ListView is null");

		mListView.setOnItemClickListener(itemClickListener);

		// Each row in the list stores country name and its status
		List<HashMap<String, Object>> aList = new ArrayList<HashMap<String, Object>>();

		for (i = 0; i < aNameFromContacts.length; i++) {
			HashMap<String, Object> hm = new HashMap<String, Object>();
			hm.put("txt", aNameFromContacts[i]);
			hm.put("numb", aNumberFromContacts[i]);
			hm.put("stat", false);
			aList.add(hm);
		}

		// Keys used in Hashmap
		String[] from = { "txt", "numb", "stat" };

		// Ids of views in listview_layout
		int[] to = { R.id.togglelist_name, R.id.togglelist_number, R.id.togglelist_status };

		// Instantiating an adapter to store each items
		// R.layout.listview_layout defines the layout of each item
		SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList,
				R.layout.togglelist, from, to);

		mListView.setAdapter(adapter);
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View item, int position,
				long id) {

			ListView listView = (ListView) parent;

			SimpleAdapter adapter = (SimpleAdapter) listView.getAdapter();

			HashMap<String, Object> hm = (HashMap) adapter.getItem(position);

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

}
