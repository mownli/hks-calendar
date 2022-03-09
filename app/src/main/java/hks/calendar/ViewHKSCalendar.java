package hks.calendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


public class ViewHKSCalendar extends ConstraintLayout {
	interface DayButtonClickListener {
		void dispatch();
	}

	DayButtonClickListener dayButtonClickListener = null;

	private int currentYear = -1;
	private int currentMonth = -1;
	private int currentDay = -1;

	private int globalYear = -1;
	private int globalMonth = -1;

	private int chosenYear = -1;
	private int chosenMonth = -1;
	private int chosenDay = -1;

	private int daysToSkipFirst = 0;
	private int endOfEligibleDays = 0;
	private int firstNonEligibleDate = 0;

	private DayButton chosenButton = null;

	final private int daysN = 7;
	final private int buttonsN = 42;
	final private int rowsN = 7;

	final private ArrayList<DayButton> buttons = new ArrayList<>(buttonsN);
	final private HashMap<Event, DayButton> eventToButtonMap = new HashMap<Event, DayButton>();

	private final int buttonSize;

	private HKSCalendar calendarEngine = null;
	private final Calendar dateTmp = new GregorianCalendar();

	private Button yearMonth;

	private void setYearMonth(int y, int m) {
		yearMonth.setText(HKSCalendarApp.appCtx.getString(R.string.topButtonText, Consts.getMonthName(m), y));
	}

	public ViewHKSCalendar(Context context, AttributeSet attrSet) {
		super(context, attrSet);

		calendarEngine = HKSCalendarApp.c;

		// Size bullshit
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int width = displayMetrics.widthPixels;
		buttonSize = width / daysN;

		setOnTouchListener((View v, MotionEvent e) ->
		{
			handleTouch(e);
			v.performClick();
			return true;
		});

		currentYear = dateTmp.get(Calendar.YEAR);
		currentMonth = dateTmp.get(Calendar.MONTH);
		currentDay = dateTmp.get(Calendar.DATE);
	}

	/**
	 * Call after view has been added to the screen.
	 * If elements are not on screen, attempting to find them by ID will return null.
	 */
	void init() {
		DayButton button;
		for(int i = 1; i < rowsN; i++) {
			for(int k = 0; k < daysN; k++) {
				button = findViewById(getResources().getIdentifier("dayButton" + String.valueOf(i - 1) + String.valueOf(
						k), "id", HKSCalendarApp.packageName));
				button.setOnClickListener(onButtonClick);
				buttons.add(button);
			}
		}

		setMonth(currentYear, currentMonth);
	}

	void linkWithYearMonthButton(Button b) {
		yearMonth = b;
	}

	/**
	 * Returns 0 on success, -1 on failure.
	 * If month is already on the screen returns 1.
	 */
	int setMonth(int year, int month) {
		if(year < Consts.MIN_YEAR || year > Consts.MAX_YEAR || month < 0 || month > 11)
			return -1;

		if(year == globalYear && month == globalMonth)
			return 1;

		globalYear = year;
		globalMonth = month;

		int weekDayFirst = Consts.getWeekDayOfFirstDayInMonth(year, month);
		daysToSkipFirst = 0;

		switch(weekDayFirst) {
			case 0:
			case 1:
				daysToSkipFirst = weekDayFirst + 6;
				break;
			default:
				daysToSkipFirst = weekDayFirst - 1;
		}

		int totalDays = Consts.getDaysInMonth(year, month);
		int totalDaysLastMonth = getDaysInLastMonth(year, month);

		firstNonEligibleDate = totalDaysLastMonth - daysToSkipFirst;

		// Setting up days belonging to the previous month
		for(int i = 0; i < daysToSkipFirst; i++) {
			buttons.get(i).setType(DayButton.DayType.PREVMONTH);
			buttons.get(i).setDay(firstNonEligibleDate + i + 1);
		}

		// Setting up days of the chosen month
		endOfEligibleDays = daysToSkipFirst + totalDays;
		for(int i = daysToSkipFirst; i < endOfEligibleDays; i++) {
			buttons.get(i).setType(DayButton.DayType.THISMONTH);
			buttons.get(i).setDay(i - daysToSkipFirst + 1);
		}

		for(int i = endOfEligibleDays; i < buttonsN; i++) {
			buttons.get(i).setType(DayButton.DayType.NEXTMONTH);
			buttons.get(i).setDay(i - endOfEligibleDays + 1);
		}

		resetEvents();


		// If currentMonth is chosen check the currentDay button
		if(globalMonth == currentMonth && globalYear == currentYear) {
			DayButton button = buttons.get(daysToSkipFirst + currentDay - 1);
			button.setType(DayButton.DayType.CURRENTDAY);
			setChosenButton(button);
		}
		else
			clearChosenDate();


		if(dayButtonClickListener != null)
			dayButtonClickListener.dispatch();

		return 0;
	}

	private void clearChosenDate() {
		if(chosenButton != null) {
			//chosenButton.checked = false;
			chosenButton.setTextSize(15f);
			chosenButton.setPaintFlags(chosenButton.getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG);
			//chosenButton.updateHintColors();
			chosenButton = null;
		}
		chosenDay = -1;
		chosenMonth = -1;
		chosenYear = -1;
	}

	void goToCurrentDay() {
		int res = setMonth(currentYear, currentMonth);
		if(1 == res) {
			setChosenButton(buttons.get(daysToSkipFirst + currentDay - 1));
			if(dayButtonClickListener != null)
				dayButtonClickListener.dispatch();
		}
		else if(res == 0)
			setYearMonth(currentYear, currentMonth);
	}

	private void setChosenButton(@NonNull DayButton newChosenButton) {
		clearChosenDate();
		chosenButton = newChosenButton;
		//newChosenButton.checked = true;
		chosenButton.setTextSize(24f);
		chosenButton.setPaintFlags(chosenButton.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG);
		//newChosenButton.updateHintColors();

		chosenDay = newChosenButton.dayNumber;

		updateChosenYearAndMonth();
	}

	void setNextMonth() {
		Log.d(HKSCalendarApp.loggerPrefix, "setNextMonth()");
		clearChosenDate();

		if(globalMonth + 1 == 12)
			setMonth(globalYear + 1, 0);
		else
			setMonth(globalYear, globalMonth + 1);
	}

	void setPreviousMonth() {
		Log.d(HKSCalendarApp.loggerPrefix, "setPreviousMonth()");
		clearChosenDate();

		if(globalMonth - 1 == -1)
			setMonth(globalYear - 1, 11);
		else
			setMonth(globalYear, globalMonth - 1);
	}

	int getChosenDay() {
		return chosenDay;
	}

	int getChosenMonth() {
		return chosenMonth;
	}

	int getChosenYear() {
		return chosenYear;
	}

	int getGlobalYear() {
		return globalYear;
	}

	int getGlobalMonth() {
		return globalMonth;
	}

	private int getDaysInLastMonth(int currentYear, int currentMonth) {
		// If January
		if(currentMonth == 0)
			return Consts.getDaysInMonth(currentYear - 1, 11);
		else
			return Consts.getDaysInMonth(currentYear, currentMonth - 1);
	}

	private int computeTypeOfMonth(int year, int month) {
		return month - globalMonth + 12 * (year - globalYear);
	}

	@Nullable
	private DayButton findDayButtonAtDate(int year, int month, int day) {
		int typeOfMonth = computeTypeOfMonth(year, month);
		if(Math.abs(typeOfMonth) > 1)
			return null;
		return findDayButtonAtDay(typeOfMonth, day);
	}

	@Nullable
	private DayButton findDayButtonAtDay(int typeOfMonth, int day) {
		int index = -1;
		if(typeOfMonth == -1) {
			index = day - firstNonEligibleDate - 1;
			if(index < 0)
				return null;
		}
		else if(typeOfMonth == 0)
			index = daysToSkipFirst + day - 1;
		else if(typeOfMonth == 1) {
			index = endOfEligibleDays + day - 1;
			if(index >= buttonsN)
				return null;
		}
		return buttons.get(index);
	}


	final View.OnClickListener onButtonClick = view ->
	{
		DayButton newChosenButton = (DayButton) view;

		// The same button clicked twice
		if(chosenButton == newChosenButton)
			clearChosenDate();
			// No DayButton was clicked before or another one was clicked
		else
			setChosenButton(newChosenButton);

		if(dayButtonClickListener != null)
			dayButtonClickListener.dispatch();
	};

	private void updateChosenYearAndMonth() {
		int index = buttons.indexOf(chosenButton);
		if(index < 0) {
			chosenYear = -1;
			chosenMonth = -1;
		}

		dateTmp.set(Calendar.YEAR, getGlobalYear());
		dateTmp.set(Calendar.MONTH, getGlobalMonth());
		if(index < daysToSkipFirst)
			dateTmp.add(Calendar.MONTH, -1);
		else if(index < endOfEligibleDays)
			dateTmp.add(Calendar.MONTH, 0);
		else
			dateTmp.add(Calendar.MONTH, 1);

		chosenYear = dateTmp.get(Calendar.YEAR);
		chosenMonth = dateTmp.get(Calendar.MONTH);
	}


	private float touchX;
	private int touchDelta;
	private boolean handleTouch(@NonNull MotionEvent event) {
		final int ev = event.getAction();

		if(ev == MotionEvent.ACTION_DOWN) {
			//Log.d(HKSCalendarApp.loggerPrefix, "handleTouch(): ACTION_DOWN");
			touchX = event.getX();
			touchDelta = 0;
		}
		else if(ev == MotionEvent.ACTION_MOVE) {
			int newTouchDelta = (int)((event.getX() - touchX)/buttonSize);
			if(newTouchDelta > touchDelta) {
				setPreviousMonth();
				touchDelta = newTouchDelta;
				setYearMonth(globalYear, globalMonth);
			}
			else if(newTouchDelta < touchDelta) {
				setNextMonth();
				touchDelta = newTouchDelta;
				setYearMonth(globalYear, globalMonth);
			}
		}
		/*else if(ev == MotionEvent.ACTION_UP) {
			Log.d(HKSCalendarApp.loggerPrefix, "handleTouch(): ACTION_UP");
			float delta = event.getX() - touchX;
			if(Math.abs(delta) > buttonSize) {
				if(delta > 0)
					setPreviousMonth();
				else
					setNextMonth();


				setYearMonth(globalYear, globalMonth);

				return true;
			}
		}*/

		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return handleTouch(event);
	}

	Vector<Event> getEventsAtChosenDay() {
		return calendarEngine.getEvents(chosenYear, chosenMonth, chosenDay);
	}

	private void resetEvents() {
		eventToButtonMap.clear();

		for(DayButton btn : buttons) {
			dateTmp.clear();
			dateTmp.set(Calendar.YEAR, globalYear);
			dateTmp.set(Calendar.MONTH, globalMonth);

			if(btn.type == DayButton.DayType.PREVMONTH)
				dateTmp.add(Calendar.MONTH, -1);
			else if(btn.type == DayButton.DayType.THISMONTH)
				dateTmp.add(Calendar.MONTH, 0);
			else if(btn.type == DayButton.DayType.NEXTMONTH)
				dateTmp.add(Calendar.MONTH, 1);

			Vector<Event> events = calendarEngine.getEvents(dateTmp.get(Calendar.YEAR),
					dateTmp.get(Calendar.MONTH),
					btn.dayNumber);

			btn.setEvents(events);

			if(events != null)
				for(Event event : events)
					eventToButtonMap.put(event, btn);
		}
	}

	void addEvent(int year,
				  int month,
				  int day,
				  int hour,
				  int minute,
				  long type,
				  String name,
				  String description) {
		Log.d(HKSCalendarApp.loggerPrefix, "addEvent(): Adding event to calendar");
		Event event = calendarEngine.addEvent(year,
				month,
				day,
				hour,
				minute,
				type,
				name,
				description);

		DayButton button = findDayButtonAtDate(year, month, day);
		if(button == null)
			return;

		eventToButtonMap.put(event, button);
		button.addEvent(event);
	}

	void updateEvent(@NonNull Event event,
					 int year,
					 int month,
					 int day,
					 int hour,
					 int minute,
					 long type,
					 String name,
					 String description) {
		Log.d(HKSCalendarApp.loggerPrefix, "onActivityResult(): Updating event");
		int yearOld = event.getYear();
		int monthOld = event.getMonth();
		int dayOld = event.getDay();

		calendarEngine.updateEvent(event, year, month, day, hour, minute, type, name, description);

		DayButton button = eventToButtonMap.get(event);
		if((year == yearOld) && (month == monthOld) && (day == dayOld)) {
			//DayButton btn = findDayButtonAtDate(year, month, day);
			button.updateEventColors();
		}
		else {
			button.removeEvent(event);

			button = findDayButtonAtDate(year, month, day);
			if(button != null) {
				button.addEvent(event);
				eventToButtonMap.replace(event, button);
			}
			else
				eventToButtonMap.replace(event, null);
		}
	}

	void removeEvent(@NonNull Event event) {
		Log.d(HKSCalendarApp.loggerPrefix, "onActivityResult(): Deleting event");

		calendarEngine.removeEvent(event);

		DayButton button = eventToButtonMap.get(event);
		if(button == null)
			return;

		eventToButtonMap.remove(event);
		button.removeEvent(event);
	}

	void updateTypes(@NonNull long[] modifiedTypes) {
		for(DayButton btn : buttons) {
			if(btn.hasColors) {
				for(Event ev : btn.events) {
					for(long modifiedType : modifiedTypes) {
						if(ev.typeID == modifiedType)
							btn.updateEventColors();
					}
				}
			}
		}
	}

	private static class DayButton extends androidx.appcompat.widget.AppCompatTextView {
		private enum DayType {
			PREVMONTH, THISMONTH, NEXTMONTH, CURRENTDAY
		}

		DayType type;

		private int dayNumber = -1;

		private GradientDrawable shape = null;
		private Vector<Event> events = null;

		private int textColorNormal = Color.BLACK;

		private boolean hasColors = false;
		//private boolean checked = false;

		public DayButton(Context context, AttributeSet attrSet) {
			super(context, attrSet);
			setTextSize(15f);

			// getForeground().set
		}

		public DayButton(Context context) {
			super(context);
			setTextSize(15f);
		}

		private void setDay(int day) {
			dayNumber = day;
			setText(String.valueOf(day));
		}

		/**
		 * Use it only in ViewHKSCalendar.setMonth().
		 */
		private void setType(DayType t) {
			type = t;

			textColorNormal = Color.BLACK;
			if(t == DayType.THISMONTH) {
				setPaintFlags(getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
				setTextColor(textColorNormal);
			}
			else if(t == DayType.CURRENTDAY) {
				setPaintFlags(getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
				setTextColor(textColorNormal);
			}
			else {
				setPaintFlags(getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
				setTextColor(Color.GRAY);
			}
		}

		private void addEvent(Event event) {
			events.add(event);
			events.sort(null);

			updateEventColors();
		}

		private void removeEvent(Event event) {
			events.remove(event);
			updateEventColors();
		}

		private void setEvents(Vector<Event> events) {
			this.events = events;
			updateEventColors();
		}

		private void updateEventColors() {
			int[] eventColors = null;
			if(events != null && events.size() != 0) {
				Set<Integer> colors = new TreeSet<>();
				for(Event ev : events)
					colors.add(ev.getEventType().getColor());

				Iterator<Integer> color = colors.iterator();
				int size = colors.size();
				if(size == 1) {
					eventColors = new int[2];
					eventColors[0] = color.next();
					eventColors[1] = eventColors[0];
				}
				else {
					eventColors = new int[size];
					for(int i = 0; color.hasNext(); i++)
						eventColors[i] = color.next();
				}
			}

			setEventColors(eventColors);
		}

		private void setEventColors(int[] eventColors) {
			if(shape == null) {
				shape = new GradientDrawable();
				shape.setCornerRadius(100f);
				shape.setOrientation(GradientDrawable.Orientation.TL_BR);
				setBackground(shape);
				shape.setAlpha(64);
			}

			if(eventColors != null) {
				shape.setColors(eventColors);
				hasColors = true;
			}
			else {
				shape.setColor(Color.TRANSPARENT);
				hasColors = false;
			}
		}
	}
}
