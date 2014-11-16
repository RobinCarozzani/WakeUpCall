package fr.robincarozzani.wakeUpCall.view.activities.alarm;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import fr.robincarozzani.wakeUpCall.constants.Values;
import fr.robincarozzani.wakeUpCall.objects.Alarm;
import fr.robincarozzani.wakeUpCall.objects.alarmManager.AlarmManagerHelper;
import fr.robincarozzani.wakeUpCall.view.activities.Utils;
import fr.robincarozzani.wakeUpCall.view.listAdapters.alarm.AlarmsListAdapter;

public class ListAlarms extends Activity {
	
	private Button newAlarmButton;
	private AlarmsListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		createButton();
		createListView();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			loadListView();
			Utils.setNotification(this);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadListView();
	}
	
	private void createButton() {
		newAlarmButton = (Button)findViewById(R.id.listNewItemButton);
		newAlarmButton.setText(getResources().getString(R.string.listAlarmsNewAlarmButtonText));
		Utils.createTouchListener(this, newAlarmButton, R.drawable.button1shape, R.drawable.button1shapepressed);
		newAlarmButton.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(ListAlarms.this, EditAlarm.class), 1);	
			}
		});
	}
	
	private void createListView() {
		adapter = new AlarmsListAdapter(ListAlarms.this, R.layout.listalarms_item, new ArrayList<Alarm>());
		ListView alarmsLV = (ListView)findViewById(R.id.list_listview);
		alarmsLV.setAdapter(adapter);
		alarmsLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Alarm a = adapter.getItem(position);
				Intent intent = new Intent(ListAlarms.this, EditAlarm.class);
				Bundle b = new Bundle();
				b.putInt(Keys.ALARMID, a.getId());
				b.putString(Keys.ALARMNAME, a.getName());
				intent.putExtras(b);
				startActivityForResult(intent, 1);
			}
		});
		alarmsLV.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				removeItem(adapter.getItem(position));
				return false;
			}
		});		
		loadListView();
	}
	
	private void loadListView() {
		adapter.clear();
		for (Alarm a : Alarm.getListAlarms("", Values.IGNORE)) {
			addItem(a);
		}
	}
	
	private void addItem(Alarm item) {
		adapter.insert(item, adapter.getCount());
	}
	
	private void removeItem(Alarm item) {
		Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
		final Alarm toRemove = item;
		new AlertDialog.Builder(this)
        .setTitle(R.string.confirmation_title)
        .setMessage(R.string.confirmation_delete)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	adapter.remove(toRemove);
            	if (toRemove.isActivated())
            		AlarmManagerHelper.cancelAlarm(ListAlarms.this, toRemove);
            	removeFromDB(toRemove.getId());
            }
        })
        .setNegativeButton(R.string.no, null)
        .setCancelable(false)
        .show();
	}
	
	private void removeFromDB(int alarmId) {
		new Alarm(alarmId).deleteFromDB();
	}
	
	public void checkboxHandler(View v) {
		Alarm a = (Alarm)v.getTag();
		a.toggleActivation();
		if ((a.getNextActivation() == null) || (!a.hasMultipleDates() && a.getNextActivation().before(new Date()))) {
			a.toggleActivation();
			((CheckBox)v).setChecked(false);
			Toast.makeText(this, getResources().getString(R.string.activationNotAllowed), Toast.LENGTH_LONG).show();
		} else {
			a.updateMultipleDatesDB();
			adapter.notifyDataSetChanged();
			if (a.isActivated()) {
				AlarmManagerHelper.setAlarm(this, a);
				a.displayRemainigTime(this);
			} else {
				AlarmManagerHelper.cancelAlarm(this, a);
			}
			Utils.setNotification(this);
		}
	}
}
