/*///////////////////////////////////////////////////////////////////////
 *
 *   Purpose: This purpose of this program is serve as the main driver
 *   for the crypto tracking app. it maintains 2 global static objects
 *   dbManager: Handling all database operations
 *   currentPortfolio: The portfolio for the account, managing the total
 *
 *
 ************************************************************************/
package edu.niu.students.z1797401.simplecryptoporfolio;



import android.os.Bundle;
import android.view.View;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;



public class MainActivity extends AppCompatActivity {
    private static View constraintLayout;  //This is for calling snack bar from a static method
    public static DatabaseManager dbManager;   //Handling database operation
    public static Portfolio currentPortfolio;   //handling all portfolio operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);






        constraintLayout = findViewById(R.id.container);
        dbManager = new DatabaseManager(this);                                               //Initialize database
        currentPortfolio = new Portfolio();                                                          //Initialize portfolio




    }
    //makeSnackbar
    //Creates a snack bar anchored to the top of the navigation view
    //String error is the text shown by the snackbar
    public static void makeSnackbar(String error) {
        Snackbar snack = Snackbar.make(constraintLayout, error, Snackbar.LENGTH_LONG); //Creates a snack bar with the message passed in
        snack.setAnchorView(constraintLayout.findViewById(R.id.nav_view));            //anchors the snack bar to the top of the navigation view (bottom bar)
        snack.show();                                                                 //displays the snac kbar
    }



}