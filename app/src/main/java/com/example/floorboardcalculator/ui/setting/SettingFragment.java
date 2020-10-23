package com.example.floorboardcalculator.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.floorboardcalculator.BuildConfig;
import com.example.floorboardcalculator.R;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.core.auth.providers.userapikey.UserApiKeyCredential;

import org.jetbrains.annotations.NotNull;

public class SettingFragment extends Fragment {
    private static final String APP_PREF = "TWO_BROTHER_SETTING";
    private static final String TAG = SettingFragment.class.getSimpleName();

    private TextView mAppVersion, mBuildNumber, mDbStats, mAppOwner;
    private SwitchCompat mUnits, mPriceExports, mDoubles;
    private ImageButton mReconnect;
    private Spinner mSiSelect;

    private DbStats currentStats;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settingpage, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        mAppVersion = (TextView) view.findViewById(R.id.txt_version);
        mBuildNumber = (TextView) view.findViewById(R.id.txt_build);
        mDbStats = (TextView) view.findViewById(R.id.txt_dbStats);
        mAppOwner = (TextView) view.findViewById(R.id.txt_appOwner);

        mUnits = (SwitchCompat) view.findViewById(R.id.setting_units);
        mPriceExports = (SwitchCompat) view.findViewById(R.id.setting_priceExport);
        mDoubles = (SwitchCompat) view.findViewById(R.id.setting_doubleUnits);

        mSiSelect = (Spinner) view.findViewById(R.id.si_selection);

        mReconnect = (ImageButton) view.findViewById(R.id.btn_reconnect);

        mAppVersion.setText(BuildConfig.VERSION_NAME);
        mBuildNumber.setText(BuildConfig.VERSION_CODE + " (Stage: " + BuildConfig.BUILD_TYPE + ")");

        mAppOwner.setText(getString(R.string.company_2));

        initSettings();
        initConnection();

        mDoubles.setOnCheckedChangeListener(this::doublesCheckedChangeResponse);
        mPriceExports.setOnCheckedChangeListener(this::priceExpCheckedChangeResponse);
        mUnits.setOnCheckedChangeListener(this::unitCheckedChangeResponse);
        mSiSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                siSelectChangeResponse(adapterView, view, i, l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        mReconnect.setOnClickListener(c -> reconnect(c));
    }

    private void doublesCheckedChangeResponse(CompoundButton compoundButton, boolean current) {
        if(current) {
            mUnits.setEnabled(false);
            mSiSelect.setEnabled(false);
        }
        else {
            mUnits.setEnabled(true);
            mSiSelect.setEnabled(true);
        }

        UpdateSetting(UpdateMode.MODE_DOUBLE, current, -1);
    }

    private void priceExpCheckedChangeResponse(CompoundButton compoundButton, boolean current) {
        UpdateSetting(UpdateMode.MODE_PRICEXP, current, -1);
    }

    private void unitCheckedChangeResponse(CompoundButton compoundButton, boolean current) {
        UpdateSetting(UpdateMode.MODE_UNIT, current, -1);

        ArrayAdapter<CharSequence> adpt;
        if(current)
            adpt = ArrayAdapter.createFromResource(getContext(), R.array.si_metric, R.layout.support_simple_spinner_dropdown_item);
        else
            adpt = ArrayAdapter.createFromResource(getContext(), R.array.si_imperial, R.layout.support_simple_spinner_dropdown_item);

        adpt.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSiSelect.setAdapter(adpt);
        mSiSelect.setSelection(0);
        UpdateSetting(UpdateMode.MODE_SI, current, 0);
    }

    private void siSelectChangeResponse(AdapterView av, View v, int i, long l){
        UpdateSetting(UpdateMode.MODE_SI, false, i);
    }

    private void initSettings() {
        SharedPreferences preferences = getContext().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        mUnits.setChecked(preferences.getBoolean("unit", false));
        mPriceExports.setChecked(preferences.getBoolean("priceExp", false));
        mDoubles.setChecked(preferences.getBoolean("doubleUnit", false));
        mUnits.setEnabled(!preferences.getBoolean("doubleUnit", false));

        ArrayAdapter<CharSequence> adpt;

        if(preferences.getBoolean("unit", false)) {
            adpt = ArrayAdapter.createFromResource(getContext(), R.array.si_metric, R.layout.support_simple_spinner_dropdown_item);
        }
        else{
            adpt = ArrayAdapter.createFromResource(getContext(), R.array.si_imperial, R.layout.support_simple_spinner_dropdown_item);
        }

        adpt.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSiSelect.setAdapter(adpt);
        mSiSelect.setSelection(preferences.getInt("unitSelect", 0));
        mSiSelect.setEnabled(!preferences.getBoolean("doubleUnit", false));
    }

    private void UpdateSetting(@NotNull UpdateMode mode, boolean current, int position) {
        SharedPreferences preferences = getContext().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        switch (mode) {
            case MODE_UNIT:
                editor.putBoolean("unit", current);
                break;

            case MODE_DOUBLE:
                editor.putBoolean("doubleUnit", current);
                break;

            case MODE_PRICEXP:
                editor.putBoolean("priceExp", current);
                break;

            case MODE_SI:
                editor.putInt("unitSelect", position);
                break;
        }

        editor.apply();
    }

    private void initConnection() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        boolean isConnected = info != null && info.isConnectedOrConnecting();

        if(isConnected) {
            if(!Stitch.hasAppClient(getString(R.string.db_name))){
                mDbStats.setText("Not configured");
                currentStats = DbStats.NO_CONF;
            }
            else {
                StitchAppClient client = Stitch.getDefaultAppClient();

                if(client.getAuth().isLoggedIn()){
                    mDbStats.setText("Connected");
                    currentStats = DbStats.OK;
                }
                else{
                    mDbStats.setText("Authentication Failed");
                    currentStats = DbStats.AUTH_FAIL;
                }
            }
        }
        else {
            mDbStats.setText("No Internet Connection");
            currentStats = DbStats.NO_LINE;
        }
    }

    private void reconnect(View v) {
        initConnection();

        switch(currentStats) {
            case NO_LINE:
                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(i);
                break;

            case NO_CONF:
                if(!Stitch.hasAppClient(getString(R.string.db_name)))
                    Stitch.initializeDefaultAppClient(getString(R.string.db_name));

                initConnection();
                break;

            case AUTH_FAIL:
                UserApiKeyCredential credential = new UserApiKeyCredential(getString(R.string.db_user));

                Stitch.getDefaultAppClient().getAuth().loginWithCredential(credential)
                        .addOnSuccessListener(stitchUser -> {
                            Log.d(TAG, "Logged in to database server!");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error logging in to database server! Reason: " + e.getMessage());
                        });

                initConnection();
                break;

            case OK:
            default:
                break;
        }
    }

    private enum UpdateMode {
        MODE_UNIT,
        MODE_PRICEXP,
        MODE_DOUBLE,
        MODE_SI
    }

    private enum DbStats {
        NO_LINE,
        NO_CONF,
        AUTH_FAIL,
        OK
    }
}