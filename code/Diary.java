package application;

import java.sql.Date;

/**
 * This class implements a diary entity which has three main attributes: the
 * associated song id, date, and content.
 * 
 * @author Fangying Zhan
 *
 */
public class Diary {
	Long songid;
	Date date;
	String content;

	Diary(Long songid, Date date, String content) {
		this.songid = songid;
		this.date = date;
		this.content = content;
	}
}
