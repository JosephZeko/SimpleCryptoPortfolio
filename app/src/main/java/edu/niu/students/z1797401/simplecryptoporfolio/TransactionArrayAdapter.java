/***********************************************************************
 *     Class Name: TransactionArrayAdapter.java
 *
 *   Purpose: An arrayAdapter for the individual entries for a specific coin
 *
 *
 *
 ************************************************************************/
package edu.niu.students.z1797401.simplecryptoporfolio;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;



public class TransactionArrayAdapter extends ArrayAdapter<CryptoCoin> {
    public TransactionArrayAdapter(Context context, List<CryptoCoin> coins){
        super(context,-1,coins);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CryptoCoin coin = getItem(position);
        ViewHolder viewHolder;



        if (convertView == null)   // no reusable ViewHolder, so create one
        {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());

            convertView =
                    inflater.inflate(R.layout.transaction_listitem, parent, false);

            viewHolder.deleteButton =
                    (Button) convertView.findViewById(R.id.deleteButton);
            viewHolder.buyPriceTextView =
                    (TextView) convertView.findViewById(R.id.buyPriceTextView);
            viewHolder.totalCostTextView =
                    (TextView) convertView.findViewById(R.id.totalCostTextView);
            viewHolder.coinAmountBoughtTextView =
                    (TextView) convertView.findViewById(R.id.coinAmountBoughtTextView);
            viewHolder.amountWorthPriceTextView =
                    (TextView) convertView.findViewById(R.id.amountWorthPriceTextView);

            convertView.setTag(viewHolder);
        } else
        {
            // reuse existing ViewHolder stored as the list item's tag
            viewHolder = (ViewHolder) convertView.getTag();
        }



        Context context = getContext();

        //Create numberformats for the numbers
        final DecimalFormat numberFormatDifference = new DecimalFormat("+$#,##0.00;-$#");
        final NumberFormat numberFormatPercent = NumberFormat.getPercentInstance();
        final NumberFormat numberFormatCurrency = NumberFormat.getCurrencyInstance();
        final DecimalFormat numberFormatCoin = new DecimalFormat("#,##0.00");
        numberFormatPercent.setMaximumFractionDigits(2);

        double coinAmountWorth = MainActivity.currentPortfolio.getTransactionDifference(coin);


        //Set all the text views to the correct numbers
        viewHolder.buyPriceTextView.setText("Buying Price\n" + numberFormatCurrency.format(coin.getPrice()));
        viewHolder.totalCostTextView.setText("Total Cost\n" + numberFormatCurrency.format(coin.getTotalValue()));
        viewHolder.coinAmountBoughtTextView.setText("Amount Bought\n" +numberFormatCoin.format(coin.getAmount()) + " " + coin.getSymbol());

        //If positive change to green, if negative change to red
        if(coinAmountWorth >= 0){
            viewHolder.amountWorthPriceTextView.setTextColor( context.getResources().getColor(R.color.green));
        }
        else{
            viewHolder.amountWorthPriceTextView.setTextColor( context.getResources().getColor(R.color.red));
        }
        viewHolder.amountWorthPriceTextView.setText("Profit \n" + numberFormatDifference.format(coinAmountWorth) + " (" + numberFormatPercent.format(MainActivity.currentPortfolio.getTransactionPercentDifference(coin)) + ")");




        return convertView;  // return completed list item to display
    }


    private static class ViewHolder
    {
        Button deleteButton;
        TextView buyPriceTextView;
        TextView totalCostTextView;
        TextView coinAmountBoughtTextView;
        TextView amountWorthPriceTextView;
    }



}
