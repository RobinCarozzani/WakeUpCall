package fr.robincarozzani.wakeUpCall.view.activities.alarm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import fr.robincarozzani.wakeUpCall.objects.Alarm;
import fr.robincarozzani.wakeUpCall.objects.Playlist;
import fr.robincarozzani.wakeUpCall.objects.alarmManager.AlarmManagerHelper;
import fr.robincarozzani.wakeUpCall.view.activities.Utils;

public class EditAlarm extends Activity {
	
	private final static int NOPLAYLISTCHOSEN = -1;
	private final static int NODATECHOSEN = -1;
	private final static int CHOSEPLAYLISTREQUESTCODE = 1;
	private final static int CHOSEDATEREQUESTCODE = 2;
	
	private Button saveButton, playlistButton, dateButton;
	private EditText alarmNameET;
	private String alarmNameETContent;
	private TimePicker timePicker;
	private CheckBox repeatCB, vibrateCB;
	private SeekBar volumeSB;
	
	private Alarm alarm;
	private int playlistId;
	private Playlist playlist;
	private int dateSelectionMode;
	private Map<String, Integer> uniqueDate;
	private boolean[] selectedDays;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editalarm);
		getActionBar().hide();
		alarm = null;
		playlistId = NOPLAYLISTCHOSEN;
		dateSelectionMode = NODATECHOSEN;
		alarmNameET = (EditText)findViewById(R.id.editAlarmNameEditText);
		uniqueDate = new HashMap<String, Integer>();
		selectedDays = new boolean[7];
		createTimePicker();
		createButtons();
		createCheckBoxes();
		createSeekBar();
		dealWithBundle();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			if (requestCode == CHOSEPLAYLISTREQUESTCODE) {
				playlistId = data.getIntExtra(Keys.PLAYLISTID, NOPLAYLISTCHOSEN);
				TextView playlistLabel = (TextView)(findViewById(R.id.editAlarmPlaylistsLabel));
				if (!playlistLabel.getText().toString().endsWith(" : ")) playlistLabel.append(" : ");
				String playlistName = data.getStringExtra(Keys.PLAYLISTNAME);
				((TextView)(findViewById(R.id.editAlarmChosenPlaylist))).setText(playlistName);
			} else if (requestCode == CHOSEDATEREQUESTCODE) {
				dateSelectionMode = data.getIntExtra("code", NODATECHOSEN);
				TextView dateLabel = (TextView)(findViewById(R.id.editAlarmDateLabel));
				if (!dateLabel.getText().toString().endsWith(" : ")) dateLabel.append(" : ");
				if (dateSelectionMode == ChooseDate.UNIQUEDATECODE) {
					int day = data.getIntExtra(Keys.DAY, NODATECHOSEN);
					int month = data.getIntExtra(Keys.MONTH, NODATECHOSEN);
					int year = data.getIntExtra(Keys.YEAR, NODATECHOSEN);
					Calendar c = Calendar.getInstance();
				    c.set(year, month, day);
				    DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM);
				    String dateString = df.format(c.getTime());
				    ((TextView)(findViewById(R.id.editAlarmChosenDate))).setText(dateString);
				    uniqueDate.put(Keys.DAY, day);
				    uniqueDate.put(Keys.MONTH, month);
				    uniqueDate.put(Keys.YEAR, year);
				    selectedDays = new boolean[7];
				} else if (dateSelectionMode == ChooseDate.REPEATDATECODE) {	
					selectedDays = data.getBooleanArrayExtra(Keys.SELECTEDDAYS);
					loadChosenDaysTVRepeatDate();
				}
			}
		}
	}
	
	private void loadChosenDaysTVRepeatDate() {
		String[] daysName = { getResources().getString(R.string.monday),
							  getResources().getString(R.string.tuesday),
							  getResources().getString(R.string.wednesday),
							  getResources().getString(R.string.thursday),
							  getResources().getString(R.string.friday),
							  getResources().getString(R.string.saturday),
							  getResources().getString(R.string.sunday) };
		TextView chosenDaysTV = (TextView)(findViewById(R.id.editAlarmChosenDate));
		chosenDaysTV.setText("");
		for (int i=0 ; i<daysName.length ; ++i) {
			if (selectedDays[i]) {
				chosenDaysTV.append(daysName[i] + "\n");
			}
		}
		if (chosenDaysTV.getText().toString().equals("")) {
			TextView dateLabel = (TextView)(findViewById(R.id.editAlarmDateLabel));
			dateLabel.setText(getResources().getString(R.string.date));
			dateSelectionMode = NODATECHOSEN;
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private void dealWithBundle() {
		Bundle b = getIntent().getExtras();
		if (b != null) {
			alarm = new Alarm(b.getInt(Keys.ALARMID));
			repeatCB.setChecked(alarm.isRepeat());
			vibrateCB.setChecked(alarm.isVibrate());
			volumeSB.setProgress(alarm.getVolume());
			alarmNameET.setText(alarm.getName());
			playlistId = alarm.getPlaylistId();
			playlist = new Playlist(alarm.getPlaylistId());
			((TextView)(findViewById(R.id.editAlarmPlaylistsLabel))).append(" : ");
			((TextView)(findViewById(R.id.editAlarmChosenPlaylist))).setText(playlist.getName());
			Calendar cal = Calendar.getInstance();
			cal.setTime(alarm.getNextActivation());
			timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
			uniqueDate.put(Keys.HOUR, cal.get(Calendar.HOUR_OF_DAY));
			uniqueDate.put(Keys.MINUTE, cal.get(Calendar.MINUTE));
			((TextView)(findViewById(R.id.editAlarmDateLabel))).append(" : ");
			if (alarm.hasMultipleDates()) {
				dateSelectionMode = ChooseDate.REPEATDATECODE;
				selectedDays = alarm.getSelectedDays();
				loadChosenDaysTVRepeatDate();
			} else {
				dateSelectionMode = ChooseDate.UNIQUEDATECODE;
				DateFormat df = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM);
			    String dateString = df.format(cal.getTime());
			    uniqueDate.put(Keys.YEAR, cal.get(Calendar.YEAR));
			    uniqueDate.put(Keys.MONTH, cal.get(Calendar.MONTH));
			    uniqueDate.put(Keys.DAY, cal.get(Calendar.DAY_OF_MONTH));
			    ((TextView)(findViewById(R.id.editAlarmChosenDate))).setText(dateString);
			}
		}
	}
	
	private void createButtons() {
		saveButton = (Button)findViewById(R.id.editAlarmSaveButton);
		Utils.createTouchListener(this, saveButton, R.drawable.button2shape, R.drawable.button2shapepressed);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alarmNameETContent = alarmNameET.getText().toString().trim();
				if (alarmNameETContent.equals("")) {
					Toast.makeText(EditAlarm.this, EditAlarm.this.getResources().getString(R.string.alarmNameEmpty), Toast.LENGTH_LONG).show();
					return;
				}
				if (playlistId == NOPLAYLISTCHOSEN) {
					Toast.makeText(EditAlarm.this, EditAlarm.this.getResources().getString(R.string.noPlaylistChosen), Toast.LENGTH_LONG).show();
					return;
				}
				if (dateSelectionMode == NODATECHOSEN) {
					Toast.makeText(EditAlarm.this, EditAlarm.this.getResources().getString(R.string.noDateChosen), Toast.LENGTH_LONG).show();
					return;
				}
				if (dateSelectionMode == ChooseDate.UNIQUEDATECODE) {
					Calendar cal = Calendar.getInstance();
					if ((uniqueDate.get(Keys.YEAR) == cal.get(Calendar.YEAR)) && (uniqueDate.get(Keys.MONTH) == cal.get(Calendar.MONTH)) && (uniqueDate.get(Keys.DAY) == cal.get(Calendar.DAY_OF_MONTH))) {
						if ((timePicker.getCurrentHour() < cal.get(Calendar.HOUR_OF_DAY)) || ((timePicker.getCurrentHour() == cal.get(Calendar.HOUR_OF_DAY)) && (timePicker.getCurrentMinute() < cal.get(Calendar.MINUTE)))) {
							Toast.makeText(EditAlarm.this, EditAlarm.this.getResources().getString(R.string.pastDate), Toast.LENGTH_LONG).show();
							return;
						}
					}
				}
				int hour = timePicker.getCurrentHour();
				int minute = timePicker.getCurrentMinute();
				uniqueDate.put(Keys.HOUR, hour);
				uniqueDate.put(Keys.MINUTE, minute);
				saveInDB();
				alarm.setActivated(true);
				AlarmManagerHelper.setAlarm(EditAlarm.this, alarm);
				alarm.displayRemainigTime(EditAlarm.this);
				
				finish();
			}
		});
		playlistButton = (Button)findViewById(R.id.editAlarmChoosePlaylistButton);
		Utils.createTouchListener(this, playlistButton, R.drawable.button1shape, R.drawable.button1shapepressed);
		playlistButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(EditAlarm.this, ChoosePlaylist.class), CHOSEPLAYLISTREQUESTCODE);
			}
		});
		dateButton = (Button)findViewById(R.id.editAlarmChooseDateButton);
		Utils.createTouchListener(this, dateButton, R.drawable.button1shape, R.drawable.button1shapepressed);
		dateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(EditAlarm.this, ChooseDate.class), CHOSEDATEREQUESTCODE);
			}
		});
	}
	
	private void createTimePicker() {
		String clockType = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
		timePicker = (TimePicker)findViewById(R.id.editAlarmTimePicker);
		timePicker.setIs24HourView(Integer.parseInt(clockType) == 24);
		Calendar c = Calendar.getInstance();
		timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
	}
	
	private void createCheckBoxes() {
		repeatCB = (CheckBox)findViewById(R.id.repeatCheckbox);
		repeatCB.setChecked(true);
		vibrateCB = (CheckBox)findViewById(R.id.vibrateCheckbox);
		vibrateCB.setChecked(false);
		vibrateCB.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (vibrateCB.isChecked()) {
					Vibrator vi = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
					vi.vibrate(1000);
				}
			}
		});
	}
	
	private void createSeekBar() {
		volumeSB = (SeekBar)findViewById(R.id.volumeSeekbar);
		AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		volumeSB.setMax(0);
		volumeSB.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		volumeSB.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
	}
	
	private void saveInDB() {
		if (alarm != null) {
			alarm.updateInDB(alarmNameETContent, vibrateCB.isChecked(), true, selectedDays, dateSelectionMode==ChooseDate.REPEATDATECODE, repeatCB.isChecked(), playlistId, volumeSB.getProgress());
			alarm.deleteDatesFromDB();
		} else {
			alarm = Alarm.createAlarmInDB(alarmNameETContent, vibrateCB.isChecked(), true, selectedDays, (dateSelectionMode==ChooseDate.REPEATDATECODE), repeatCB.isChecked(), playlistId, volumeSB.getProgress());		
		}
		saveAlarmDates();
	}
	
	private void saveAlarmDates() {
		if (dateSelectionMode == ChooseDate.UNIQUEDATECODE) {
			alarm.saveAlarmDate(uniqueDate.get(Keys.YEAR)+0, uniqueDate.get(Keys.MONTH)+1, uniqueDate.get(Keys.DAY)+0, uniqueDate.get(Keys.HOUR)+0, uniqueDate.get(Keys.MINUTE)+0);
		} else if (dateSelectionMode == ChooseDate.REPEATDATECODE) {
			alarm.saveAlarmDates(uniqueDate.get(Keys.HOUR), uniqueDate.get(Keys.MINUTE));
		}
	}
}
