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

	String[] countries = new String[] { "India", "Pakistan", "Sri Lanka",
			"China", "Bangladesh", "Nepal", "Afghanistan", "North Korea",
			"South Korea", "Japan", "Deutschland", "Halo", "Hallo",
			"hallohallo" };

	// Array of booleans to store toggle button status
	public boolean[] status = { true, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false };

	//A ProgressDialog object  
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

		String projection[] = null;
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
				+ ("1") + "'" + " AND "
				+ ContactsContract.Contacts.HAS_PHONE_NUMBER;
		String selectionArgs[] = null;
		String sortOrder = Phone.DISPLAY_NAME + " ASC";

		Cursor contacts = getContentResolver().query(Phone.CONTENT_URI,
				projection, selection, selectionArgs, sortOrder);
		String aNameFromContacts[] = new String[contacts.getCount() + 1];
		String aNumberFromContacts[] = new String[contacts.getCount() + 1];

		Log.d("Lenght getCount", String.valueOf(contacts.getCount()));
		int i = 0;

		while (contacts.moveToNext()) {

			String contactName = contacts
					.getString(contacts
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			aNameFromContacts[i] = contactName;

			String contactNumber = contacts
					.getString(contacts
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			aNumberFromContacts[i] = parseNumber(contactNumber);
			i++;
		}

		contacts.close();

		GcmServer.INSTANCE.getKnownNumbers(Arrays.asList(aNumberFromContacts));

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
		int[] to = { R.id.togglelist_name, R.id.togglelist_number,
				R.id.togglelist_status };

		// Instantiating an adapter to store each items
		// R.layout.listview_layout defines the layout of each item
		SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList,
				R.layout.togglelist, from, to);

		mListView.setAdapter(adapter);
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

	 private class LoadViewTask extends AsyncTask<Void, Integer, Void>  
	    {  
	        //Before running code in separate thread  
	        @Override  
	        protected void onPreExecute()  
	        {  
	            //Create a new progress dialog  
	            progressDialog = new ProgressDialog(AllowedNumbersActivity.this);  
	            //Set the progress dialog to display a horizontal progress bar  
	            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
	            //Set the dialog title to 'Loading...'  
	            progressDialog.setTitle("Loading...");  
	            //Set the dialog message to 'Loading application View, please wait...'  
	            progressDialog.setMessage("Loading application View, please wait...");  
	            //This dialog can't be canceled by pressing the back key  
	            progressDialog.setCancelable(false);  
	            //This dialog isn't indeterminate  
	            progressDialog.setIndeterminate(false);  
	            //The maximum number of items is 100  
	            progressDialog.setMax(100);  
	            //Set the current progress to zero  
	            progressDialog.setProgress(0);  
	            //Display the progress dialog  
	            progressDialog.show();  
	        }  
	  
	        //The code to be executed in a background thread.  
	        @Override  
	        protected Void doInBackground(Void... params)  
	        {  
	            /* This is just a code that delays the thread execution 4 times, 
	             * during 850 milliseconds and updates the current progress. This 
	             * is where the code that is going to be executed on a background 
	             * thread must be placed. 
	             */  
	            try  
	            {  
	                //Get the current thread's token  
	                synchronized (this)  
	                {  
	                    //Initialize an integer (that will act as a counter) to zero  
	                    int counter = 0;  
	                    //While the counter is smaller than four  
	                    while(counter <= 4)  
	                    {  
	                        //Wait 850 milliseconds  
	                        this.wait(850);  
	                        //Increment the counter  
	                        counter++;  
	                        //Set the current progress.  
	                        //This value is going to be passed to the onProgressUpdate() method.  
	                        publishProgress(counter*25);  
	                    }  
	                }  
	            }  
	            catch (InterruptedException e)  
	            {  
	                e.printStackTrace();  
	            }  
	            return null;  
	        }  
	  
	        //Update the progress  
	        @Override  
	        protected void onProgressUpdate(Integer... values)  
	        {  
	            //set the current progress of the progress dialog  
	            progressDialog.setProgress(values[0]);  
	        }  
	  
	        //after executing the code in the thread  
	        @Override  
	        protected void onPostExecute(Void result)  
	        {  
	            //close the progress dialog  
	            progressDialog.dismiss();  
	            //initialize the View  
	            setContentView(R.layout.activity_allowed_numbers);  
	        }  
	    } 
}
