package application;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * This class implements all necessary connections and SQL queries to the mySQL
 * database management system with our data set tables: diary, song, album, and
 * artist.
 * 
 * @author Fangying Zhan
 *
 */
public class SQL_Connect {

	static final String databasePrefix = "music_diary";
	static final String netID = "root";
	static final String hostName = "localhost:3306";
//	 static final String databaseURL ="jdbc:mysql://"+hostName+"/"+databasePrefix+"?autoReconnect=true&useSSL=false";
	static final String password = "123456"; // please enter your own password

	static final String databaseURL = "jdbc:mysql://" + hostName + "/" + databasePrefix
			+ "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

	private Connection connection = null;
	private Statement statement = null;
	private ResultSet resultSet = null;

	// Added saved list
	private ArrayList<Diary> recentDiarylv;
	private ArrayList<Diary> searchDiarylv;
	private Diary newDiaryEntry;
	private ArrayList<Long> searchSongsid;
	private ArrayList<Long> recommendid;
	private ArrayList<Long> songidInAlbum;
	private ArrayList<Long> albumsidOfArtist;
	private ArrayList<Long> songsidOfArtist;

	public void Connection() {

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("databaseURL" + databaseURL);
			connection = DriverManager.getConnection(databaseURL, netID, password);
			System.out.println("Successfully connected to the database");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	} // end of Connection

	public void createNewDiary(Long songid, Date nowDate) throws SQLIntegrityConstraintViolationException {
		String sqlQuery = "insert into diary values (?, ?, ?); ";
//		String content = "<new>\n";
		// song-artist as initial title and content
		String songAndArtist = getSongName(songid) + " - " + getArtistName(songid);
		String content = songAndArtist + "\n";

		try {
			PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
			preparedStmt.setLong(1, songid);
			preparedStmt.setDate(2, nowDate);
			preparedStmt.setString(3, content);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			// save to newDiaryEntry
			newDiaryEntry = new Diary(songid, nowDate, content);

		} catch (SQLIntegrityConstraintViolationException e1) {
			throw e1;
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
	}

	public void deleteEmptyDiary(int index, int dnum) {
		String sqlQuery = "delete from diary where song_id = ? and date = ?;";
		Long songid = Integer.toUnsignedLong(1);// default
		Date date = Date.valueOf("2000-01-01");// default

		switch (dnum) {
		case 1:// index of recent list
			songid = recentDiarylv.get(index).songid;
			date = recentDiarylv.get(index).date;
			break;
		case 2: // index of search list
			songid = searchDiarylv.get(index).songid;
			date = searchDiarylv.get(index).date;
			break;
		case 3: // new diary entry
			songid = newDiaryEntry.songid;
			date = newDiaryEntry.date;
			break;
		}

		try {
			PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
			preparedStmt.setLong(1, songid);
			preparedStmt.setDate(2, date);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setDiaryNewEntryContent(Date newDate, String content) throws SQLIntegrityConstraintViolationException {
		// get ready for SQL query
		Long songid = newDiaryEntry.songid;
		Date originalDate = newDiaryEntry.date;
		String sqlQuery = "update diary set content = ?, date = ? where song_id = ? and date= ?;";

		try {
			PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
			preparedStmt.setString(1, content);
			preparedStmt.setDate(2, newDate);
			preparedStmt.setLong(3, songid);
			preparedStmt.setDate(4, originalDate);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			// update
			newDiaryEntry.date = newDate;
			newDiaryEntry.content = content;

		} catch (SQLIntegrityConstraintViolationException e1) {
			throw e1;
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		// then: update new recentDiaryvl
	}

	public void setDiaryRecentContent(int index, Date newDate, String content)
			throws SQLIntegrityConstraintViolationException {
		// get ready for SQL query
		Long songid = recentDiarylv.get(index).songid;
		Date originalDate = recentDiarylv.get(index).date;
		String sqlQuery = "update diary set content = ?, date = ? where song_id = ? and date= ?;";

		try {
			PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
			preparedStmt.setString(1, content);
			preparedStmt.setDate(2, newDate);
			preparedStmt.setLong(3, songid);
			preparedStmt.setDate(4, originalDate);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

		} catch (SQLIntegrityConstraintViolationException e1) {
			throw e1;
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		// then: update new recentDiaryvl
	}

	public void setDiarySearchContent(int index, Date newDate, String content)
			throws SQLIntegrityConstraintViolationException {
		// get ready for SQL query
		Long songid = searchDiarylv.get(index).songid;
		Date originalDate = searchDiarylv.get(index).date;
		String sqlQuery = "update diary set content = ?, date = ? where song_id = ? and date= ?;";

		try {
			PreparedStatement preparedStmt = connection.prepareStatement(sqlQuery);
			preparedStmt.setString(1, content);
			preparedStmt.setDate(2, newDate);
			preparedStmt.setLong(3, songid);
			preparedStmt.setDate(4, originalDate);

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

		} catch (SQLIntegrityConstraintViolationException e1) {
			throw e1;
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		// then: update new recentDiaryvl
	}

	public String getDiaryRecentContent(int index) {
		return recentDiarylv.get(index).content;
	}

	public String getDiarySearchContent(int index) {
		return searchDiarylv.get(index).content;
	}

	// every time called, update recentDiarylv saved for here
	public ArrayList<String> recentDiarylv() {
		ArrayList<String> diary = new ArrayList<>();
		recentDiarylv = new ArrayList<Diary>();

		try {
			String sqlQuery = "select song_id, date, content from diary order by date desc;";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				Long songid = (Long) resultSet.getObject(1);
				Date date = (Date) resultSet.getObject(2);
				String content = (String) resultSet.getObject(3);
				String title = content.split("\n")[0];

				// update
				recentDiarylv.add(new Diary(songid, date, content));

				diary.add(title + " (" + date + ")");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return diary;
	}

	public ArrayList<String> searchDiarylv(String searchKey) {
		ArrayList<String> diary = new ArrayList<>();
		searchDiarylv = new ArrayList<Diary>();

		try {
			String sqlQuery = "select d.song_id, d.date, d.content from song s, "
					+ "diary d where s.song_id = d.song_id and (s.song_name like '%" + searchKey
					+ "%' or d.content like '%" + searchKey + "%');";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				Long songid = (Long) resultSet.getObject(1);
				Date date = (Date) resultSet.getObject(2);
				String content = (String) resultSet.getObject(3);
				String title = content.split("\n")[0];

				// update
				searchDiarylv.add(new Diary(songid, date, content));

				diary.add(title + " (" + date + ")");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return diary;
	}

	public ArrayList<Song> searchSongResults(String song, String album, String artist) {
		searchSongsid = new ArrayList<Long>();
		ArrayList<Song> songs = new ArrayList<Song>();

		try {
			String sqlQuery = "select s.song_name, al.album_name, al.prod_name, s.song_id from song s, album al "
					+ "where s.album_id = al.album_id and s.song_name like '%" + song + "%' and al.album_name like '"
					+ album + "%' and al.prod_name like '" + artist + "%';";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				String s = (String) resultSet.getObject(1);
				String al = (String) resultSet.getObject(2);
				String ar = (String) resultSet.getObject(3);
				Long songid = (Long) resultSet.getObject(4);

				// update
				searchSongsid.add(songid);
				songs.add(new Song(s, al, ar, songid));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

//		System.out.println(searchSongsid);

		return songs;
	}

	public ArrayList<Song> getRecommend5Songs() {

		ArrayList<Song> songs = new ArrayList<>();
		recommendid = new ArrayList<>();

		// fix a genre for recommendation
		String genre = "";
		if (!recentDiarylv.isEmpty()) {
			Long songid = recentDiarylv.get(0).songid;
			genre = getSongGenre(songid);
		}

		try {
			String sqlQuery = "select s.song_name, al.album_name, al.prod_name, s.song_id from song s, album al "
					+ "where s.album_id = al.album_id and album_genre like '%" + genre + "%' "
					+ "order by rand() limit 5;";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				String s = (String) resultSet.getObject(1);
				String al = (String) resultSet.getObject(2);
				String ar = (String) resultSet.getObject(3);
				Long songid = (Long) resultSet.getObject(4);

				// add to return list and recommendid list
				songs.add(new Song(s, al, ar, songid));
				recommendid.add(songid);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return songs;

	}

	public String getSongGenre(Long songid) {
		String genre = ""; // if has multiple genre, parse and only return the first one
		try {
			String sqlQuery = "select al.album_genre from song s, album al where s.album_id = al.album_id and song_id = "
					+ songid + ";";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				genre = (String) resultSet.getObject(1);
				genre = genre.split(",")[0].trim();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genre;
	}

	public Long getAlbumid(Long songid) {
		Long albumid = Integer.toUnsignedLong(1);// Default
		try {
			String sqlQuery = "select album_id from song where song_id = " + songid + ";";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				albumid = (Long) resultSet.getObject(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return albumid;
	}

	public String getSongName(Long songid) {
		String songName = "<song>";
		try {
			String sqlQuery = "select song_name from song where song_id = " + songid + ";";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				songName = (String) resultSet.getObject(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return songName;
	}

	public String getArtistName(Long songid) {
		String artistName = "";// Default
		try {
			String sqlQuery = "select prod_name from album al, song s "
					+ "where s.album_id = al.album_id and song_id = " + songid + ";";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				artistName = (String) resultSet.getObject(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return artistName;
	}

	public Long getPageSongid(int index, int num) {
		Long songid = Integer.toUnsignedLong(1);// Default

		switch (num) {
		case 1:// search key song id on search list
			songid = searchSongsid.get(index);
			break;
		case 2:// on recommendid list
			songid = recommendid.get(index);
			break;
		case 3: // on songsid in album list
			songid = songidInAlbum.get(index);
			break;
		case 4: // on songsid of an artist
			songid = songsidOfArtist.get(index);
		}
		return songid;
	}

	public String getSongAndArtistForDiary(int index, int dnum) {
		String sa = "<song> - <artist>";
		String s = "<song>";
		String a = "<artist>";
		Long songid = Integer.toUnsignedLong(1);// default
		switch (dnum) {
		case 1:// index of recent list
			songid = recentDiarylv.get(index).songid;
			break;
		case 2: // index of search list
			songid = searchDiarylv.get(index).songid;
			break;
		case 3: // new diary entry
			songid = newDiaryEntry.songid;
			break;
		}
		s = getSongName(songid);
		a = getArtistName(songid);
		// combine to a string ready to display in a label
		sa = s + " - " + a;

		return sa;
	}

	public ArrayList<String> getAllAlbumsOfArtist(Long songid) {

		// get artist name of this song
		String artistName = getArtistName(songid);
		// get all albums of this artist
		ArrayList<String> albums = new ArrayList<>();
		albumsidOfArtist = new ArrayList<>();

		try {
			String sqlQuery = "select album_name, album_id from album where prod_name like '" + artistName + "';";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				String al = (String) resultSet.getObject(1);
				Long alid = (Long) resultSet.getObject(2);

				// add to return album names and albumid in albumsidOfArtist list
				albums.add(al);
				albumsidOfArtist.add(alid);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return albums;
	}

	public ArrayList<String> getAllSongsOfArtist(Long songid) {

		// get artist name of this song
		String artistName = getArtistName(songid);
		// get all albums of this artist
		ArrayList<String> songs = new ArrayList<>();
		songsidOfArtist = new ArrayList<>();

		try {
			String sqlQuery = "select song_name, song_id from song s, "
					+ "album al where s.album_id = al.album_id and prod_name like '" + artistName + "';";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				String s = (String) resultSet.getObject(1);
				Long sid = (Long) resultSet.getObject(2);

				// add to return song names and songid in songsidOfArtist list
				songs.add(s);
				songsidOfArtist.add(sid);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return songs;
	}

	public ArrayList<String> getSongsInAlbum(Long albumid) {

		ArrayList<String> songs = new ArrayList<>();
		songidInAlbum = new ArrayList<>();
//		Long albumid = getAlbumid(songid);

		try {
			String sqlQuery = "select song_name, song_id from song where album_id = " + albumid + ";";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				String s = (String) resultSet.getObject(1);
				Long sid = (Long) resultSet.getObject(2);

				// add to return song names and songsid in album list
				songs.add(s);
				songidInAlbum.add(sid);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return songs;
	}

	public ArrayList<String> getArtistInfo(Long songid) {

		ArrayList<String> artistInfo = new ArrayList<>();

		try {
			String sqlQuery = "select artist_name, artist_genre " + "from artist ar, song s, album al where "
					+ "s.album_id = al.album_id and al.prod_name = ar.artist_name and s.song_id = " + songid + ";";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				String ar = (String) resultSet.getObject(1);
				String g = (String) resultSet.getObject(2);

				// to return
				artistInfo.add(ar);
				artistInfo.add(g);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return artistInfo;
	}

	public Long getPageAlbumid(int index, int snum, int anum) {
		Long albumid = Integer.toUnsignedLong(1);// default
		switch (anum) {
		case 1:// from a song page
			Long songid = getPageSongid(index, snum);
			albumid = getAlbumid(songid);
			break;
		case 2: // from albumsOfArtist list
			albumid = albumsidOfArtist.get(index);
			break;
		}
		return albumid;
	}

	public ArrayList<String> getAlbumInfo(Long albumid) {

		ArrayList<String> albumInfo = new ArrayList<>();
//		Long albumid = getPageAlbumid(index, snum, anum);

		try {
			String sqlQuery = "select album_name, prod_name, release_date, album_genre, copyright, label "
					+ "from album where album_id = " + albumid + ";";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				String al = (String) resultSet.getObject(1);
				String ar = (String) resultSet.getObject(2);
				String r = (String) resultSet.getObject(3);
				String g = (String) resultSet.getObject(4);
				String cp = (String) resultSet.getObject(5);
				String l = (String) resultSet.getObject(6);

				// to return
				albumInfo.add(al);
				albumInfo.add(ar);
				albumInfo.add(r);
				albumInfo.add(g);
				albumInfo.add(cp);
				albumInfo.add(l);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return albumInfo;
	}

	public ArrayList<String> getSongInfo(Long songid) {

		ArrayList<String> songInfo = new ArrayList<>();
//		Long songid = getPageSongid(index, num);

		try {
			String sqlQuery = "select s.song_name, s.time, s.price, al.album_name, al.prod_name, al.release_date, al.album_genre "
					+ "from song s, album al where s.album_id = al.album_id and s.song_id = " + songid + ";";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlQuery);

			while (resultSet.next()) {
				String s = (String) resultSet.getObject(1);
				String t = (String) resultSet.getObject(2);
				String p = (String) resultSet.getObject(3);
				String al = (String) resultSet.getObject(4);
				String ar = (String) resultSet.getObject(5);
				String r = (String) resultSet.getObject(6);
				String g = (String) resultSet.getObject(7);

				// to return
				songInfo.add(s);
				songInfo.add(t);
				songInfo.add(p);
				songInfo.add(al);
				songInfo.add(ar);
				songInfo.add(r);
				songInfo.add(g);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return songInfo;
	}

//	public static void main(String args[]) {

	// connect
//		SQL_Connect demoObj = new SQL_Connect();
//		demoObj.Connection();

	// simple store
//		String spName = "getTotalStudent";
//		demoObj.simpleStoreProcedure(spName);

	// simple query
//		String sqlQuery = "select sname from student where level = 'JR';";
//		demoObj.simpleQuery(sqlQuery);
//	}

}
