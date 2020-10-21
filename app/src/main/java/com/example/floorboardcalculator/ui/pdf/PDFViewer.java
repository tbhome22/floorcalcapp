package com.example.floorboardcalculator.ui.pdf;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.floorboardcalculator.R;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PDFViewer extends AppCompatActivity {
    private PDFView viewer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewreport);

        Intent data = getIntent();
        String url = data.getStringExtra("path_str");
        String fileName = data.getStringExtra("file_name");

        setTitle(fileName);

        if(url.length() > 0 && fileName.length() > 0) {
            viewer = (PDFView) findViewById(R.id.pdf_viewer);
            File file = new File(fileName);

            viewer.fromUri(Uri.fromFile(file)).load();
        }
        else {
            this.finish();
        }
    }
}
