package com.example.floorboardcalculator;

import android.os.Bundle;
import android.widget.Toast;

import com.example.floorboardcalculator.ui.lists.ListDataFragment;
import com.example.floorboardcalculator.ui.mainpg.HomeFragment;
import com.example.floorboardcalculator.ui.rate.RateFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_mainpg, R.id.navigation_listpg, R.id.navigation_ratepg ,R.id.navigation_addpg)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        Fragment navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);

        if(fragment instanceof ListDataFragment) {
            ListDataFragment frag = (ListDataFragment) fragment;

            if(!frag.allowBackPressed())
                Toast.makeText(this, "Please wait for list loading!", Toast.LENGTH_SHORT).show();
            else
                super.onBackPressed();
        }
        else if(fragment instanceof RateFragment) {
            RateFragment frag = (RateFragment) fragment;

            if(!frag.allowBackPressed())
                Toast.makeText(this, "Please wait for process in operation!", Toast.LENGTH_SHORT).show();
            else
                super.onBackPressed();
        }
        else if(fragment instanceof HomeFragment) {
            HomeFragment frag = (HomeFragment) fragment;

            if(!frag.allowBackPressed())
                Toast.makeText(this, "Please wait for process in operation!", Toast.LENGTH_SHORT).show();
            else
                super.onBackPressed();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}