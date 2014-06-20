package de.andreasgiemza.ubicomproject.helpers;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;

public final class Phonebook {

	public static List<String> getAllNumbers(Context context) {
		List<String> allNumbers = new ArrayList<>();

		String projection[] = { Phone.NUMBER };
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
				+ ("1") + "'" + " AND "
				+ ContactsContract.Contacts.HAS_PHONE_NUMBER;
		String selectionArgs[] = null;
		String sortOrder = Phone.DISPLAY_NAME + " ASC";

		Cursor contacts = context.getContentResolver().query(Phone.CONTENT_URI,
				projection, selection, selectionArgs, sortOrder);

		// Save all Numbers in a List
		while (contacts.moveToNext()) {
			String contactNumber = contacts
					.getString(contacts
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			allNumbers.add(parseNumber(contactNumber));
		}

		contacts.close();

		return allNumbers;
	}

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

	private static String parseNumber(String string) {
		string = string.replaceAll("\\s", "");

		if (string.startsWith("+"))
			return string;
		else if (string.startsWith("00"))
			return string.replaceFirst("00", "+");
		else
			return string.replaceFirst("0", "+49");
	}
}
