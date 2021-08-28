package com.oilpalm3f.gradingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.oilpalm3f.gradingapp.cloudhelper.Log;
import com.oilpalm3f.gradingapp.ui.QRScanActivity;

public class MainActivity extends AppCompatActivity {

    ImageView scanImg, reportsImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Home Screen");
        setSupportActionBar(toolbar);


        scanImg = findViewById(R.id.scanImg);
        reportsImg = findViewById(R.id.reportsImg);

        scanImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent scanintent = new Intent(MainActivity.this, QRScanActivity.class);
                startActivity(scanintent);
            }
        });
    }
}