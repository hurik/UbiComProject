package de.andreasgiemza.ubicomproject.helpers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import de.andreasgiemza.ubicomproject.MainActivity;

public class Preferences {

	public static final String GCMPoject = "470689489809";
	public static final String GCMServer = "http://ucp.g8j.de/";

	public static final int MAX_DISTANCE = 250;
	public static final int MIN_TIME = 20 * 60 * 1000;

	private static final String NUMBER = "number";
	private static final String REG_ID = "regId";
	private static final String APP_VERSION = "appVersion";
	private static final String ALLOWED = "allowed";

	private final Context context;
	private final SharedPreferences prefs;
	private final SharedPreferences.Editor editor;

	public Preferences(Context context) {
		this.context = context;
		prefs = context.getSharedPreferences(
				MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		editor = prefs.edit();
	}

	public String getNumber() {
		return prefs.getString(NUMBER, "");
	}

	public void setNumber(String number) {
		editor.putString(NUMBER, number);
		editor.commit();
	}

	public String getRegId() {
		return prefs.getString(REG_ID, "");
	}

	public void setRegId(String number) {
		editor.putString(REG_ID, number);
		editor.commit();
	}

	public void setAllowedNumbers(String allowedNumbers) {
		editor.putString(ALLOWED, allowedNumbers);
		editor.commit();
	}

	public String getAllowedNumbers() {
		return prefs.getString(ALLOWED, "");
	}

	public List<String> getAllowedNumbersAsList() {
		List<String> result = new ArrayList<>();
		String numbers = prefs.getString(ALLOWED, "");

		if (numbers.equals(""))
			return result;

		String numberList[] = numbers.split(";");

		for (String number : numberList) {
			result.add(number);
		}

		return result;
	}

	public int getAppVersion() {
		return prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
	}

	public void setAppVersion(int appVersion) {
		editor.putInt(APP_VERSION, appVersion);
		editor.commit();
	}

	public int getCurrentAppVersion() {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			Log.d("RegisterActivity",
					"I never expected this! Going down, going down!" + e);
			throw new RuntimeException(e);
		}
	}

	public boolean isRegistered() {
		// Check if number and regId is set and if the current app version is
		// the same like the app version when it was registered
		if (getRegId().isEmpty() || getNumber().isEmpty()
				|| getAppVersion() != getCurrentAppVersion())
			return false;
		else
			return true;

	}
}
