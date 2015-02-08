package fr.robincarozzani.wakeUpCall.view.activities.playlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import fr.robincarozzani.wakeUpCall.constants.Values;
import fr.robincarozzani.wakeUpCall.objects.Alarm;
import fr.robincarozzani.wakeUpCall.objects.Playlist;
import fr.robincarozzani.wakeUpCall.view.activities.Utils;

public class ListPlaylists extends Activity {
	
	private Button newPlaylistButton;
	
	private ArrayList<Playlist> listPlaylists;
	
    private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    private SimpleAdapter adapter;
    private ListView playlistsLV;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		createButton();
		adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2,
									new String[] {Keys.TITLE, Keys.SUBTITLE},
				 					new int[] { android.R.id.text1,
      						 					android.R.id.text2 });
		createListView();
		loadListView();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			loadListView();
		}
	}
	
	private void createButton() {
		newPlaylistButton = (Button)findViewById(R.id.listNewItemButton);
		newPlaylistButton.setText(getResources().getString(R.string.listPlaylistsNewPlaylistButtonText));
		Utils.createTouchListener(this, newPlaylistButton, R.drawable.button1shape, R.drawable.button1shapepressed);
		newPlaylistButton.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(ListPlaylists.this, EditPlaylist.class), 1);	
			}
		});
	}
	
	private void createListView() {
		playlistsLV = (ListView)findViewById(R.id.list_listview);
		playlistsLV.setAdapter(adapter);
		playlistsLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(ListPlaylists.this, EditPlaylist.class);
				@SuppressWarnings("unchecked")
				HashMap<String, String> map = (HashMap<String, String>)playlistsLV.getItemAtPosition(position);
				Bundle b = new Bundle();
				b.putInt(Keys.PLAYLISTID, Integer.parseInt((String)map.get(Keys.ID)));
				intent.putExtras(b);
				startActivityForResult(intent, 1);
			}
		});
		playlistsLV.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				removeItem(position);
				return true;
			}
		});
	}
	
	private void loadListView() {
		data.clear();
		adapter.notifyDataSetChanged();
		listPlaylists = Playlist.getListPlaylists("", Values.IGNORE);
		for (Playlist p : listPlaylists) {
			int nbSongs = p.getNbSongs();
			if (nbSongs > 1)
				addItems(p.getName(), nbSongs + " " + getResources().getString(R.string.songs) + " - " + p.getDurationString(this), ""+p.getId());
			else
				addItems(p.getName(), nbSongs + " " + getResources().getString(R.string.song) + " - " + p.getDurationString(this), ""+p.getId());
		}
	}
	
	private void addItems(String item, String subItem, String id) {
		Map<String, String> fullItem = new HashMap<String, String>(3);
		fullItem.put(Keys.TITLE, item);
		fullItem.put(Keys.SUBTITLE, subItem);
		fullItem.put(Keys.ID, id);
		data.add(fullItem);
		adapter.notifyDataSetChanged();
	}
	
	private void removeItem(int position) {
		Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
		final int pos = position;
		final ArrayList<Alarm> alarmsUsing = listPlaylists.get(position).getAlarmsUsing();
		new AlertDialog.Builder(this)
        .setTitle(R.string.confirmation_title)
        .setMessage(getDeleteMessage(alarmsUsing.size()))
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	removeFromDB(pos);
            	for (Alarm a : alarmsUsing) {
            		a.deleteFromDB();
            	}
            	listPlaylists.remove(pos);
            	data.remove(pos);
				adapter.notifyDataSetChanged();
            }
        })
        .setNegativeButton(R.string.no, null)
        .setCancelable(false)
        .show();
	}
	
	private String getDeleteMessage(int nbAlarmsUsing) {
		if (nbAlarmsUsing == 0) {
			return getResources().getString(R.string.confirmation_delete);
		}
		String alarmSingOrPlural = (nbAlarmsUsing>1) ? getResources().getString(R.string.alarmsPlural) : getResources().getString(R.string.alarmsSingular);
		String deleteAlarmSingOrPlural = (nbAlarmsUsing>1) ? getResources().getString(R.string.deleteAlarmsPlural) : getResources().getString(R.string.deleteAlarmSingular);
		return (getResources().getString(R.string.playlistUsedIn) + " " + nbAlarmsUsing + " " + alarmSingOrPlural + ".\n" + deleteAlarmSingOrPlural);
	}
	
	private void removeFromDB(int position) {
		listPlaylists.get(position).deleteFromDB();
	}
}
