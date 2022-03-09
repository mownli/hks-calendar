package hks.calendar;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.content.Context;
import android.text.style.StrikethroughSpan;
import android.util.Log;

import java.util.Calendar;
import org.json.JSONObject;

class Event implements Comparable<Event> {
	long typeID = 0;
	String name = "";
	String description = "";
	Calendar ts = Calendar.getInstance();
	long id = ts.getTimeInMillis();

	Event() {
	}

	/**
	 * <p>Creates event from a JSON representation. Five fields are expected:</p><ol>
	 * <li>long <strong>id</strong> -- event id.</li>
	 * <li>long <strong>ts</strong> -- event timestamp in milliseconds.</li>
	 * <li>short <strong>type</strong> -- event type.</li>
	 * <li>String <strong>name</strong> -- name of event.</li>
	 * <li>String <strong>description</strong> -- description of event.</li>
	 * </ol>
	 * @param json JSON with fields to fill the event.
	 */
	static Event fromJSON(JSONObject json) {
		Event ev = new Event();
		try {
			ev.id = json.getLong("id");
			ev.ts.setTimeInMillis(json.getLong("ts"));
			ev.typeID = json.getLong("typeID");
			ev.name = json.getString("name");
			ev.description = json.getString("description");
		}
		catch(Exception e) {
			Log.e(HKSCalendarApp.loggerPrefix, "Event(JSONObject): failed to construct Event from JSON representation", e);
			return null;
		}
		return ev;
	}
	JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("id", id);
			json.put("ts", ts.getTimeInMillis());
			json.put("typeID", typeID);
			json.put("name", name);
			json.put("description", description);
		}
		catch(Exception e) {
			Log.e(HKSCalendarApp.loggerPrefix, "Event.toJSON(): failed to construct JSONObject", e);
			return null;
		}
		return json;
	}

	EventType getEventType() {
		return HKSCalendarApp.c.getEventType(typeID);
	}

	int getYear()  {
		return ts.get(Calendar.YEAR);
	}
	int getMonth()  {
		return ts.get(Calendar.MONTH);
	}
	int getDay()  {
		return ts.get(Calendar.DAY_OF_MONTH);
	}
	int getHour() {
		return ts.get(Calendar.HOUR_OF_DAY);
	}
	int getMinute() {
		return ts.get(Calendar.MINUTE);
	}

	/**
	 * @return A string ({@link Spannable}) representation of event in form of (colored bullet) HH:mm name.
	 */
	Spannable toStringShort() {
		int hour = ts.get(Calendar.HOUR_OF_DAY);
		int minute = ts.get(Calendar.MINUTE);
		String time = (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute;

		Spannable sb = new SpannableString(HKSCalendarApp.bullet + " " + time  + " " + name);
		sb.setSpan(new ForegroundColorSpan(getEventType().getColor()), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sb.setSpan(new ForegroundColorSpan(HKSCalendarApp.appCtx.getResources().getColor(R.color.timeColor)), 2, 2 + time.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return sb;
	}

	@Override
	public String toString() {
		return "{id=" + id + ",name=" + name + ",type=" + typeID + ",ts=" + ts.getTimeInMillis() + "}";
	}

	@Override
	public int compareTo(Event ev) {
		long diff = ts.getTimeInMillis() - ev.ts.getTimeInMillis();
		if(diff > 0)
			return 1;
		if(diff < 0)
			return -1;
		diff = id - ev.id;
		if(diff > 0)
			return 1;
		if(diff < 0)
			return -1;
		return 0;
	}
}
