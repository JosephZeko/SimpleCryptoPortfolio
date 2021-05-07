/***********************************************************************
 *     Class Name: InsertFragment.java
 *
 *   Purpose: creates and manages the insertFragment
 *             the insertFragment only shows a SearchView to search a coin
 *
 ************************************************************************/
package edu.niu.students.z1797401.simplecryptoporfolio.ui.insert;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import edu.niu.students.z1797401.simplecryptoporfolio.R;


public class insertFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.insert_layout, container, false);


        //Create a new searchManager
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        //Find the searchView
        SearchView searchView = (SearchView) root.findViewById(R.id.searchCoin);
        //Sets the reccomandations displayed to the read in data in searchProvider
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));


        return root;
    }

}

