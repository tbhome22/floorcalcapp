package com.example.floorboardcalculator.core.core;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.floorboardcalculator.LoadingActivity;
import com.example.floorboardcalculator.R;

import java.util.ArrayList;
import java.util.List;

public class ProcessCallbacks implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    private static final String TAG = ProcessCallbacks.class.getSimpleName();
    private static boolean isInBackground = false;
    private CoreProcessor currentProcessor;

    private List<Activity> activeActivities;
    private List<String> activeName;

    public ProcessCallbacks(CoreProcessor currentProcessor) {
        activeActivities = new ArrayList<Activity>();
        activeName = new ArrayList<String>();
        this.currentProcessor = currentProcessor;
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
                Runtime.getRuntime().exit(1);
            }
        }

        Log.i(TAG, "Current Active Activities: " + activeActivities.size());
    }

    @Override
    public void onTrimMemory(int i) {
        if(i == TRIM_MEMORY_UI_HIDDEN) {
            Log.d(TAG, "App goes to background, system exit call!");
            isInBackground = true;

            currentProcessor.logoutRealm();
            Runtime.getRuntime().exit(1);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration configuration) {

    }

    @Override
    public void onLowMemory() {

    }
}
