package hks.calendar;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.util.GregorianCalendar;
import java.util.Random;
import org.json.JSONObject;

class EventType {
	long id = new GregorianCalendar().getTimeInMillis();
	String name = "Event";
	private int color = 0;
	int usages = 0;
	boolean usesSeparateNotificationChannel = false;


	EventType() {}
	/**
	 * <p>Creates event type from a JSON representation.</p>
	 * <p>Four fields are expected:<br/>
	 * long id<br/>
	 * String name<br/>
	 * int color<br/>
	 * boolean uSNC (usesSeparateNotificationChannel).</p>
	 */
	static EventType fromJSON(JSONObject json) {
		EventType et = new EventType();
		try {
			et.id = json.getLong("id");
			et.name = json.getString("name");
			et.color = json.getInt("color");
			et.usesSeparateNotificationChannel = json.getBoolean("uSNC");
		}
		catch(Exception e) {
			Log.e(HKSCalendarApp.loggerPrefix, "EventType.formJSON(): Failed to construct from JSON representation", e);
			return null;
		}
		return et;
	}

	JSONObject toJSON() {
		JSONObject json;
		try {
			json = new JSONObject();
			json.put("id", id);
			json.put("name", name);
			json.put("color", color);
			json.put("uSNC", usesSeparateNotificationChannel);
		}
		catch(Exception e) {
			Log.e(HKSCalendarApp.loggerPrefix, "EventType.toJSON(): failed to construct JSONObject", e);
			return null;
		}
		return json;
	}

	String getNotificationChannelID() {
		return HKSCalendarApp.notificationChannelPrefix + id;
	}

	String getNotificationChannelIDEffective() {
		if(usesSeparateNotificationChannel)
			return getNotificationChannelID();
		return HKSCalendarApp.notificationChannelID;
	}

	String getNotificationChannelDescription() {
		if(usesSeparateNotificationChannel)
			return "";
		return HKSCalendarApp.appCtx.getResources().getString(R.string.notificationChannelUsesGlobalSettings);
	}

	Spannable toStringShort() {
		Spannable sb = new SpannableString(HKSCalendarApp.bullet + " " + name);
		sb.setSpan(new ForegroundColorSpan(getColor()), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return sb;
	}

	boolean isInUse() {
		return usages != 0;
	}

	/**
	 * <p>Sets color from four ints -- <strong>r</strong>ed, <strong>g</strong>reen <strong>b</strong>lue and <strong>a</strong>lpha.</p>
	 * <p>All ints will have their "extra" bits removed via &255.</p>
	 *
	 * @param r Red component.
	 * @param g Green component.
	 * @param b Blue component.
	 * @param a Alpha component.
	 */
	void setColor(int r, int g, int b, int a) {
		color = ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);
	}
	/**
	 * <p>Sets color from three ints -- <strong>r</strong>ed, <strong>g</strong>reen <strong>b</strong>lue. Alpha will be set to 255 (100% opaque).</p>
	 * <p>All ints will have their "extra" bits removed via &255.</p>
	 *
	 * @param r Red component.
	 * @param g Green component.
	 * @param b Blue component.
	 */
	void setColor(int r, int g, int b) {
		setColor(r, g, b, 255);
	}
	/**
	 * <p>Sets color represented by a single int.</p>
	 * @param argb An int of structure (8 bits for Alpha, 8 bits for Red, 8 bits for Green, 8 bits for Blue).
	 */
	void setColor(int argb) {
		color = argb;
	}
	/**
	 * <p>Sets random color.</p>
	 */
	void setColor() {
		Random rand = new Random();
		setColor(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
	}

	/**
	 * @return An int representing color as ARGB;
	 */
	int getColor() {
		return color;
	}

	/**
	 * @return Alpha component of color from [0, 255].
	 */
	int getColorA() {
		return (color >> 24) & 255;
	}
	/**
	 * @return Red component of color from [0, 255].
	 */
	int getColorR() {
		return (color >> 16) & 255;
	}
	/**
	 * @return Green component of color from [0, 255].
	 */
	int getColorG() {
		return (color >> 8) & 255;
	}
	/**
	 * @return Blue component of color from [0, 255].
	 */
	int getColorB() {
		return color & 255;
	}


	/**
	 * @return A string representation of color in form of #AARRGGBB as hexadecimal.
	 */
	String getColorHEX() {
		return "#" + Integer.toHexString(color);
	}
	/**
	 * @return A string representation of color in form of rgb(<strong>r</strong>, <strong>g</strong>, <strong>b</strong>).
	 * <strong>r</strong>, <strong>g</strong> and <strong>b</strong> are within [0, 255].
	 */
	String getColorRGB() {
		return "rgb(" + getColorR() + "," + getColorG() + "," + getColorB() + ")";
	}
	/**
	 * @return A string representation of color in form of rgba(<strong>r</strong>, <strong>g</strong>, <strong>b</strong>, <strong>a</strong>).
	 * <strong>r</strong>, <strong>g</strong>, <strong>b</strong> and <strong>a</strong> are within [0, 255].
	 */
	String getColorRGBA() {
		return "rgba(" + getColorR() + "," + getColorG()  + "," + getColorB() + "," + getColorA() + ")";
	}
	/**
	 * @return A string representation of color in form of argb(<strong>a</strong>, <strong>r</strong>, <strong>g</strong>, <strong>b</strong>).
	 * <strong>r</strong>, <strong>g</strong>, <strong>b</strong> and <strong>a</strong> are within [0, 255].
	 */
	String getColorARGB() {
		return "argb(" + getColorA() + "," + getColorR()  + "," + getColorG() + "," + getColorB() + ")";
	}
}
