package fr.robincarozzani.wakeUpCall.view.activities.playlist;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import fr.robincarozzani.wakeUpCall.objects.Playlist;
import fr.robincarozzani.wakeUpCall.objects.Song;
import fr.robincarozzani.wakeUpCall.view.activities.Utils;
import fr.robincarozzani.wakeUpCall.view.listAdapters.playlist.PlaylistAdapter;

public class EditPlaylist extends Activity {
	
	private EditText nameEditText;
	private Button saveButton, addButton, removeAllButton;
	private ProgressBar savePB;
	private PlaylistAdapter adapter;
	
	private Bundle bundle;
	private Intent returnIntent;
	
	private Playlist playlist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editplaylist);
		returnIntent = new Intent();
		playlist = null;
		nameEditText = (EditText)findViewById(R.id.editPlaylistNameEditText);
		savePB = (ProgressBar)findViewById(R.id.editPlaylistSaveProgressBar);
		savePB.setVisibility(View.INVISIBLE);
		createListView();
		bundle = getIntent().getExtras();
		dealWithBundle();
		createButtons();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			@SuppressWarnings("unchecked")
			ArrayList<File> selectedFiles = (ArrayList<File>)data.getSerializableExtra(Keys.FILES);
			for (File f : selectedFiles) {
				addMusicFile(f);
			}
		}
	}

	private void createButtons() {
		saveButton = (Button)findViewById(R.id.editPlaylistSaveButton);
		Utils.createTouchListener(this, saveButton, R.drawable.button2shape, R.drawable.button2shapepressed);
		addButton = (Button)findViewById(R.id.editPlaylistAddSongsButton);
		Utils.createTouchListener(this, addButton, R.drawable.button1shape, R.drawable.button1shapepressed);
		removeAllButton = (Button)findViewById(R.id.editPlaylistRemoveAllButton);
		Utils.createTouchListener(this, removeAllButton, R.drawable.button1shape, R.drawable.button1shapepressed);
		final Context c = this;
		saveButton.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				String playlistName = nameEditText.getText().toString().trim();
				if (playlistName.equals("")) {
					Toast.makeText(c, getResources().getString(R.string.playlistNameEmpty), Toast.LENGTH_SHORT).show();
				} else {
					if (adapter.getCount() == 0) {
						Toast.makeText(c, getResources().getString(R.string.playlistNoSongs), Toast.LENGTH_SHORT).show();
					} else {
						savePB.setVisibility(View.VISIBLE);
						saveButton.setVisibility(View.INVISIBLE);
						saveOrUpdate(playlistName);
						finish();
					}
				}
			}
		});
		addButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(EditPlaylist.this, SongsSelector.class), 1);
			}
		});
		removeAllButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (adapter.getCount() > 0) {
					new AlertDialog.Builder(c)
					.setTitle(R.string.confirmation_title)
					.setMessage(R.string.confirmation_delete)
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							adapter.clear();
						}
					})
					.setNegativeButton(R.string.no, null)
					.setCancelable(false)
					.show();
				}
			}
		});
	}
	
	private void saveOrUpdate(String playlistName) {
		ArrayList<Song> songs = new ArrayList<Song>();
		for (int i=0 ; i<adapter.getCount() ; ++i) {
			songs.add(adapter.getItem(i));
		}
		if (playlist != null) {
			playlist.updateInDB(playlistName, songs);
		} else {
			playlist = Playlist.createPlaylistInDB(playlistName, songs);
		}
		setResult(0, returnIntent);
		Toast.makeText(this, getResources().getString(R.string.playlistSaved) + "\n" +
					   getResources().getString(R.string.playlist) + " : " + playlist.getName(),
					   Toast.LENGTH_SHORT).show();
	}

	private void createListView() {
		adapter = new PlaylistAdapter(EditPlaylist.this, R.layout.editplaylist_item, new ArrayList<Song>());
		ListView songsList = (ListView)findViewById(R.id.editPlaylistSongsListView);
		songsList.setAdapter(adapter);
		loadListView();
	}
	
	private void loadListView() {
		if (playlist != null) {
			for (String path : playlist.getSongsPaths()) {
				addMusicFile(new File(path));
			}
		}
	}
	
	private void dealWithBundle() {
		if (bundle != null) {
			playlist = new Playlist(bundle.getInt(Keys.PLAYLISTID));
			nameEditText.setText(playlist.getName());
			for (String path : playlist.getSongsPaths()) {
				addMusicFile(new File(path));
			}
		}
	}
	
	private void addMusicFile(File f) {
		if (f.isFile() && f.canRead()) {
			HashMap<String, String> musicInfo = fr.robincarozzani.wakeUpCall.objects.Utils.getMusicFileInfos(this, f);
			addItem(new Song(f.getAbsolutePath(), musicInfo.get(Keys.TITLE), musicInfo.get(Keys.ARTIST)));
		}
	}

	private void addItem(Song item) {
		adapter.add(item);
	}

	public void upOnClickHandler(View v) {
		Song itemToPutUp = (Song)v.getTag();
		int pos = adapter.getPosition(itemToPutUp);
		if (pos-1 >= 0) {
			adapter.remove(itemToPutUp);
			adapter.insert(itemToPutUp, pos-1);
		}
	}

	public void downOnClickHandler(View v) {
		Song itemToPutUp = (Song)v.getTag();
		int pos = adapter.getPosition(itemToPutUp);
		if (pos+1 < adapter.getCount()) {
			adapter.remove(itemToPutUp);
			adapter.insert(itemToPutUp, pos+1);
		}
	}

	public void deleteOnClickHandler(View v) {
		final View v1 = v;
		new AlertDialog.Builder(this)
		.setTitle(R.string.confirmation_title)
		.setMessage(R.string.confirmation_delete)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Song itemToDelete = (Song)v1.getTag();
				adapter.remove(itemToDelete);
			}
		})
		.setNegativeButton(R.string.no, null)
		.setCancelable(false)
		.show();
	}

}
