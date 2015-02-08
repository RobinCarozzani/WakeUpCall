package fr.robincarozzani.wakeUpCall.view.activities.alarm;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import fr.robincarozzani.wakeUpCall.constants.db.AlarmDatesDB;
import fr.robincarozzani.wakeUpCall.objects.Alarm;
import fr.robincarozzani.wakeUpCall.objects.Playlist;
import fr.robincarozzani.wakeUpCall.objects.Song;
import fr.robincarozzani.wakeUpCall.objects.alarmManager.AlarmManagerHelper;
import fr.robincarozzani.wakeUpCall.objects.database.Database;
import fr.robincarozzani.wakeUpCall.view.activities.Utils;


@SuppressWarnings("deprecation")
public class ActiveAlarm extends Activity {
	
	private static final int progressBarRefreshingTime = 500;
	
	private WakeLock wakeLock;
	private KeyguardLock keyguardLock;
	
	private Alarm alarm;
	private Playlist playlist;
	private List<Song> songs;
	private int currentSongIndex;
	
	private MediaPlayer mPlayer;
	
	private AudioManager audio;
	private int originalVolume;
	private Vibrator v;
	private Thread vibrateThread, songProgressThread;
	
	private TextView nameTV;
	private TextView playlistTV;
	private TextView songTV;
	private ProgressBar songPB;
	private TextView timeBeforeVibrateTV;
	private Button dismissButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activealarm);
		Database.create(this);
		getInfos();
		createTextViews();
		createProgressBar();
		createButton();
		setVolume();
		songProgressThread = null;
		createMediaPlayer();
		v = null;
		vibrateThread = null;
		manageVibrator();
		updateDB();
		Utils.setNotification(this);
		wakeUpScreen();
	}
	
	@Override
	public void onDestroy() {
		end();
		super.onDestroy();
	}
	
	private void getInfos() {
		int alarmId = getIntent().getIntExtra(Keys.ALARMID, 0);
		alarm = new Alarm(alarmId);
		playlist = new Playlist(alarm.getPlaylistId());
		songs = new ArrayList<Song>();
		for (String path : playlist.getSongsPaths()) {
			HashMap<String, String> infos = fr.robincarozzani.wakeUpCall.objects.Utils.getMusicFileInfos(this, new File(path));
			songs.add(new Song(path, infos.get(Keys.TITLE), infos.get(Keys.ARTIST)));
		}
	}

	private void updateDB() {
		Database.executeNoResult("DELETE FROM " + AlarmDatesDB.TABLENAME
							  + " WHERE " + AlarmDatesDB.ALARMID + " = " + alarm.getId()
							  + " AND " + AlarmDatesDB.ACTIVATIONDATE + " = (SELECT " + AlarmDatesDB.ACTIVATIONDATE + " FROM " + AlarmDatesDB.TABLENAME
														 				 + " WHERE " + AlarmDatesDB.ALARMID + " = " + alarm.getId()
														 				 + " ORDER BY " + AlarmDatesDB.ACTIVATIONDATE
														 				 + " LIMIT 1)");
		if (alarm.hasMultipleDates()) {
			Calendar cal = Calendar.getInstance();
			int index = (cal.get(Calendar.DAY_OF_WEEK)-Calendar.MONDAY)%7;
			boolean[] selectedDays = alarm.getSelectedDays();
			if (selectedDays[index]) {
				cal.add(Calendar.DATE, 7);
				alarm.saveAlarmDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
				AlarmManagerHelper.setAlarm(this, alarm);
			}
		} else {
			alarm.toggleActivation();
		}
	}
	
	private void createTextViews() {
		nameTV = (TextView)findViewById(R.id.activeAlarmNameTV);
		nameTV.setText(alarm.getName());
		playlistTV = (TextView)findViewById(R.id.activeAlarmPlaylistTV);
		playlistTV.setText(getResources().getString(R.string.playlist) + ": " + playlist.getName());
		songTV = (TextView)findViewById(R.id.activeAlarmCurrentSongTV);
		timeBeforeVibrateTV = (TextView)findViewById(R.id.remainingTimeBeforeVibrateTV);
	}
	
	private void createProgressBar() {
		songPB = (ProgressBar)findViewById(R.id.songProgressBar);
	}
	
	private void createButton() {
		dismissButton = (Button)findViewById(R.id.activeAlarmDismissButton);
		Utils.createTouchListener(this, dismissButton, R.drawable.button1shape, R.drawable.button1shapepressed);
		dismissButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				end();
			}
		});
	}
	
	private void setVolume() {
		audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		originalVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		audio.setStreamVolume(AudioManager.STREAM_MUSIC, alarm.getVolume(), AudioManager.FLAG_VIBRATE);
	}
	
	private void createMediaPlayer() {
		currentSongIndex = -1;
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				songTV.setText("");
				if ((currentSongIndex < songs.size()-1) || (alarm.isRepeat() && currentSongIndex == songs.size()-1)) {
					mPlayer.stop();
					mPlayer.reset();
					playNextSong();
				} else {
					mPlayer.release();
				}
			}
		});
		playNextSong();
	}
	
	private void playNextSong() {
		if (songProgressThread != null) {
			songProgressThread.interrupt();
			songProgressThread = null;
		}
		if (currentSongIndex == songs.size()-1) currentSongIndex = -1;
		playSong(songs.get(++currentSongIndex).getPath());
		songTV.setText(getResources().getString(R.string.song) + ": " + songs.get(currentSongIndex).getName() + " (" + songs.get(currentSongIndex).getArtist() + ")");
	}
	
	private void playSong(String path) {
		try {
	        mPlayer.setDataSource(path);
	        mPlayer.prepare();
	        mPlayer.start();
	        manageProgressBar();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	private void manageProgressBar() {
		songPB.setMax(mPlayer.getDuration());
		songPB.setProgress(0);
		final Handler mHandler = new Handler();
		songProgressThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						int currentProgress = songPB.getProgress();
						int remaining = songPB.getMax()-currentProgress;
						int addProgress = (remaining<progressBarRefreshingTime) ? remaining : progressBarRefreshingTime;
						final int newProgress = currentProgress+addProgress;
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								songPB.setProgress(newProgress);
							}
						});
						Thread.sleep(progressBarRefreshingTime);
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		});
		songProgressThread.start();
	}
	
	private void manageVibrator() {
		if (alarm.isVibrate()) {
			final String vibratorString = getResources().getString(R.string.vibrate);
			final Handler mHandler = new Handler();
			vibrateThread = new Thread(new Runnable() {	
				@Override
				public void run() {
					int minutes = alarm.getVibrateDelay();
					int seconds = 0;
					while ((minutes > 0) || (seconds > 0)) {
						try {
							final int min = minutes;
							final int sec = seconds;
							final String minS = (minutes<10) ? "0"+minutes : ""+minutes;
							final String secS = (seconds<10) ? "0"+seconds : ""+seconds;
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									timeBeforeVibrateTV.setText(vibratorString + " - " + minS + ":" + secS);
									if ((min == 0) && (sec < 10)) {
										timeBeforeVibrateTV.setTextColor(Color.RED);
									}
								}
							});
							Thread.sleep(1000);
							seconds--;
							if (seconds < 0) {
								seconds = 59;
								minutes--;
							}
						} catch (InterruptedException e) {
							return;
						}
					}
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							timeBeforeVibrateTV.setText(vibratorString + " - 00:00");
						}
					});
					v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
					long[] pattern = {0, 1500, 1000};
					v.vibrate(pattern, 0);
				}
			});
			vibrateThread.start();
		}
	}
	
	@SuppressLint("Wakelock")
	private void wakeUpScreen() {
		PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
        wakeLock.acquire();
        KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE); 
        keyguardLock =  keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();
	}
	
	private void end() {
		if (songProgressThread != null) {
			songProgressThread.interrupt();
			songProgressThread = null;
		}
		if (vibrateThread != null) {
			vibrateThread.interrupt();
			vibrateThread = null;
		}
		if (v != null) {
			v.cancel();
		}
		mPlayer.stop();
		audio.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, AudioManager.FLAG_VIBRATE);
		keyguardLock.reenableKeyguard();
		finish();
	}
}
