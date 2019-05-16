package com.android.passmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class Database extends SQLiteOpenHelper {
    private String TABLE_NAME ;
    private String ID = "id";
    private String TITLE ="title";
    private String ACCOUNT = "account";
    private String PASSWORD = "password";
    private String DATE = "date";
    private String UPTIME = "uptime";
    private String point = ", ";
    private Context context;

    Database(Context context , String name , SQLiteDatabase.CursorFactory factory , int version , String tableName) {
        super( context, name, factory, version );
        this.context =context;
        this.TABLE_NAME=tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( "create table "+ TABLE_NAME +"("+ID + " "+"integer PRIMARY KEY AUTOINCREMENT" + point
                +TITLE+" "+"TEXT"+ point
                +ACCOUNT+ " "+"TEXT"+ point
                +PASSWORD+ " "+"TEXT"+ point
                +DATE+ " "+"TEXT"+ point
                +UPTIME+ " "+"integer DEFAULT 1"+")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Account cursorToObject(Cursor cursor) {
        Account h = new Account();
        h.setId( cursor.getInt( cursor.getColumnIndex( ID ) ) );
        h.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
        h.setAccount(cursor.getString(cursor.getColumnIndex(ACCOUNT)));
        h.setPassword(cursor.getString(cursor.getColumnIndex(PASSWORD)));
        h.setDate( cursor.getString(cursor.getColumnIndex(DATE)) );
        return h;
    }

    void addData(Account account){
        SQLiteDatabase database = this.getWritableDatabase();
        String title = account.getTitle();
        String name = account.getAccount();
        String pass =account.getPassword();
        @SuppressLint("SimpleDateFormat")
        String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format( new Date() );
        String query =    "INSERT INTO " + TABLE_NAME + "(" + TITLE + point + ACCOUNT + point + PASSWORD + point + DATE  + ")" +
                " VALUES ( '" + title + "', '" + name + "', '" + pass + "', '" + date + "' )";
        database.execSQL( query);
        //showToast(context,context.getString(R.string.successfully_added),1000);
        database.close();

    }

    public LinkedList<Account> getAll(){
        SQLiteDatabase database = this.getReadableDatabase();
        LinkedList<Account> historyList = new LinkedList<>();

        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME /*+" ORDER BY "+TITLE */, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Account h = cursorToObject(cursor);
                    historyList.add(h);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        database.close();
        return historyList;
    }
    public LinkedList<Account> getAll(String str){
        SQLiteDatabase database = this.getReadableDatabase();
        LinkedList<Account> historyList = new LinkedList<>();

        Cursor cursor=null;
        try {
            cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME +" WHERE "+TITLE+" LIKE "+"'"+"%"+str+"%"+ "'"+" OR "+ACCOUNT+" LIKE "+"'"+"%"+str+"%"+ "'", null);
            if (cursor.moveToFirst()) {
                do {
                    Account h = cursorToObject(cursor);
                    historyList.add(h);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        database.close();
        return historyList;
    }


    String[] getDate(){
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor =database.rawQuery( "SELECT * FROM " + TABLE_NAME , null );
        try {
            String [] str =new String[2];
            if(cursor.moveToFirst()){
                String loginName =cursor.getString( cursor.getColumnIndex( ACCOUNT ) );
                String loginPass =cursor.getString( cursor.getColumnIndex( PASSWORD ) );
                str[0]=loginName;
                str[1]=loginPass;
                return str;

            }
        }catch (Exception e){

        }finally {
            if(cursor!=null&&!cursor.isClosed()){
                cursor.close();
            }
        }
        database.close();
        return null;
    }

    void upData(Account account , int position){
        SQLiteDatabase database = this.getWritableDatabase();
        String title = account.getTitle();
        String name = account.getAccount();
        String pass =account.getPassword();
        @SuppressLint("SimpleDateFormat")
        String date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format( new Date() );
        //int uptime =account.getUpTime();
        String query = " UPDATE "+TABLE_NAME+" "+"SET "+TITLE+"="+"'"+title+"'"+point
                +ACCOUNT+"="+"'"+name+"'"+point
                +PASSWORD+"="+"'"+pass+"'"+point
                +DATE+"="+"'"+date+"'"+" "+"WHERE "+ID+"="+position;
        database.execSQL( query);
        //showToast(context,context.getString(R.string.successfully_added),1000);
        database.close();
    }

    void deleteData(int id){
        SQLiteDatabase database = this.getWritableDatabase();
        String query ="DELETE FROM " +TABLE_NAME+ " WHERE "+ID+"="+id;
        database.execSQL( query );
        database.close();
    }
    void deleteAll(){
        SQLiteDatabase database = this.getWritableDatabase();
        String query ="DELETE FROM " +TABLE_NAME;
        database.execSQL( query );
        database.close();
    }

    public int getUpTime(int id){
        return 0;
    }
}
