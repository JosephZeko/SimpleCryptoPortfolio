/***********************************************************************
 *     Class Name: SearchableActivity
 *
 *   Purpose: The searchable activity receives the uri from the search bar
 *            and handles displaying the data.
 *            It also handles inserting coins into the database
 *
 *
 ************************************************************************/
package edu.niu.students.z1797401.simplecryptoporfolio;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


import static edu.niu.students.z1797401.simplecryptoporfolio.MainActivity.dbManager;


public class SearchableActivity extends AppCompatActivity {

    TextView titleText;
    EditText buyingPrice;
    EditText coinAmountTV;
    Button submitButton;
    String name;
    Uri data;
    String[] values;
    String pictureURL;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.searchable_activity);


        //Create the textviews
         titleText = (TextView) findViewById(R.id.titleView);
         buyingPrice = (EditText) findViewById(R.id.editTextBuyPrice);
         coinAmountTV = (EditText) findViewById(R.id.editTextCoinAmount);
         submitButton = (Button) findViewById(R.id.button);


        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        //This happens if you press enter on the search without selecting a recommendation
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            finish();             //Immediately send back to the search bar since this page should only be accessible by recommendation
        }
        //The user selected a recommendation
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            data = intent.getData();                                             //intent.getData() gets the id of the coin selected on the recommendation
            name = intent.getExtras().getString(SearchManager.EXTRA_DATA_KEY);   //Get the extra data sent from the search
            values =  name.split(",");                                    //the data was formated with commas for ease of database storage so it is split into a array
            titleText.setText(values[0]);                                        //the first value in the extra was the Name
            URL url = createURL(data.toString());                                // create a url with the passed in id
            GetCoinTask localGetCoinTask = new GetCoinTask();                     //create new coin task
            localGetCoinTask.execute(url);                                      //execute the coin task

        }

        //Submit button onClickListener
        //Gets the values entered in the edit text boxes and assigns them
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double price, coinAmount = 0;

                //Error Checking
                if(buyingPrice.getText().toString().matches("")){
                    Snackbar.make(view, "Buying Price is blank", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if(coinAmountTV.getText().toString().matches("")){
                    Snackbar.make(view, "Coin Amount is blank", Snackbar.LENGTH_LONG).show();
                    return;
                }
                price = Double.parseDouble(buyingPrice.getText().toString());
                if(price < 0){
                    Snackbar.make(view, "Amount Paid must be above 0", Snackbar.LENGTH_LONG).show();
                    return;
                }
                coinAmount =  Double.parseDouble(coinAmountTV.getText().toString());
                if(coinAmount <= 0){
                    Snackbar.make(view, "Coins Bought must be above 0", Snackbar.LENGTH_LONG).show();
                    return;
                }


                //Create a new cryptoCoin entry with all the correct data
                CryptoCoin newCoin = new CryptoCoin(0,data.toString(),values[0],values[1],pictureURL, coinAmount, price);
                //Insert
               dbManager.insert(newCoin);
                //end the activity
                finish();
            }});





    }


    //createURL
    //Creates a url for coin gecko api
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
    //background retrieval of json for the coin
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

        //Process json response and set values
        @Override
        protected void onPostExecute(JSONObject coin) {
            try {
                JSONObject temp;
                temp = coin.getJSONObject("image");
                pictureURL = temp.getString("large");                    //set the picture url for the insert
                ImageView coinIcon = (ImageView) findViewById(R.id.iconView);
                new LoadImageTask(coinIcon).execute(temp.getString("large")); //start new loadimagetask to load the image url for this page

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //LoadImageTask
    //Background loading of image
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

        // set cryptocoin image on the page
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            imageView.setImageBitmap(bitmap);
        }
    }


}
