/***********************************************************************
 *     Class Name: HomeFragment.java
 *
 *   Purpose: creates and manages the homeFragment
 *            The homefragment shows the total of the current Account
 *            and also displays various information about the market as a whole
 *
 ************************************************************************/
package edu.niu.students.z1797401.simplecryptoporfolio.ui.home;


import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.text.DecimalFormat;
import java.text.NumberFormat;



import edu.niu.students.z1797401.simplecryptoporfolio.MainActivity;
import edu.niu.students.z1797401.simplecryptoporfolio.R;

public class HomeFragment extends Fragment {
    private  View view;
    TextView totalValue;
    TextView totalDifference;
    TextView totalPercentChange;
    TextView[] textViewArray;
    TextView totalCoins,marketAmount, totMarketCap, totCVolume, btcDominance;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_home, container, false);


        MainActivity.currentPortfolio.updateAll(); //Update the entire portfolio before running

        //setting the textviews
        totalValue =  root.findViewById(R.id.totalAccount);
        totalDifference = root.findViewById(R.id.totalDifference);
        totalPercentChange =  root.findViewById(R.id.totalPercentChange);

        //Creating the numberformats
        final NumberFormat numberFormatCurrency = NumberFormat.getCurrencyInstance();
        numberFormatCurrency.setMaximumFractionDigits(2);
        final NumberFormat numberFormatPercent = NumberFormat.getPercentInstance();
        numberFormatPercent.setMaximumFractionDigits(2);




        //Setting the textviews with the live data
             MainActivity.currentPortfolio.getTotalAccountValue().observe(getViewLifecycleOwner(), new Observer<Double>() {
                 @Override
                 public void onChanged(Double aDouble) {
                    String temp = numberFormatCurrency.format(aDouble);
                    totalValue.setText(temp);
                 }
        });
        MainActivity.currentPortfolio.getTotalDifference().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                if(aDouble >= 0){
                    totalDifference.setTextColor(getResources().getColor(R.color.green));
                }
                else{
                    totalDifference.setTextColor(getResources().getColor(R.color.red));
                }
                String temp = numberFormatCurrency.format(aDouble);
                totalDifference.setText(temp);
            }
        });
        MainActivity.currentPortfolio.getPercentChange().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                if(aDouble >= 0){
                    totalPercentChange.setTextColor(getResources().getColor(R.color.green));
                }
                else{
                    totalPercentChange.setTextColor(getResources().getColor(R.color.red));
                }
                String temp = numberFormatPercent.format(aDouble);
                totalPercentChange.setText(temp);
            }
        });


        view = root;

        //TRENDING COIN
        //Create and array of textViews, this is for the trending coins
        textViewArray = new TextView[7];
        textViewArray[0] = (TextView) view.findViewById(R.id.trending0);
        textViewArray[1] = (TextView) view.findViewById(R.id.trending1);
        textViewArray[2] = (TextView) view.findViewById(R.id.trending2);
        textViewArray[3] = (TextView) view.findViewById(R.id.trending3);
        textViewArray[4] = (TextView) view.findViewById(R.id.trending4);
        textViewArray[5] = (TextView) view.findViewById(R.id.trending5);
        textViewArray[6] = (TextView) view.findViewById(R.id.trending6);

        //Create the url and start the task of retrieving the trending coins
        URL url = createURL(getString(R.string.trendingURL));
        GetTrendingCoinTask getLocalTrending = new GetTrendingCoinTask();
        getLocalTrending.execute(url);


        //MARKET DATA
        //Creates the textviews for the marketdata
        totalCoins = view.findViewById(R.id.totalCoins);
        marketAmount = view.findViewById(R.id.marketAmount);
        totMarketCap = view.findViewById(R.id.totMarketCap);
        totCVolume = view.findViewById(R.id.totCVolume);
        btcDominance = view.findViewById(R.id.btcDominance);

        //Creates and starts the task for retrieving the market data
        url = createURL(getString(R.string.marketURL));
        GetMarketTask getLocalMarket = new GetMarketTask();
        getLocalMarket.execute(url);

        return root;
    }



    //CreateURL
    //creates a url from the string passed in
    private URL createURL(String urlString) {
        try {
            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    //GetTrendingCoinTask
    //Background task for getting the json data for the trending coins
    class GetTrendingCoinTask extends AsyncTask<URL, Void, JSONObject> {

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

        //Process json response and update trendingcoins
        @Override
        protected void onPostExecute(JSONObject coin) {
            populateTrendingCoins(coin);

        }


        //populateTrendingCoins
        //called from onPostExecute
        //Populates the trending Coins textViews
        private void populateTrendingCoins(JSONObject coin) {

            try {
                JSONArray coins = coin.getJSONArray("coins");      //Get the array of coins
                for(int i = 0; i<=6; i++){                               //for each of the 7 coins
                  JSONObject temp =  coins.getJSONObject(i);
                    temp = temp.getJSONObject("item");
                  String tempText = temp.getString("name");     //grab the name
                    textViewArray[i].setText(tempText);               //set the textview to the name
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //GetMarketTask
    //Background function for retrieving market json
    class GetMarketTask extends AsyncTask<URL, Void, JSONObject> {

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

        //Process json response and update market textviews
        @Override
        protected void onPostExecute(JSONObject market) {
            populateMarket(market);

        }


        //populateMarket
        //updates the market textViews
        private void populateMarket(JSONObject market) {

            //creates number formats
            NumberFormat money = NumberFormat.getCurrencyInstance();
            money.setMaximumFractionDigits(2);
            DecimalFormat percent = new DecimalFormat("#'%'");

            try {
                    //Gets and sets the total amount of coins
                    JSONObject data = market.getJSONObject("data");
                   int activeCryptos = data.getInt("active_cryptocurrencies");
                   totalCoins.setText(String.valueOf(activeCryptos));

                   //gets and setsthe total amount of markets
                   int marketAmountNum = data.getInt("markets");
                   marketAmount.setText(String.valueOf(marketAmountNum));

                   //gets and sets the total marketcap (the %change in the past 24h)
                    JSONObject marketTemp = data.getJSONObject("total_market_cap");
                    Double totMarketCapNum = marketTemp.getDouble("usd");
                    Double perChangeDouble = data.getDouble("market_cap_change_percentage_24h_usd");
                    totMarketCap.setText(money.format(totMarketCapNum) +" (" +  percent.format(perChangeDouble)+")");

                    //Gets and sets the total volume
                    marketTemp = data.getJSONObject("total_volume");
                    Double totVolume = marketTemp.getDouble("usd");
                    totCVolume.setText(money.format(totVolume));

                    //gets and sets the % of market cap that is bitcoin
                    marketTemp = data.getJSONObject("market_cap_percentage");
                    Double btcDominanceNum = marketTemp.getDouble("btc");
                    btcDominance.setText(percent.format(btcDominanceNum));

                }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


