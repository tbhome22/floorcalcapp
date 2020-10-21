package com.example.floorboardcalculator.ui.pdf;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrintProcessAdapter extends PrintDocumentAdapter {
    private Context context;
    private String path;

    public PrintProcessAdapter(Context context, @NonNull String path) {
        this.context = context;
        this.path = path;
    }

    @Override
    public void onFinish() {
        super.onFinish();
    }

    @Override
    public void onLayout(PrintAttributes printAttributes,
                         PrintAttributes printAttributes1,
                         CancellationSignal cancellationSignal,
                         LayoutResultCallback layoutResultCallback,
                         Bundle bundle) {

        if(cancellationSignal.isCanceled()) {
            layoutResultCallback.onLayoutCancelled();
            return;
        }

        DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.UK);
        String jobName = "checklist_" + format.format(new Date());

        PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder(jobName);
        builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build();

        layoutResultCallback.onLayoutFinished(builder.build(),
                !printAttributes1.equals(printAttributes));
    }

    @Override
    public void onWrite(PageRange[] pageRanges,
                        ParcelFileDescriptor parcelFileDescriptor,
                        CancellationSignal cancellationSignal,
                        WriteResultCallback writeResultCallback) {
        InputStream in = null;
        OutputStream out = null;

        try {
            File file = new File(path);
            in = new FileInputStream(file);
            out = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

            byte[] buf = new byte[16384];
            int size;

            while((size = in.read(buf)) >= 0 && !cancellationSignal.isCanceled()) {
                out.write(buf, 0, size);
            }

            if(cancellationSignal.isCanceled())
                writeResultCallback.onWriteCancelled();
            else
                writeResultCallback.onWriteFinished(new PageRange[]{ PageRange.ALL_PAGES });
        }
        catch(Exception e) {
            writeResultCallback.onWriteFailed(e.getMessage());
            Log.e("ERROR", e.getMessage());
        }
        finally {
            try{
                in.close();
                out.close();
            }
            catch(IOException e) {
                Log.e("Error", e.getMessage());
            }
        }
    }
}