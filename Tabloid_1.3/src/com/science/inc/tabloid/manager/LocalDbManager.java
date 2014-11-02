package com.science.inc.tabloid.manager;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.science.inc.tabloid.data.TagData;

public class LocalDbManager {
	
	private static LocalDbManager instance;
	
	private SQLiteDatabase tabloidSQLiteDb;
	private TabloidSQLiteHelper tabloidDbHelper;
	
	private static final String DATABASE_NAME = "tabloid.db";
	private static final int DATABASE_VERSION = 1;
	
	// table name definitions 
	public static final String TABLE_TAG = "tag";
	
	// table column definitions
	
	// TAGS table columns 
	private static final String KEY_COLUMN_ID = "_id";
	private static final String KEY_COLUMN_SERVER_ID = "server_id";
	private static final String KEY_COLUMN_NAME = "name";
	
	// table create statement definitions
	private static final String CREATE_TAGS = "create table "
		      + TABLE_TAG + "(" + KEY_COLUMN_ID
		      + " integer primary key autoincrement, " + KEY_COLUMN_SERVER_ID
		      + " text not null, " + KEY_COLUMN_NAME
		      + " text not null);";
	
	private LocalDbManager() {
		
	}
	
	public static LocalDbManager getInstance() {
		if (instance == null) {
			instance = new LocalDbManager();
		}
		
		return instance;
	}
	
	public void init(Context context) {
		if (tabloidDbHelper != null) {
			tabloidDbHelper.close();
			tabloidSQLiteDb.close();
		}
		
		tabloidDbHelper = new TabloidSQLiteHelper(context);
		tabloidSQLiteDb = tabloidDbHelper.getWritableDatabase(); 
	}
	
	public long insertTag(String serverId, String name) {
		ContentValues values = new ContentValues();
		values.put(KEY_COLUMN_SERVER_ID, serverId);
		values.put(KEY_COLUMN_NAME, name);
		
		long id = tabloidSQLiteDb.insert(TABLE_TAG, null, values);
		
		return id;
	}
	
	public ArrayList<TagData> getAllTags() {
		ArrayList<TagData> tags = new ArrayList<TagData>();
		String selectQuery = "SELECT  * FROM " + TABLE_TAG;

		Cursor c = tabloidSQLiteDb.rawQuery(selectQuery, null);

		if (c.moveToFirst()) {
			do {
				TagData tag = new TagData();
				tag.id = c.getLong(c.getColumnIndex(KEY_COLUMN_ID));
				tag.serverId = c.getString(c.getColumnIndex(KEY_COLUMN_SERVER_ID));
				tag.name = c.getString(c.getColumnIndex(KEY_COLUMN_NAME));

				tags.add(tag);
			} while (c.moveToNext());
		}
		c.close();
		
		return tags;
	}
	
	public ArrayList<TagData> getTagsByCount(int count) {
		ArrayList<TagData> tags = new ArrayList<TagData>();
		String selectQuery = "SELECT  * FROM " + TABLE_TAG;

		Cursor c = tabloidSQLiteDb.rawQuery(selectQuery, null);

		if (c.moveToFirst()) {
			do {
				TagData tag = new TagData();
				tag.id = c.getLong(c.getColumnIndex(KEY_COLUMN_ID));
				tag.serverId = c.getString(c.getColumnIndex(KEY_COLUMN_SERVER_ID));
				tag.name = c.getString(c.getColumnIndex(KEY_COLUMN_NAME));
				
				tags.add(tag);
				
				count--;
			} while (c.moveToNext() || count==0);
		}
		c.close();
		
		return tags;
	}
	
	public boolean checkIfTableExists(String tableName) {
		boolean doesExist = false;
		Cursor cursor = tabloidSQLiteDb.rawQuery(
				"select DISTINCT tbl_name from sqlite_master where tbl_name = '"
						+ tableName + "'", null);
		
		if (cursor != null && cursor.getCount() > 0) {
			doesExist = true;
		}
		cursor.close();
		return doesExist;
	}
	
	public class TabloidSQLiteHelper extends SQLiteOpenHelper {
		
		public TabloidSQLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TAGS);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG);
		    onCreate(db);
		}
		
	}

}
