package fr.robincarozzani.wakeUpCall.objects.database;

import android.content.Context;
import android.database.Cursor;

public class Database {
	
	private static DataBaseAdapter db = null;
	
	public static void create(Context context) {
		if ((db == null) || (db.isClosed())) {
			db = new DataBaseAdapter(context);
			db.createDatabase();
			db.open();
		}
	}
	
	public static int getNewId(String tableName) {
		Cursor count = executeWithResult("SELECT MAX(id) FROM " + tableName);
		int id = count.getInt(0) + 1;
		return id;
	}
	
	public static Cursor executeWithResult(String query) {
		return db.executeWithResult(query);
	}
	
	public static void executeNoResult(String query) {
		db.executeNoResult(query);
	}
	
	public static void destroy() {
		db.close();
	}

}
