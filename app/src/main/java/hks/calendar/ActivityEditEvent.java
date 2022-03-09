package hks.calendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ActivityEditEvent extends AppCompatActivity {

	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int daysInSelectedMonth;

	private TextView selectedDayNameField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(HKSCalendarApp.loggerPrefix, "Entered ActivityEditEvent");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_event);

		Intent intent = getIntent();

		getSupportActionBar().setTitle(R.string.titleActivityEditEvent);

		Button deleteButton = findViewById(R.id.deleteButton);

		String action = intent.getAction();
		if(action != null && action.equals(ActivityMain.ADD_ACTION)) {
			deleteButton.setEnabled(false);
		}
		else {
			deleteButton.setOnClickListener(view -> {
				Intent data = new Intent();
				data.putExtra(ActivityMain.DELETE_EVENT_MSG, true);
				setResult(RESULT_OK, data);
				finish();
			});
		}

		EditText yearEdit = findViewById(R.id.yearEdit);
		EditText monthEdit = findViewById(R.id.monthEdit);
		EditText dayEdit = findViewById(R.id.dayEdit);
		EditText hourEdit = findViewById(R.id.hourEdit);
		EditText minuteEdit = findViewById(R.id.minuteEdit);
		EditText nameEdit = findViewById(R.id.nameEdit);
		EditText descriptionEdit = findViewById(R.id.descriptionEdit);
		selectedDayNameField = findViewById(R.id.selectedDayName);

		ArrayList<EditText> edits = new ArrayList<>();
		edits.add(yearEdit);
		edits.add(monthEdit);
		edits.add(dayEdit);
		edits.add(hourEdit);
		edits.add(minuteEdit);
		edits.add(nameEdit);
		//edits.add(descriptionEdit);


		Calendar now = new GregorianCalendar();

		year = intent.getIntExtra(ActivityMain.YEAR_MSG, -1);
		if(year == -1)
			year = now.get(Calendar.YEAR);
		yearEdit.setText(String.valueOf(year));
		yearEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int val;
				try {
					year = val = Integer.valueOf(s.toString());
					if(val < Consts.MIN_YEAR || val > Consts.MAX_YEAR)
						yearEdit.setTextColor(Color.RED);
					else
						yearEdit.setTextColor(Color.BLACK);
				}
				catch(NumberFormatException E) {
					yearEdit.setTextColor(Color.RED);
					val = year;
				}
				daysInSelectedMonth = Consts.getDaysInMonth(year, val);
				updateSelectedDayName();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		month = intent.getIntExtra(ActivityMain.MONTH_MSG, -1);
		if(month == -1)
			month = now.get(Calendar.MONTH);
		daysInSelectedMonth = Consts.getDaysInMonth(year, month);
		Log.d(HKSCalendarApp.loggerPrefix, "Month: " + month);
		monthEdit.setText(Consts.getMonthName(month));

		monthEdit.setOnClickListener(view -> {
			new DialogMonthSelection(this, false, 0) {
				@Override
				public void selected(int m, int y) {
					month = m;
					daysInSelectedMonth = Consts.getDaysInMonth(year, month);
					monthEdit.setText(Consts.getMonthName(m));
					updateSelectedDayName();
				}
			};

		});

		day = intent.getIntExtra(ActivityMain.DAY_MSG, -1);
		if(day == -1)
			day = now.get(Calendar.DATE);
		dayEdit.setText(String.valueOf(day));
		dayEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					int val = Integer.valueOf(s.toString());
					day = val;
					if(val < 0 || val > daysInSelectedMonth)
						dayEdit.setTextColor(Color.RED);
					else
						dayEdit.setTextColor(Color.BLACK);
				}
				catch(NumberFormatException E) {
					dayEdit.setTextColor(Color.RED);
				}
				updateSelectedDayName();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		updateSelectedDayName();

		hour = intent.getIntExtra(ActivityMain.HOUR_MSG, -1);
		if(hour != -1)
			hourEdit.setText(String.valueOf(hour));
		else
			hourEdit.setTextColor(Color.RED);
		hourEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					int val = Integer.valueOf(s.toString());
					hour = val;
					if(val < 0 || val > 23)
						hourEdit.setTextColor(Color.RED);
					else
						hourEdit.setTextColor(Color.BLACK);
				}
				catch(NumberFormatException E) {
					hourEdit.setTextColor(Color.RED);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		minute = intent.getIntExtra(ActivityMain.MINUTE_MSG, -1);
		if(minute != -1)
			minuteEdit.setText(String.valueOf(minute));
		else
			minuteEdit.setTextColor(Color.RED);
		minuteEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					int val = Integer.valueOf(s.toString());
					minute = val;
					if(val < 0 || val > 59)
						minuteEdit.setTextColor(Color.RED);
					else
						minuteEdit.setTextColor(Color.BLACK);
				}
				catch(NumberFormatException E) {
					minuteEdit.setTextColor(Color.RED);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		String name = intent.getStringExtra(ActivityMain.NAME_MSG);
		if(name != null)
			nameEdit.setText(name);

		String description = intent.getStringExtra(ActivityMain.DESCRIPTION_MSG);
		if(description != null)
			descriptionEdit.setText(description);


		// Type shit
		HKSCalendar calendar = HKSCalendarApp.c;
		ArrayList<Spannable> list = new ArrayList<>();

		for(int i = 0; i < calendar.eventTypes.size(); i++)
			list.add(calendar.eventTypes.valueAt(i).toStringShort());

		/*for(EventType et : calendar.eventTypes)
			list.add(et.toStringShort());*/

		Spinner eventTypeDropdown = findViewById(R.id.eventTypeDropdown);
		ArrayAdapter<Spannable> arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, list);
		eventTypeDropdown.setAdapter(arrayAdapter);

		long typeID = intent.getLongExtra(ActivityMain.TYPE_MSG, 0);
		int index = calendar.eventTypes.indexOfKey(typeID);
		eventTypeDropdown.setSelection(index);

		Button saveButton = findViewById(R.id.saveButton);
		saveButton.setOnClickListener(view -> {
			Log.d(HKSCalendarApp.loggerPrefix, "Add button clicked");
			for(EditText edit : edits) {
				if(edit.getText().toString().isEmpty()) {
					showErrorToast(R.string.notAllFieldsAreFilled);
					return;
				}
			}
			if(year < Consts.MIN_YEAR || year > Consts.MAX_YEAR ||
					day < 0 || day > daysInSelectedMonth ||
					hour < 0 || hour > 23 ||
					minute < 0 || minute > 59) {
				showErrorToast(R.string.errorsInFieldValues);
				return;
			}

			Intent data = new Intent();
			data.putExtra(ActivityMain.YEAR_MSG, year);
			data.putExtra(ActivityMain.MONTH_MSG, month);
			data.putExtra(ActivityMain.DAY_MSG, day);
			data.putExtra(ActivityMain.HOUR_MSG, hour);
			data.putExtra(ActivityMain.MINUTE_MSG, minute);
			data.putExtra(ActivityMain.NAME_MSG, nameEdit.getText().toString());
			data.putExtra(ActivityMain.DESCRIPTION_MSG, descriptionEdit.getText().toString());

			int position = eventTypeDropdown.getSelectedItemPosition();
			long id = calendar.eventTypes.valueAt(position).id;
			data.putExtra(ActivityMain.TYPE_MSG, id);

			setResult(RESULT_OK, data);
			finish();
		});
	}


	void updateSelectedDayName() {
		selectedDayNameField.setText(Consts.getWeekdayShort(Consts.getWeekDay(year, month, day)));
	}

	void showErrorToast(int id) {
		Toast.makeText(this, HKSCalendarApp.appCtx.getResources().getString(id), Toast.LENGTH_LONG).show();
	}
}
