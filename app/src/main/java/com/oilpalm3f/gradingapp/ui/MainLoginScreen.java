package com.oilpalm3f.gradingapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.oilpalm3f.gradingapp.MainActivity;
import com.oilpalm3f.gradingapp.R;
import com.oilpalm3f.gradingapp.database.DataAccessHandler;
import com.oilpalm3f.gradingapp.utils.UiUtils;

public class MainLoginScreen extends AppCompatActivity {

    public static final String LOG_TAG = MainLoginScreen.class.getName();


    private EditText username;
    private EditText password;
    private Button signInBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Login");
        setSupportActionBar(toolbar);

        username = (EditText) findViewById(R.id.userID);
        password = (EditText) findViewById(R.id.passwordEdit);
        signInBtn = (Button) findViewById(R.id.signInBtn);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validations()){

                    Intent homeintent = new Intent(MainLoginScreen.this, MainActivity.class);
                    startActivity(homeintent);

                }
            }
        });

    }

    private boolean validations() {
        if (TextUtils.isEmpty(username.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Username", MainLoginScreen.this,0);
            return false;
        }
      else if (TextUtils.isEmpty(password.getText().toString().trim())) {
            UiUtils.showCustomToastMessage("Please Enter Password", MainLoginScreen.this,0);
            return false;
        }

        return true;
    }
}