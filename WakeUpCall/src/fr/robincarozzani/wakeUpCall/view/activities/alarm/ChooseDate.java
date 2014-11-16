package fr.robincarozzani.wakeUpCall.view.activities.alarm;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;
import android.widget.ToggleButton;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import fr.robincarozzani.wakeUpCall.view.activities.Utils;

public class ChooseDate extends Activity {
	
	public static final int UNIQUEDATECODE = 1;
	public static final int REPEATDATECODE = 2;
	
	private TabHost tabHost;
	private ToggleButton[] daysButtons;
	private DatePicker uniqueDatePicker;
	
	private Intent returnIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choosedate);
		returnIntent = new Intent();
		createTabs();
		createButtons();
		dealWithBundle();
	}
	
	private void createTabs() {
		tabHost = (TabHost)findViewById(R.id.chooseDateTabHost);
		tabHost.setup();
		TabSpec spec = tabHost.newTabSpec(Keys.UNIQUE);
		spec.setIndicator(getResources().getString(R.string.uniqueDate));
		spec.setContent(R.id.chooseDateTab1);
		tabHost.addTab(spec);
		spec = tabHost.newTabSpec(Keys.REPEAT);
		spec.setIndicator(getResources().getString(R.string.repeatDate));
		spec.setContent(R.id.chooseDateTab2);
		tabHost.addTab(spec);
	}
	
	private void createButtons() {
		uniqueDatePicker = (DatePicker)findViewById(R.id.chooseDateUniqueDatePicker);
		Button uniqueDoneButton = (Button)findViewById(R.id.chooseUniqueDateDoneButton);
		Utils.createTouchListener(this, uniqueDoneButton, R.drawable.button2shape, R.drawable.button2shapepressed);
		Button repeatDoneButton = (Button)findViewById(R.id.chooseRepeatDateDoneButton);
		Utils.createTouchListener(this, repeatDoneButton, R.drawable.button2shape, R.drawable.button2shapepressed);
		daysButtons = new ToggleButton[7];
		daysButtons[0] = (ToggleButton)findViewById(R.id.chooseDateToggleMon);
		daysButtons[1] = (ToggleButton)findViewById(R.id.chooseDateToggleTue);
		daysButtons[2] = (ToggleButton)findViewById(R.id.chooseDateToggleWed);
		daysButtons[3] = (ToggleButton)findViewById(R.id.chooseDateToggleThu);
		daysButtons[4] = (ToggleButton)findViewById(R.id.chooseDateToggleFri);
		daysButtons[5] = (ToggleButton)findViewById(R.id.chooseDateToggleSat);
		daysButtons[6] = (ToggleButton)findViewById(R.id.chooseDateToggleSun);
		for (ToggleButton tb : daysButtons) {
			Utils.createTouchListener(this, tb, R.drawable.button3shape, R.drawable.button3shapepressed);
		}
	}
	
	public void uniqueDone(View v) {
		int day = uniqueDatePicker.getDayOfMonth();
		int month = uniqueDatePicker.getMonth();
		int year = uniqueDatePicker.getYear();
		Calendar c = Calendar.getInstance();
		if ((year < c.get(Calendar.YEAR)) ||
			((year == c.get(Calendar.YEAR)) && (month < c.get(Calendar.MONTH))) ||
			((year == c.get(Calendar.YEAR)) && (month == c.get(Calendar.MONTH)) && (day < c.get(Calendar.DAY_OF_MONTH)))
		   ) {
			Toast.makeText(this, getResources().getString(R.string.pastDate), Toast.LENGTH_LONG).show();
		} else {
			returnIntent.putExtra(Keys.CODE, UNIQUEDATECODE);
			returnIntent.putExtra(Keys.DAY, day);
			returnIntent.putExtra(Keys.MONTH, month);
			returnIntent.putExtra(Keys.YEAR, year);
			setResult(0, returnIntent);
			finish();
		}
	}
	
	public void toggleDay(View v) {
		ToggleButton theButton = (ToggleButton)findViewById(v.getId());
		if (theButton.isChecked()) {
			theButton.setBackground(getResources().getDrawable(R.drawable.button2shape));
			Utils.createTouchListener(this, theButton, R.drawable.button2shape, R.drawable.button2shapepressed);
		} else {
			theButton.setBackground(getResources().getDrawable(R.drawable.button3shape));
			Utils.createTouchListener(this, theButton, R.drawable.button3shape, R.drawable.button3shapepressed);
		}
	}
	
	public void repeatDone(View v) {
		boolean[] selectedDays = new boolean[7];
		for (int i=0 ; i<daysButtons.length ; ++i) {
			selectedDays[i] = daysButtons[i].isChecked();
		}
		returnIntent.putExtra(Keys.CODE, REPEATDATECODE);
		returnIntent.putExtra(Keys.SELECTEDDAYS, selectedDays);
		setResult(0, returnIntent);
		finish();
	}

	
	private void dealWithBundle() {
		Bundle b = getIntent().getExtras();
		if (b != null) {
			if (b.getInt(Keys.DATESELECTIONMODE) == REPEATDATECODE) {
				tabHost.setCurrentTabByTag(Keys.REPEAT);
				boolean[] currentSelectedDays = b.getBooleanArray(Keys.REPEAT);
				for (int i=0 ; i<currentSelectedDays.length ; ++i) {
					if (currentSelectedDays[i]) {
						daysButtons[i].setChecked(true);
						daysButtons[i].setBackground(getResources().getDrawable(R.drawable.button2shape));
						Utils.createTouchListener(this, daysButtons[i], R.drawable.button2shape, R.drawable.button2shapepressed);
					}
				}
			} else {
				uniqueDatePicker.updateDate(b.getInt(Keys.YEAR), b.getInt(Keys.MONTH), b.getInt(Keys.DAY));
			}
		}
	}
}
