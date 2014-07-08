package fr.robincarozzani.wakeUpCall.objects.alarmManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import fr.robincarozzani.wakeUpCall.view.activities.alarm.ActiveAlarm;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		int id = intent.getExtras().getInt(Keys.ALARMID);
		Intent newIntent = new Intent(context, ActiveAlarm.class);
		newIntent.putExtra(Keys.ALARMID, id);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(newIntent);
	}

}
