/***********************************************************************
 *     Class Name: Portfolio.java
 *
 *   Purpose: the portfolio class, handles the total prices
 *            differences and stores the currentValues of each coin
 *        
 *
 ************************************************************************/
package edu.niu.students.z1797401.simplecryptoporfolio;

import android.os.AsyncTask;
import android.os.Build;


import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static edu.niu.students.z1797401.simplecryptoporfolio.MainActivity.dbManager;

public class Portfolio {
    private static HashMap<String, Double> currentValues;  //Stores the current value of each coin

    //Live data for easy updating other views
    private MutableLiveData<Double> totalAccountValue;
    private MutableLiveData<Double> totalDifference;
    private MutableLiveData<Double> percentChange;

    //Contstructor
    public Portfolio() {
        currentValues = new HashMap<String,Double>();
        totalAccountValue = new MutableLiveData<>();
        totalDifference = new MutableLiveData<>();
        percentChange = new MutableLiveData<>();
        setCurrentPrices();
        setTotal();
    }

//Getters for the live data
    public LiveData<Double> getTotalAccountValue() {
        return totalAccountValue;
    }

    public LiveData<Double> getPercentChange() {
        return percentChange;
    }

    public LiveData<Double> getTotalDifference() {
        return totalDifference;
    }

    public static HashMap<String, Double> getCurrentValues() {
        return currentValues;
    }

    //Clears and updates the currentValues and then updates the totals
    public void updateAll(){
        currentValues.clear();
        setCurrentPrices();
        setTotal();
    }


    //Update the total account value
    public void setTotal() {
        double initialTotal = 0;
        double total = 0;
        double difference=0;
        ArrayList<CryptoCoin> coins = dbManager.selectAll(); //Grab all coins
        for (CryptoCoin coin : coins) {                     //for each coin
            if (currentValues.containsKey(coin.getCoinID())) {  //if the map has the value
                initialTotal += coin.getAmount() * coin.getPrice();  //get the intial total
                total += coin.getAmount() * currentValues.get(coin.getCoinID()); //get the current up to date total
            }
        }
        difference= total-initialTotal;             //get the differnce of current total and bought total

        //set the values of the live data
        totalDifference.setValue(difference);
        percentChange.setValue((difference/initialTotal));
        totalAccountValue.setValue(total);
    }

    //gets the coin name based on the coins id
    public String getCoinNameFromID(String coinID){
        ArrayList<CryptoCoin> cryptoCoins = dbManager.selectAllByCoinID(coinID);  //selects all by coin id
     return cryptoCoins.get(0).getName();    //grab the first one's name since they should all be the same

    }

    //Get icon URL from id
    public String getIconURLFromID(String coinID){
        ArrayList<CryptoCoin> cryptoCoins = dbManager.selectAllByCoinID(coinID); //selects all by coin id
    if(!cryptoCoins.isEmpty())
        return cryptoCoins.get(0).getPictureURL();  //return the url from the first one since they should all be the same
    return "";
    }

    //getcoinPercentDifference
    //gets the inital total then does some math to get the perecent difference
    public double getCoinPercentDifference(String coinID){
        double temp = 0;
        if(currentValues.containsKey(coinID)){
            double intitalTotal = getCoinTotalValue(coinID);;
            try {
                temp = ((currentValues.get(coinID) * getTotalCoinAmount(coinID) - intitalTotal) / intitalTotal);      // ((currentPrice * totalAmountOfCoins) - InitialTotal) / InitialTotal      to get the percent difference
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return temp;
    }

    //getCoinDiffernece
    //gets the difference between initial total and currentTotal
    public double getCoinDifference(String coinID){
        double temp = 0;
        if(currentValues.containsKey(coinID)){
            double intitalTotal = getCoinTotalValue(coinID);
            try {
                temp = ((currentValues.get(coinID) * getTotalCoinAmount(coinID) - intitalTotal));
            }
            catch(Exception e){
                e.printStackTrace();
            }
    }
        return temp;
    }

    //getCoinTotalValue
    //Returns the total of all the transactions for a specific coin
    public double getCoinTotalValue(String coinID) {
        double total = 0;
        ArrayList<CryptoCoin> cryptoCoins = dbManager.selectAllByCoinID(coinID);
        for (CryptoCoin coin : cryptoCoins) {
            total += coin.getTotalValue();
        }
        return total;
    }

    //getCoinCurrentTotalValue
    //returns the totalCoins * CurrentPrice
    public double getCoinCurrentTotalValue(String coinID){
        double total = getTotalCoinAmount(coinID) * getCurrentCoinPrice(coinID);
        return total;
    }

    //getTotalCoinAmount
    //Returns the total amount of coins for a specific id
    public double getTotalCoinAmount(String coinID){
        double total = 0;
        ArrayList<CryptoCoin> cryptoCoins = dbManager.selectAllByCoinID(coinID);
        for (CryptoCoin coin : cryptoCoins) {
            total += coin.getAmount();
        }
        return total;
    }

    //getCurrentCoinPrice
    //returns the current value of a coin in the hashmap
    public double getCurrentCoinPrice(String coinID){
        if(currentValues.containsKey(coinID)){
            return currentValues.get(coinID);
        }
        else return 0;
    }

    //getTransactionDifference
    //gets the difference for a specific transaction
    public double getTransactionDifference(CryptoCoin coin){

        if(currentValues.containsKey(coin.getCoinID())){
         return  (currentValues.get(coin.getCoinID()) -coin.getPrice()) * coin.getAmount();
        }
        return 0;
    }

    //getTransactionPercentDifference
    //gets the percent difference for a specific transaction
    public double getTransactionPercentDifference(CryptoCoin coin){
        if(currentValues.containsKey(coin.getCoinID())){
            try {
                return ((currentValues.get(coin.getCoinID()) * coin.getAmount()) - coin.getTotalValue()) / coin.getTotalValue();      // ((currentPrice * totalAmountOfCoins) - InitialTotal) / InitialTotal      to get the percent difference
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        return 0;
    }

    //setCurrentPrices
    //gets the prices of all coins in the database
    public void setCurrentPrices() {
        ArrayList<String> coinIDNames = dbManager.selectAllCoinIDUnique();

        String allCoinIDNames = "";
        for (String coinID : coinIDNames) {     //for each coinId returned by the database
            allCoinIDNames = allCoinIDNames.concat(coinID);     //concat the coinID onto the string
            if(coinIDNames.indexOf(coinID) != coinIDNames.size()-1) //if its not the end
            allCoinIDNames = allCoinIDNames.concat(",");        //add a comma to format the string into one url call
        }
        //Create the url and send the task
        URL url = createURL(allCoinIDNames);
        GetCoinTask getLocalCoinTask = new GetCoinTask();
        getLocalCoinTask.execute(url);
    }

    //Create a url with the passed in string
    private URL createURL(String coinID) {
        try {
            String getPriceUrl = "https://api.coingecko.com/api/v3/simple/price?ids="+ URLEncoder.encode(coinID, "UTF-8") + "&vs_currencies=usd";
            return new URL(getPriceUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


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

        //Process json response and update ListView
        @Override
        protected void onPostExecute(JSONObject coin) {

            populateCurrentCoinValue(coin);
            setTotal();
        }

        //populateCurrentCoinValue
        //takes the JSONObject and puts it into the hashman
        private void populateCurrentCoinValue(JSONObject coin) {

            try {
                String currentName;
                Iterator<String> iter = coin.keys();
                while(iter.hasNext()) {
                    currentName = iter.next();           //This gets the name of the first title in the json, since it could be any of the coin names
                    JSONObject price = coin.getJSONObject(currentName);
                    currentValues.put(currentName, price.getDouble("usd"));
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }
}
