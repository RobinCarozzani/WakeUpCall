package fr.robincarozzani.wakeUpCall.objects;

import java.io.Serializable;

public class Song implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String _path;
	private String _name;
	private String _artist;
	
	public Song(String path, String name, String artist) {
		_path = path;
		_name = name;
		_artist = artist;
	}

	public String getPath() {
		return _path;
	}

	public String getName() {
		return _name;
	}
	
	public String getArtist() {
		return _artist;
	}
}
