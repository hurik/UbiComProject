package de.andreasgiemza.ubicomproject.helpers;

import java.util.Calendar;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;

/*
 * TODO
 * ACHTUNG!!! Feiertage werden auch mit angezeigt -> als kompletter belegter Tag -< FEATURE ;)
 * 
 */

public enum CalendarEvents {
	INSTANCE;
	
	public static class MyCalenderEvent {
		private String title;
		private Date start;
		private Date end;

		public MyCalenderEvent(String title, Date start, Date end) {
			this.title = title;
			this.start = start;
			this.end = end;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public Date getStart() {
			return start;
		}

		public void setStart(Date start) {
			this.start = start;
		}

		public Date getEnd() {
			return end;
		}

		public void setEnd(Date end) {
			this.end = end;
		}
	}

	private CalendarEvents() {
	}

	public MyCalenderEvent getNextEvent(Context context) {
		return readCalendar(context, false);
	}

	public MyCalenderEvent getCurrentEvent(Context context) {
		return readCalendar(context, true);
	}

	public  boolean isBusy(Context context) {
		return (readCalendar(context, true) != null);

	}

	/*
	 * current == true -> get current Event current == false -> getNextEvent
	 */
	private MyCalenderEvent readCalendar(Context context, boolean current) {

		long time = Calendar.getInstance().getTimeInMillis();

		ContentResolver contentResolver = context.getContentResolver();

		// Get Title, Start and End-Time
		String[] projection = new String[] { CalendarContract.Events.TITLE,
				CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND };

		String selection = null;
		if (current) {

			selection = "(" + CalendarContract.Events.DTSTART + " < " + time
					+ " and " + time + " < " + CalendarContract.Events.DTEND
					+ " )";
		} else {
			selection = "( " + CalendarContract.Events.DTSTART + " >" + time
					+ " )";
		}

		Cursor cursor = contentResolver.query(
				CalendarContract.Events.CONTENT_URI, projection, selection,
				null, CalendarContract.Events.DTSTART + " ASC");
		if (!cursor.moveToFirst())
			return null; // Keine Daten -> return

		Date start = new Date(cursor.getLong(1));
		Date end = new Date(cursor.getLong(2));

		MyCalenderEvent event = new MyCalenderEvent(cursor.getString(0), start,
				end);

		cursor.close();

		return event;

	}
}
