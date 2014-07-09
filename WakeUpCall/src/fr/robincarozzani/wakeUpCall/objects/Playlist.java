package fr.robincarozzani.wakeUpCall.objects;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Values;
import fr.robincarozzani.wakeUpCall.constants.db.AlarmDB;
import fr.robincarozzani.wakeUpCall.constants.db.PContainsDB;
import fr.robincarozzani.wakeUpCall.constants.db.PlaylistDB;
import fr.robincarozzani.wakeUpCall.objects.database.Database;

public class Playlist {

	private int _id;
	
	public static Playlist createPlaylistInDB(String playlistName, ArrayList<Song> songs) {
		int id = Database.getNewId("Playlist");
		Database.executeNoResult("INSERT INTO " + PlaylistDB.TABLENAME + " VALUES(" + id + ", \"" + playlistName + "\")");
		Playlist p = new Playlist(id);
		p.saveSongs(songs);
		return p;
	}
	
	public static ArrayList<Playlist> getListPlaylists(String conditionFieldName, int conditionValue) {
		String condition = "";
		if (conditionValue != Values.IGNORE) {
			condition += " WHERE " + conditionFieldName + " = " + conditionValue;
		}
		ArrayList<Playlist> playlists = new ArrayList<Playlist>();
		Cursor c = Database.executeWithResult("SELECT " + PlaylistDB.ID + " FROM " + PlaylistDB.TABLENAME + condition);
		if (c.moveToFirst()) {
			do {
				playlists.add(new Playlist(c.getInt(0)));
			} while (c.moveToNext());
		}
		return playlists;
	}
	
	public Playlist(int id) {
		_id = id;
	}
	
	public int getId() {
		return _id;
	}
	
	public String getName() {
		Cursor c = Database.executeWithResult("SELECT " + PlaylistDB.NAME + " FROM " + PlaylistDB.TABLENAME + " WHERE " + PlaylistDB.ID + " = " + getId());
		return c.getString(0);
	}
	
	public ArrayList<String> getSongsPaths() {
		ArrayList<String> songsPaths = new ArrayList<String>();
		Cursor c = Database.executeWithResult("SELECT " + PContainsDB.SONGPATH + " FROM " + PContainsDB.TABLENAME + " WHERE " + PContainsDB.PLAYLISTID + " = " + getId() + " ORDER BY " + PContainsDB.POSITION);
		if (c.moveToFirst()) {
			do {
				songsPaths.add(c.getString(0));
			} while (c.moveToNext());
		}
		return songsPaths;
	}
	
	public int getNbSongs() {
		return getSongsPaths().size();
	}
	
	private int getDurationSeconds() {
		int duration = 0;
		MediaPlayer mp = new MediaPlayer();
		try {
			for (String path : getSongsPaths()) {
				mp.setDataSource(path);
				mp.prepare();
				duration += mp.getDuration()/1000;
				mp.reset();
			}
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException | IOException e) {
			e.printStackTrace();
		} finally {
			mp.release();
		}
		return duration;
	}
	
	public String getDurationString(Context c) {
		int seconds = getDurationSeconds();
		int hours = seconds/3600;
		seconds -= 3600*hours;
		int minutes = seconds/60;
		seconds -= 60*minutes;
		String sHours = "", sMinutes = "", sSeconds = "";
		if (hours > 0) {
			sHours = (hours>1) ? hours+" "+c.getResources().getString(R.string.hours) : hours+" "+c.getResources().getString(R.string.hour);
		}
		if (minutes > 0) {
			sMinutes = (minutes>1) ? minutes+" "+c.getResources().getString(R.string.minutes) : hours+" "+c.getResources().getString(R.string.minute);
		}
		if (seconds > 0) {
			sSeconds = (seconds>1) ? seconds+" "+c.getResources().getString(R.string.seconds) : hours+" "+c.getResources().getString(R.string.second);
		}
		return (sHours + " " + sMinutes + " " + sSeconds);
	}
	
	public void updateInDB(String playlistName, ArrayList<Song> songs) {
		Database.executeNoResult("UPDATE " + PlaylistDB.TABLENAME + " SET " + PlaylistDB.NAME + " = \"" + playlistName + "\" WHERE " + PlaylistDB.ID + " = " + getId());
		Database.executeNoResult("DELETE FROM " + PContainsDB.TABLENAME + " WHERE " + PContainsDB.PLAYLISTID + " = " + getId());
		saveSongs(songs);
	}
	
	private void saveSongs(ArrayList<Song> songs) {
		int id = getId();
		int i = 0;
		for (Song s : songs) {
			Database.executeNoResult("INSERT INTO " + PContainsDB.TABLENAME + " VALUES(" + id + ", \"" + s.getPath() + "\", " + (i++) + ")");
		}
	}
	
	public ArrayList<Alarm> getAlarmsUsing() {
		ArrayList<Alarm> alarms = new ArrayList<Alarm>();
		Cursor c = Database.executeWithResult("SELECT " + AlarmDB.ID + " FROM " + AlarmDB.TABLENAME + " WHERE " + AlarmDB.PLAYLISTID + " = " + getId());
		if (c.moveToFirst()) {
			do {
				alarms.add(new Alarm(c.getInt(0)));
			} while (c.moveToNext());
		}
		return alarms;
	}
	
	public void deleteFromDB() {
		Database.executeNoResult("DELETE FROM " + PContainsDB.TABLENAME + " WHERE " + PContainsDB.PLAYLISTID + " = " + getId());
		Database.executeNoResult("DELETE FROM " + PlaylistDB.TABLENAME + " WHERE " + PlaylistDB.ID + " = " + getId());
	}
}
