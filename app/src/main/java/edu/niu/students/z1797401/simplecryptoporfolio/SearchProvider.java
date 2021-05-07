/***********************************************************************
 *     Class Name: SearchProvider.java
 *
 *   Purpose: A "Content Provider" class specifically designed for the
 *              search view.
 *              This is necessary to be able to give custom
 *              recommendations, although it is very slow
 *
 *
 ************************************************************************/

package edu.niu.students.z1797401.simplecryptoporfolio;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;


import static android.app.SearchManager.SUGGEST_COLUMN_INTENT_DATA;
import static android.app.SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_1;
import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_2;



public class SearchProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.example.SimpleCryptoPorfolio.SearchProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/coins";
    static final Uri CONTENT_URI = Uri.parse(URL);


    static final String _ID = "_id";
    static final String COINID = SUGGEST_COLUMN_INTENT_DATA; //coin id
    static final String NAME =  SUGGEST_COLUMN_TEXT_1;
     static final String VALUES = SUGGEST_COLUMN_INTENT_EXTRA_DATA;
   static final String SYMBOL = SUGGEST_COLUMN_TEXT_2; //symbol

    private static HashMap<String, String> COINS_PROJECTION_MAP;

    static final int COINS = 1;
    static final int COINS_ID = 2;

     static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        uriMatcher.addURI(URL,"coins", COINS);
        uriMatcher.addURI(URL, "coins/#", COINS_ID);

    }



    public  SQLiteDatabase db;
    static final String DATABASE_NAME = "cgCoins";
    static final String COINS_TABLE_NAME = "coins";
    static final int DATABASE_VERSION = 1;

    //Creates a database tabel in fts3 so its all text making it faster to read and query from
    static final String CREATE_DB_TABLE = " CREATE VIRTUAL TABLE " + COINS_TABLE_NAME + " USING fts3(" + _ID + ", "+ NAME + ", " + SYMBOL + ", " + COINID + ", " + VALUES +   ");";



    static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        //create the database table
        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            db.execSQL("DROP TABLE IF EXISTS " + COINS_TABLE_NAME);
        }

    }

    @Override
    public boolean onCreate(){
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return(db == null)? false:true;     //return true/false if the database is null
    }

    //insert
    //inserts data into the database
    //Since content providers are not transactional, it moves relatively slowly when inserting lots of data
    @Override
    public Uri insert(Uri uri, ContentValues values){
        //Add a new record to the database
      long rowID = db.insert(COINS_TABLE_NAME, "", values);
        //If record is added successfully
      if(rowID > 0){
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
           getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
       }
       throw new SQLException("Failed to add record" + uri);
    }

    //Delete
    //Drops the table then recreates it
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        db.execSQL("DROP TABLE IF EXISTS " + COINS_TABLE_NAME);
        db.execSQL(CREATE_DB_TABLE);
        return 0;
    }

    //update
    //not actually used but required to declare
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }


    //query
    //querys the database with multiple passed in paramaters
    //returns a cursor with all the hits
    //automatically called for suggestions whenever anything is entered in search provider
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder ){
       Cursor c = null;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(COINS_TABLE_NAME);                   //sets the tables
        qb.setProjectionMap(COINS_PROJECTION_MAP);        //sets the projection map

        if (sortOrder == null || sortOrder == "") { //check for sort order
            sortOrder = SUGGEST_COLUMN_TEXT_1;       //default sort order is just by name
        }

        //db = database being searcher
        //projection = projection map
        //selection is the column
        // selectionArgs carries the actual search word
        c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        return c;
    }


    //GetType
    //no used but required for implementation
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }


}

