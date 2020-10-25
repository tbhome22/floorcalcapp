package com.example.floorboardcalculator.ui.addon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.floorboardcalculator.R;

import org.jetbrains.annotations.NotNull;

public class InternetDialog extends AlertDialog.Builder{
    private TextView mText;
    private ImageView mInetOk;
    private ImageView mInetNoIP;
    private ImageView mInetNoConn;
    private InternetStatus currentStatus;
    private Context context;

    public InternetDialog(@NonNull Context context) {
        super(context);

        this.context = context;
    }

    public void setStatus(@NotNull InternetStatus status) {
        setStatus(status, 0);
    }

    public void setStatus(@NotNull InternetStatus status, int counter) {
        this.currentStatus = status;

        switch(status) {
            case INTERNET_OK:
                if(mInetOk.getVisibility() == View.GONE)
                    mInetOk.setVisibility(View.VISIBLE);

                if(mInetNoIP.getVisibility() == View.VISIBLE)
                    mInetNoIP.setVisibility(View.GONE);

                if(mInetNoConn.getVisibility() == View.VISIBLE)
                    mInetNoConn.setVisibility(View.GONE);

                mText.setText("Internet Ok!");
                break;

            case INTERNET_NOCONNECTED:
                if(mInetNoConn.getVisibility() == View.GONE)
                    mInetNoConn.setVisibility(View.VISIBLE);

                if(mInetOk.getVisibility() == View.VISIBLE)
                    mInetOk.setVisibility(View.GONE);

                if(mInetNoIP.getVisibility() == View.VISIBLE)
                    mInetNoIP.setVisibility(View.GONE);

                mText.setText("Not Connected! (Application exit automatically after " + counter + " seconds)");
                break;

            case INTERNET_NOIP:
                if(mInetNoIP.getVisibility() == View.GONE)
                    mInetNoIP.setVisibility(View.VISIBLE);

                if(mInetOk.getVisibility() == View.VISIBLE)
                    mInetOk.setVisibility(View.GONE);

                if(mInetNoConn.getVisibility() == View.VISIBLE)
                    mInetNoConn.setVisibility(View.GONE);

                mText.setText("No Internet Access! (Application exit automatically after " + counter + " seconds)");
                break;
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public AlertDialog show() {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_networkcheck, null);
        setView(v);
        setCancelable(false);

        mText = (TextView) v.findViewById(R.id.wifi_text);
        mInetOk = (ImageView) v.findViewById(R.id.wifi_ok);
        mInetNoConn = (ImageView) v.findViewById(R.id.wifi_problem);
        mInetNoIP = (ImageView) v.findViewById(R.id.wifi_noip);

        if(currentStatus == null)
            setStatus(InternetStatus.INTERNET_OK);

        return super.show();
    }
}
