package hks.calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Vector;

public class ActivityEventTypesList extends AppCompatActivity {
	public final static String ACTION_EDIT_EVENT_TYPE = "aE";
	public final static String EDIT_EVENT_PARAM_COLOR = "c";
	public final static String EDIT_EVENT_PARAM_NAME = "n";
	public final static String EDIT_EVENT_PARAM_USNC = "uSNC";
	public final static String EDIT_EVENT_PARAM_DELETION_ALLOWED = "dA";

	public final static int ACTION_TYPE_ADD_EVENT = 0;
	public final static int ACTION_TYPE_EDIT_EVENT = 1;

	public final static String ACTION_RESULT = "r";
	public final static int ACTION_RESULT_DELETED = 0;
	public final static int ACTION_RESULT_EDITED = 1;

	private LinearLayout list;
	private LongSparseArray<EventType> eventTypes;
	private Vector<TextView> buttonsEventTypes;

	private EventType editedEventType;
	private TextView editedEventTypeButton;
	private ImageButton editedEventTypeNCButton;

	private Vector<Long> editedEventTypes;
	private Vector<Integer> eventTypesColorsInitial;

	private HKSCalendar c;

	private LayoutInflater li;

	void addButton(EventType et) {
		ConstraintLayout layout = (ConstraintLayout) li.inflate(R.layout.layout_event_type_button, list, false);
		list.addView(layout);

		TextView button = layout.findViewById(R.id.eventTypeButton);
		buttonsEventTypes.add(button);

		ImageButton ncSettings = layout.findViewById(R.id.eventTypeNCSettingsButton);
		ncSettings.setOnClickListener(view -> {
			Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
			intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
			intent.putExtra(Settings.EXTRA_CHANNEL_ID, et.getNotificationChannelID());
			startActivity(intent);
		});

		button.setText(et.toStringShort());
		button.setOnClickListener(view -> {
			Intent intent = new Intent(this, ActivityEditEventType.class);

			intent.setAction(ACTION_EDIT_EVENT_TYPE);
			intent.putExtra(EDIT_EVENT_PARAM_COLOR, et.getColor());
			intent.putExtra(EDIT_EVENT_PARAM_NAME, et.name);
			intent.putExtra(EDIT_EVENT_PARAM_USNC, et.usesSeparateNotificationChannel);
			intent.putExtra(EDIT_EVENT_PARAM_DELETION_ALLOWED, (buttonsEventTypes.size() > 1) && !et.isInUse());

			editedEventType = et;
			editedEventTypeButton = button;
			editedEventTypeNCButton = ncSettings;

			startActivityForResult(intent, ACTION_TYPE_EDIT_EVENT);
		});


		if(!et.usesSeparateNotificationChannel)
			ncSettings.setImageAlpha(50);
	}

	void updateButton() {
		editedEventTypeButton.setText(editedEventType.toStringShort());
		editedEventTypeNCButton.setImageAlpha(50 + (editedEventType.usesSeparateNotificationChannel ? 50 : 0));
	}

	@Override
	protected void onActivityResult(int actionType, int resultCode, Intent data) {
		super.onActivityResult(actionType, resultCode, data);

		if(resultCode == RESULT_OK) {
			if(actionType == ACTION_TYPE_EDIT_EVENT) {
				final int actionResult = data.getIntExtra(ACTION_RESULT, 1);

				if(actionResult == ACTION_RESULT_DELETED) {
					int idx = buttonsEventTypes.indexOf(editedEventTypeButton);
					c.removeEventType(editedEventType);
					buttonsEventTypes.remove(idx);
					if(eventTypesColorsInitial.size() < idx)
						eventTypesColorsInitial.removeElementAt(idx);
					list.removeView((View) editedEventTypeButton.getParent());
				}
				else {
					if(editedEventType.isInUse()) {

						if(editedEventTypes.indexOf(editedEventType.id) == -1)
							editedEventTypes.add(editedEventType.id);
					}

					c.editEventType(editedEventType,
							data.getIntExtra(EDIT_EVENT_PARAM_COLOR, 0),
							data.getStringExtra(EDIT_EVENT_PARAM_NAME),
							data.getBooleanExtra(EDIT_EVENT_PARAM_USNC, false));
					updateButton();
				}
			}
			else if(actionType == ACTION_TYPE_ADD_EVENT) {
				EventType et = new EventType();
				et.setColor(data.getIntExtra(EDIT_EVENT_PARAM_COLOR, 0));
				et.name = data.getStringExtra(EDIT_EVENT_PARAM_NAME);
				c.addEventType(et);
				addButton(et);
			}
		}
		else {
			//Shit happened
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_types_list);

		getSupportActionBar().setTitle(R.string.titleActivityEventTypeList);

		li = LayoutInflater.from(this);

		list = findViewById(R.id.eventTypesList);

		c = HKSCalendarApp.c;

		eventTypes = c.eventTypes;
		buttonsEventTypes = new Vector<>();
		editedEventTypes = new Vector<>();
		eventTypesColorsInitial = new Vector<>();

		int size = eventTypes.size();
		EventType et;
		for(int i = 0; i < size; i++) {
			et = eventTypes.valueAt(i);
			addButton(et);
			eventTypesColorsInitial.add(et.getColor());
		}

		Button addButton = findViewById(R.id.buttonAddEventType);

		addButton.setOnClickListener(view -> {
			Intent intent = new Intent(this, ActivityEditEventType.class);
			startActivityForResult(intent, ACTION_TYPE_ADD_EVENT);
		});
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		Intent data = new Intent();
		int size = editedEventTypes.size();
		if(size > 0) {
			Vector<Long> eventTypesWithEditedColors = new Vector<>();
			for(int i = 0; i < size; i++)
				if(eventTypes.get(editedEventTypes.get(i)).getColor() != eventTypesColorsInitial.get(i))
					eventTypesWithEditedColors.add(editedEventTypes.get(i));
			size = eventTypesWithEditedColors.size();
			if(size > 0) {
				long[] arr = new long[size];
				for(int i = 0; i < size; i++)
					arr[i] = eventTypesWithEditedColors.get(i);
				data.putExtra(ActivityMain.EDITED_EVENT_TYPES_MSG, arr);
			}
		}
		setResult(RESULT_OK, data);
		finish();
	}
}
