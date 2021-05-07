/***********************************************************************
 *     Class Name: DatabaseManger.java
 *
 *   Purpose: creates and manages the the database
 *
 ************************************************************************/
package edu.niu.students.z1797401.simplecryptoporfolio;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.util.ArrayList;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "cryptoHoldings";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_HOLDINGS = "holdings";
    private static final String ID = "id";                         //id for table
    private static final String COINID = "coinID";                 //coin id for coingecko
    private static final String CNAME = "cName";                   //coin name
    private static final String SYMBOL = "symbol";                //coin symbol
    private static final String PICTUREURL = "pictureURL";
    private static final String AMOUNT = "amount";
    private static final String PRICE = "price";
    private static final String TOTAL = "total";

    public DatabaseManager(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    //create table ( ID integer primary key autoincrement, coinID text, cNAME text,  AMOUNT integer, PRICE double)
    //Creates the table
    public void onCreate(SQLiteDatabase db){
        String sqlCreate = "create table " + TABLE_HOLDINGS + "(" + ID;
        sqlCreate += " integer primary key autoincrement, " + COINID + " text," + CNAME + " text," + SYMBOL + " text," + PICTUREURL;
        sqlCreate += " text," + AMOUNT +  " double";
        sqlCreate += "," + PRICE + " double,";
        sqlCreate += TOTAL + " double)";
        db.execSQL(sqlCreate);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //Drop old table
        db.execSQL("drop table if exists " + TABLE_HOLDINGS);
        onCreate(db);
    }

    //insert
    public void insert(CryptoCoin cryptoCoin){
        SQLiteDatabase db = this.getWritableDatabase();
        String sqlInsert = "insert into " + TABLE_HOLDINGS;
        sqlInsert += " values( null, '" + cryptoCoin.getCoinID() + "', '" + cryptoCoin.getName()  + "', '" + cryptoCoin.getSymbol() + "', '" + cryptoCoin.getPictureURL() ;
        sqlInsert+= "', '" + cryptoCoin.getAmount() + "', '" + cryptoCoin.getPrice() + "', '" + cryptoCoin.getTotalValue()+ "' )";
        db.execSQL(sqlInsert);
        db.close();

    }

    //deletebyID
    public void deleteByID(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String sqlDelete = "delete from " + TABLE_HOLDINGS;
        sqlDelete += " where " + ID + " = " + id;
        db.execSQL(sqlDelete);
        db.close();
    }

    public void deleteTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE "+TABLE_HOLDINGS);
        db.close();
    }




    //selectAll
    public ArrayList<CryptoCoin> selectAll(){
        String sqlQuery = "SELECT * FROM " + TABLE_HOLDINGS; //Select all

        SQLiteDatabase db = this.getReadableDatabase(); //Get readable database, becuase you dont need to open it
        Cursor cursor = db.rawQuery(sqlQuery, null); //create a cursor

        ArrayList<CryptoCoin> cryptoCoin = new ArrayList<CryptoCoin>(); //create a arraylist of routines
        while(cursor.moveToNext()){ //create a routine and set it equal to the current routine
            CryptoCoin currentCryptoCoin = new CryptoCoin(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), Double.parseDouble(cursor.getString(5)), Double.parseDouble(cursor.getString(6)));
            cryptoCoin.add(currentCryptoCoin); //add it to the ArrayList
        }
        cursor.close();
        db.close();
        return cryptoCoin; //return the array list
    }

    //Grabs all the unique names in the database
    //It dosent matter which data is selected, just the names
    public ArrayList<String> selectAllCoinIDUnique(){
        String sqlQuery = "SELECT DISTINCT coinID FROM " + TABLE_HOLDINGS; //Select distict names

        SQLiteDatabase db = this.getReadableDatabase(); //Get readable database, becuase you dont need to open it
        Cursor cursor = db.rawQuery(sqlQuery, null); //create a cursor

        ArrayList<String> coinName = new ArrayList<String>(); //create a list of strings
        while(cursor.moveToNext()){ //create a routine and set it equal to the current routine
          coinName.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return coinName; //return the array list
    }


    //Select all entries from the coin idw
    public ArrayList<CryptoCoin> selectAllByCoinID(String coinID){
        String sqlQuery = "SELECT * FROM " +  TABLE_HOLDINGS + " WHERE coinID = ?";
        SQLiteDatabase db2 = this.getReadableDatabase(); //Get readable database
        Cursor cursor = db2.rawQuery(sqlQuery, new String[] {coinID});
        ArrayList<CryptoCoin> cryptoCoins = new ArrayList<>(); //create a list of strings
        while(cursor.moveToNext()){
            CryptoCoin currentCryptoCoin = new CryptoCoin(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2),cursor.getString(3), cursor.getString(4), Double.parseDouble(cursor.getString(5)), Double.parseDouble(cursor.getString(6)));
            cryptoCoins.add(currentCryptoCoin); //add it to the ArrayList
        }
        cursor.close();

        return cryptoCoins;
    }


}
