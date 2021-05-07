/***********************************************************************
 *     Class Name: StartActivity
 *
 *   Purpose: This class handles the boot up functions, which mainly consist
 *            of loading in the 6000+ entries to the search database
 *            It displays a please wait loading screen until its finished then
 *            proceeds the MainActivity
 *
 *            It also saves the jsonArray passed in from coingecko list api
 *            to bypass loading the array everytime
 *
 *            If this wasn't here the app crashed when loading in all the data
 *
 *
 ************************************************************************/

package edu.niu.students.z1797401.simplecryptoporfolio;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class StartActivity extends Activity {
    static String jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Grab the saved array
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        jsonArray = sharedPref.getString("jsonArray", " ");

        setContentView(R.layout.loadingscreen); //Set content view to the loading screen



        URL url = null;
        try {
            url = new URL("https://api.coingecko.com/api/v3/coins/list?include_platform=false"); ///Create a url for the list
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        GetCoinTask getLocalCoinTask = new GetCoinTask(); //Execute the background loading
       getLocalCoinTask.execute(url);

    }


    //LoadCoins
    //This is called at the end of retrieving the json from CoinGecko
    public void loadCoins(JSONArray coins){
        getContentResolver().delete(SearchProvider.CONTENT_URI, null, null);    //Delete the current database since its out of date
        ContentValues values = new ContentValues();                                                 //Create a new ContentValues, a object for inserting into content providers
        try {
            for (int i = 0; i < coins.length(); ++i) {                                              //for each entry in the list of coins
                JSONObject currentCoin = coins.getJSONObject(i);                                    //grab the current entry
                String temp =  currentCoin.getString("name") + "," + currentCoin.getString("symbol");  //get the name and symbol in a string,
                                                                                                                    //this is so it can be passed in on string during the search as extra data
                                                                                                                    //It avoids having to look up the name in the search via CoinGecko call since we already have it
                   values.put(SearchProvider.COINID, currentCoin.getString("id"));          //put the id
                   values.put(SearchProvider.VALUES, temp);                                       //This is the values that will sent to the searchable activity as a extra
                   values.put(SearchProvider.NAME, currentCoin.getString("name"));          //put the name in
                   values.put(SearchProvider.SYMBOL, currentCoin.getString("symbol"));      //put the symbol in
                getContentResolver().insert(SearchProvider.CONTENT_URI, values);                //insert the all the values
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Save the current JSONArray in SharedPreferences
        //This will be compared to new list once the app is booted up again
        //so it docent have to redo the entire database if there was no update
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("jsonArray", coins.toString());
        editor.apply();

        //End the activity
        endActivity();
        }


        //EndActivity
    //start a intent and go to mainActivity
    public void endActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



    class GetCoinTask extends AsyncTask<URL, Void, JSONArray> {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        //This was apparently needed for "Try-with-resources"
        @Override
        protected JSONArray doInBackground(URL... params) {
            HttpURLConnection connection = null;  //set connect to null

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();   //gets the response code from the server

                if (response == HttpURLConnection.HTTP_OK) {  //should be 200
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {   //start making reader
                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line);  //start building the json begin read in as a string
                        }
                    } catch (IOException e) {
                        MainActivity.makeSnackbar("R.string.read_error");
                        e.printStackTrace();
                    }

                    //Inserts the coins in the background so the app dosent crash
                    return new JSONArray(builder.toString());  //send builder to onPostExecution
                } else {

                    MainActivity.makeSnackbar("  R.string.connect_error");
                }
            } catch (Exception e) {
                MainActivity.makeSnackbar("  R.string.connect_error");
                e.printStackTrace();
            } finally {
                connection.disconnect(); //disconnect from server
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONArray coin) {
          if(!jsonArray.equals(coin.toString())) {       //if the database has been updated
                loadCoins(coin);
         }
          else{
               endActivity();                             //else leave the database alone and continue

            }

        }
    }
}
