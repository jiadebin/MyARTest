/**
 *这是数据库连接工具类 
 * 
 * @author JiaDebin
 * @version 1.0, 20/3/2012
 */

package dbtool.hit.edu.cn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBTool extends SQLiteOpenHelper {

	public static final String DATABASE_NAME="db_MyLocation";
	public static final String TABLE_NAME="tb_MyLocations";
	public static final int VERSION=1;
	public static final String LOCATION_ID="location_id";
	public static final String LOCATION_NAME="location_name";
	public static final String LOCATION_LAT="location_latitude";
	public static final String LOCATION_LON="location_longitude";
	public static final String LOCATION_DESC="location_description";
	
	public DBTool(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE " + TABLE_NAME + " (" + LOCATION_ID
		+ " INTEGER primary key autoincrement, " + LOCATION_NAME + " text, "
		+ LOCATION_LAT + " real, " + LOCATION_LON + " real, "+ LOCATION_DESC +" text);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);

	}

	public Cursor select(){
		SQLiteDatabase db=this.getReadableDatabase();
		Cursor cursor=db.query(TABLE_NAME, null, null, null, null, null, null, null);
		return cursor;
	}
	
	public long insert(String name, double lat, double lon, String desc){
		SQLiteDatabase db=this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		cv.put(LOCATION_NAME, name);
		cv.put(LOCATION_DESC, desc);
		cv.put(LOCATION_LAT, lat);
		cv.put(LOCATION_LON, lon);
		long rowId=db.insert(TABLE_NAME, null, cv);
		return rowId;
	}
	public int delete(int id){
		SQLiteDatabase db=this.getWritableDatabase();
		String where=LOCATION_ID+"=?";
		String[] whereValue={Integer.toString(id)};
		int rows=db.delete(TABLE_NAME, where, whereValue);
		return rows;
	}
	public int update(int id, String name, double lat, double lon, String desc){
		SQLiteDatabase db=this.getWritableDatabase();
		String where=LOCATION_ID+"=?";
		String[] whereValue={ Integer.toString(id)};
		
		ContentValues cv=new ContentValues();
		cv.put(LOCATION_NAME, name);
		cv.put(LOCATION_DESC, desc);
		cv.put(LOCATION_LAT, lat);
		cv.put(LOCATION_LON, lon);
		int rows=db.update(TABLE_NAME, cv, where, whereValue);
		return rows;
	}
}
