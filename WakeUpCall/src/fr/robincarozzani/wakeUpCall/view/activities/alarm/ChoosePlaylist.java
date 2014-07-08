package fr.robincarozzani.wakeUpCall.view.activities.alarm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import fr.robincarozzani.wakeUpCall.constants.Values;
import fr.robincarozzani.wakeUpCall.objects.Playlist;
import fr.robincarozzani.wakeUpCall.view.activities.Utils;
import fr.robincarozzani.wakeUpCall.view.activities.playlist.EditPlaylist;

public class ChoosePlaylist extends Activity {
	
	private Button newButton;
	
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	private SimpleAdapter adapter;
	private ListView playlistLV;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chooseplaylist);
		getActionBar().hide();
		createButton();
		adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2,
									new String[] {Keys.TITLE, Keys.SUBTITLE},
									new int[] { android.R.id.text1, android.R.id.text2 });
		createListView();
		loadListView();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnData) {
		if ((requestCode == 1) && (returnData != null)) {
			loadListView();
			@SuppressWarnings("unchecked")
			HashMap<String, String> map = (HashMap<String, String>)playlistLV.getItemAtPosition(data.size()-1);
			Intent returnIntent = new Intent();
			returnIntent.putExtra(Keys.PLAYLISTID, Integer.parseInt((String)map.get(Keys.ID)));
			returnIntent.putExtra(Keys.PLAYLISTNAME, (String)map.get(Keys.TITLE));
			setResult(0, returnIntent);
			finish();
		}
	}

	private void createButton() {
		newButton = (Button)findViewById(R.id.choosePlaylistNewButton);
		Utils.createTouchListener(this, newButton, R.drawable.button1shape, R.drawable.button1shapepressed);
		newButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(ChoosePlaylist.this, EditPlaylist.class),  1);
			}
		});
	}
	
	private void createListView() {
		playlistLV = (ListView)findViewById(R.id.choosePlaylistLV);
		playlistLV.setAdapter(adapter);
		playlistLV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				@SuppressWarnings("unchecked")
				HashMap<String, String> map = (HashMap<String, String>)playlistLV.getItemAtPosition(position);
				Intent returnIntent = new Intent();
				returnIntent.putExtra(Keys.PLAYLISTID, Integer.parseInt((String)map.get(Keys.ID)));
				returnIntent.putExtra(Keys.PLAYLISTNAME, (String)map.get(Keys.TITLE));
				setResult(0, returnIntent);
				finish();
			}
		});
	}
	
	private void loadListView() {
		data.clear();
		adapter.notifyDataSetChanged();
		for (Playlist p : Playlist.getListPlaylists("", Values.IGNORE)) {
			int nbSongs = p.getNbSongs();
			if (nbSongs > 1) 
				addItems(p.getName(), nbSongs + " " + getResources().getString(R.string.songs), ""+p.getId());
			else
				addItems(p.getName(), nbSongs + " " + getResources().getString(R.string.song), ""+p.getId());
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
}
