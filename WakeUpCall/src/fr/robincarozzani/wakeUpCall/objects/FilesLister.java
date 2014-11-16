package fr.robincarozzani.wakeUpCall.objects;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Locale;

import fr.robincarozzani.wakeUpCall.view.listAdapters.playlist.FileListAdapter;

public class FilesLister {
	
	private final static String STARTPATH = "/storage";
	
	private File f;
	
	public FilesLister() {
		f = new File(STARTPATH);
	}
	
	public void listFilesInAdapter(FileListAdapter adapter) {
		adapter.clear();
		for (File f : getDirectories()) {
			adapter.add(f);
		}
		for (File f : getMusicFiles()) {
			adapter.add(f);
		}
	}
	
	private File[] getDirectories() {
		File[] res =  f.listFiles(new FileFilter() {	
			@Override
			public boolean accept(File pathname) {
				return (pathname.isDirectory() && pathname.canRead());
			}
		});
		Arrays.sort(res);
		return res;
	}
	
	public File[] getMusicFiles() {
		File[] res = f.listFiles(new FileFilter() {	
			@Override
			public boolean accept(File pathname) {
				if (pathname.isFile()) {
					return (pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".3gp") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".mp4") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".m4a") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".aac") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".ts") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".flac") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".mp3") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".mid") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".xmf") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".mxmf") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".rtttl") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".rtx") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".ota") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".imy") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".ogg") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".mkv") ||
							pathname.getName().toLowerCase(Locale.getDefault()).endsWith(".wav"));
							
				}
				return false;
			}
		});
		Arrays.sort(res);
		return res;
	}
	
	public boolean goToParent() {
		if (f.getParent() != null) {
			String parent = f.getParent().toString();
			f = new File(parent);
			return true;
		}
		return false;
	}
	
	public boolean goTo(File subFolder) {
		if (subFolder.isDirectory()) {
			f = subFolder;
			return true;
		}
		return false;
	}
	
	public String getCurrentPath() {
		return f.getAbsolutePath();
	}
	
	public int getNumberOfMusicFiles() {
		return getMusicFiles().length;
	}
	
}
