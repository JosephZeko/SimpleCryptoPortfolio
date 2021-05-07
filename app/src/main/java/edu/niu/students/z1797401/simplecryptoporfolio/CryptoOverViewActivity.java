/***********************************************************************
 *     Class Name: CryptoOVerViewActivity.java
 *
 *   Purpose: creates and manages the overview of a specific coin passed in
 *            by selecting it from the list view in dashboard
 *            It dispalys data bout the coin, current holdings, and individual transactions
 *
 ************************************************************************/
package edu.niu.students.z1797401.simplecryptoporfolio;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import edu.niu.students.z1797401.simplecryptoporfolio.ui.dashboard.DashboardFragment;

import static edu.niu.students.z1797401.simplecryptoporfolio.MainActivity.dbManager;

public class CryptoOverViewActivity extends AppCompatActivity {
    //Declaring variables
    double totalCoinAmount;
    String coinName;
    String coinSymbol;
    double profit;
    double currentValue;
    double marketcap;
    public static TransactionArrayAdapter transactionArrayAdapter;
    private ListView transactionListView;
    private ArrayList<CryptoCoin> coinList;
    String id;
    private Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.crypto_overview);


        Intent intent = getIntent();                                                          //Recieveing the intent
         id = intent.getStringExtra("id");                                             //Get the id as an extra
        coinList = new ArrayList<>();
        setCoinList(id);                                                                 //Creates the list with the id passed in

        //Grab all nessesary variables
        totalCoinAmount=  MainActivity.currentPortfolio.getTotalCoinAmount(id);
        coinName = MainActivity.currentPortfolio.getCoinNameFromID(id);
        currentValue = MainActivity.currentPortfolio.getCoinCurrentTotalValue(id);
        profit = MainActivity.currentPortfolio.getCoinDifference(id);


        //Sets the arrayAdapter for the transactions
        transactionListView = (ListView) findViewById(R.id.transactionListView);
        transactionArrayAdapter = new TransactionArrayAdapter(this.getApplicationContext(), coinList);
        transactionListView.setAdapter(transactionArrayAdapter);

        //SetOnItemLongClickListener
        //This displays the delete button when held on a specific transaction
        transactionListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteButton = (Button) view.findViewById(R.id.deleteButton);
                if(deleteButton.getVisibility() == View.GONE)  //if the delete button is not displayed
                    deleteButton.setVisibility(View.VISIBLE); //display it
                else
                    deleteButton.setVisibility(View.GONE);  //else hide it again
                deleteButton.setFocusable(false);           //set the button  not focusable, so it dosent override the list

                final CryptoCoin temp = (CryptoCoin) parent.getItemAtPosition(position);    //get the transaction selected
                deleteButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {              //when delete button is pressed
                        deleteButton.setVisibility(View.GONE);       //hide the delete button
                        deleteEntry(temp.getID());                   //delete entry
                    }
                });
                return false;
            }
        });

            //Create and start the getCoin task
            URL url = createURL(id);
            GetCoinTask localGetCoinTask = new GetCoinTask();
            localGetCoinTask.execute(url);
    }



    //populateUi
    //Handles all the UI related setting for the coin data
    public void populateUI(JSONObject coin) throws JSONException {
        //creates numberformats for the numbers
        final NumberFormat numberFormatCurrency = NumberFormat.getCurrencyInstance();
        numberFormatCurrency.setMaximumFractionDigits(2);
        final DecimalFormat numberFormatCoin = new DecimalFormat("#,##0.00");

        JSONObject temp;

        //COIN SYMBOL AND NAME (TITLE)
        coinSymbol = coin.getString("symbol");
        TextView titleCoinTV = (TextView) findViewById(R.id.titleCoin);
         titleCoinTV.setText(coinName + " (" + coinSymbol + ")");

         //COINS OWNED
        TextView coinsOwnedTV = (TextView) findViewById(R.id.coinsOwned);
        coinsOwnedTV.setText(totalCoinAmount + " " + coinSymbol);

        //CURRENT MARKET VALUE
        TextView currentPriceTV = (TextView) findViewById(R.id.currentValue);
        currentPriceTV.setText(numberFormatCurrency.format(currentValue));

        //CURRENT PROFIT
        TextView currentProfitTV = (TextView) findViewById(R.id.currentProfit);
        if(profit >= 0){
            currentProfitTV.setTextColor(getResources().getColor(R.color.green));
        }
        else currentProfitTV.setTextColor(getResources().getColor(R.color.red));
        currentProfitTV.setText(numberFormatCurrency.format(profit));

        //MARKET CAP RNAK
       TextView marketCapRank = (TextView) findViewById(R.id.marketRank);
       marketCapRank.setText(coin.getString("market_cap_rank"));

       JSONObject marketData = coin.getJSONObject("market_data");

       //LOW 24H
       TextView low24h = (TextView) findViewById(R.id.low24);
       temp = marketData.getJSONObject("low_24h");
       low24h.setText(numberFormatCurrency.format(temp.getDouble("usd")));

       //HIGH 24H
        TextView high24h = (TextView) findViewById(R.id.high24);
        temp = marketData.getJSONObject("high_24h");
        high24h.setText(numberFormatCurrency.format(temp.getDouble("usd")));

        //TRADING VOLUME
        TextView tradingVolume = (TextView) findViewById(R.id.volume);
        temp = marketData.getJSONObject("total_volume");
        tradingVolume.setText(numberFormatCurrency.format(temp.getDouble("usd")));

        //TOTAL SUPPLY
        TextView circulatingSupply = (TextView) findViewById(R.id.circulatingSupply);
        circulatingSupply.setText(numberFormatCoin.format(marketData.getDouble("circulating_supply")));


       //MarketCap
        temp = marketData.getJSONObject("market_cap");
        marketcap = temp.getDouble("usd");
        TextView marketCapTV  = (TextView) findViewById(R.id.marketCap);
        marketCapTV.setText( numberFormatCurrency.format(marketcap));


        //IMAGE
        temp = coin.getJSONObject("image");
        ImageView coinIcon = (ImageView) findViewById(R.id.imageView2);
        new LoadImageTask(coinIcon).execute(temp.getString("large")); //starts the background task for the image
    }



 //creates the url for retrieving coin data
    private URL createURL(String coinID) {
        try {
            String getPriceUrl = "https://api.coingecko.com/api/v3/coins/"+ URLEncoder.encode(coinID, "UTF-8") + "?localization=false&tickers=false&community_data=false&developer_data=false&sparkline=false";
            return new URL(getPriceUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    //GetCoinTask
    //Starts background task of getting coin data from Coin Gecko
    class GetCoinTask extends AsyncTask<URL, Void, JSONObject> {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        //This was apparently needed for "Try-with-resources" that is on line 159
        @Override
        protected JSONObject doInBackground(URL... params) {
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
                    return new JSONObject(builder.toString());  //send builder to onPostExecution
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

        //Process json response and update ui
        @Override
        protected void onPostExecute(JSONObject coin) {
            try {
                populateUI(coin);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }



   //Updates the list of transactions
    public void setCoinList(String id){
       ArrayList<CryptoCoin> coinArrayList = dbManager.selectAllByCoinID(id);
       if(!coinArrayList.isEmpty())
        coinList.addAll(coinArrayList);
    }


    //deletes and entry in the database and updates the things dependent on it
    public void deleteEntry(int databaseID){
        dbManager.deleteByID(databaseID);
        MainActivity.currentPortfolio.setTotal();
        coinList.clear();
        setCoinList(id);
        DashboardFragment.setPortfolioList();
        transactionArrayAdapter.notifyDataSetChanged();

    }

    //Override the backbutton on the android ui
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class); //send the user back to MainActivity instead of dashboard
        startActivity(intent);
    }



    private class LoadImageTask extends AsyncTask<String, Void, Bitmap>
    {
        private ImageView imageView;  // displays the thumbnail

        // store imageView on which to set the downloaded Bitmap
        public LoadImageTask(ImageView imageView)
        {
            this.imageView = imageView;
       }


        // load image: params[0] is the String URL representing the image
        @RequiresApi(api = Build.VERSION_CODES.KITKAT) //Once again, this was need for line 145
        @Override
        protected Bitmap doInBackground(String... params)
        {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try
            {
                URL url = new URL(params[0]);  // create URL for image

                // open a HttpURLConnection, get its InputStream
                // and download the image

                connection = (HttpURLConnection) url.openConnection();

                try ( InputStream inputStream = connection.getInputStream())
                {
                    bitmap = BitmapFactory.decodeStream(inputStream);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                connection.disconnect();  // close the HttpURLConnection
            }

            return bitmap;
        }

        // set coin image at the top
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            imageView.setImageBitmap(bitmap);
        }
    }
}
