package fr.robincarozzani.wakeUpCall.objects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Period;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Values;
import fr.robincarozzani.wakeUpCall.constants.db.AlarmDB;
import fr.robincarozzani.wakeUpCall.constants.db.AlarmDatesDB;
import fr.robincarozzani.wakeUpCall.objects.database.Database;

public class Alarm {

	private int _id;
	
	public static Alarm createAlarmInDB(String name, boolean vibrate, boolean activated, boolean[] selectedDays, boolean multipleDates, boolean repeat, int playlistId, int volume) {
		int newAlarmId = Database.getNewId(AlarmDB.TABLENAME);
		Database.executeNoResult("INSERT INTO " + AlarmDB.TABLENAME + " VALUES(" + newAlarmId + ", "
																				 + "\"" + name + "\", "
																				 + null + ", " // phone no
																				 + null + ", " // message
																				 + boolToInt(vibrate) + ", "
																				 + boolToInt(activated) + ", "
																				 + boolToInt(selectedDays[0]) + ", "
																				 + boolToInt(selectedDays[1]) + ", "
																				 + boolToInt(selectedDays[2]) + ", "
																				 + boolToInt(selectedDays[3]) + ", "
																				 + boolToInt(selectedDays[4]) + ", "
																				 + boolToInt(selectedDays[5]) + ", "
																				 + boolToInt(selectedDays[6]) + ", "
																				 + boolToInt(multipleDates) + ", "
																				 + boolToInt(repeat) + ", "
																				 + playlistId + ", "
																				 + volume + ")"
								);
		return new Alarm(newAlarmId);
	}
	
	public static ArrayList<Alarm> getListAlarms(String conditionFieldName, int conditionValue) {
		String condition = "";
		if (conditionValue != Values.IGNORE) {
			condition += " WHERE " + conditionFieldName + " = " + conditionValue;
		}
		ArrayList<Alarm> alarms = new ArrayList<Alarm>();
		Cursor c = Database.executeWithResult("SELECT " + AlarmDB.ID + " FROM " + AlarmDB.TABLENAME + condition);
		if (c.moveToFirst()) {
			do {
				alarms.add(new Alarm(c.getInt(0)));
			} while (c.moveToNext());
		}
		return alarms;
	}
	
	public static int nbActivatedAlarms() {
		return getListAlarms(AlarmDB.ACTIVATED, Values.TRUE).size();
	}
	
	public static String nextAlarmDate() {
		Cursor c = Database.executeWithResult("SELECT " + AlarmDatesDB.ALARMID + " FROM " + AlarmDatesDB.TABLENAME + ", " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.ACTIVATED + " = " + Values.TRUE + " AND " + AlarmDB.ID + " = " + AlarmDatesDB.ALARMID + " ORDER BY DATE(" + AlarmDatesDB.ACTIVATIONDATE + ") LIMIT 1");
		if (c.moveToFirst()) {
			Date d = new Alarm(c.getInt(0)).getNextActivation();
			DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
			if (d != null)
				return format.format(d);
		}
		return "";
	}
	
	public Alarm(int id) {
		_id = id;
	}

	public int getId() {
		return _id;
	}

	public String getName() {
		Cursor c = Database.executeWithResult("SELECT " + AlarmDB.NAME + " FROM " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.ID + " = " + getId());
		return c.getString(0);
	}
	
	public boolean isVibrate() {
		Cursor c = Database.executeWithResult("SELECT " + AlarmDB.VIBRATE + " FROM " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.ID + " = " + getId());
		return (c.getInt(0) != Values.FALSE);
	}

	public boolean isActivated() {
		Cursor c = Database.executeWithResult("SELECT " + AlarmDB.ACTIVATED + " FROM " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.ID + " = " + getId());
		return (c.getInt(0) != Values.FALSE);
	}
	
	public void toggleActivation() {
		Database.executeNoResult("UPDATE " + AlarmDB.TABLENAME + " SET " + AlarmDB.ACTIVATED + " = " + (isActivated() ? 0 : 1) + " WHERE " + AlarmDB.ID + " = " + getId());
	}
	
	public void setActivated(boolean activated) {
		Database.executeNoResult("UPDATE " + AlarmDB.TABLENAME + " SET " + AlarmDB.ACTIVATED + " = " + (activated ? 1 : 0) + " WHERE " + AlarmDB.ID + " = " + getId());
	}
	
	public void setMultipleDates(boolean hasMultipleDates) {
		Database.executeNoResult("UPDATE " + AlarmDB.TABLENAME + " SET " + AlarmDB.MULTIPLEDATES + " = " + (hasMultipleDates ? 1 : 0) + " WHERE " + AlarmDB.ID + " = " + getId());
	}
	
	public boolean hasMultipleDates() {
		Cursor c = Database.executeWithResult("SELECT " + AlarmDB.MULTIPLEDATES + " FROM " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.ID + " = " + getId());
		return (c.getInt(0) != Values.FALSE);
	}
	
	public boolean isRepeat() {
		Cursor c = Database.executeWithResult("SELECT " + AlarmDB.REPEAT + " FROM " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.ID + " = " + getId());
		return (c.getInt(0) != Values.FALSE);
	}
	
	public int getPlaylistId() {
		Cursor c = Database.executeWithResult("SELECT " + AlarmDB.PLAYLISTID + " FROM " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.ID + " = " + getId());
		return c.getInt(0);
	}
	
	public int getVolume() {
		Cursor c = Database.executeWithResult("SELECT " + AlarmDB.VOLUME + " FROM " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.ID + " = " + getId());
		return c.getInt(0);
	}
	
	public boolean[] getSelectedDays() {
		boolean[] selectedDays = new boolean[7];
		if (hasMultipleDates()) {
			Cursor c = Database.executeWithResult("SELECT " + AlarmDB.MONDAY + ", " + AlarmDB.TUESDAY + ", " + AlarmDB.WEDNESDAY + ", " + AlarmDB.THURSDAY + ", " + AlarmDB.FRIDAY + ", " + AlarmDB.SATURDAY + ", " + AlarmDB.SUNDAY + " FROM " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.ID + " = " + getId());
			if (c.moveToFirst()) {
				for (int i=0 ; i<7 ; ++i) {
					selectedDays[i] = c.getInt(i)!=0;
				}
			}
		}
		return selectedDays;
	}
	
	@SuppressLint("SimpleDateFormat")
	public Date getNextActivation() {
		if (isActivated()) {
			Date date = new Date();
			Cursor cDate = Database.executeWithResult("SELECT " + AlarmDatesDB.ACTIVATIONDATE + " FROM " + AlarmDatesDB.TABLENAME + " WHERE " + AlarmDatesDB.ALARMID + " = " + getId() + " ORDER BY DATE(" + AlarmDatesDB.ACTIVATIONDATE + ") LIMIT 1");
			if (!cDate.moveToFirst()) return null;
			String sDate = cDate.getString(0).toString();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				date = sdf.parse(sDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return date;
		}
		return new Date();
	}
	
	@SuppressLint("SimpleDateFormat")
	public String getNextActivationString(Context c) {
		if (isActivated()) {
			DateFormat format = SimpleDateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
			Date date = getNextActivation();
			if (date != null)
				return format.format(date);
		}
		return c.getResources().getString(R.string.notActivated);
	}
	
	@SuppressLint("SimpleDateFormat")
	public void updateMultipleDatesDB() {
		if (hasMultipleDates()) {
			Cursor c = Database.executeWithResult("SELECT " + AlarmDatesDB.ACTIVATIONDATE + " FROM " + AlarmDatesDB.TABLENAME + " WHERE " + AlarmDatesDB.ALARMID + " = " + getId());
			if (c.moveToFirst()) {
				do {
					String sDate = c.getString(0).toString();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						Date date = sdf.parse(sDate);
						Date now = new Date();
						if (date.before(now)) {
							Database.executeNoResult("DELETE FROM " + AlarmDatesDB.TABLENAME + " WHERE " + AlarmDatesDB.ALARMID + " = " + getId() + " AND " + AlarmDatesDB.ACTIVATIONDATE + " = \"" + sDate + "\"");
							Calendar nowCal = Calendar.getInstance();
							Calendar cal = (Calendar)nowCal.clone();
							cal.setTime(date);
							int theDay = cal.get(Calendar.DAY_OF_WEEK);
							int dayOfWeek = nowCal.get(Calendar.DAY_OF_WEEK);
							int daysUntilTheDay = theDay - dayOfWeek;
							if ((daysUntilTheDay < 0) || ((daysUntilTheDay == 0) && (isPastTime(cal)))) {
								daysUntilTheDay += 7;
							}
							Calendar nextDay = (Calendar)cal.clone();
							nextDay.add(Calendar.DAY_OF_WEEK, daysUntilTheDay);
							saveAlarmDate(nextDay.get(Calendar.YEAR), nextDay.get(Calendar.MONTH)+1, nextDay.get(Calendar.DAY_OF_MONTH), nextDay.get(Calendar.HOUR_OF_DAY), nextDay.get(Calendar.MINUTE));
							//Database.executeNoResult("INSERT INTO " + AlarmDatesDB.TABLENAME + " VALUES(" + getId() + ", \"" + dateString + "\")");
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} while (c.moveToNext());
			}
		}
	}
	
	public void updateInDB(String name, boolean vibrate, boolean activated, boolean[] selectedDays, boolean multipleDates, boolean repeat, int idPlaylist, int volume) {
		Database.executeNoResult("UPDATE " + AlarmDB.TABLENAME + " SET " + AlarmDB.NAME + " = \"" + name + "\", " +
																		   AlarmDB.VIBRATE + " = " + boolToInt(vibrate) + ", " +
																   	       AlarmDB.ACTIVATED + " = " + boolToInt(activated) + ", " +
																   	       AlarmDB.MONDAY + " = " + boolToInt(selectedDays[0]) + ", " +
																   	       AlarmDB.TUESDAY + " = " + boolToInt(selectedDays[1]) + ", " +
																   	       AlarmDB.WEDNESDAY + " = " + boolToInt(selectedDays[2]) + ", " +
																   	       AlarmDB.THURSDAY + " = " + boolToInt(selectedDays[3]) + ", " +
																   	       AlarmDB.FRIDAY + " = " + boolToInt(selectedDays[4]) + ", " +
																   	       AlarmDB.SATURDAY + " = " + boolToInt(selectedDays[5]) + ", " +
																   	       AlarmDB.SUNDAY + " = " + boolToInt(selectedDays[6]) + ", " +
																   	       AlarmDB.MULTIPLEDATES + " = " + boolToInt(multipleDates) + ", " +
																   	       AlarmDB.REPEAT + " = " + boolToInt(repeat) + ", " +
																   	       AlarmDB.PLAYLISTID + " = " + idPlaylist + ", " +
																   	       AlarmDB.VOLUME + " = " + volume + " " +
							     "WHERE " + AlarmDB.ID + " = " + getId());
	}
	
	public void deleteDatesFromDB() {
		if (getNextActivation() != null)
			Database.executeNoResult("DELETE FROM " + AlarmDatesDB.TABLENAME + " WHERE " + AlarmDatesDB.ALARMID + " = " + getId());
	}
	
	public void deleteFromDB() {
		deleteDatesFromDB();
		Database.executeNoResult("DELETE FROM " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.ID + " = " + getId());
	}
	
	public void saveAlarmDate(int year, int month, int day, int hour, int minute) {
		String dateString = Utils.stringDate(year, month, day, hour, minute);
		Database.executeNoResult("INSERT INTO " + AlarmDatesDB.TABLENAME + " VALUES(" + getId() + ", \"" + dateString + "\")");
	}
	
	public void saveAlarmDates(int hour, int minute) {
		boolean selectedDays[] = getSelectedDays();
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		for (int i=0 ; i<7 ; ++i) {
			if (selectedDays[i]) {
				int theDay = Calendar.MONDAY+i;
				int daysUntilTheDay = theDay - dayOfWeek;
				if ((daysUntilTheDay < 0) || ((daysUntilTheDay == 0) && (isPastTime(hour, minute))))
					daysUntilTheDay += 7;
				Calendar nextDay = (Calendar)cal.clone();
				nextDay.add(Calendar.DAY_OF_WEEK, daysUntilTheDay);
				saveAlarmDate(nextDay.get(Calendar.YEAR), nextDay.get(Calendar.MONTH)+1, nextDay.get(Calendar.DAY_OF_MONTH), hour, minute);
			}
		}
	}
	
	private boolean isPastTime(int hour, int minute) {
		Calendar c = Calendar.getInstance();
		return ((hour < c.get(Calendar.HOUR_OF_DAY)) ||
				((hour == c.get(Calendar.HOUR_OF_DAY)) && (minute <= c.get(Calendar.MINUTE))));
	}
	
	private boolean isPastTime(Calendar time) {
		Date date = new Date();
		Calendar now = (Calendar)time.clone();
		now.setTime(date);
		return ((time.get(Calendar.HOUR) < now.get(Calendar.HOUR)) || ((time.get(Calendar.HOUR) == now.get(Calendar.HOUR)) && (time.get(Calendar.MINUTE) < now.get(Calendar.MINUTE))));
	}
	
	private static int boolToInt(boolean b) {
		return b ? Values.TRUE : Values.FALSE;
	}
	
	public void displayRemainigTime(Context context) {
		DateTime now = new DateTime(new Date());
		DateTime alarmDate = new DateTime(getNextActivation());
		Period period = new Period(now, alarmDate);
		int years = period.getYears();
		int months = period.getMonths();
		int days = period.getDays();
		int hours = period.getHours();
		int minutes = period.getMinutes();
		int seconds = period.getSeconds();
		String text = context.getResources().getString(R.string.alarmIn);
		if (years > 0) {
			text += " " + years + " ";
			text += (years>1) ? context.getResources().getString(R.string.years) : context.getResources().getString(R.string.year); 
		}
		if (months > 0) {
			text += " " + months + " ";
			text += (months>1) ? context.getResources().getString(R.string.months) : context.getResources().getString(R.string.month); 
		}
		if (days > 0) {
			text += " " + days + " ";
			text += (days>1) ? context.getResources().getString(R.string.days) : context.getResources().getString(R.string.day); 
		}
		if (hours > 0) {
			text += " " + hours + " ";
			text += (hours>1) ? context.getResources().getString(R.string.hours) : context.getResources().getString(R.string.hour); 
		}
		if (minutes > 0) {
			text += " " + minutes + " ";
			text += (minutes>1) ? context.getResources().getString(R.string.minutes) : context.getResources().getString(R.string.minute); 
		}
		if (seconds > 0) {
			text += " " + seconds + " ";
			text += (seconds>1) ? context.getResources().getString(R.string.seconds) : context.getResources().getString(R.string.second); 
		}
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}
}
