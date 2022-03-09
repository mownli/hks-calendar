package hks.calendar;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.LongSparseArray;

import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Vector;

public class HKSCalendarApp extends Application {
	static Context appCtx;
	static String bullet = "0";
	static String storageLocation = "storage";
	static String packageName = "";
	static String loggerPrefix = "";

	static HKSCalendar c;


	@Override
	public void onCreate() {
		super.onCreate();

		appCtx = getApplicationContext();

		storageLocation = getFilesDir().getAbsolutePath() + "/" + storageLocation;
		packageName = getPackageName();
		loggerPrefix = packageName + ".log";
		bullet = getResources().getString(R.string.bullet);

		c = getHKSCalendar();

		notificationChannelsSetup();
		alarmManagerSetup();
	}

	private static HKSCalendar getHKSCalendar() {
		HKSCalendar cal = new HKSCalendar();
		cal.init();
		return cal;
	}

	static AlarmManager alarmManager;
	private void alarmManagerSetup() {
		alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
	}


	private static NotificationManager mNotificationManager;
	private static final String notificationGroupID = "notificationGroupID";
	static final String notificationChannelID = "primary_notification_channel";
	static final String notificationChannelPrefix = "notification_channel_";
	private void notificationChannelsSetup() {
		/*if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O)
			return;*/
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		Vector<String> channelIDs = new Vector<>();
		LongSparseArray<EventType> eventTypes = c.eventTypes;
		int size = eventTypes.size();
		for(int i = 0; i < size; i++)
			channelIDs.add(eventTypes.valueAt(i).getNotificationChannelID());

		List<NotificationChannel> existingChannels;
		try {
			existingChannels = mNotificationManager.getNotificationChannels();
			String ncID;
			for(NotificationChannel nc : existingChannels) {
				ncID = nc.getId();
				if(ncID.equals(notificationChannelID) || channelIDs.indexOf(ncID) != -1)
					continue;
				Log.d(loggerPrefix, "notificationChannelsSetup(): Notification channel with ID " + ncID +  " not found");
				deleteNotificationChannel(ncID);
			}
		}
		catch(NullPointerException npe) {
			Log.e(loggerPrefix, "notificationChannelsSetup(): NPE while retrieving list of notification channels", npe);
		}

		createNotificationChannel(notificationChannelID, getResources().getString(R.string.notificationChannelName), null);
		size = channelIDs.size();
		for(int i = 0; i < size; i++)
			createNotificationChannel(eventTypes.valueAt(i));
	}

	static void createNotificationChannel(EventType et) {
		createNotificationChannel(et.getNotificationChannelID(), et.name, et.getNotificationChannelDescription());
	}
	static void createNotificationChannel(String id, String name, String desc) {
		Log.d(loggerPrefix, "createNotificationChannel(): Creating notification channel with ID=" + id + ",name=" + name);
		NotificationChannel nc = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
		nc.enableLights(true);
		nc.setDescription(desc);
		nc.enableVibration(false);
		mNotificationManager.createNotificationChannel(nc);
	}

	static void editNotificationChannel(EventType et) {
		editNotificationChannel(et.getNotificationChannelID(), et.name, et.getNotificationChannelDescription());
	}
	static void editNotificationChannel(String id, String name, String desc) {
		Log.d(loggerPrefix, "editNotificationChannel(): Editing notification channel with ID=" + id + ",name=" + name);
		NotificationChannel nc = mNotificationManager.getNotificationChannel(id);
		if(nc == null) {
			Log.d(loggerPrefix, "editNotificationChannel(): \tNot found");
			return;
		}
		nc.setName(name);
		nc.setDescription(desc);
		mNotificationManager.createNotificationChannel(nc);
	}
	static void deleteNotificationChannel(String id) {
		Log.d(loggerPrefix, "deleteNotificationChannel(): Deleting notification channel with ID=" + id);
		mNotificationManager.deleteNotificationChannel(id);
	}


	static int getNotificationId(long ts) {
		return (int) ts/1000;
	}
	static void deliverNotification(Context ctx, int color, String name, int id, String notificationChannel) {
		Intent contentIntent = new Intent(ctx, ActivityMain.class);
		PendingIntent contentPendingIntent = PendingIntent.getActivity(ctx, id, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Spannable sb = new SpannableString(bullet + " " + name);
		sb.setSpan(new ForegroundColorSpan(color), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		//sb.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, notificationChannel)
				.setContentIntent(contentPendingIntent)
				.setSmallIcon(R.drawable.icon_main_notification)
				.setColor(color)
				.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
				//.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.main_icon))
				.setContentTitle(sb)
				//.setContentText(desc)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setAutoCancel(true)
				.setGroup(notificationGroupID)
				.setDefaults(NotificationCompat.DEFAULT_ALL);

		mNotificationManager.notify(id, builder.build());
	}
}
