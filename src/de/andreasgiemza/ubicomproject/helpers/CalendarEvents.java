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
	
	public final static int MAX_EVENT_LENGHT = 20; // In Hours
	public final static int MS_PER_HOUR = 3600000;

	public static class MyCalenderEvent {
		private String title;
		private Date start;
		private Date end;
		private String description;

		public MyCalenderEvent(String title, Date start, Date end, String description) {
			this.title = title;
			this.start = start;
			this.end = end;
			this.description = description;
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

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	public MyCalenderEvent getNextEvent(Context context) {
		return readCalendar(context, false);
	}

	public MyCalenderEvent getCurrentEvent(Context context) {
		return readCalendar(context, true);
	}

	public boolean isBusy(Context context) {
		return (readCalendar(context, true) != null);

	}

	/*
	 * current == true -> get current Event current == false -> getNextEvent
	 */
	private MyCalenderEvent readCalendar(Context context, boolean current) {

		long time = Calendar.getInstance().getTimeInMillis();

		ContentResolver contentResolver = context.getContentResolver();

		// Get Title, Start and End-Time, Description
		String[] projection = new String[] { CalendarContract.Events.TITLE,
				CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND,
				CalendarContract.Events.DESCRIPTION };

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

		long start_ms = cursor.getLong(1);
		long end_ms = cursor.getLong(2);

		// delete all Events with more than MAX_EVENT_LENGHT Hours
		while (true) {
			if ((end_ms - start_ms) >= MAX_EVENT_LENGHT * MS_PER_HOUR) { // next event?
				if (!cursor.moveToNext())
					return null;
			} else {
				break;
			}
		}

		Date start = new Date(start_ms);
		Date end = new Date(end_ms);

		MyCalenderEvent event = new MyCalenderEvent(cursor.getString(0), start,
				end, cursor.getString(3));

		cursor.close();

		return event;

	}
}
