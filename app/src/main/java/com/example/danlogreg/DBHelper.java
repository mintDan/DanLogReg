package com.example.danlogreg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "Userdata.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table SchoolExams(x REAL, y REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists SchoolExams");
    }


    public Boolean insertdata(String TableName, double x, double y){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("x",x);
        contentValues.put("y",y);
        long result = DB.insert(TableName,null, contentValues);
        if (result == -1){
            return false;
        } else{
            return true;
        }
    }//end insertdata()

    public void deletedata(String TableName, double x){
        SQLiteDatabase DB = this.getWritableDatabase();
        DB.execSQL("delete from "+ "SchoolExams");
        DB.execSQL("vacuum");
        DB.close();
    }//end deletedata()


    public Cursor getdata(String TableName){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from "+TableName,null);
        return cursor;
    }//end getdata()


    public Boolean updatedata(String TableName, double x, double y){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("y",y);
        Cursor cursor = DB.rawQuery("Select * from "+TableName+" where x = ?", new String[] {String.valueOf(x)});
        if (cursor.getCount()>0){
            long result = DB.update(TableName,contentValues,"x=?",new String[] {String.valueOf(x)});
            if (result == -1){
                return false;
            } else{
                return true;
            }
        } else{
            return false;
        }
    }//end updatedata()


}