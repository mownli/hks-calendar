package hks.calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlertReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context ctx, Intent intent) {
		HKSCalendar c = HKSCalendarApp.c;

		long ts = intent.getLongExtra("ts", -1);
		long id = intent.getLongExtra("id", -1);

		Event ev = c.findEvent(id, ts);
		EventType et = ev.getEventType();

		HKSCalendarApp.deliverNotification(ctx, et.getColor(), ev.name, HKSCalendarApp.getNotificationId(id), et.getNotificationChannelIDEffective());

		c.closestEvent = ev;
		c.alarmSet = false;
		c.reschedule(ts, id);
	}
}
