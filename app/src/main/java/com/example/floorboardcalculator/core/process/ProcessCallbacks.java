package com.example.floorboardcalculator.core.process;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.floorboardcalculator.LoadingActivity;
import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.ui.addon.InternetDialog;
import com.example.floorboardcalculator.ui.addon.InternetStatus;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessCallbacks implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2, Runnable {
    private static final String TAG = ProcessCallbacks.class.getSimpleName();
    private static boolean isInBackground = false;
    private CoreProcessor currentProcessor;
    private AlertDialog dialog = null;
    private InternetDialog inetDialog = null;

    private List<Activity> activeActivities;
    private List<String> activeName;

    private final ScheduledExecutorService statusService;

    private int noConnTimer = 0;

    public ProcessCallbacks(CoreProcessor currentProcessor) {
        activeActivities = new ArrayList<Activity>();
        activeName = new ArrayList<String>();
        this.currentProcessor = currentProcessor;

        statusService = Executors.newSingleThreadScheduledExecutor();
        statusService.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPrePaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityPostSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        Log.d(TAG, "Acitvity Created: " + activity.getLocalClassName());

        if(activity.getClass() == LoadingActivity.class) {
            currentProcessor.initializeRealm();
        }
        else {
            activeName.add(activity.getLocalClassName());
            activeActivities.add(activity);
        }

        Log.i(TAG, "Current Active Activities: " + activeActivities.size());
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.d(TAG, "Activity Started: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if(isInBackground) {
            Log.d(TAG, "App comes to foreground!");
            isInBackground = false;

            Log.d(TAG, "Activity Resumed: " + activity.getLocalClassName());
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log.d(TAG, "Activity Paused: " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log.d(TAG, "Acitvity Stopped: " + activity.getLocalClassName());
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log.d(TAG, "Acitvity Destroyed: " + activity);

        if(activity.getClass() != LoadingActivity.class) {
            int i = activeName.indexOf(activity.getLocalClassName());

            activeName.remove(i);
            activeActivities.remove(i);

            if(activeActivities.size() == 0) {
                currentProcessor.logoutRealm();
                statusService.shutdown();
                System.exit(0);
            }
        }

        Log.i(TAG, "Current Active Activities: " + activeActivities.size());
    }

    @Override
    public void onTrimMemory(int i) {
        if(i == TRIM_MEMORY_UI_HIDDEN) {
            Log.d(TAG, "App goes to background, system exit call!");
            isInBackground = true;


            if(activeActivities.size() > 0) {
                activeActivities.get(activeActivities.size() - 1).finishAffinity();
            }

            currentProcessor.logoutRealm();
            statusService.shutdown();
            System.exit(0);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration configuration) {

    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public void run() {
        if(activeActivities.size() > 0) {
            Activity latest = activeActivities.get(activeActivities.size() - 1);

            if(latest instanceof ControlActivity) {
                ControlActivity current = (ControlActivity) latest;

                if(isNetworkAvailable(current)) {
                    if(isInternetAvailable()) {
                        current.tickConnectionStatus(InternetStatus.INTERNET_OK, 0);
                        noConnTimer = 0;
                    }
                    else {
                        if(noConnTimer >= 30) {
                            if(activeActivities.size() > 0) {
                                activeActivities.get(activeActivities.size() - 1).finishAffinity();
                            }

                            currentProcessor.logoutRealm();
                            statusService.shutdown();
                            System.exit(0);
                        }
                        else {
                            current.tickConnectionStatus(InternetStatus.INTERNET_NOIP, (30 - noConnTimer));
                            noConnTimer++;
                        }
                    }
                }
                else {
                    if(noConnTimer >= 30) {
                        if(activeActivities.size() > 0) {
                            activeActivities.get(activeActivities.size() - 1).finishAffinity();
                        }

                        currentProcessor.logoutRealm();
                        statusService.shutdown();
                        System.exit(0);
                    }
                    else {
                        current.tickConnectionStatus(InternetStatus.INTERNET_NOCONNECTED, (30 - noConnTimer));
                        noConnTimer++;
                    }
                }
            }
        }
    }

    private boolean isNetworkAvailable(@NotNull Context context) {
        ConnectivityManager manager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));

        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnected();
    }

    private boolean isInternetAvailable() {
        try{
            InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        }
        catch(UnknownHostException e) { }

        return false;
    }
}
