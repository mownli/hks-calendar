package hks.calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import org.json.*;


class HKSCalendar {
	private class Day {
		LinkedList<Event> events = new LinkedList<>();

		Day() {
		}

		boolean isEmpty() {
			return events.isEmpty();
		}
	}
	private class Month {
		int nonemptyDays = 0;
		Day[] days;

		Month(int size) {
			days = new Day[size];
		}
	}
	private class Year {
		Month[] months = new Month[12];
		int nonemptyMonths = 0;

		Year() {
		}
	}

	private SparseArray<Year> years = new SparseArray<>();
	LongSparseArray<EventType> eventTypes = new LongSparseArray<>();

	Event closestEvent;


	HKSCalendar() {
	}

	void init() {
		loadData();
		closestEvent = getNextEvent(Calendar.getInstance(), -1);
		if(closestEvent != null)
			alarmSet = true;
	}

	private void insertEvent(Event ev, int year, int month, int day) {
		day -= 1;

		Year y;
		Month m;
		Day d;

		if((y = years.get(year)) == null)
			years.put(year, y = new Year());

		if((m = y.months[month]) == null) {
			y.nonemptyMonths++;
			y.months[month] = m = new Month(Consts.getDaysInMonth(year, month));
		}
		if((d = m.days[day]) == null) {
			m.nonemptyDays++;
			m.days[day] = d = new Day();
		}

		ListIterator<Event> it = d.events.listIterator();

		while(it.hasNext()) {
			if(it.next().compareTo(ev) > 0) {
				it.previous();
				break;
			}
		}
		it.add(ev);
	}
	private void cleanUp(int y, int m, int d) {
		Year year = years.get(y);
		Month month = year.months[m];
		Day day = month.days[d];

		if(!day.isEmpty())
			return;
		month.days[d] = null;
		month.nonemptyDays--;

		if(month.nonemptyDays > 0)
			return;
		year.months[m] = null;
		year.nonemptyMonths--;

		if(year.nonemptyMonths > 0)
			return;
		years.remove(y);
	}

	private void repositionEvent(Event ev, int origYear, int origMonth, int origDay) {
		years.get(origYear).months[origMonth].days[origDay - 1].events.remove(ev);
		insertEvent(ev, ev.ts.get(Calendar.YEAR), ev.ts.get(Calendar.MONTH), ev.ts.get(Calendar.DATE));
		storeData();
	}
	private void resortEvent(Event ev, int year, int month, int day) {
		years.get(year).months[month].days[day - 1].events.sort(null);
		storeData();
	}


	private Event getNextEvent() {
		return getNextEvent(System.currentTimeMillis(), -1);
	}
	private Event getNextEvent(long ts, long id) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(ts);
		return getNextEvent(cal, id);
	}
	private Event getNextEvent(Calendar ts, long id) {
		Log.d(HKSCalendarApp.loggerPrefix, "getNextEvent(Calendar, long): ts=" + ts.getTimeInMillis() + ",id=" + id);
		int y = ts.get(Calendar.YEAR);
		int m = ts.get(Calendar.MONTH);
		int d = ts.get(Calendar.DATE)-1;

		Vector<Integer> arr = new Vector<>();
		for(int i = 0; i < years.size(); i++)
			arr.add(years.keyAt(i));
		arr.sort(null);
		int yearsSize = arr.size();


		Year year;
		Month month;
		Day day;

		int selectedYear;
		for(int i = arr.indexOf(y); i < yearsSize; i++) {
			if(i == -1)
				continue;
			selectedYear = arr.get(i);
			year = years.get(selectedYear);
			for(; m < 12; m++) {
				month = year.months[m];
				if(month == null)
					continue;
				for(; d < month.days.length; d++) {
					day = month.days[d];
					if(day == null)
						continue;
					for(Event ev : day.events) {
						Log.d(HKSCalendarApp.loggerPrefix, "getNextEvent(Calendar, long): checking event " + ev);
						if(ev.ts.getTimeInMillis() == ts.getTimeInMillis()) {
							if(ev.id > id)
								return ev;
						}
						else if(ev.ts.getTimeInMillis() > ts.getTimeInMillis())
							return ev;
					}
				}
				d = 0;
			}
			m = 0;
		}
		return null;
	}

	EventType getEventType(long id) {
		return eventTypes.get(id);
	}


	boolean alarmSet = false;
	private void clearAlarm(Event ev) {
		Log.d(HKSCalendarApp.loggerPrefix, "clearAlarm(): Clearing alarm for event " + ev);
		if(ev == null) {
			Log.d(HKSCalendarApp.loggerPrefix, "clearAlarm(): null cannot be processed; alarm currently is " + (alarmSet ? "" : "not ") + "set");
			return;
		}
		if(!alarmSet) {
			Log.d(HKSCalendarApp.loggerPrefix, "clearAlarm(): No alarm is set, nothing to clear");
			return;
		}
		Intent notifyIntent = new Intent(HKSCalendarApp.appCtx, AlertReceiver.class);
		PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(HKSCalendarApp.appCtx, HKSCalendarApp.getNotificationId(ev.id), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		HKSCalendarApp.alarmManager.cancel(notifyPendingIntent);
		alarmSet = false;
	}
	private void setAlarm(Event ev) {
		Log.d(HKSCalendarApp.loggerPrefix, "setAlarm(): Setting alarm for event " + ev);
		if(ev == null) {
			Log.d(HKSCalendarApp.loggerPrefix, "setAlarm(): null cannot be processed; alarm currently is " + (alarmSet ? "" : "not ") + "set");
			return;
		}
		if(alarmSet) {
			Log.d(HKSCalendarApp.loggerPrefix, "setAlarm(): Alarm is already set");
			return;
		}

		Intent notifyIntent = new Intent(HKSCalendarApp.appCtx, AlertReceiver.class);
		notifyIntent.putExtra("ts", ev.ts.getTimeInMillis());
		notifyIntent.putExtra("id", ev.id);
		PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(HKSCalendarApp.appCtx, HKSCalendarApp.getNotificationId(ev.id), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		//HKSCalendarApp.alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(ev.ts.getTimeInMillis(), notifyPendingIntent), notifyPendingIntent);
		 HKSCalendarApp.alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ev.ts.getTimeInMillis(), notifyPendingIntent);
		alarmSet = true;
	}

	void reschedule(long ts, long id) {
		Log.d(HKSCalendarApp.loggerPrefix, "reschedule(ts, id): ts=" + ts + ",id=" + id);
		Event closestEventCurrent = closestEvent;
		closestEvent = getNextEvent(ts, id);
		if(closestEventCurrent == closestEvent) {
			Log.d(HKSCalendarApp.loggerPrefix, "reschedule(ts, id): Closest event didn't change");
			return;
		}
		clearAlarm(closestEvent);
		Log.d(HKSCalendarApp.loggerPrefix, "reschedule(ts, id): New closest event is " + closestEvent);
		if(closestEvent == null)
			return;

		setAlarm(closestEvent);
	}
	private final static int RESCHEDULE_ACTION_ADD = 0;
	private final static int RESCHEDULE_ACTION_EDIT = 1;
	private final static int RESCHEDULE_ACTION_REMOVE = 2;
	private void reschedule(int action, Event ev) {
		Log.d(HKSCalendarApp.loggerPrefix, "reschedule(): For event " + ev);
		long now = System.currentTimeMillis();
		long evTS = ev.ts.getTimeInMillis();
		if(action != RESCHEDULE_ACTION_EDIT && evTS < now) {
			Log.d(HKSCalendarApp.loggerPrefix, "reschedule()/!edit: Event is in the past");
			return;
		}

		if(action == RESCHEDULE_ACTION_ADD) {
			if(closestEvent == null) {
				Log.d(HKSCalendarApp.loggerPrefix, "reschedule()/added: Event is new closestEvent from null");
				closestEvent = ev;
			}
			else {
				long closestTS = closestEvent.ts.getTimeInMillis();
				if(closestTS < evTS) {
					Log.d(HKSCalendarApp.loggerPrefix, "reschedule()/added: Event is not closest");
					return;
				}
				Log.d(HKSCalendarApp.loggerPrefix, "reschedule()/added: Event is new closestEvent from " + closestEvent);
				clearAlarm(closestEvent);
				closestEvent = ev;
			}
			setAlarm(closestEvent);
		}
		else if(action == RESCHEDULE_ACTION_REMOVE) {
			if(closestEvent != ev) {
				Log.d(HKSCalendarApp.loggerPrefix, "reschedule()/removed: Event is not closest");
				return;
			}
			Log.d(HKSCalendarApp.loggerPrefix, "reschedule()/removed: Event is closestEvent");
			clearAlarm(closestEvent);
			closestEvent = getNextEvent(ev.ts, ev.id);
			Log.d(HKSCalendarApp.loggerPrefix, "reschedule()/removed: New closestEvent is " + closestEvent);
			setAlarm(closestEvent);
		}
		else if(action == RESCHEDULE_ACTION_EDIT) {
			closestEvent = getNextEvent();
			Log.d(HKSCalendarApp.loggerPrefix, "reschedule()/edited: New closestEvent is " + closestEvent);
			clearAlarm(ev);
			setAlarm(closestEvent);
		}
	}

	/**
	 * <p>Adds a new event with parameters provided to the calendar</p>
	 * @return Event object created as a result of the operation.
	 */
	Event addEvent(int year, int month, int day, int hour, int minute, long typeID, String name, String description) {
		Event ev = new Event();
		ev.typeID = typeID;
		ev.name = name;
		ev.description = description;

		ev.ts.set(Calendar.YEAR, year);
		ev.ts.set(Calendar.MONTH, month);
		ev.ts.set(Calendar.DATE, day);
		ev.ts.set(Calendar.HOUR_OF_DAY, hour);
		ev.ts.set(Calendar.MINUTE, minute);
		ev.ts.set(Calendar.SECOND, 0);
		ev.ts.set(Calendar.MILLISECOND, 0);

		insertEvent(ev, year, month, day);

		reschedule(RESCHEDULE_ACTION_ADD, ev);

		eventTypes.get(typeID).usages += 1;

		storeData();

		return ev;
	}
	void removeEvent(Event ev) {
		years.get(ev.ts.get(Calendar.YEAR))
			.months[ev.ts.get(Calendar.MONTH)]
			.days[ev.ts.get(Calendar.DATE)-1]
			.events
			.remove(ev);

		cleanUp(ev.ts.get(Calendar.YEAR), ev.ts.get(Calendar.MONTH), ev.ts.get(Calendar.DATE)-1);

		reschedule(RESCHEDULE_ACTION_REMOVE, ev);

		eventTypes.get(ev.typeID).usages -= 1;

		storeData();
	}
	/**
	 * <p>Updates fields of event from provided values and repositions it within calendar structure if necessary.<p>
	 * @param ev Event to update.
	 * @param year Year to set for the event.
	 * @param month Month to set for the event, from [0, 11] being [January, December].
	 * @param day Year to set for the event, from [1, ?].
	 * @param hour Year to set for the event, from [0, 23].
	 * @param minute Year to set for the event, from [0, 59].
	 * @param typeID Type to set for the event.
	 * @param name Name to set for the event.
	 * @param description Description to set for the event.
	 */
	void updateEvent(Event ev, int year, int month, int day, int hour, int minute, long typeID, String name, String description) {
		int origYear = ev.ts.get(Calendar.YEAR);
		int origMonth = ev.ts.get(Calendar.MONTH);
		int origDay = ev.ts.get(Calendar.DATE);

		boolean dateChanges = false;
		boolean sortChanges = false;

		Log.d(HKSCalendarApp.loggerPrefix, "updateEvent(): from " + ev);

		if(origYear != year) {
			ev.ts.set(Calendar.YEAR, year);
			dateChanges = true;
		}
		if(origMonth != month) {
			ev.ts.set(Calendar.MONTH, month);
			dateChanges = true;
		}
		if(origDay != day) {
			ev.ts.set(Calendar.DATE, day);
			dateChanges = true;
		}

		if(ev.ts.get(Calendar.HOUR_OF_DAY) != hour) {
			ev.ts.set(Calendar.HOUR_OF_DAY, hour);
			sortChanges = true;
		}
		if(ev.ts.get(Calendar.MINUTE) != minute) {
			ev.ts.set(Calendar.MINUTE, minute);
			sortChanges = true;
		}

		Log.d(HKSCalendarApp.loggerPrefix, "updateEvent(): updated to " + ev);
		if(dateChanges)
			repositionEvent(ev, origYear, origMonth, origDay);
		else if(sortChanges)
			resortEvent(ev, year, month, day);

		if(dateChanges || sortChanges)
			reschedule(RESCHEDULE_ACTION_EDIT, ev);

		eventTypes.get(ev.typeID).usages -= 1;
		eventTypes.get(typeID).usages += 1;
		ev.typeID = typeID;
		ev.name = name;
		ev.description = description;

		storeData();
	}

	void addEventType(EventType et) {
		eventTypes.append(et.id, et);
		storeData();
		HKSCalendarApp.createNotificationChannel(et);
	}
	void removeEventType(EventType et) {
		int index = eventTypes.indexOfValue(et);
		if(index == -1)
			return;
		eventTypes.removeAt(index);
		//shrinkEventTypes(index);
		storeData();
		HKSCalendarApp.deleteNotificationChannel(et.getNotificationChannelID());
	}
	void editEventType(EventType et, int color, String name, boolean uSNC) {
		boolean changesMade = false;
		boolean ncChangesMade = false;
		if(et.getColor() != color) {
			et.setColor(color);
			changesMade = true;
		}
		if(!name.equals(et.name)) {
			et.name = name;
			changesMade = true;
			ncChangesMade = true;
		}
		if(uSNC != et.usesSeparateNotificationChannel) {
			et.usesSeparateNotificationChannel = uSNC;
			changesMade = true;
			ncChangesMade = true;
		}
		if(changesMade)
			storeData();
		if(ncChangesMade)
			HKSCalendarApp.editNotificationChannel(et);
	}

	Event findEvent(long id, long ts) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(ts);

		Year year;
		Month month;
		Day day;

		try {
			year =  years.get(cal.get(Calendar.YEAR));
		}
		catch(Exception e) {
			return null;
		}
		if(year == null)
			return null;

		month = year.months[cal.get(Calendar.MONTH)];
		if(month == null)
			return null;

		day = month.days[cal.get(Calendar.DATE) - 1];
		if(day == null)
			return null;

		Iterator<Event> it = day.events.iterator();

		Event ev;
		while(it.hasNext())
			if((ev = it.next()).id == id)
				return ev;
		return null;
	}

	/**
	 * <p>Returns collection of events of specified year.</p>
	 * @param y Year.
	 * @return Vector&lt;Event&gt; of events from specified year.
	 */
	Vector<Event> getEvents(int y) {
		Vector<Event> events = new Vector<>();
		Year year = years.get(y);
		if(year == null)
			return events;

		Month month;
		Day day;

		Iterator<Event> it;

		for(int m = 0; m < 12; m++) {
			month = year.months[m];
			if(month == null)
				continue;
			for(int d = 0; d < month.days.length; d++) {
				day = month.days[d];
				if(day == null)
					continue;
				it = day.events.iterator();
				while(it.hasNext())
					events.add(it.next());
			}
		}

		return events;
	}

	/**
	 * <p>Returns collection of events of specified month of specified year.</p>
	 * @param y Year.
	 * @param m Month from [0, 11] being [January, December].
	 * @return Vector&lt;Event&gt; of events of specified year-month.
	 */
	Vector<Event> getEvents(int y, int m) {
		Vector<Event> events = new Vector<>();
		Year year = years.get(y);
		if(year == null)
			return events;

		Month month = year.months[m];
		if(month == null)
			return events;

		Day day;
		Iterator<Event> it;

		for(int d = 0; d < month.days.length; d++) {
			day = month.days[d];
			if(day == null)
				continue;
			it = day.events.iterator();
			while(it.hasNext())
				events.add(it.next());
		}

		return events;
	}

	/**
	 * <p>Returns collection of events of specified day of specified month of specified year.</p>
	 * @param y Year.
	 * @param m Month from [0, 11] being [January, December].
	 * @param d Day from [1, ?].
	 * @return Vector&lt;Event&gt; of events from specified year-month-day.
	 */
	Vector<Event> getEvents(int y, int m, int d) {
		Vector<Event> events = new Vector<>();
		Year year = years.get(y);
		if(year == null)
			return events;

		Month month = year.months[m];
		if(month == null)
			return events;

		Day day = month.days[d-1];
		if(day == null)
			return events;

		events.addAll(day.events);

		return events;
	}

	/**
	 * <p>Returns collection of days of specified year and month that have at least one event.</p>
	 * @param y Year.
	 * @param m Month.
	 * @return Vector&lt;int&gt; containing days indexes from [1, ?].
	 */
	Vector<Integer> getEventDays(int y, int m) {
		Vector<Integer> eventDays = new Vector<>();
		Year year = years.get(y);
		if(year == null)
			return eventDays;

		Month month = year.months[m];
		if(month == null)
			return eventDays;

		for(int i = 0; i < month.days.length; i++)
			eventDays.add(i+1);

		return eventDays;
	}





	@SuppressWarnings("serial")
	class StorageException extends RuntimeException {
		String message;
		StorageException(String msg) {
			message = msg;
		}
	}


	private void storeData() {
		JSONProcessing(true, HKSCalendarApp.storageLocation);
	}
	private void loadData() {
		JSONProcessing(false, HKSCalendarApp.storageLocation);
	}


	private static int storageVersion = 4;

	private void JSONProcessing(boolean writing, String path) {
		JSONObject json = null;

		if(writing) {
			json = new JSONObject();
			try {
				json.put("version", storageVersion);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			FileInputStream fis = null;
			String jsonString;
			try {
				fis = new FileInputStream(path);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				for(int bytesRead; (bytesRead = fis.read(buffer)) != -1; baos.write(buffer, 0, bytesRead));
				jsonString = baos.toString("UTF-8");
				fis.close();
			}
			catch(FileNotFoundException e) {
				jsonString = "{\"version\": 0}";
				Log.i(HKSCalendarApp.loggerPrefix, "JSONProcessing(writing=" + writing + "): storage no found", e);
			}
			catch(IOException e) {
				e.printStackTrace();
				return;
			}
			finally {
				if(fis != null) {
					try {
						fis.close();
					}
					catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				json = new JSONObject(jsonString);
				DataMigrator m = new DataMigrator();
				m.migrate(json);

				if(storageVersion != json.getInt("version"))
					throw new StorageException("Version of storage read did not reach that of HKSCalendar");
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
		}

		if(writing) {
			Year year;
			Month month;
			Day day;

			JSONArray jsonArrayOfEvents = new JSONArray();

			Iterator<Event> itEv;
			for(int i = 0; i < years.size(); i++) {
				year = years.valueAt(i);

				for(int m = 0; m < 12; m++) {
					month = year.months[m];
					if(month == null)
						continue;
					for(int d = 0; d < month.days.length; d++) {
						day = month.days[d];
						if(day == null)
							continue;
						itEv = day.events.iterator();
						try {
							while(itEv.hasNext())
								jsonArrayOfEvents.put(itEv.next().toJSON());
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			try {
				json.put("events", jsonArrayOfEvents);
			}
			catch(Exception e) {
				e.printStackTrace();
			}


			try {
				JSONArray jsonArrayOfEventTypes = new JSONArray();
				for(int i = 0; i < eventTypes.size(); i++)
					jsonArrayOfEventTypes.put(eventTypes.valueAt(i).toJSON());

				json.put("eventTypes", jsonArrayOfEventTypes);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			Event ev;
			int size;
			try {
				JSONArray eventTypesArray = json.getJSONArray("eventTypes");
				size = eventTypesArray.length();
				EventType et;
				for(int i = 0; i < size; i++) {
					et = EventType.fromJSON(eventTypesArray.getJSONObject(i));
					if(et != null)
						eventTypes.append(et.id, et);
				}

				JSONArray eventsArray = json.getJSONArray("events");
				size = eventsArray.length();
				for(int i = 0; i < size; i++) {
					ev = Event.fromJSON(eventsArray.getJSONObject(i));
					if(ev == null)
						continue;
					insertEvent(ev, ev.ts.get(Calendar.YEAR), ev.ts.get(Calendar.MONTH), ev.ts.get(Calendar.DATE));
					eventTypes.get(ev.typeID).usages += 1;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		if(writing) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(path);
				byte[] jsonBytes = json.toString().getBytes();
				for(byte b : jsonBytes)
					fos.write(b);
			}
			catch(FileNotFoundException e) {
				e.printStackTrace();
				Toast.makeText(HKSCalendarApp.appCtx, HKSCalendarApp.appCtx.getResources().getString(R.string.errorCouldNotSaveStorage) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
			catch(IOException e) {
				e.printStackTrace();
				Toast.makeText(HKSCalendarApp.appCtx, HKSCalendarApp.appCtx.getResources().getString(R.string.errorCouldNotSaveStorage) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
			/*catch(JSONException e) {
				e.printStackTrace();
				Toast.makeText(HKSCalendarApp.appCtx, HKSCalendarApp.appCtx.getResources().getString(R.string.errorCouldNotSaveStorage) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
			}*/
			finally {
				if(fos != null) {
					try {
						fos.close();
					}
					catch(IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else {
		}
	}

	@SuppressWarnings("unused")
	private class DataMigrator {
		void migrate(JSONObject json) throws JSONException {
			int version = json.getInt("version");
			Method migrator;
			while(true) {
				try {
					Log.i(HKSCalendarApp.loggerPrefix, "Migrating storage V" + version);
					migrator = DataMigrator.class.getMethod("V" + version, JSONObject.class);
					migrator.invoke(this, json);
					version++;
				}
				catch(NoSuchMethodException e) {
					Log.i(HKSCalendarApp.loggerPrefix, "Migrator not found");
					break;
				}
				catch(SecurityException e) {
					e.printStackTrace();
					return;
				}
				catch(IllegalAccessException e) {
					e.printStackTrace();
					return;
				}
				catch(IllegalArgumentException e) {
					e.printStackTrace();
					return;
				}
				catch(InvocationTargetException e) {
					e.printStackTrace();
					return;
				}
			}
			json.put("version", version);
		}


		public void V0(JSONObject json) throws JSONException {
			json.put("events", new JSONArray());
			JSONArray predefinedEventTypes = new JSONArray();
			EventType et = new EventType();
			et.name = "Event";
			et.setColor();
			predefinedEventTypes.put(et.toJSON());
			json.put("eventTypes", predefinedEventTypes);
		}
		public void V1(JSONObject json) {
			try {
				JSONArray events = json.getJSONArray("events");
				int size = events.length();
				JSONObject evObj;
				for(int i = 0; i < size; i++) {
					evObj = events.getJSONObject(i);
					evObj.put("id", (long)i);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		public void V2(JSONObject json) {
			try {
				JSONArray eventTypes = json.getJSONArray("eventTypes");
				int size = eventTypes.length();
				JSONObject etObj;
				for(int i = 0; i < size; i++) {
					etObj = eventTypes.getJSONObject(i);
					etObj.put("id", (long)i);
					etObj.put("uSNC", false);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		public void V3(JSONObject json) {
			try {
				JSONArray events = json.getJSONArray("events");
				JSONArray eventTypes = json.getJSONArray("eventTypes");

				int size = events.length();
				JSONObject evObj;
				for(int i = 0; i < size; i++) {
					evObj = events.getJSONObject(i);
					evObj.put("typeID", eventTypes.getJSONObject(evObj.getInt("type")).getLong("id"));
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
