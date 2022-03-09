package hks.calendar;

class Consts {
	public final static int MIN_YEAR = 1970;
	public final static int MAX_YEAR = 9999;

	final private static int[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

	/**
	 * @param y Year.
	 * @param m Month index, normally from [0, 11] being [January, December]. Values outside this range will be converted for extra years.
	 * @return Count of days in month of year, taking leap years in consideration.
	 */
	static int getDaysInMonth(int y, int m) {
		while(m < 0) {
			m += 12;
			y -= 1;
		}
		while(m > 11) {
			m -= 12;
			y += 1;
		}
		return Consts.daysInMonth[m] + (m == 1 && y % 4 == 0 ? 1 : 0);
	}

	final private static int[] magicInts = {0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4};

	/**
	 * @param y Year.
	 * @param m Month index from [0, 11] being [January, December]. Values outside this range will be converted for extra years.
	 * @return Day of week of 1st day of month, from [0, 6] being [Sunday, Saturday].
	 */
	static int getWeekDayOfFirstDayInMonth(int y, int m) {
		return getWeekDay(y, m, 1);
	}
	/**
	 * @param y Year.
	 * @param m Month index from [0, 11] being [January, December]. Values outside this range will be converted for extra years.
	 * @param d Date index from [0, ?].
	 * @return Day of week of 1st day of month, from [0, 6] being [Sunday, Saturday].
	 */
	static int getWeekDay(int y, int m, int d) {
		while(m < 0) {
			m += 12;
			y -= 1;
		}
		while(m > 11) {
			m -= 12;
			y += 1;
		}
		if(m < 2)
			y -= 1;
		return (y + y/4 - y/100 + y/400 + magicInts[m] + d) % 7;
	}

	/**
	 * @param m Month index from [0, 11] being [January, December].
	 * @return Name of month as string.
	 */
	static String getMonthName(int m) {
		return HKSCalendarApp.appCtx.getString(HKSCalendarApp.appCtx.getResources().getIdentifier("month_" + m, "string", HKSCalendarApp.packageName));
	}
	/**
	 * @param m Month index from [0, 11] being [January, December].
	 * @return Short name of month as string.
	 */
	static String getMonthNameShort(int m) {
		return HKSCalendarApp.appCtx.getString(HKSCalendarApp.appCtx.getResources().getIdentifier("month_short_" + m, "string", HKSCalendarApp.packageName));
	}
	/**
	 * @param day Day index in week from [0, 6] being [Sunday, Saturday].
	 * @return Name of week day as string.
	 */
	static String getWeekdayName(int day) {
		return HKSCalendarApp.appCtx.getString(HKSCalendarApp.appCtx.getResources().getIdentifier("weekday_" + day, "string", HKSCalendarApp.packageName));
	}
	/**
	 * @param day Day index in week from [0, 6] being [Sunday, Saturday].
	 * @return Short name of week day as string.
	 */
	static String getWeekdayShort(int day) {
		return HKSCalendarApp.appCtx.getString(HKSCalendarApp.appCtx.getResources().getIdentifier("weekday_short_" + day, "string",  HKSCalendarApp.packageName));
	}
}
