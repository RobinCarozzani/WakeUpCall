package fr.robincarozzani.wakeUpCall.view.activities.playlist;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import fr.robincarozzani.wakeUpCall.objects.FilesLister;
import fr.robincarozzani.wakeUpCall.view.activities.Utils;
import fr.robincarozzani.wakeUpCall.view.listAdapters.playlist.FileListAdapter;

public class SongsSelector extends Activity {
	
	private Button addAllFilesButton, doneButton;
	private TextView currentPathTextView;
	
	private ListView filesListView;
	private FileListAdapter adapter;
	
	private FilesLister fl;
	
	private ArrayList<File> selectedFiles;
	private Intent returnIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_songsselector);
		getActionBar().hide();
		selectedFiles = new ArrayList<File>();
		returnIntent = new Intent();
		createButtons();
		currentPathTextView = (TextView)findViewById(R.id.songsSelectorCurrentPath);
		createListView();
		fl = new FilesLister();
		updateListViewAndPath();
	}
	
	@Override
	public void onBackPressed() {
		if (fl.goToParent())
			updateListViewAndPath();
		else
			done();
	}
	
	private void createButtons() {
		addAllFilesButton = (Button)findViewById(R.id.songsSelectorSelectAllFilesButton);
		Utils.createTouchListener(this, addAllFilesButton, R.drawable.button1shape, R.drawable.button1shapepressed);
		final Context c = this;
		addAllFilesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int n = fl.getNumberOfMusicFiles();
				if (n == 0)
					Toast.makeText(c, c.getResources().getString(R.string.noFilesFound), Toast.LENGTH_LONG).show();
				else {
					for (File f : fl.getMusicFiles())
						selectedFiles.add(f);
					if (n == 1) 
						Toast.makeText(c, c.getResources().getString(R.string.oneFileAdded), Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(c, n + " " + c.getResources().getString(R.string.manyFilesAdded), Toast.LENGTH_SHORT).show();
				}
			}
		});
		doneButton = (Button)findViewById(R.id.songsSelectorDoneButton);
		Utils.createTouchListener(this, doneButton, R.drawable.button2shape, R.drawable.button2shapepressed);
		doneButton.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				done();
			}
		});
	}
	
	private void createListView() {
		filesListView = (ListView)findViewById(R.id.songSelectorFilesListView);
		adapter = new FileListAdapter(SongsSelector.this, R.layout.songsselector_item, new ArrayList<File>());
		filesListView.setAdapter(adapter);
		final Context c = this;
		filesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (fl.goTo(adapter.getItem(position))) {
					updateListViewAndPath();
				} else {
					selectedFiles.add(adapter.getItem(position));
					Toast.makeText(c, c.getResources().getString(R.string.thisFileAdded), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	private void updateListViewAndPath() {
		fl.listFilesInAdapter(adapter);
		filesListView.setSelectionAfterHeaderView();
		currentPathTextView.setText(fl.getCurrentPath());
	}
	
	private void done() {
		returnIntent.putExtra(Keys.FILES, selectedFiles);
		setResult(0, returnIntent);
		finish();
	}
	
}
