package fr.robincarozzani.wakeUpCall.view.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.objects.Alarm;

public class Utils {

	public static void createTouchListener(Context c, Button b, int drawableId, int drawablePressedId) {
		final Button b1 = b;
		final Context c1 = c;
		final int drawableId1 = drawableId, drawablePressedId1 = drawablePressedId;
		b1.setOnTouchListener(new OnTouchListener() {
			@SuppressWarnings("deprecation")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					b1.setBackgroundDrawable(c1.getResources().getDrawable(drawablePressedId1));
				else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
					b1.setBackgroundDrawable(c1.getResources().getDrawable(drawableId1));
				return false;
			}
		});
	}
	
	public static void setNotification(Context c) {
		NotificationManager notifMgr = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		notifMgr.cancel(1);
		if (Alarm.nbActivatedAlarms() > 0) {
			Intent resultIntent = new Intent(c, Home.class);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(c, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			Notification notif = new NotificationCompat.Builder(c)
			.setContentTitle(c.getResources().getString(R.string.app_name))
			.setContentText(c.getResources().getString(R.string.nextAlarm) + " " + Alarm.nextAlarmDate())
			.setWhen(System.currentTimeMillis())
			.setAutoCancel(true)
			.setSmallIcon(R.drawable.launcher_ico)
			.setOngoing(true)
			.setContentIntent(resultPendingIntent)
			.build();
			notif.flags = Notification.FLAG_NO_CLEAR;
			notifMgr.notify(1, notif);
		}
	}
}
