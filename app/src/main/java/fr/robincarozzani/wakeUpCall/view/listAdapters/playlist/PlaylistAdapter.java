package fr.robincarozzani.wakeUpCall.view.listAdapters.playlist;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.objects.Song;

public class PlaylistAdapter extends ArrayAdapter<Song> {

	private Context _context;
	private int _resource;
	private List<Song> _objects;

	public PlaylistAdapter(Context context, int resource, List<Song> objects) {
		super(context, resource, objects);
		_context = context;
		_resource = resource;
		_objects = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		LayoutInflater inflater = ((Activity)_context).getLayoutInflater();
		row = inflater.inflate(_resource, parent, false);
		StringHolder holder = new StringHolder();
		holder.song = _objects.get(position);
		holder.name = (TextView)row.findViewById(R.id.editPlayListItemName);
		holder.name.setText(holder.song.getName() + "\n" + holder.song.getArtist());
		holder.up = (ImageButton)row.findViewById(R.id.editPlayListUpButton);
		holder.up.setTag(holder.song);
		holder.down = (ImageButton)row.findViewById(R.id.editPlayListDownButton);
		holder.down.setTag(holder.song);
		holder.delete = (ImageButton)row.findViewById(R.id.editPlayListDeleteButton);
		holder.delete.setTag(holder.song);
		row.setTag(holder);
		return row;
	}

	public static class StringHolder {
		private Song song;
		private TextView name;
		private ImageButton up;
		private ImageButton down;
		private ImageButton delete;
	}

}
