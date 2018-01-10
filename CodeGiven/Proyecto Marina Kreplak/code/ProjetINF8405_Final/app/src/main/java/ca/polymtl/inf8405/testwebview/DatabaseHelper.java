package ca.polymtl.inf8405.testwebview;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ndndic on 2017-03-02.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String Database_name ="Members.db";
    public static final String table_name ="Profile_table";
    public static final String col_4 ="EMAIL";
    public static final String col_5 ="PSEUDO";
    public static final String col_6 ="PASSWORD";
    public static final String col_7 ="IMAGE";

    public DatabaseHelper(Context context) {
        super(context, Database_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table "+ table_name +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, EMAIL TEXT, PSEUDO TEXT, PASSWORD TEXT, IMAGE BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ table_name);
    }
    public boolean insertData( String email, String pseudo, String password, byte[] image){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues cv= new ContentValues();
        cv.put(col_4, email);
        cv.put(col_5, pseudo);
        cv.put(col_6, password);
        cv.put(col_7, image);
        long res = sqLiteDatabase.insert(table_name, null,cv);
        if (res == -1)
            return false;
        else return true;
    }
    public Cursor getAllData(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor res = sqLiteDatabase.rawQuery("select * from "+table_name, null);
        return res;

    }
}
