package hks.calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Vector;


public class ActivityMain extends AppCompatActivity {
	static final String YEAR_MSG = "y";
	static final String MONTH_MSG = "m";
	static final String DAY_MSG = "d";
	static final String HOUR_MSG = "h";
	static final String MINUTE_MSG = "min";
	static final String NAME_MSG = "n";
	static final String DESCRIPTION_MSG = "desc";
	static final String TYPE_MSG = "t";
	static final String EDITED_EVENT_TYPES_MSG = "eET";

	static final String DELETE_EVENT_MSG = "del";

	static final int ADD_EVENT = 0;
	static final int EDIT_EVENT = 1;
	static final int EDIT_TYPES = 2;

	static final String ADD_ACTION = "add";
	static final String EDIT_ACTION = "edit";

	final private ArrayList<EventButton> eventButtons = new ArrayList<>();

	private ViewHKSCalendar calView;
	private LinearLayout eventsLayout;

	private Event editedEvent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		eventsLayout = findViewById(R.id.eventsLayout);

		calView = findViewById(R.id.viewHKSCalendar);
		calView.init();
		calView.dayButtonClickListener = () -> {
			Log.d(HKSCalendarApp.loggerPrefix, "onDayChanged(): Day changed");
			updateEventButtons();
		};


		Button addButton = findViewById(R.id.addEventButton);
		addButton.setOnClickListener(view -> {
			Log.d(HKSCalendarApp.loggerPrefix, "onAddEventButtonClicked(): addButton clicked");

			Intent intent = new Intent(this, ActivityEditEvent.class);

			intent.putExtra(YEAR_MSG, calView.getChosenYear());
			intent.putExtra(MONTH_MSG, calView.getChosenMonth());
			intent.putExtra(DAY_MSG, calView.getChosenDay());

			intent.setAction(ADD_ACTION);

			startActivityForResult(intent, ADD_EVENT);

			/*Calendar date = new GregorianCalendar();
			int hh = date.get(Calendar.HOUR_OF_DAY);
			int mm = date.get(Calendar.MINUTE) + 1;

			addEvent(2020, 5, 18, hh, mm, (short)0, "asd", "qweqw");*/
		});

		updateEventButtons();
	}

	private Button yearMonth;
	private void setYearMonth(int y, int m) {
		yearMonth.setText(getString(R.string.topButtonText, Consts.getMonthName(m), y));
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_settings, menu);

		// Should be after inflation
		yearMonth = (Button) menu.findItem(R.id.menu_date_chooser_button).getActionView();
		calView.linkWithYearMonthButton(yearMonth);
		yearMonth.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

		setYearMonth(calView.getGlobalYear(), calView.getGlobalMonth());

		TypedValue outValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
		yearMonth.setBackgroundResource(outValue.resourceId);

		yearMonth.setOnClickListener(v -> {
			new DialogMonthSelection(this, true, calView.getGlobalYear()) {
				@Override
				public void selected(int m, int y) {
					if(y == -1)
						y = calView.getGlobalYear();

					calView.setMonth(y, m);

					setYearMonth(y, m);
				}
			};
		});

		return true;
	}

	private void updateEventButtons() {
		Log.d(HKSCalendarApp.loggerPrefix, "updateEventButtons(): Updating");
		// When not chosen (i.e. changing month)
		if(calView.getChosenDay() == -1) {
			Log.d(HKSCalendarApp.loggerPrefix, "updateEventButtons(): No day chosen");
			eventButtons.clear();
			eventsLayout.removeAllViews();
			return;
		}

		Vector<Event> events = calView.getEventsAtChosenDay();
		int minSize = Math.min(events.size(), eventButtons.size());

		for(int i = 0; i < minSize; i++) {
			Log.d(HKSCalendarApp.loggerPrefix, "updateEventButtons(): Editing existing buttons");
			eventButtons.get(i).setEvent(events.get(i));
		}

		// If there are more events than buttons
		if(events.size() > eventButtons.size()) {
			for(int i = eventButtons.size(); i < events.size(); i++) {
				Log.d(HKSCalendarApp.loggerPrefix, "updateEventButtons(): Adding buttons");
				EventButton btn = new EventButton(this, events.get(i));
				eventsLayout.addView(btn);
				btn.setOnClickListener(onEventButtonClicked);
				eventButtons.add(btn);
			}
		}
		// If there are extra buttons
		else if(events.size() < eventButtons.size()) {
			Log.d(HKSCalendarApp.loggerPrefix, "updateEventButtons(): Deleting buttons");
			int btnsSize = eventButtons.size();
			for(int i = events.size(); i < btnsSize; i++) {
				eventsLayout.removeView(eventButtons.get(eventButtons.size() - 1));
				eventButtons.remove(eventButtons.size() - 1);
			}
		}
	}

	final View.OnClickListener onEventButtonClicked = view ->
	{
		Log.d(HKSCalendarApp.loggerPrefix, "onEventButtonClicked(): Event button clicked");
		EventButton btn = (EventButton) view;
		editedEvent = btn.getEvent();

		Intent intent = new Intent(this, ActivityEditEvent.class);

		int year = -1;
		if((year = calView.getChosenYear()) == -1)
			year = calView.getGlobalYear();
		intent.putExtra(YEAR_MSG, year);

		int month = -1;
		if((month = calView.getChosenMonth()) == -1)
			month = calView.getGlobalMonth();
		intent.putExtra(MONTH_MSG, month);

		intent.putExtra(DAY_MSG, calView.getChosenDay());

		intent.putExtra(HOUR_MSG, editedEvent.getHour());
		intent.putExtra(MINUTE_MSG, editedEvent.getMinute());

		intent.putExtra(NAME_MSG, editedEvent.name);
		intent.putExtra(DESCRIPTION_MSG, editedEvent.description);
		intent.putExtra(TYPE_MSG, editedEvent.typeID);

		intent.setAction(EDIT_ACTION);

		startActivityForResult(intent, EDIT_EVENT);
	};

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		// Handle item selection
		Intent intent = null;
		switch(item.getItemId()) {
			case R.id.action_event_types:
				intent = new Intent(this, ActivityEventTypesList.class);
				startActivityForResult(intent, EDIT_TYPES);
				return true;
			case R.id.action_notification_settings:
				intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
				intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
				startActivity(intent);
				return true;
			case R.id.action_go_home:
				calView.goToCurrentDay();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(HKSCalendarApp.loggerPrefix, "onActivityResult(): Returned to ActivityMain, resultCode: " + resultCode);

		if(resultCode == RESULT_OK) {
			Log.d(HKSCalendarApp.loggerPrefix, "onActivityResult(): resultCode == RESULT_OK");
			if(requestCode == ADD_EVENT) {
				Log.d(HKSCalendarApp.loggerPrefix, "onActivityResult(): ADD_EVENT result processing");
				int year = data.getIntExtra(YEAR_MSG, -1);
				int month = data.getIntExtra(MONTH_MSG, -1);
				int day = data.getIntExtra(DAY_MSG, -1);
				int hour = data.getIntExtra(HOUR_MSG, -1);
				int minute = data.getIntExtra(MINUTE_MSG, -1);
				String name = data.getStringExtra(NAME_MSG);
				String description = data.getStringExtra(DESCRIPTION_MSG);
				long type = data.getLongExtra(TYPE_MSG, -1L);

				if(year == -1 || month == -1 || day == -1 || hour == -1 || minute == -1 || name == null || description == null || type == -1)
					Log.d(HKSCalendarApp.loggerPrefix, "onActivityResult(): ADD_EVENT msg error");

				calView.addEvent(
						year,
						month,
						day,
						hour,
						minute,
						type,
						name,
						description);
			}
			else if(requestCode == EDIT_EVENT) {
				Log.d(HKSCalendarApp.loggerPrefix, "onActivityResult(): EDIT_EVENT result processing");
				if(data.getBooleanExtra(DELETE_EVENT_MSG, false)) {
					calView.removeEvent(editedEvent);
					editedEvent = null;
				}
				else {
					int year = data.getIntExtra(YEAR_MSG, -1);
					int month = data.getIntExtra(MONTH_MSG, -1);
					int day = data.getIntExtra(DAY_MSG, -1);
					int hour = data.getIntExtra(HOUR_MSG, -1);
					int minute = data.getIntExtra(MINUTE_MSG, -1);
					String name = data.getStringExtra(NAME_MSG);
					String description = data.getStringExtra(DESCRIPTION_MSG);
					long type = data.getLongExtra(TYPE_MSG, 0L);

					if(year == -1 || month == -1 || day == -1 || hour == -1 || minute == -1 || name == null || description == null || type == -1)
						Log.d(HKSCalendarApp.loggerPrefix, "onActivityResult(): EDIT_EVENT msg error");

					calView.updateEvent(editedEvent, year, month, day, hour, minute, type, name, description);
					editedEvent = null;
				}
			}
			else if(requestCode == EDIT_TYPES) {
				long[] modifiedTypes = data.getLongArrayExtra(EDITED_EVENT_TYPES_MSG);
				if(modifiedTypes == null)
					return;

				calView.updateTypes(modifiedTypes);
			}

			updateEventButtons();
		}
	}
}
