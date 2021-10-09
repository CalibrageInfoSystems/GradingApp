package com.oilpalm3f.gradingapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.Result;
import com.oilpalm3f.gradingapp.R;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


//To Scan the QR Code and handle the result
public class QRScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("QR Scan");
        setSupportActionBar(toolbar);

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        scannerView = new ZXingScannerView(this);
        contentFrame.addView(scannerView);
    }

    @Override
    public void handleResult(Result result) {

        Log.e("result.getText()",result.getText()+"");

        if (!TextUtils.isEmpty(result.getText())){
            Intent gradingintent = new Intent(QRScanActivity.this, GradingActivity.class);
            gradingintent.putExtra("qrvalue", result.getText() + "");
            startActivity(gradingintent);
        }else{
            Log.d("QRCode Scan", "Failed");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(QRScanActivity.this);
        scannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }
}