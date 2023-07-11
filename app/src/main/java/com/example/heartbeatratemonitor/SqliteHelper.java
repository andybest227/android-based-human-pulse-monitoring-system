package com.example.heartbeatratemonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteHelper extends SQLiteOpenHelper {
    //Database name
    public static final String DATABASE_NAME = "hp_monitor";
    //Database version
    public static final int DATABASE_VERSION = 1;
    //Table name
    public static  final String TABLE_USERS = "users";
    //TABLE USERS COLUMNS
    //ID COLUMN @primaryKey
    public static  final String KEY_ID = "id";

    //COLUMN username
    public static final String KEY_USERNAME = "username";
    //COLUMN email
    public static final String KEY_EMAIL = "email";
    //COLUMN password
    public static final String KEY_PASSWORD = "password";

    //SQL for creating user table
    public static final String SQL_TABLE_USERS = " CREATE TABLE " + TABLE_USERS + "(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_USERNAME + " TEXT, "+ KEY_EMAIL + " TEXT, " +KEY_PASSWORD + " TEXT" + ") ";


    public SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create Table when onCreate gets called
        sqLiteDatabase.execSQL(SQL_TABLE_USERS);
    }

        //using this method we can add users to user table
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //drop table to create new one if database version is updated
        sqLiteDatabase.execSQL(" DROP TABLE IF EXISTS " + TABLE_USERS);
    }
    //Using this method we can add user to the table
    public void addUser(User user){
        //get writable database
        SQLiteDatabase db = this.getWritableDatabase();

        //create content values to insert
        ContentValues values = new ContentValues();

        //Put username in @values
        values.put(KEY_USERNAME, user.userName);
        //Put email in @values
        values.put(KEY_EMAIL, user.email);
        //put password in @values
        values.put(KEY_PASSWORD, user.password);
        //insert row
        long todo_id = db.insert(TABLE_USERS, null, values);
    }
    public User Authenticate(User user){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, //Selecting table
                new String[]{KEY_ID, KEY_USERNAME, KEY_EMAIL, KEY_PASSWORD}, //Selecting columns want to query
                KEY_EMAIL + "=?",
                new String[]{user.email}, //where clause
                null, null,null);
        if(cursor != null && cursor.moveToFirst()&& cursor.getCount() >0){
            //if cursor has value then in user database there is user associated with the given email
            User user1 = new User(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            //Math both password check they are same or not
            if (user.password.equalsIgnoreCase(user1.password)){
                return  user1;
            }
        }
        //if user password does not matches or there is no record with that email in then return @false
        return null;
    }
    public boolean isEmailExists(String email){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_ID, KEY_USERNAME, KEY_EMAIL, KEY_PASSWORD}, KEY_EMAIL + "=?", new String[]{email}, null, null, null);
        return cursor != null && cursor.moveToFirst() && cursor.getCount() > 0;
    }
}
