package fr.robincarozzani.wakeUpCall.view.listAdapters.playlist;

import java.io.File;
import java.util.List;

import fr.robincarozzani.wakeUpCall.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends ArrayAdapter<File> {
	
	private Context _context;
	private int _resource;
	private List<File> _objects;

	public FileListAdapter(Context context, int resource, List<File> objects) {
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
		FileHolder holder = new FileHolder();
		holder.file = _objects.get(position);
		holder.imageView = (ImageView)row.findViewById(R.id.songsSelectorFileIcon);
		if (holder.file.isDirectory()) {
			holder.imageView.setImageResource(R.drawable.directory_icon);
		} else {
			holder.imageView.setImageResource(R.drawable.musicfile_icon);
		}
		holder.textView = (TextView)row.findViewById(R.id.songsSelectorFileText);
		holder.textView.setText(holder.file.getName());
		row.setTag(holder);
		return row;
	}
	
	public static class FileHolder {
		private File file;
		private ImageView imageView;
		private TextView textView;
	}

}
