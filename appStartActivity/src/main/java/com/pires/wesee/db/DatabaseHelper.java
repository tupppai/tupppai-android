package com.pires.wesee.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.pires.wesee.model.PhotoItem;
import com.pires.wesee.Logger;

import java.sql.SQLException;

/**
 * Database helper class used to manage the creation and upgrading of your
 * database. This class also usually provides the DAOs used by the other
 * classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static final String TAG = DatabaseHelper.class.getSimpleName();
	private static final String DATABASE_NAME = "pires.wesee.db";
	private static final int DATABASE_VERSION = 1;

	private Dao<PhotoItem, Long> mPhotoItemDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"onCreate");
			// TableUtils
			// .createTableIfNotExists(connectionSource, UserInfo.class);
			// TableUtils
			// .createTableIfNotExists(connectionSource, PhotoItem.class);
			TableUtils.createTable(connectionSource, PhotoItem.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		try {
			Logger.log(Logger.LOG_LEVEL_DEBUG, Logger.USER_LEVEL_COLOR, TAG,
					"onUpgrade");
			// TableUtils.dropTable(connectionSource, UserInfo.class, true);
			TableUtils.dropTable(connectionSource, PhotoItem.class, true);
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Logger.log(Logger.LOG_LEVEL_ERROR, Logger.USER_LEVEL_COLOR, TAG,
					"onUpgrade: Can't drop databases");
			throw new RuntimeException(e);
		}
	}

	private static DatabaseHelper instance;

	/**
	 * 单例获取该Helper
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized DatabaseHelper getHelper(Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context);
		}
		return instance;
	}

	public synchronized Dao<PhotoItem, Long> getPhotoItemDao()
			throws SQLException {
		if (mPhotoItemDao == null) {
			mPhotoItemDao = getDao(PhotoItem.class);
		}
		return mPhotoItemDao;
	}

	// public synchronized Dao<UserInfo, Long> getLocalDataDao()
	// throws SQLException {
	// if (mLocalData == null) {
	// mLocalData = getDao(UserInfo.class);
	// }
	// return mLocalData;
	// }

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		mPhotoItemDao = null;
		// mLocalData = null;
	}
}
