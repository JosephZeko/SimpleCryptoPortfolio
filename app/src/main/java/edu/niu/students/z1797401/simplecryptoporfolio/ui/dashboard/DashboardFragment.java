/***********************************************************************
 *     Class Name: DashBoardFragment
 *
 *   Purpose: creates and manages the DashBoard Fragment
 *            The dashboard fragment displays all current positions
 *            and total of account
 *
 *
 *
 ************************************************************************/
package edu.niu.students.z1797401.simplecryptoporfolio.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import edu.niu.students.z1797401.simplecryptoporfolio.CryptoOverViewActivity;
import edu.niu.students.z1797401.simplecryptoporfolio.MainActivity;
import edu.niu.students.z1797401.simplecryptoporfolio.R;

import static edu.niu.students.z1797401.simplecryptoporfolio.MainActivity.currentPortfolio;
import static edu.niu.students.z1797401.simplecryptoporfolio.MainActivity.dbManager;

public class DashboardFragment extends Fragment {


    public static PortfolioArrayAdapter portfolioArrayAdapter;         //Array adapter for handling the current positions
    public  ListView portfolioListView;                                //List view for the array adapter
    public static List<String> portfolioList;                          //List of strings for the coin names

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);


        portfolioList = dbManager.selectAllCoinIDUnique();            //initialize the list ot all the unique coin id names


        //Initialize and set the adapter
        portfolioListView = (ListView) root.findViewById(R.id.porfolioListView);
        portfolioArrayAdapter = new PortfolioArrayAdapter(this.getContext(), portfolioList);
        portfolioListView.setAdapter(portfolioArrayAdapter);


        //Create on item click listener for the list view
        portfolioListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent(getContext(), CryptoOverViewActivity.class);        //Create a new intent
            intent.putExtra("id", portfolioList.get(position));                     //Put the id of the selected coin
            startActivity(intent);                                                         //start the activity

            }

        });


        //Create the NumberFormats and Decimal Formats for displaying the numbers
        final NumberFormat numberFormatCurrency = NumberFormat.getCurrencyInstance();
        numberFormatCurrency.setMaximumFractionDigits(2);
        final DecimalFormat numberFormatDifference = new DecimalFormat("+$#,##0.00;-$#");   //Display leading sign for the difference
        final NumberFormat numberFormatPercent = NumberFormat.getPercentInstance();
        numberFormatPercent.setMaximumFractionDigits(2);

        //Create the textveiws for total and total change
        final TextView porfoliototaltextView = root.findViewById(R.id.porfoliototaltextView);
        final TextView porfolioTotalChange = root.findViewById(R.id.porfoliototalChange);

        //Whenever the total Account value changes: do this
        MainActivity.currentPortfolio.getTotalAccountValue().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                //Set the total and total difference
                String temp = numberFormatCurrency.format(aDouble);
                porfoliototaltextView.setText(temp);
                temp =numberFormatDifference.format(currentPortfolio.getTotalDifference().getValue()) + " (" +  numberFormatPercent.format(currentPortfolio.getPercentChange().getValue()) + ")";
                porfolioTotalChange.setText(temp);

                //Set the color to green or red depending on price
                if(currentPortfolio.getTotalDifference().getValue() >= 0)
                    porfolioTotalChange.setTextColor(getContext().getResources().getColor(R.color.green));
                else
                    porfolioTotalChange.setTextColor(getContext().getResources().getColor(R.color.red));


                DashboardFragment.portfolioArrayAdapter.notifyDataSetChanged();
            }
        });


        //Starting Insert Fragment here
        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(DashboardFragment.this)
                        .navigate(R.id.navigation_dashboard_to_insertFragment);
            }}


        );
        return root;
    }

    //setPortfolioList()
    //updates the the portfolio list
    //Called outside this fragment to update just incase this fragment is resumed instead of re-created

    public static void setPortfolioList() {
          portfolioList = dbManager.selectAllCoinIDUnique();
        try {
            DashboardFragment.portfolioArrayAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



}
