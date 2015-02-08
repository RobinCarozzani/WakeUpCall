package fr.robincarozzani.wakeUpCall.view.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.objects.database.Database;
import fr.robincarozzani.wakeUpCall.view.activities.alarm.ListAlarms;
import fr.robincarozzani.wakeUpCall.view.activities.playlist.ListPlaylists;

public class Home extends Activity {
	
	private Button alarmsButton;
	private Button playlistsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Database.create(this);
		setContentView(R.layout.activity_home);
		createButtons();
	}
	
	@Override
    protected void onDestroy() {
    	Database.destroy();
    	super.onDestroy();
    }
	
	private void createButtons() {
		alarmsButton = (Button)findViewById(R.id.homeAlarmsButton);
		Utils.createTouchListener(this, alarmsButton, R.drawable.button1shape, R.drawable.button1shapepressed);
		playlistsButton = (Button)findViewById(R.id.homePlaylistsButton);
		Utils.createTouchListener(this, playlistsButton, R.drawable.button2shape, R.drawable.button2shapepressed);
		alarmsButton.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Home.this, ListAlarms.class));
			}
		});
		playlistsButton.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Home.this, ListPlaylists.class));
			}
		});
	}
}
