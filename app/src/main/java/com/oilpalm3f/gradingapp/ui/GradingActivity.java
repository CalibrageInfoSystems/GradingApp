package com.oilpalm3f.gradingapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.oilpalm3f.gradingapp.BuildConfig;
import com.oilpalm3f.gradingapp.R;
import com.oilpalm3f.gradingapp.cloudhelper.ApplicationThread;
import com.oilpalm3f.gradingapp.common.CommonConstants;
import com.oilpalm3f.gradingapp.common.CommonUtils;
import com.oilpalm3f.gradingapp.common.InputFilterMinMax;
import com.oilpalm3f.gradingapp.database.DataAccessHandler;
import com.oilpalm3f.gradingapp.utils.ImageUtility;
import com.oilpalm3f.gradingapp.utils.UiUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import static com.oilpalm3f.gradingapp.common.CommonUtils.REQUEST_CAM_PERMISSIONS;

public class GradingActivity extends AppCompatActivity {

    private static final String LOG_TAG = GradingActivity.class.getName();

    String qrvalue;
    public TextView tokenNumber, millcode, type, grossweight,tokendate;

    EditText unripen, underripe, ripen, overripe, diseased,
            emptybunches, longstalk, mediumstalk, shortstalk, optimum,loosefruitweight, rejectedBunches,gradingdoneby;
    Button submit;
    Spinner isloosefruitavailable_spinner;
    LinearLayout loosefruitweightLL;
    
    private DataAccessHandler dataAccessHandler;
    private ImageView slipImage, slipIcon;
    private static final int CAMERA_REQUEST = 1888;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static  String mCurrentPhotoPath;
    private  File finalFile;
    private Bitmap currentBitmap = null;
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
        tokendate = findViewById(R.id.tokendate);

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
        slipImage = (ImageView) findViewById(R.id.slip_image);
        slipIcon = (ImageView) findViewById(R.id.slip_icon);
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
//
        Log.d("String1", splitString[0] + "");
        Log.d("String2", splitString[1] + "");
        Log.d("String3", splitString[2] + "");
        Log.d("String4", splitString[3] + "");
        Log.d("String5", splitString[4] + "");

        tokenNumber.setText(splitString[0] + "");
        millcode.setText(splitString[1] + "");
        type.setText(splitString[2] + "");
        grossweight.setText(splitString[3] + "");
        tokendate.setText(splitString[4] + "");

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
        slipImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (!CommonUtils.isPermissionAllowed(GradingActivity.this, Manifest.permission.CAMERA))) {
               Log.v(LOG_TAG, "Location Permissions Not Granted");
                    ActivityCompat.requestPermissions(
                            GradingActivity.this,
                            PERMISSIONS_STORAGE,
                            REQUEST_CAM_PERMISSIONS
                    );
                } else {

                    dispatchTakePictureIntent(CAMERA_REQUEST);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                   // Toast.makeText(GradingActivity.this, "Submit Success", Toast.LENGTH_SHORT).show();

                    List<LinkedHashMap> details = new ArrayList<>();
                    LinkedHashMap map = new LinkedHashMap();

                    map.put("TokenNumber", splitString[0] + "");
                    map.put("CCCode", splitString[1] + "");
                    map.put("FruitType", splitString[2] + "");
                    map.put("GrossWeight", splitString[3] + "");
                    map.put("TokenDate", splitString[4] + "");

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
                    map.put("GraderName", gradingdoneby.getText().toString());
                    map.put("CreatedByUserId", CommonConstants.USER_ID);
                    map.put("CreatedDate", CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMMYYYY_HHMMSS));

                    details.add(map);

                    dataAccessHandler.saveData("FFBGrading", details, new ApplicationThread.OnComplete<String>() {
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

    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        switch(actionCode) {
            case CAMERA_REQUEST:
                File f = null;
                mCurrentPhotoPath = null;
                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
//                    FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
//                            BuildConfig.APPLICATION_ID + ".provider", file);
                    Uri photoURI = FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            f);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        }
        startActivityForResult(takePictureIntent, actionCode);
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        Log.e("===========>",mCurrentPhotoPath);

        return f;
    }

    private File createImageFile() throws IOException {
        String root = Environment.getExternalStorageDirectory().toString();
        File rootDirectory = new File(root + "/3F_GradingPictures");
        File pictureDirectory = new File(root + "/3F_GradingPictures/" + "GradingPhotos");

        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs();
        }

        if (!pictureDirectory.exists()) {
            pictureDirectory.mkdirs();
        }
        finalFile = new File(pictureDirectory, Calendar.getInstance().getTimeInMillis() + CommonConstants.JPEG_FILE_SUFFIX);
        return finalFile;
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
    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult ( int requestCode, int resultCode, Intent data){
        switch (requestCode) {
            case CAMERA_REQUEST: {
                if (resultCode == RESULT_OK) {
                    try {
//                        UiUtils.decodeFile(mCurrentPhotoPath,finalFile);
                        handleBigCameraPhoto();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }


                }
                break;
            }
        }
    }
    private void handleBigCameraPhoto () {

        if (mCurrentPhotoPath != null) {
            setPic();
            galleryAddPic();
//            mCurrentPhotoPath = null;
        }

    }

    private void setPic()
    {

        /* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */


        
        /* Get the size of the ImageView */
        int targetW = slipImage.getWidth();
        int targetH = slipImage.getHeight();

        /* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        /* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

        /* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        /* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        bitmap = ImageUtility.rotatePicture(90, bitmap);

        currentBitmap = bitmap;
        slipImage.setImageBitmap(bitmap);
        slipImage.setVisibility(View.VISIBLE);
        slipIcon.setVisibility(View.GONE);
        slipImage.invalidate();
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}