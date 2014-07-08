package fr.robincarozzani.wakeUpCall.objects;

import java.io.File;
import java.util.HashMap;

import fr.robincarozzani.wakeUpCall.R;
import fr.robincarozzani.wakeUpCall.constants.Keys;
import android.content.Context;
import android.media.MediaMetadataRetriever;

public class Utils {

	public static HashMap<String, String> getMusicFileInfos(Context c, File musicFile) {
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(musicFile.getAbsolutePath());
		String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
		if (title == null) title = musicFile.getName();
		String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
		if (artist == null) {
			artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
			if (artist == null) artist = c.getResources().getString(R.string.unknownArtist);
		}
		mmr.release();
		HashMap<String, String> content = new HashMap<String, String>();
		content.put(Keys.TITLE, title);
		content.put(Keys.ARTIST, artist);
		return content;
	}
	
	public static String stringDate(int year, int month, int day, int hour, int minute) {
		String sYear = ""+year;
		String sMonth = (month<10) ? "0"+month : ""+month;
		String sDay = (day<10) ? "0"+day : ""+day;
		String sHour = (hour<10) ? "0"+hour : ""+hour;
		String sMinute = (minute<10) ? "0"+minute : ""+minute;
		String sDate = sYear + "-" + sMonth + "-" + sDay + " " + sHour + ":" + sMinute + ":00";
		return sDate;
	}
}
