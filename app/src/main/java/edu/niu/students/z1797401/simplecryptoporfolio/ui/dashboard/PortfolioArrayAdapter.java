/***********************************************************************
 *     Class Name: PortfolioArrayAdapter
 *
 *   Purpose: an array adapter for displaying the current positions for each coin
 *
 *
 *
 ************************************************************************/

package edu.niu.students.z1797401.simplecryptoporfolio.ui.dashboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.niu.students.z1797401.simplecryptoporfolio.MainActivity;
import edu.niu.students.z1797401.simplecryptoporfolio.R;

public class PortfolioArrayAdapter extends ArrayAdapter<String> {

    private Map<String, Bitmap> bitmaps = new HashMap<>();
    public PortfolioArrayAdapter(Context context, List<String> coins){
        super(context,-1,coins);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String coin = getItem(position);
        ViewHolder viewHolder;



        if (convertView == null)   // no reusable ViewHolder, so create one
        {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());

            convertView =
                    inflater.inflate(R.layout.list_item, parent, false);


            viewHolder.conditionImageView =
                    (ImageView) convertView.findViewById(R.id.conditionImageView);
            viewHolder.coinNameTextView =
                    (TextView) convertView.findViewById(R.id.coinNameTextView);
            viewHolder.coinAmountTextView =
                    (TextView) convertView.findViewById(R.id.coinAmountTextView);
            viewHolder.totalTextView =
                    (TextView) convertView.findViewById(R.id.totalTextView);
            viewHolder.percentDiffPriceTextView =
                    (TextView) convertView.findViewById(R.id.percentDiffPriceTextView);

            convertView.setTag(viewHolder);
        } else
        {
            // reuse existing ViewHolder stored as the list item's tag
            viewHolder = (ViewHolder) convertView.getTag();
        }


        if (bitmaps.containsKey(MainActivity.currentPortfolio.getIconURLFromID(coin))) {
            viewHolder.conditionImageView.setImageBitmap(
                    bitmaps.get(MainActivity.currentPortfolio.getIconURLFromID(coin)));
        }
        else {
            new LoadImageTask(viewHolder.conditionImageView).execute(MainActivity.currentPortfolio.getIconURLFromID(coin)); //Load the image by getting the url from the coin name

        }

        Context context = getContext();  // for loading String resources


        //Create the various number formats
        final DecimalFormat numberFormatDifference = new DecimalFormat("+$#,##0.00;-$#");
        final NumberFormat numberFormatPercent = NumberFormat.getPercentInstance();
        final NumberFormat numberFormatCurrency = NumberFormat.getCurrencyInstance();
        final DecimalFormat numberFormatCoin = new DecimalFormat("#,##0.00");
        numberFormatPercent.setMaximumFractionDigits(2);


        //Grab all the values being displayed in the list
        double total = MainActivity.currentPortfolio.getCoinCurrentTotalValue(coin);
        double coinTotalAmount = MainActivity.currentPortfolio.getTotalCoinAmount(coin);
        double coinCurrentPrice = MainActivity.currentPortfolio.getCurrentCoinPrice(coin);
        double coinDifference = MainActivity.currentPortfolio.getCoinDifference(coin);
        double coinPercentDiff = MainActivity.currentPortfolio.getCoinPercentDifference(coin);





        //Sets the name
        viewHolder.coinNameTextView.setText(MainActivity.currentPortfolio.getCoinNameFromID(coin));

        //Sets the Total for this specific coin
        viewHolder.totalTextView.setText(numberFormatCurrency.format(total));

        //Sets the amount of coins | the current price
        viewHolder.coinAmountTextView.setText(numberFormatCoin.format(coinTotalAmount) + " | " + numberFormatCurrency.format(coinCurrentPrice));

        //Changes the color based on if its positive or negative
        if(coinDifference >= 0)
            viewHolder.percentDiffPriceTextView.setTextColor( context.getResources().getColor(R.color.green));
        else
            viewHolder.percentDiffPriceTextView.setTextColor( context.getResources().getColor(R.color.red));
        //SEts the difference (percentDifference)
        viewHolder.percentDiffPriceTextView.setText(numberFormatDifference.format(coinDifference) + " (" + numberFormatPercent.format(coinPercentDiff) + ")");




        return convertView;  // return completed list item to display
    }




    //Loads the image
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
                    bitmaps.put(params[0], bitmap);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
            finally
            {
                connection.disconnect();  // close the HttpURLConnection
            }

            return bitmap;
        }

        // set coin image in List item
        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            imageView.setImageBitmap(bitmap);
        }
    }

private static class ViewHolder
{
    ImageView conditionImageView;
    TextView coinNameTextView;
    TextView coinAmountTextView;
    TextView totalTextView;
    TextView percentDiffPriceTextView;
}



}
