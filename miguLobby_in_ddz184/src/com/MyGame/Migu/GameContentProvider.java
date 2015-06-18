package com.MyGame.Migu;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class GameContentProvider extends ContentProvider {
	private final String tag = "=GameContentProvider=";

	private SQLiteDatabase db = null;

	private String tableName = "userInfo";

	/** 新版本数据格式 **/
	public static final String MINYOU_DATA = "mingyou_data";

	public static final String OLD_DATA = "data";

	public static final String contentURI = "content://com.mingyou.gameLobby";

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri resultUri = null;
		long rowId = db.insert("userInfo", "id", values);
		if (rowId > 0) {
			resultUri = Uri.withAppendedPath(Uri.parse(contentURI), String.valueOf(rowId));
		}
		return resultUri;
	}

	@Override
	public boolean onCreate() {
		Log.i(tag, "onCreate");
		db = this.getContext().openOrCreateDatabase("mingyouGameDB", Activity.MODE_PRIVATE, null);
		// 先看看表是否存在
		Cursor c = db.rawQuery("select count(*) from sqlite_master where type='table' and name='" + tableName + "'", null);
		if (c.moveToFirst()) {
			final int count = c.getInt(0);
			if (count < 1) { // 还没有创建
				Log.i(tag, "create table");
				db.execSQL("create table " + tableName + "(id INTEGER PRIMARY KEY AUTOINCREMENT, " + OLD_DATA + " TEXT NOT NULL, " + MINYOU_DATA
						+ " TEXT NOT NULL)");
				ContentValues cv = new ContentValues();
				cv.put(OLD_DATA, "");
				cv.put(MINYOU_DATA, "");
				db.insert(tableName, "id", cv);
			} else { // 已创建
				c = db.rawQuery("select * from " + tableName, null);
				c.moveToFirst();
				final int colCount = c.getColumnCount();
				if (colCount == 2) { // 只有老数据
					Log.i(tag, "add new col ");
					db.execSQL("alter table " + tableName + " add " + MINYOU_DATA + " TEXT NOT NULL DEFAULT('')");
					ContentValues cv = new ContentValues();
					cv.put(MINYOU_DATA, "");
					db.insert(tableName, "id", cv);
				} 
			}
		}
		// db.close();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Cursor c = db.query(tableName, null, null, null, null, null, null);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int result = db.update(tableName, values, null, null);
		return result;
	}
}
