package fr.robincarozzani.wakeUpCall.objects;

import java.util.ArrayList;

import android.database.Cursor;
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
