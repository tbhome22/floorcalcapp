package com.example.floorboardcalculator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.floorboardcalculator.core.database.MainProcessApp;
import com.example.floorboardcalculator.ui.lists.ListDataFragment;
import com.example.floorboardcalculator.ui.rate.RateFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1001;
    private static final int STORAGE_CODE = 1000;

    private MainProcessApp dbApp;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private NavHostFragment hostFragment;
    private boolean locationPermissionGranted = false, storagePermissionGranted = false;

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

        dbApp = (MainProcessApp) getApplication();
        if(savedInstanceState == null){
            dbApp.incrementAcitvityCounter();
        }
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            dbApp.decrementActivityCounter();
        }

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("activityCounter", ((MainProcessApp) getApplication()).getActivityCounter());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int activityCounter = savedInstanceState.getInt("activityCounter");
        ((MainProcessApp) getApplication()).setActivityCounter(activityCounter);
        Log.d(TAG, "Reset activity counter in application after process death to [" + activityCounter + "]");
    }

    @Override
    protected void onStart() {
        super.onStart();

        getPermission();
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
        else {
            super.onBackPressed();
        }
    }

    private void getPermission() {
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            Log.d("Permission", "Location permission granted");
        }
        else {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            storagePermissionGranted = true;
            Log.d("Permission", "Storage permission granted");
        }
        else {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, STORAGE_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResult) {
        locationPermissionGranted = false;

        switch(requestCode) {
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                if(grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED){
                    locationPermissionGranted = true;
                    Log.d("Permission", "Location permission granted");
                }
                else {
                    Log.e("Permission", "Location permission denied");
                }
            }
            break;

            case STORAGE_CODE: {
                if(grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED){
                    storagePermissionGranted = true;
                    Log.d("Permission", "Storage permission granted");
                }
                else {
                    Log.e("Permission", "Storage permission denied");
                }
            }
            break;
        }
    }
}