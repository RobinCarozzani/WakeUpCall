package fr.robincarozzani.wakeUpCall.view.listAdapters.alarm;

import java.util.List;

import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.objects.Alarm;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class AlarmsListAdapter extends ArrayAdapter<Alarm> {
	
	private Context _context;
	private int _resource;
	private List<Alarm> _objects;
	
	public AlarmsListAdapter(Context context, int resource, List<Alarm> objects) {
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
		AlarmHolder holder = new AlarmHolder();
		holder.alarm = _objects.get(position);
		holder.name = (TextView)row.findViewById(R.id.listAlarmsItemName);
		holder.name.setText(holder.alarm.getName());
		holder.nextActivation = (TextView)row.findViewById(R.id.listAlarmsItemNextDate);
		holder.nextActivation.setText(row.getResources().getString(R.string.next) + " : " +
									  holder.alarm.getNextActivationString(_context));
		holder.activated = (CheckBox)row.findViewById(R.id.listAlarmsItemCheckbox);
		holder.activated.setChecked(holder.alarm.isActivated());
		holder.activated.setTag(holder.alarm);
		row.setTag(holder);
		return row;
	}
	
	public static class AlarmHolder {
		private Alarm alarm;
		private TextView name;
		private TextView nextActivation;
		private CheckBox activated;
	}

}
