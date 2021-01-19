package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Date;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

/**
 * This class implements the user interface of My Music Diary application.
 * 
 * The application requires login to get to the main page.
 * 
 * On the left is the diary section, in which the user may browse, search,
 * modify or delete any diary entries, which are organized in a latest-date
 * order.
 * 
 * On the right is the music section, in which the user may search for songs to
 * write a diary associated with, or may search for information about any song,
 * artist, and album by inputting key words in the corresponding search box.
 * 
 * There is also a recommendation section, in which the application randomly
 * recommend some songs in the same genre that the user has recently written a
 * diary for. The user may also browse and write a diary for any of them.
 * 
 * Within this minimalism-style application, enjoy writing a diary while
 * listening to some music!
 * 
 * @author Fangying Zhan
 *
 */
public class MD_Login extends Application {

	// Fields
	private static SQL_Connect sql;
	private static Stage window;
	private static Stage diarySearchWindow;
	private static Stage songSearchWindow;
	private static Stage songInfoWindow;
	private static Stage albumInfoWindow;
	private static Stage artistInfoWindow;
	private static Scene loginScene, mainScene;
	private static TableView<Song> table;
	private static ObservableList<String> diary;
	private static ListView<String> diarylv;
	private static ArrayList<Song> recommSongs;
	private static boolean isSaved = false;
	private static final String username = "Bucky Badger";
	private static final String password = "123456";
	private static final int WINDOW_WIDTH = 800;
	private static final int WINDOW_HEIGHT = 400;
	private static String diaryText = "<empty>";
	private static LocalDate diaryDate = LocalDate.now();
	private static final String DIARY_PAGE = "My Music Diary - Diary Page";
	private static final String DIARY_PAGE_EDIT = "My Music Diary - Diary Page (Edit)";
	private static final String DIARY_PAGE_SAVED = "My Music Diary - Diary Page (Saved)";
	private static final String SEARCH_PAGE = "My Music Diary - Search";
	private static final String SEARCH_DIARY = "My Music Diary - Search Diary";
	private static final String SONG_INFO = "My Music Diary - Song Info";
	private static final String ALBUM_INFO = "My Music Diary - Album Info";
	private static final String ARTIST_INFO = "My Music Diary - Artist Info";
	private static final String LOGIN = "My Music Diary - Login";
	private static final String MAIN = "My Music Diary - Welcome back " + username + "!";

	@Override
	public void start(Stage PrimaryStage) {
		window = PrimaryStage;
		loginSetup();
	}

	public static void mainPageSetup() {
		if (window != null && window.isShowing())
			window.close();
		window = new Stage();
		window.setTitle(MAIN);

		// left
		VBox left = new VBox();
		left.setPrefHeight(400);
		left.setPrefWidth(300);
		leftPaneSetup(window, left);

		// right
		VBox right = new VBox();
		right.setPrefHeight(400);
		right.setPrefWidth(300);
		rightUpPaneSetup(window, right);
		rightDownPaneSetup(window, right);

		GridPane root = new GridPane();
		// root.setLeft(left);
		// root.setright(right);
		root.setAlignment(Pos.CENTER);
		root.add(left, 0, 0);
		root.add(right, 1, 0);
		root.setStyle("-fx-background-color: linen;");

		// create a scene object
		mainScene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
		// show in main window
		window.setScene(mainScene);
		window.show();
	}

	/**
	 * @param title
	 * @param index
	 */
	public static void queryAndEditDiary(int index, int dnum) {
//		LocalDate diaryDate = LocalDate.now();

		// SQL get song and artist for label to display and initial title
		String songAndArtist = sql.getSongAndArtistForDiary(index, dnum);
		// to get content from diary, initialized title
		String content = songAndArtist + "\n";

		switch (dnum) {
		case 1:// index of recent list
			content = sql.getDiaryRecentContent(index);
			break;
		case 2: // index of search list
			content = sql.getDiarySearchContent(index);
			break;
		case 3: // new diary entry
			break;
		}

		Stage window = new Stage();
		window.setTitle(DIARY_PAGE);

		// date picker
		Label dateLabel = new Label("no date selected");
		DatePicker datePicker = new DatePicker();
		// set min and max date
		LocalDate minDate = LocalDate.of(2000, 01, 01);
		LocalDate maxDate = LocalDate.now();
		datePicker.setDayCellFactory(d -> new DateCell() {
			@Override
			public void updateItem(LocalDate item, boolean empty) {
				super.updateItem(item, empty);
				setDisable(item.isAfter(maxDate) || item.isBefore(minDate));
			}
		});

		datePicker.setOnAction(e -> {
			isSaved = false;

			window.setTitle(DIARY_PAGE_EDIT);
			// get the date picker value
			LocalDate d = datePicker.getValue();
			// get the selected date
			dateLabel.setText("Date :" + d);
		});

		// show week numbers
		datePicker.setShowWeekNumbers(true);

		// display diary content
		TextArea textArea = new TextArea(content);
		textArea.setPrefHeight(400);

		textArea.setOnKeyTyped(e -> {
			window.setTitle(DIARY_PAGE_EDIT);
			isSaved = false;
		});

		Button button = new Button("Save");

		button.setOnAction(e -> {
			isSaved = true;

			// update new input diary content
			diaryText = textArea.getText();

			if (datePicker.getValue() != null)
				diaryDate = datePicker.getValue();

			try {
				// SQL set diary content
				switch (dnum) {
				case 1:
					sql.setDiaryRecentContent(index, Date.valueOf(diaryDate), diaryText);
//					System.out.println("Setting recent diary index: " + index);
					break;
				case 2:
					sql.setDiarySearchContent(index, Date.valueOf(diaryDate), diaryText);
					// close the diary search window
//					System.out.println("Setting search diary index: " + index);
					diarySearchWindow.close();
					break;
				case 3:
					sql.setDiaryNewEntryContent(Date.valueOf(diaryDate), diaryText);
//					System.out.println("Setting new diary");
					break;
				}

				// SQL update recent diary list
//				updateDiaryListView(); // DO NOT UPDATE

				window.setTitle(DIARY_PAGE_SAVED);

			} catch (SQLIntegrityConstraintViolationException exception) {
				AlertBox.display("Fail", "Diary already exists.");
			}
		});

		// Label for song-artist
		Label songArtistLabel = new Label(songAndArtist);

		TilePane tilePane1 = new TilePane(datePicker, dateLabel);

		VBox vbox = new VBox(tilePane1, textArea, button, songArtistLabel);
		songArtistLabel.setAlignment(Pos.BOTTOM_RIGHT);

		// check while closing window
		window.setOnCloseRequest(e -> {
			// if is content is empty, delete diary entry
			if (isSaved && textArea.getText().trim().equals(""))
				sql.deleteEmptyDiary(index, dnum);

			// after editing and possibly deleting
			// SQL update recent diary list while closing window
			updateDiaryListView();// UPDATE

			// reset date to now
			diaryDate = LocalDate.now();
		});

		Scene scene = new Scene(vbox, 400, 400);
		window.setScene(scene);
		window.show();

	}

	private static void updateDiaryListView() {
		// SQL update recent diary list
		ArrayList<String> al = sql.recentDiarylv();
		// update in list view
		diarylv.getItems().removeAll();
		diarylv.getItems().setAll(al);
	}

	/**
	 * @param stage
	 * @param left
	 */
	/**
	 * @param stage
	 * @param left
	 */
	public static void leftPaneSetup(Stage stage, VBox left) {
		Text recentLabel = new Text("Recent diary");

		// sSQL fetch some recent diary titles (1st line appended date)
		// add titles to observable list
		ArrayList<String> al = sql.recentDiarylv();
		diary = FXCollections.observableArrayList();

		if (!al.isEmpty()) {
			diary.addAll(al);
		}

		diarylv = new ListView<String>(diary);
		diarylv.setPrefSize(300, 200);

		diarylv.setOnMouseClicked(e -> {
			// double-clicked
			if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2
					&& diarylv.getSelectionModel().getSelectedItem() != null
					&& !diarylv.getSelectionModel().getSelectedItem().isEmpty()) {

				int ind = diarylv.getSelectionModel().getSelectedIndex();

				// edit selected diary
				queryAndEditDiary(ind, 1);// case 1: recent
			}
		});

		// add search box for diary(song, date)
		TextField searchText = new TextField("search diary for <song>");
		searchText.setPrefWidth(270);

		// prompt text go transparent when typed
//		songText.styleProperty()
//				.bind(Bindings.when(songText.focusedProperty()).then("-fx-prompt-text-fill: transparent;")
//						.otherwise("-fx-prompt-text-fill: derive(-fx-control-inner-background, -30%);"));

		Button searchButton = new Button("GO");

		searchButton.setOnAction(e -> {
			String searchKey = searchText.getText().trim();

			if (!searchKey.isEmpty()) {
				// get search result by helper method
				diarySearchPage(searchKey);
				searchText.clear();
			}
		});

		GridPane gridPane = new GridPane();
		gridPane.setMinSize(500, 400);
		gridPane.setPadding(new Insets(10, 10, 10, 10));
		gridPane.setVgap(5);
		gridPane.setHgap(5);
		gridPane.setAlignment(Pos.CENTER);

		gridPane.add(recentLabel, 0, 0);
		gridPane.add(diarylv, 0, 1);
		gridPane.add(new HBox(searchText, searchButton), 0, 2);

		recentLabel.setStyle("-fx-font: normal bold 30px 'serif' ");
		searchButton.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
		gridPane.setStyle("-fx-background-color: GHOSTWHITE;");

		left.getChildren().add(gridPane);
	}

	private static void diarySearchPage(String searchKey) {
		// 7/26 update
		if (diarySearchWindow != null && diarySearchWindow.isShowing())
			diarySearchWindow.close();
		diarySearchWindow = new Stage();
		diarySearchWindow.setTitle(SEARCH_DIARY + " \"" + searchKey + "\"");

		// SQL get all diary associated with the song name
		ArrayList<String> al = sql.searchDiarylv(searchKey);
		ObservableList<String> searchDiary = FXCollections.observableArrayList();

		if (!al.isEmpty()) {
			searchDiary.addAll(al);
		}

		ListView<String> searchlv = new ListView<String>(searchDiary);
		searchlv.setPrefSize(400, 400);

		searchlv.setOnMouseClicked(e -> {
			// double-clicked
			if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2
					&& searchlv.getSelectionModel().getSelectedItem() != null
					&& !searchlv.getSelectionModel().getSelectedItem().isEmpty()) {
				int ind = searchlv.getSelectionModel().getSelectedIndex();
				// edit selected diary
				queryAndEditDiary(ind, 2);// case 2: search list diary
				// false means the diary on search page, not recent list
			}
		});

		VBox vBox = new VBox();
		vBox.getChildren().addAll(searchlv);
		vBox.setStyle("-fx-background-color: FLORALWHITE;");

		Scene scene = new Scene(vBox);

		// Styling nodes
		// scene.getStylesheets().add("lisStyles.css");

		diarySearchWindow.setScene(scene);
		diarySearchWindow.show();
	}

	public static void rightDownPaneSetup(Stage stage, VBox right) {

		Text likeLabel = new Text("You \n  may \n    like... ");

		// SQL get list of 5 recommended songs from database
		// (at most 5, may not have enough songs)
		recommSongs = sql.getRecommend5Songs();

		// set as buttons
		ArrayList<Button> buttons = new ArrayList<Button>();
		for (int i = 0; i < recommSongs.size(); ++i) {
			buttons.add(new Button(recommSongs.get(i).getSong() + " - " + recommSongs.get(i).getArtist()));
			int ind = i;// index in the 5 songs

			buttons.get(i).setOnAction(e -> {
				// display song info if clicked on
				songInfoPage(ind, 2);// case 2: from recommendation
			});
		}

		GridPane gridPane = new GridPane();
		GridPane likePane = new GridPane();

		// Setting size for the pane
		gridPane.setMinSize(200, 200);
		likePane.setMinSize(50, 200);

		// Setting the padding
		gridPane.setPadding(new Insets(10, 10, 10, 10));

		// Setting the vertical and horizontal gaps between the columns
		gridPane.setVgap(5);
		gridPane.setHgap(5);

		// Setting the Grid alignment
		gridPane.setAlignment(Pos.CENTER);
		likePane.setAlignment(Pos.CENTER);

		// Arranging all the nodes in the grid
		likePane.add(likeLabel, 0, 0);

		for (int i = 0; i < 5; ++i) {
			gridPane.add(buttons.get(i), 0, i);
			// Styling
			if (i % 2 == 0)
				buttons.get(i).setStyle("-fx-background-color: thistle; -fx-text-fill: white;");
			else
				buttons.get(i).setStyle("-fx-background-color: LAVENDER; -fx-text-fill: grey;");
		}
		HBox hb = new HBox();
		hb.getChildren().add(gridPane);
		hb.getChildren().add(likePane);
		// Styling nodes
		likeLabel.setStyle("-fx-font: normal bold 16px 'serif' ");

		hb.setStyle("-fx-background-color: FLORALWHITE;");

		right.getChildren().add(hb);
	}

	@SuppressWarnings("unchecked")
	public static void songSearchPage(String song, String album, String artist) {
		if (songSearchWindow != null && songSearchWindow.isShowing())
			songSearchWindow.close();
		songSearchWindow = new Stage();
		String userInput = song + " " + album + " " + artist;
		songSearchWindow.setTitle(SEARCH_PAGE + " - \"" + userInput.trim() + "\"");

		// set up table for result page
		// Song column
		TableColumn<Song, String> songColumn = new TableColumn<>("Song");
		songColumn.setMinWidth(200);
		songColumn.setCellValueFactory(new PropertyValueFactory<>("song"));

		// Album column
		TableColumn<Song, String> albumColumn = new TableColumn<>("Album");
		albumColumn.setMinWidth(100);
		albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));

		// Artist column
		TableColumn<Song, String> artistColumn = new TableColumn<>("Artist");
		artistColumn.setMinWidth(100);
		artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));

		table = new TableView<>();

		// SQL get search results and add to table view
		ArrayList<Song> songlist = sql.searchSongResults(song, album, artist);
		ObservableList<Song> songs = FXCollections.observableArrayList();
		songs.addAll(songlist);
		table.setItems(songs);

		table.getColumns().addAll(songColumn, albumColumn, artistColumn);

		table.setOnMouseClicked(e -> {
			// double-clicked
			if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2
					&& table.getSelectionModel().getSelectedItem() != null) {
				int ind = table.getSelectionModel().getSelectedIndex();

				// fetch selected song information
				songInfoPage(ind, 1);// case 1: from song search table
			}
		});

		VBox vBox = new VBox();
		vBox.getChildren().addAll(table);

		Scene scene = new Scene(vBox);

		// Styling nodes
		// scene.getStylesheets().add("lisStyles.css");

		// gridPane.setStyle("-fx-background-color: FLORALWHITE;");

		songSearchWindow.setScene(scene);
		songSearchWindow.show();
	}

	private static void songInfoPage(int index, int num) {
		if (songInfoWindow != null && songInfoWindow.isShowing())
			songInfoWindow.close();
		songInfoWindow = new Stage();
		songInfoWindow.setTitle(SONG_INFO);

		ArrayList<String> songInfo = new ArrayList<>();

		// SQL query to song information
		Long songid = sql.getPageSongid(index, num);
		songInfo = sql.getSongInfo(songid);
//		case 1: from song search table
//		case 2: from recommendation (5 songs)
//		case 3: from songs in an album
//		case 4: from songs of an artist

		Text songName, time, price, album, artist, release, genre;

		if (!songInfo.isEmpty() && songInfo.size() == 7) {
			songName = new Text(songInfo.get(0));
			time = new Text("Time: " + songInfo.get(1));
			price = new Text("Price: " + songInfo.get(2));
			album = new Text("Album: " + songInfo.get(3));
			artist = new Text("Artist: " + songInfo.get(4));
			release = new Text("Release date: " + songInfo.get(5));
			genre = new Text("Genre : " + songInfo.get(6).replaceAll(",", ", "));
			// genre modified for better display
		} else {
			songName = new Text("<song name>");
			time = new Text("Time: " + "<?:??>");
			price = new Text("Price: " + "$");
			album = new Text("Album: " + "<album>");
			artist = new Text("Artist: " + "<artist>");
			release = new Text("Release date: " + "<date>");
			genre = new Text("Genre : " + "<genre>");
		}

		Button writeButton = new Button("Write a diary");
		Button albumButton = new Button("+");
		Button artistButton = new Button("+");

		writeButton.setOnAction(e -> {
			writeNewDiary(songid);
		});

		albumButton.setOnAction(e -> {
			albumInfoPage(index, num, 1);// case 1: from a song page
		});

		artistButton.setOnAction(e -> {
			artistInfoPage(songid);
		});

		GridPane gridPane = new GridPane();
		gridPane.setMinSize(400, 200);
		gridPane.setPadding(new Insets(10, 10, 10, 10));
		gridPane.setVgap(5);
		gridPane.setHgap(5);
		gridPane.setAlignment(Pos.CENTER);

		// Arranging all the nodes in the grid
		gridPane.add(songName, 0, 0);
		gridPane.add(time, 0, 1);
		gridPane.add(price, 0, 2);
		gridPane.add(release, 0, 3);
		gridPane.add(new HBox(album, albumButton), 0, 4);
		gridPane.add(new HBox(artist, artistButton), 0, 5);
		gridPane.add(genre, 0, 6);

		VBox vb = new VBox(gridPane, writeButton);
		vb.setPadding(new Insets(50, 50, 50, 50));
		vb.setMinSize(400, 300);
		vb.setAlignment(Pos.CENTER);

		// Styling nodes
		writeButton.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
		albumButton.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
		artistButton.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");

		songName.setStyle("-fx-font: normal bold 30px 'serif' ");

		time.setStyle("-fx-font: normal bold 20px 'serif' ");
		price.setStyle("-fx-font: normal bold 20px 'serif' ");
		release.setStyle("-fx-font: normal bold 20px 'serif' ");
		album.setStyle("-fx-font: normal bold 20px 'serif' ");
		artist.setStyle("-fx-font: normal bold 20px 'serif' ");
		genre.setStyle("-fx-font: normal bold 20px 'serif' ");

		gridPane.setStyle("-fx-background-color: honeydew;");
		vb.setStyle("-fx-background-color: honeydew;");

		// wrapping long texts
		songName.setWrappingWidth(400);
		album.setWrappingWidth(400);
		artist.setWrappingWidth(400);
		genre.setWrappingWidth(400);

		// Creating a scene object and display
		Scene scene = new Scene(vb);
		songInfoWindow.setScene(scene);
		songInfoWindow.show();
	}

	private static void artistInfoPage(Long songid) {
		if (artistInfoWindow != null && artistInfoWindow.isShowing())
			artistInfoWindow.close();
		artistInfoWindow = new Stage();
		artistInfoWindow.setTitle(ARTIST_INFO);

		// SQL get artist info
		// NOTE: artist has a genre attribute
		ArrayList<String> artistInfo = sql.getArtistInfo(songid);

		Text artistName = new Text(artistInfo.get(0));
		Text genre = new Text("Artist genre: " + artistInfo.get(1).replaceAll(",", ", "));
		// genre modified for better display

		// 1) album list
		Label albumListLabel = new Label(" All Albums");

		// SQL get all albums of this artist
		ObservableList<String> albumsOfArtist = FXCollections.observableArrayList();
		albumsOfArtist.addAll(sql.getAllAlbumsOfArtist(songid));

		ListView<String> albumlv = new ListView<>(albumsOfArtist);
		albumlv.setPrefSize(250, 100);
		albumlv.setOnMouseClicked(e -> {
			// double-clicked
			if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2
					&& albumlv.getSelectionModel().getSelectedItem() != null
					&& !albumlv.getSelectionModel().getSelectedItem().isEmpty()) {

				int ind = albumlv.getSelectionModel().getSelectedIndex();

				// SQL get album info page by index in the albumsidOfArtist
				albumInfoPage(ind, 0, 2);// case 2: from albumsOfArtist list
			}
		});

		// 2) song list
		Label songListLabel = new Label(" All Songs");

		// SQL get all songs from albums in album list
		ObservableList<String> songsInAlbum = FXCollections.observableArrayList();
		songsInAlbum.addAll(sql.getAllSongsOfArtist(songid));

		ListView<String> songlv = new ListView<>(songsInAlbum);
		songlv.setPrefSize(250, 100);
		songlv.setOnMouseClicked(e -> {
			// double-clicked
			if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2
					&& songlv.getSelectionModel().getSelectedItem() != null
					&& !songlv.getSelectionModel().getSelectedItem().isEmpty()) {
				int ind = songlv.getSelectionModel().getSelectedIndex();

				// get song info when clicked in the songsidOfArtist
				songInfoPage(ind, 4);// case 4: from songs of an artist
			}
		});

		// top display artist info
		GridPane gridPane1 = new GridPane();
		gridPane1.setMinSize(400, 200);
		gridPane1.setPadding(new Insets(10, 10, 10, 10));
		gridPane1.setVgap(5);
		gridPane1.setHgap(5);
		gridPane1.setAlignment(Pos.CENTER);

		gridPane1.add(artistName, 0, 0);
		gridPane1.add(genre, 0, 1);

		// bottom display list views
		GridPane gridPane2 = new GridPane();
		gridPane2.setMinSize(400, 200);
		gridPane2.setPadding(new Insets(10, 10, 10, 10));
		gridPane2.setVgap(5);
		gridPane2.setHgap(5);
		gridPane2.setAlignment(Pos.CENTER);

		// Arranging all the nodes in the grid
		VBox vb1 = new VBox(songListLabel, songlv);
		VBox vb2 = new VBox(albumListLabel, albumlv);
		vb1.setAlignment(Pos.TOP_LEFT);
		vb2.setAlignment(Pos.TOP_LEFT);
		gridPane2.add(vb1, 0, 0);
		gridPane2.add(vb2, 1, 0);

		VBox vb = new VBox(gridPane1, gridPane2);
		vb.setMinSize(400, 300);
		vb.setAlignment(Pos.CENTER);

		// Styling nodes
		artistName.setStyle("-fx-font: normal bold 30px 'serif' ");
		genre.setStyle("-fx-font: normal bold 20px 'serif' ");
		songListLabel.setStyle("-fx-font: normal bold 20px 'serif' ");
		albumListLabel.setStyle("-fx-font: normal bold 20px 'serif' ");

		// wrapping long texts
		artistName.setWrappingWidth(400);
		genre.setWrappingWidth(400);

		gridPane1.setStyle("-fx-background-color: lavender;");
		gridPane2.setStyle("-fx-background-color: lavender;");
		vb.setStyle("-fx-background-color: ghostwhite;");

		// Creating a scene object and display
		Scene scene = new Scene(vb);
		artistInfoWindow.setScene(scene);
		artistInfoWindow.show();

	}

	private static void albumInfoPage(int index, int snum, int anum) {
		if (albumInfoWindow != null && albumInfoWindow.isShowing())
			albumInfoWindow.close();
		Stage albumInfoWindow = new Stage();
		albumInfoWindow.setTitle(ALBUM_INFO);

		// top part info
		// SQL to album info
		ArrayList<String> albumInfo;
		Long albumid = sql.getPageAlbumid(index, snum, anum);
		albumInfo = sql.getAlbumInfo(albumid);
//		case 1: from a song page
//		case 2: from albumsOfArtist list

		Text albumName = new Text(albumInfo.get(0));
		Text prod = new Text("Producer: " + albumInfo.get(1));
		Text release = new Text("Release date: " + albumInfo.get(2));
		Text genre = new Text("Genre: " + albumInfo.get(3).replaceAll(",", ", "));
		// genre modified for better display
		Text copyright = new Text("Copy right: " + albumInfo.get(4));
		Text companylabel = new Text("Label: " + albumInfo.get(5));

		GridPane gridPane1 = new GridPane();
		gridPane1.setMinSize(400, 300);
		gridPane1.setPadding(new Insets(10, 10, 10, 10));
		gridPane1.setVgap(5);
		gridPane1.setHgap(5);
		gridPane1.setAlignment(Pos.CENTER);

		// Arranging all the nodes in the grid
		gridPane1.add(albumName, 0, 0);
		gridPane1.add(prod, 0, 1);
		gridPane1.add(release, 0, 2);
		gridPane1.add(genre, 0, 3);
		gridPane1.add(copyright, 0, 4);
		gridPane1.add(companylabel, 0, 5);

		// bottom part album track list
		Label trackListLabel = new Label(" Track List");
		trackListLabel.setAlignment(Pos.TOP_LEFT);
		// SQL get song list in this album
		ArrayList<String> songs = sql.getSongsInAlbum(albumid);
		ObservableList<String> songsInAlbum = FXCollections.observableArrayList();
		songsInAlbum.addAll(songs);

		ListView<String> songlv = new ListView<>(songsInAlbum);
		songlv.setPrefSize(200, 100);

		songlv.setOnMouseClicked(e -> {
			// double-clicked
			if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2
					&& songlv.getSelectionModel().getSelectedItem() != null
					&& !songlv.getSelectionModel().getSelectedItem().isEmpty()) {

				int ind = songlv.getSelectionModel().getSelectedIndex();

				// get song info when clicked in the song list of this album
				songInfoPage(ind, 3);// case 3: from songs in an album
			}
		});

		GridPane gridPane2 = new GridPane();
		gridPane2.setMinSize(400, 200);
		gridPane2.setPadding(new Insets(10, 10, 10, 10));
		gridPane2.setVgap(5);
		gridPane2.setHgap(5);
		gridPane2.setAlignment(Pos.CENTER);

		songlv.setPrefWidth(250);
		gridPane2.add(new VBox(trackListLabel, songlv), 0, 0);

		VBox vb = new VBox(gridPane1, gridPane2);
		vb.setMinSize(400, 300);
		vb.setAlignment(Pos.CENTER);

		// Styling nodes
		albumName.setStyle("-fx-font: normal bold 30px 'serif' ");
		prod.setStyle("-fx-font: normal bold 20px 'serif' ");
		release.setStyle("-fx-font: normal bold 20px 'serif' ");
		genre.setStyle("-fx-font: normal bold 20px 'serif' ");
		copyright.setStyle("-fx-font: normal bold 20px 'serif' ");

		// wrapping long texts
		copyright.setWrappingWidth(400);
		albumName.setWrappingWidth(400);
		genre.setWrappingWidth(400);
		prod.setWrappingWidth(400);
		albumName.setWrappingWidth(400);

		companylabel.setStyle("-fx-font: normal bold 20px 'serif' ");
		trackListLabel.setStyle("-fx-font: normal bold 20px 'serif' ");
		gridPane1.setStyle("-fx-background-color: aliceblue;");
		gridPane2.setStyle("-fx-background-color: aliceblue;");
		vb.setStyle("-fx-background-color: white;");

		// Creating a scene object and display
		Scene scene = new Scene(vb);
		albumInfoWindow.setScene(scene);
		albumInfoWindow.show();

	}

	private static void writeNewDiary(Long songid) {

		// SQL create new diary entry: (songid, date.now)
		try {
			sql.createNewDiary(songid, Date.valueOf(LocalDate.now()));
			// SQL update diary list view
//			updateDiaryListView();// DO NOT UPDATE

			// query and edit this new diary
			queryAndEditDiary(0, 3);// case 3: new diary entry (auto-update listview) (index 0 for nothing)
			// autoClearTrashDiary();

		} catch (SQLIntegrityConstraintViolationException e) {
			AlertBox.display("Fail", "Diary already exists.");
		}

	}

	public static void rightUpPaneSetup(Stage stage, VBox right) {

		Text searchLabel = new Text("Looking for a song...");
		Text artistLabel = new Text("Artist");
		TextField artistText = new TextField();
		Text songLabel = new Text("Song");
		TextField songText = new TextField();
		Text albumLabel = new Text("Album");
		TextField albumText = new TextField();

		Button searchButton = new Button("GO");

		searchButton.setOnAction(e -> {
			String song = songText.getText().trim();
			String album = albumText.getText().trim();
			String artist = artistText.getText().trim();
			if (!song.isEmpty() || !album.isEmpty() || !artist.isEmpty()) {
				// get search result by helper method
				songSearchPage(song, album, artist);

				songText.clear();
				artistText.clear();
				albumText.clear();
			}
		});

		GridPane gridPane = new GridPane();

		// Setting size for the pane
		gridPane.setMinSize(200, 200);

		// Setting the padding
		gridPane.setPadding(new Insets(10, 10, 10, 10));

		// Setting the vertical and horizontal gaps between the columns
		gridPane.setVgap(5);
		gridPane.setHgap(5);

		// Setting the Grid alignment
		gridPane.setAlignment(Pos.CENTER);

		// Arranging all the nodes in the grid
		gridPane.add(searchLabel, 1, 0);
		gridPane.add(artistLabel, 0, 1);
		gridPane.add(artistText, 1, 1);
		gridPane.add(songLabel, 0, 2);
		gridPane.add(songText, 1, 2);
		gridPane.add(albumLabel, 0, 3);
		gridPane.add(albumText, 1, 3);
		gridPane.add(searchButton, 1, 4);

		// Styling nodes
		searchLabel.setStyle("-fx-font: normal bold 20px 'serif' ");
		artistLabel.setStyle("-fx-font: normal bold 15px 'serif' ");
		songLabel.setStyle("-fx-font: normal bold 15px 'serif' ");
		albumLabel.setStyle("-fx-font: normal bold 15px 'serif' ");

		searchButton.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");

		gridPane.setStyle("-fx-background-color: ALICEBLUE;");

		right.getChildren().add(gridPane);
	}

	public static void loginSetup() {
		// creating label email
		Text text1 = new Text("User Name");

		// creating label password
		Text text2 = new Text("Password");

		// Creating Text Filed for email
		TextField textField1 = new TextField();

		// Creating Text Filed for password
		PasswordField textField2 = new PasswordField();

		// Creating Buttons
		Button button1 = new Button("Submit");
		Button button2 = new Button("Clear");

		// process user input
		button1.setOnAction(e -> {
			if (textField1.getText() != null) {
				// check user name and password
				if (textField1.getText().trim().equals(username) && textField2.getText().trim().equals(password)) {
					mainPageSetup();
				} else {
					AlertBox.display("Invalid", "User name or password incorrect!");
				}
			}
		});
		button2.setOnAction(e -> {
			textField1.clear();
			textField2.clear();
		});

		// Creating a Grid Pane
		GridPane gridPane = new GridPane();

		// Setting size for the pane
		gridPane.setMinSize(400, 200);

		// Setting the padding
		gridPane.setPadding(new Insets(10, 10, 10, 10));

		// Setting the vertical and horizontal gaps between the columns
		gridPane.setVgap(5);
		gridPane.setHgap(5);

		// Setting the Grid alignment
		gridPane.setAlignment(Pos.CENTER);

		// Arranging all the nodes in the grid
		gridPane.add(text1, 0, 0);
		gridPane.add(textField1, 1, 0);
		gridPane.add(text2, 0, 1);
		gridPane.add(textField2, 1, 1);
		gridPane.add(button1, 0, 2);
		gridPane.add(button2, 1, 2);

		// Styling nodes
		button1.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
		button2.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");

		text1.setStyle("-fx-font: normal bold 20px 'serif' ");
		text2.setStyle("-fx-font: normal bold 20px 'serif' ");
		gridPane.setStyle("-fx-background-color: lightyellow;");

		// Creating a scene object
		loginScene = new Scene(gridPane);
		// show in window
		window.setTitle(LOGIN);
		window.setScene(loginScene);
		window.show();
	}

	public static void main(String args[]) {
		// connect to SQL database
		sql = new SQL_Connect();
		sql.Connection();
		// launch application
		launch(args);
	}
}