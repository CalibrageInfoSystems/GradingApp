package com.oilpalm3f.gradingapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.oilpalm3f.gradingapp.R;
import com.oilpalm3f.gradingapp.cloudhelper.ApplicationThread;
import com.oilpalm3f.gradingapp.common.InputFilterMinMax;
import com.oilpalm3f.gradingapp.database.DataAccessHandler;
import com.oilpalm3f.gradingapp.utils.UiUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class GradingActivity extends AppCompatActivity {

    String qrvalue;
    public TextView tokenNumber, millcode, type, grossweight;

    EditText unripen, underripe, ripen, overripe, diseased,
            emptybunches, longstalk, mediumstalk, shortstalk, optimum,loosefruitweight, rejectedBunches,gradingdoneby;
    Button submit;
    Spinner isloosefruitavailable_spinner;
    LinearLayout loosefruitweightLL;
    private DataAccessHandler dataAccessHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grading);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Grading Screen");
        setSupportActionBar(toolbar);

        dataAccessHandler = new DataAccessHandler(GradingActivity.this);


        tokenNumber = findViewById(R.id.tokenNumber);
        millcode = findViewById(R.id.millcode);
        type = findViewById(R.id.type);
        grossweight = findViewById(R.id.grossweight);

        unripen = findViewById(R.id.unripen);
        underripe = findViewById(R.id.underripe);
        ripen = findViewById(R.id.ripen);
        overripe = findViewById(R.id.overipe);
        diseased = findViewById(R.id.diseased);
        emptybunches = findViewById(R.id.emptybunch);
        longstalk = findViewById(R.id.longstake);
        mediumstalk = findViewById(R.id.mediumstake);
        shortstalk = findViewById(R.id.shortstake);
        optimum = findViewById(R.id.optimum);
        loosefruitweight = findViewById(R.id.loosefruitweight);
        rejectedBunches = findViewById(R.id.rejectedbunches);
        gradingdoneby = findViewById(R.id.gradingdoneby);

        isloosefruitavailable_spinner = findViewById(R.id.isloosefruitavailable_spinner);
        loosefruitweightLL = findViewById(R.id.loosefruitweightLL);
        submit = findViewById(R.id.gradingsubmit);

        unripen.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        underripe.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        ripen.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        overripe.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        diseased.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        emptybunches.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        longstalk.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        mediumstalk.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        shortstalk.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
        optimum.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            qrvalue = extras.getString("qrvalue");
            Log.d("QR Code Value is", qrvalue + "");
        }

        String[] splitString = qrvalue.split("/");

        Log.d("String1", splitString[0] + "");
        Log.d("String2", splitString[1] + "");
        Log.d("String3", splitString[2] + "");
        Log.d("String4", splitString[3] + "");

        tokenNumber.setText(splitString[0] + "");
        millcode.setText(splitString[1] + "");
        type.setText(splitString[2] + "");
        grossweight.setText(splitString[3] + "");

        String[] isloosefruitavailableArray = getResources().getStringArray(R.array.yesOrNo_values);
        List<String> isloosefruitavailableList = Arrays.asList(isloosefruitavailableArray);
        ArrayAdapter<String> isloosefruitavailableAdapter = new ArrayAdapter<>(GradingActivity.this, android.R.layout.simple_spinner_item, isloosefruitavailableList);
        isloosefruitavailableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        isloosefruitavailable_spinner.setAdapter(isloosefruitavailableAdapter);

        isloosefruitavailable_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1) {

                    loosefruitweightLL.setVisibility(View.VISIBLE);
                } else {
                    loosefruitweightLL.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                   // Toast.makeText(GradingActivity.this, "Submit Success", Toast.LENGTH_SHORT).show();

                    List<LinkedHashMap> details = new ArrayList<>();
                    LinkedHashMap map = new LinkedHashMap();

                    map.put("Token", splitString[0] + "");
                    map.put("MillCode", splitString[1] + "");
                    map.put("Type", splitString[2] + "");
                    map.put("GrossWeight", splitString[3] + "");

                    map.put("UnRipen", Integer.parseInt(unripen.getText().toString()));
                    map.put("UnderRipe", Integer.parseInt(underripe.getText().toString()));
                    map.put("Ripen", Integer.parseInt(ripen.getText().toString()));
                    map.put("OverRipe", Integer.parseInt(overripe.getText().toString()));
                    map.put("Diseased", Integer.parseInt(diseased.getText().toString()));
                    map.put("EmptyBunches", Integer.parseInt(emptybunches.getText().toString()));
                    map.put("FFBQualityLong", Integer.parseInt(longstalk.getText().toString()));
                    map.put("FFBQualityMedium", Integer.parseInt(mediumstalk.getText().toString()));
                    map.put("FFBQualityShort", Integer.parseInt(shortstalk.getText().toString()));
                    map.put("FFBQualityOptimum", Integer.parseInt(optimum.getText().toString()));
                    int isfruitavailable = 0;

                    if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1){

                        isfruitavailable = 1;
                    }else if (isloosefruitavailable_spinner.getSelectedItemPosition() == 2){
                        isfruitavailable = 0;
                    }

                    map.put("LooseFruit", isfruitavailable);

                    if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1) {

                        map.put("LooseFruitWeight", Integer.parseInt(loosefruitweight.getText().toString()));
                    }

                    map.put("RejectedBunches",rejectedBunches.getText().toString());
                    map.put("Gradingdoneby", gradingdoneby.getText().toString());

                    details.add(map);

                    dataAccessHandler.saveData("Grading", details, new ApplicationThread.OnComplete<String>() {
                        @Override
                        public void execute(boolean success, String result, String msg) {

                            if (success) {
                                Log.d(GradingActivity.class.getSimpleName(), "==>  Analysis ==> TABLE_Grading INSERT COMPLETED");
                                Toast.makeText(GradingActivity.this, "Submit Successfully", Toast.LENGTH_SHORT).show();
                                finish();

                            } else {
                                Toast.makeText(GradingActivity.this, "Submit Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Log.d(GradingActivity.class.getSimpleName(), "==>  Analysis ==> TABLE_Grading INSERT Failed");
                    Toast.makeText(GradingActivity.this, "Submit Failedd", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public boolean validate() {

        if (TextUtils.isEmpty(unripen.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Unripen", GradingActivity.this, 0);
            return false;
        }
        if (TextUtils.isEmpty(underripe.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Underripe", GradingActivity.this, 0);
            return false;
        }
        if (TextUtils.isEmpty(ripen.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Ripen", GradingActivity.this, 0);
            return false;
        }
        if (TextUtils.isEmpty(overripe.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Overripe", GradingActivity.this, 0);
            return false;
        }
        if (TextUtils.isEmpty(diseased.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Diseased", GradingActivity.this, 0);
            return false;
        }
        if (TextUtils.isEmpty(emptybunches.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Empty Bunches", GradingActivity.this, 0);
            return false;
        }
        if (TextUtils.isEmpty(longstalk.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Long Stock Quality", GradingActivity.this, 0);
            return false;
        }
        if (TextUtils.isEmpty(mediumstalk.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Medium Stock Quality", GradingActivity.this, 0);
            return false;
        }
        if (TextUtils.isEmpty(shortstalk.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Short Stock Quality", GradingActivity.this, 0);
            return false;
        }
        if (TextUtils.isEmpty(optimum.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Optimum Stock Quality", GradingActivity.this, 0);
            return false;
        }

        if (isloosefruitavailable_spinner.getSelectedItemPosition() == 0) {
            UiUtils.showCustomToastMessage("Please Select Is Loose Fruit Available", GradingActivity.this, 0);
            return false;
        }

        if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1){
            if (TextUtils.isEmpty(loosefruitweight.getText().toString())) {
                UiUtils.showCustomToastMessage("Please Enter Loose Fruit Weight", GradingActivity.this, 0);
                return false;
            }
        }

        if (TextUtils.isEmpty(rejectedBunches.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Rejected Bunches", GradingActivity.this, 0);
            return false;
        }

        if (TextUtils.isEmpty(gradingdoneby.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Grading Done By", GradingActivity.this, 0);
            return false;
        }


        if ((Double.parseDouble(unripen.getText().toString()) + Double.parseDouble(underripe.getText().toString()) + Double.parseDouble(ripen.getText().toString()) + Double.parseDouble(overripe.getText().toString()) + Double.parseDouble(diseased.getText().toString()) + Double.parseDouble(emptybunches.getText().toString())) != 100) {
            UiUtils.showCustomToastMessage("FFB Bunch Quality should be equal to 100%", GradingActivity.this, 0);
            return false;
        }

        if ((Double.parseDouble(longstalk.getText().toString()) + Double.parseDouble(mediumstalk.getText().toString()) + Double.parseDouble(shortstalk.getText().toString()) + Double.parseDouble(optimum.getText().toString())) != 100) {
            UiUtils.showCustomToastMessage("FFB Stalk Quality should be equal to 100%", GradingActivity.this, 0);
            return false;
        }

        return true;
    }
}