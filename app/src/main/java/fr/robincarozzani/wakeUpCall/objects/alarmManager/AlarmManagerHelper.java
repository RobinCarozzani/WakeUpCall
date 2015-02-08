package fr.robincarozzani.wakeUpCall.objects.alarmManager;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import fr.robincarozzani.wakeUpCall.constants.Values;
import fr.robincarozzani.wakeUpCall.constants.db.AlarmDB;
import fr.robincarozzani.wakeUpCall.objects.Alarm;

public class AlarmManagerHelper {
	
	public static void setAlarm(Context context, Alarm alarm) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(alarm.getNextActivation());
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intentAlarm = new Intent(context, AlarmReceiver.class);
		intentAlarm.putExtra(Keys.ALARMID, alarm.getId());
		alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				PendingIntent.getBroadcast(context, alarm.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
	public static void setAlarms(Context context) {
		for (Alarm a : Alarm.getListAlarms(AlarmDB.ACTIVATED, Values.TRUE))
			setAlarm(context, a);
	}
	
	public static void cancelAlarm(Context context, Alarm alarm) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(alarm.getNextActivation());
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intentAlarm = new Intent(context, AlarmReceiver.class);
		intentAlarm.putExtra(Keys.ALARMID, alarm.getId());
		alarmManager.cancel(PendingIntent.getBroadcast(context, alarm.getId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
	public static void cancelAlarms(Context context) {
		for (Alarm a : Alarm.getListAlarms(AlarmDB.ACTIVATED, Values.TRUE))
			cancelAlarm(context, a);
	}

}
