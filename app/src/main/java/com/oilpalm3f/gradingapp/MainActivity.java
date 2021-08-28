package com.oilpalm3f.gradingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.oilpalm3f.gradingapp.cloudhelper.Log;
import com.oilpalm3f.gradingapp.ui.GradingReportActivity;
import com.oilpalm3f.gradingapp.ui.QRScanActivity;
import com.oilpalm3f.gradingapp.ui.RefreshSyncActivity;

public class MainActivity extends AppCompatActivity {

    ImageView scanImg, reportsImg;
    LinearLayout synclyt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle("Home Screen");
//        setSupportActionBar(toolbar);

        synclyt = findViewById(R.id.synclyt);
        scanImg = findViewById(R.id.scanImg);
        reportsImg = findViewById(R.id.reportsImg);

        synclyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent syncintent = new Intent(MainActivity.this, RefreshSyncActivity.class);
                startActivity(syncintent);
            }
        });

        reportsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent syncintent = new Intent(MainActivity.this, GradingReportActivity.class);
                startActivity(syncintent);

            }
        });

        scanImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent scanintent = new Intent(MainActivity.this, QRScanActivity.class);
                startActivity(scanintent);
            }
        });
    }
}