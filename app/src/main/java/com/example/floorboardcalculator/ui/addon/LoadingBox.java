package com.example.floorboardcalculator.ui.addon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.floorboardcalculator.R;

import org.jetbrains.annotations.NotNull;

public class LoadingBox extends AlertDialog.Builder {
    private String loadingMsg = "loading...";
    private TextView mLoadingText;
    private Context context;
    private AlertDialog mAlertDialog;
    private ImageView mDoneImg, mFailImg;
    private ProgressBar mProgress;

    public LoadingBox(@NonNull Context context) {
        super(context);

        this.context = context;
    }

    public void setMessage(@NonNull String message) {
        mLoadingText.setText(message);
    }

    @SuppressLint("InflateParams")
    @Override
    public AlertDialog show() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        setView(view);

        mLoadingText = (TextView) view.findViewById(R.id.loading_text);
        mDoneImg = (ImageView) view.findViewById(R.id.loading_done);
        mFailImg = (ImageView) view.findViewById(R.id.loading_fail);
        mProgress = (ProgressBar) view.findViewById(R.id.loading_inProg);

        mLoadingText.setText(loadingMsg);
        setCancelable(false);

        mAlertDialog = super.show();

        return mAlertDialog;
    }

    public void setLoadingType(@NotNull MessageType type) {
        switch (type) {
            case IN_PROGRESS:
                mProgress.setVisibility(View.VISIBLE);
                mDoneImg.setVisibility(View.GONE);
                mFailImg.setVisibility(View.GONE);
                break;

            case FAILED:
                mProgress.setVisibility(View.GONE);
                mDoneImg.setVisibility(View.GONE);
                mFailImg.setVisibility(View.VISIBLE);
                break;

            case SUCCESS:
                mProgress.setVisibility(View.GONE);
                mDoneImg.setVisibility(View.VISIBLE);
                mFailImg.setVisibility(View.GONE);
                break;
        }
    }

    public static enum MessageType {
        IN_PROGRESS,
        SUCCESS,
        FAILED
    }
}
