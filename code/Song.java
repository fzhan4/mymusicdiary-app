package application;

import javafx.beans.property.SimpleStringProperty;

/**
 * This class implements a song entity, which includes three main attributes
 * that are needed to display in the song search page: song name, album name and
 * artist name.
 * 
 * @author Fangying Zhan
 *
 */
public class Song {
	// NOTE: fields and methods are necessary for table view
	// DO NOT MODIFY

	SimpleStringProperty song;
	SimpleStringProperty album;
	SimpleStringProperty artist;
	Long songid;

	Song(String song, String album, String artist, Long songid) {
		this.song = new SimpleStringProperty(song);
		this.album = new SimpleStringProperty(album);
		this.artist = new SimpleStringProperty(artist);
		this.songid = songid;
	}

	public String getSong() {
		return song.get();
	}

	public void setSong(String ssong) {
		song.set(ssong);
	}

	public String getAlbum() {
		return album.get();
	}

	public void setAlbum(String salbum) {
		album.set(salbum);
	}

	public String getArtist() {
		return artist.get();
	}

	public void setArtist(String sartist) {
		artist.set(sartist);
	}

}
