package com.oilpalm3f.gradingapp.ui;

import static android.util.Base64.encodeToString;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.print.sdk.Barcode;
import com.android.print.sdk.PrinterConstants;
import com.android.print.sdk.PrinterInstance;
import com.oilpalm3f.gradingapp.BuildConfig;
import com.oilpalm3f.gradingapp.MainActivity;
import com.oilpalm3f.gradingapp.R;
import com.oilpalm3f.gradingapp.cloudhelper.ApplicationThread;
import com.oilpalm3f.gradingapp.common.CommonConstants;
import com.oilpalm3f.gradingapp.common.CommonUtils;
import com.oilpalm3f.gradingapp.common.InputFilterMinMax;
import com.oilpalm3f.gradingapp.database.DataAccessHandler;
import com.oilpalm3f.gradingapp.database.Queries;
import com.oilpalm3f.gradingapp.datasync.helpers.DataManager;
import com.oilpalm3f.gradingapp.datasync.helpers.DataSyncHelper;
import com.oilpalm3f.gradingapp.dbmodels.UserDetails;
import com.oilpalm3f.gradingapp.printer.BluetoothDevicesFragment;
import com.oilpalm3f.gradingapp.printer.PrinterChooserFragment;
import com.oilpalm3f.gradingapp.printer.UsbDevicesListFragment;
import com.oilpalm3f.gradingapp.printer.onPrinterType;
import com.oilpalm3f.gradingapp.utils.ImageUtility;
import com.oilpalm3f.gradingapp.utils.UiUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import static com.oilpalm3f.gradingapp.datasync.helpers.DataManager.USER_DETAILS;

import static com.oilpalm3f.gradingapp.common.CommonUtils.REQUEST_CAM_PERMISSIONS;

import org.apache.commons.lang3.StringUtils;

public class GradingActivity extends AppCompatActivity implements BluetoothDevicesFragment.onDeviceSelected, onPrinterType, UsbDevicesListFragment.onUsbDeviceSelected  {

    private static final String LOG_TAG = GradingActivity.class.getName();

    String qrvalue;
    public TextView tokenNumber, millcode, type, grossweight,tokendate;
    String[] splitString;

    EditText unripen, underripe, ripen, overripe, diseased,
            emptybunches, longstalk, mediumstalk, shortstalk, optimum,loosefruitweight, rejectedBunches,gradingdoneby;
    Button submit;
    Spinner isloosefruitavailable_spinner;
    LinearLayout loosefruitweightLL;

   // String[] splitString;
    String firsteight;
    String fruitType;

    String somestring = "202109021331";
    int tokenexists;
    int tokenssize;
    int printtokenssize;
    String createdByName;
    String tokenCount;

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

        //Log.d("FirstEight",firstEight(somestring));

        UserDetails userDetails = (UserDetails) DataManager.getInstance().getDataFromManager(USER_DETAILS);

        if (null != userDetails) {
            createdByName = userDetails.getFirstName();
            Log.d("CreatedBYName", createdByName + "");
        }
        Log.d("CreatedBYName1", createdByName + "");


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

        gradingdoneby.setText(createdByName);

//        unripen.setText("100");
//        underripe.setText("0");
//        ripen.setText("0");
//        overripe.setText("0");
//        diseased.setText("0");
//        emptybunches.setText("0");
//        longstalk.setText("100");
//        mediumstalk.setText("0");
//        shortstalk.setText("0");
//        optimum.setText("0");
//        isloosefruitavailable_spinner.setSelection(1);
//        loosefruitweight.setText("42");
//        rejectedBunches.setText("66");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            qrvalue = extras.getString("qrvalue");
            Log.d("QR Code Value is", qrvalue + "");
        }

      splitString = qrvalue.split("/");
//
        Log.d("String1", splitString[0] + "");
        Log.d("String2", splitString[1] + "");
        Log.d("String3", splitString[2] + "");
        Log.d("String4", splitString[3] + "");
        //Log.d("String5", splitString[4] + "");


        tokenNumber.setText(splitString[0] + "");
        millcode.setText(splitString[1] + "");
        type.setText(splitString[2] + "");
        grossweight.setText(splitString[3] + "");
       // tokendate.setText(splitString[4] + "");

//        Log.d("String1", splitString[0] + "");
//        Log.d("String2", splitString[1] + "");
//        Log.d("String3", splitString[2] + "");
//        Log.d("String4", splitString[3] + "");
//        //Log.d("String5", splitString[4] + "");

        tokenexists = dataAccessHandler.getOnlyOneIntValueFromDb(Queries.getInstance().getTokenExistQuery(splitString[0], splitString[2]));
        Log.d("tokenexists",tokenexists + "");

        if (tokenexists == 1){

            showDialog(GradingActivity.this, "Grading Already done for this Token");

        }
//        tokenssize = dataAccessHandler.getOnlyOneIntValueFromDb(Queries.getInstance().getTokenSizeQuery());
//        printtokenssize = tokenssize  + 1;
//        Log.d("tokenssize", tokenssize + "");
//        Log.d("printtokenssize", printtokenssize + "");


        firsteight = firstEight(splitString[0]);
        firsteight = firsteight.substring(0, 4) + "-" + firsteight.substring(4, firsteight.length());
        firsteight = firsteight.substring(0, 7) + "-" + firsteight.substring(7, firsteight.length());
        Log.d("FirstEightString",firsteight);

        if (splitString[2].equalsIgnoreCase("01")){

            fruitType = "Collection";
        }else{
            fruitType = "Consignment";
        }

        Log.d("fruitType is", fruitType);

         tokenCount = tokenNumber.getText().toString().substring(11);
        Log.d("tokenCount", tokenCount + "");


        tokenNumber.setText(splitString[0] + "");
        millcode.setText(splitString[1] + "");
        type.setText(fruitType);
        grossweight.setText(splitString[3] + " (Kgs)");
        tokendate.setText(firsteight + "");


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

//                    String base64String = null;
//
//                    try {
//                        Log.d("Base64String", CommonUtils.encodeFileToBase64Binary(new File(mCurrentPhotoPath)));
//                        base64String = CommonUtils.encodeFileToBase64Binary(new File(mCurrentPhotoPath));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    // Toast.makeText(GradingActivity.this, "Submit Success", Toast.LENGTH_SHORT).show();


                    List<LinkedHashMap> repodetails = new ArrayList<>();
                    LinkedHashMap maprepo = new LinkedHashMap();

//                    maprepo.put("TokenNumber", splitString[0] + "");
//                    maprepo.put("CCCode", splitString[1] + "");
//                    maprepo.put("FruitType", splitString[2] + "");
//                    maprepo.put("GrossWeight", splitString[3] + "");
//                    maprepo.put("FileName", splitString[1] + "");
//                    maprepo.put("TokenNumber", "1234");
//                    maprepo.put("CCCode", "CCGVG01");
//                    maprepo.put("FruitType",  "CType");
//                    maprepo.put("GrossWeight", "120");
//                    maprepo.put("FileName",  "CCGVG01");

                    maprepo.put("ImageString", "null");
                    maprepo.put("TokenNumber", splitString[0] + "");
                    maprepo.put("CCCode", splitString[1] + "");
                    maprepo.put("FruitType", splitString[2] + "");
                    maprepo.put("GrossWeight", splitString[3] + "");

                    maprepo.put("FileName", splitString[1] + "");

                    maprepo.put("FileLocation", mCurrentPhotoPath);
                    maprepo.put("FileExtension", ".jpg");

                    maprepo.put("CreatedByUserId", CommonConstants.USER_ID);
                    maprepo.put("CreatedDate", CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMMYYYY_HHMMSS));

                    maprepo.put("ServerUpdatedStatus", false);

                    repodetails.add(maprepo);

                    dataAccessHandler.saveData("FFBGradingRepository", repodetails, new ApplicationThread.OnComplete<String>() {
                        @Override
                        public void execute(boolean success, String result, String msg) {

                            if (success) {
                                Log.d(GradingActivity.class.getSimpleName(), "==>  Analysis ==> TABLE_FFBGradingRepository INSERT COMPLETED");
                            } else {
                                Toast.makeText(GradingActivity.this, "Submit Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    List<LinkedHashMap> details = new ArrayList<>();
                    LinkedHashMap map = new LinkedHashMap();

//                    map.put("TokenNumber", "1234");
//                    map.put("CCCode", "CCGVG01");
//                    map.put("FruitType",  "CType");
//                    map.put("GrossWeight", "120");
//                    map.put("TokenDate",  "2021-08-19T15:44:54.527");



                    map.put("TokenNumber", splitString[0] + "");
                    map.put("CCCode", splitString[1] + "");
                    map.put("FruitType", splitString[2] + "");
                    map.put("GrossWeight", splitString[3] + "");
                    map.put("TokenDate", firsteight + "");


                    map.put("UnRipen", unripen.getText().toString());
                    map.put("UnderRipe", underripe.getText().toString());
                    map.put("Ripen", ripen.getText().toString());
                    map.put("OverRipe", overripe.getText().toString());
                    map.put("Diseased", diseased.getText().toString());
                    map.put("EmptyBunches", emptybunches.getText().toString());
                    map.put("FFBQualityLong", longstalk.getText().toString());
                    map.put("FFBQualityMedium", mediumstalk.getText().toString());
                    map.put("FFBQualityShort", shortstalk.getText().toString());
                    map.put("FFBQualityOptimum", optimum.getText().toString());
                    int isfruitavailable = 0;

                    if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1){

                        isfruitavailable = 1;
                    }else if (isloosefruitavailable_spinner.getSelectedItemPosition() == 2){
                        isfruitavailable = 0;
                    }

                    map.put("LooseFruit", isfruitavailable);

                    if (isloosefruitavailable_spinner.getSelectedItemPosition() == 1) {

                        map.put("LooseFruitWeight", loosefruitweight.getText().toString());
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

                                if (CommonUtils.isNetworkAvailable(GradingActivity.this)) {
                                    DataSyncHelper.performRefreshTransactionsSync(GradingActivity.this, new ApplicationThread.OnComplete() {
                                        @Override
                                        public void execute(boolean success, Object result, String msg) {
                                            if (success) {
                                                ApplicationThread.uiPost(LOG_TAG, "transactions sync message", new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        UiUtils.showCustomToastMessage("Successfully data sent to server", GradingActivity.this, 0);
                                                        startActivity(new Intent(GradingActivity.this, MainActivity.class));
                                                    }
                                                });
                                            } else {
                                                ApplicationThread.uiPost(LOG_TAG, "transactions sync failed message", new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        UiUtils.showCustomToastMessage("Data sync failed", GradingActivity.this, 1);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                } else {
                                    startActivity(new Intent(GradingActivity.this, MainActivity.class));
                                }
                                //Toast.makeText(GradingActivity.this, "Submit Successfully", Toast.LENGTH_SHORT).show();

//                                FragmentManager fm = getSupportFragmentManager();
//                                PrinterChooserFragment printerChooserFragment = new PrinterChooserFragment();
//                                printerChooserFragment.setPrinterType(GradingActivity.this);
//                                printerChooserFragment.show(fm, "bluetooth fragment");
                              //  finish();

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

        if (TextUtils.isEmpty(gradingdoneby.getText().toString())) {
            UiUtils.showCustomToastMessage("Please Enter Grading Done By", GradingActivity.this, 0);
            return false;
        }

        if (TextUtils.isEmpty(mCurrentPhotoPath)) {
            UiUtils.showCustomToastMessage("Please Take Grading Image", GradingActivity.this, 0);
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
       ;


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
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
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

    @Override
    public void selectedDevice(PrinterInstance printerInstance) {

        Log.v(LOG_TAG, "selected address is ");
        if (null != printerInstance) {
            enablePrintBtn(false);
            for (int i = 0; i < 1; i++) {
                printGradingData(printerInstance, false, i);
            }
        } else {
            UiUtils.showCustomToastMessage("Printing failed", GradingActivity.this, 1);
        }

    }

    public void enablePrintBtn(final boolean enable) {
        ApplicationThread.uiPost(LOG_TAG, "updating ui", new Runnable() {
            @Override
            public void run() {
                submit.setEnabled(enable);
                submit.setClickable(enable);
                submit.setFocusable(enable);
            }
        });

    }

    @Override
    public void enablingPrintButton(boolean rePrint) {
        enablePrintBtn(rePrint);
    }

    @Override
    public void onPrinterTypeSelected(int printerType) {

        if (printerType == PrinterChooserFragment.USB_PRINTER) {
            FragmentManager fm = getSupportFragmentManager();
            UsbDevicesListFragment usbDevicesListFragment = new UsbDevicesListFragment();
            usbDevicesListFragment.setOnUsbDeviceSelected(GradingActivity.this);
            usbDevicesListFragment.show(fm, "usb fragment");
        } else {
            FragmentManager fm = getSupportFragmentManager();
            BluetoothDevicesFragment bluetoothDevicesFragment = new BluetoothDevicesFragment();
            bluetoothDevicesFragment.setOnDeviceSelected(GradingActivity.this);
            bluetoothDevicesFragment.show(fm, "bluetooth fragment");
        }

    }

    public void printGradingData(PrinterInstance mPrinter, boolean isReprint, int printCount) {

        mPrinter.init();
        StringBuilder sb = new StringBuilder();
        mPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER);
        mPrinter.setCharacterMultiple(0, 1);
        mPrinter.printText(" 3F Oil Palm Agrotech PVT LTD " + "\n");
        mPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER);
        mPrinter.setCharacterMultiple(0, 1);
        mPrinter.printText("   FFB Grading Receipt" + "\n");
        mPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT);
        mPrinter.setCharacterMultiple(0, 0);
        mPrinter.setLeftMargin(15, 15);
        sb.append("==============================================" + "\n");

        sb.append(" ");
        sb.append(" Token Number : ").append(splitString[0] + "").append("\n");
        sb.append(" ");
        sb.append(" CCCode : ").append(splitString[1] + "").append("\n");
        sb.append(" ");
        sb.append(" Fruit Type : ").append(fruitType + "").append("\n");
        sb.append(" ");
        sb.append(" Gross Weight(Kgs) : ").append(splitString[3] + "").append("\n");
        sb.append(" ");
        sb.append(" Grading Date : ").append(CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_4) + "").append("\n");

//        sb.append(" ");
//        sb.append("-----------------------------------------------\n");
//        sb.append("  FFB Quality Details" + "\n");
//        sb.append("-----------------------------------------------\n");
//
//        if (!unripen.getText().toString().equalsIgnoreCase("0")) {
//            sb.append(" ");
//            sb.append(" Unripen : ").append(unripen.getText().toString() + "%").append("\n");
//        }
//        if (!underripe.getText().toString().equalsIgnoreCase("0")) {
//            sb.append(" ");
//            sb.append(" Under Ripe : ").append(underripe.getText().toString() + "%").append("\n");
//        }
//        if (!ripen.getText().toString().equalsIgnoreCase("0")) {
//            sb.append(" ");
//            sb.append(" Ripen : ").append(ripen.getText().toString() + "%").append("\n");
//        }
//        if (!overripe.getText().toString().equalsIgnoreCase("0")) {
//            sb.append(" ");
//            sb.append(" Over Ripe : ").append(overripe.getText().toString() + "%").append("\n");
//        }
//        if (!diseased.getText().toString().equalsIgnoreCase("0")) {
//            sb.append(" ");
//            sb.append(" Diseased : ").append(diseased.getText().toString() + "%").append("\n");
//        }
//        if (!emptybunches.getText().toString().equalsIgnoreCase("0")) {
//            sb.append(" ");
//            sb.append(" Empty Bunch's : ").append(emptybunches.getText().toString() + "%").append("\n");
//        }
//
//        sb.append(" ");
//        sb.append("-----------------------------------------------\n");
//        sb.append("  Stalk Quality Details" + "\n");
//        sb.append("-----------------------------------------------\n");
//
//        if (!longstalk.getText().toString().equalsIgnoreCase("0")) {
//            sb.append(" ");
//            sb.append(" Long : ").append(longstalk.getText().toString() + "%").append("\n");
//        }
//        if (!mediumstalk.getText().toString().equalsIgnoreCase("0")) {
//            sb.append(" ");
//            sb.append(" Medium : ").append(mediumstalk.getText().toString() + "%").append("\n");
//        }
//        if (!shortstalk.getText().toString().equalsIgnoreCase("0")) {
//            sb.append(" ");
//            sb.append(" Short : ").append(shortstalk.getText().toString() + "%").append("\n");
//        }
//        if (!optimum.getText().toString().equalsIgnoreCase("0")) {
//            sb.append(" ");
//            sb.append(" Optimum : ").append(optimum.getText().toString() + "%").append("\n");
//        }
//
//        sb.append(" ");
//        sb.append("-----------------------------------------------\n");
//
//        if (!TextUtils.isEmpty(loosefruitweight.getText().toString())){
//            sb.append(" ");
//            sb.append(" Loose Fruit Approx.Quantity : ").append(loosefruitweight.getText().toString() + "(Kgs)").append("\n");
//        }
//
//        if (!TextUtils.isEmpty(rejectedBunches.getText().toString())) {
//            sb.append(" ");
//            sb.append(" Rejected Bunches : ").append(rejectedBunches.getText().toString() + "").append("\n");
//        }
//            sb.append(" ");
//            sb.append(" Grader Name : ").append(gradingdoneby.getText().toString()).append("\n");
//
//
//        sb.append(" ");
//        sb.append("\n");
//        sb.append(" ");
//        sb.append("\n");
//        sb.append(" ");
//        sb.append("\n");
//        sb.append(" ");
//        sb.append("\n");
//        sb.append(" Ramp Incharge/Grader signature");
//        sb.append(" ");
//        sb.append("\n");
//        sb.append(" ");
//        sb.append("\n");
//        sb.append(" ");
//        sb.append("\n");
//        sb.append(" ");
//        sb.append("\n");
//        sb.append(" Farmer signature");
//        sb.append(" ");
//        sb.append("\n");
//        sb.append(" ");

        mPrinter.printText(sb.toString());

        boolean fruitavailable;

       if( isloosefruitavailable_spinner.getSelectedItemPosition() == 1){

           fruitavailable = true;
        }else {
           fruitavailable = false;
       }

       String fruightweight;
       String rejectedbunches;

       if(TextUtils.isEmpty(loosefruitweight.getText().toString())){
           fruightweight = "0";
       }else{
           fruightweight = loosefruitweight.getText().toString();
       }

        if(TextUtils.isEmpty(rejectedBunches.getText().toString())){
            rejectedbunches = "0";
        }else{
            rejectedbunches = rejectedBunches.getText().toString();
        }

        String hashString = qrvalue+"/"+CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMM)+"/"+unripen.getText().toString()+"/"+underripe.getText().toString()+"/"+ripen.getText().toString()
                +"/"+overripe.getText().toString()+"/"+diseased.getText().toString()+"/"+emptybunches.getText().toString()+"/"
                +longstalk.getText().toString()+"/"+mediumstalk.getText().toString()+"/"+shortstalk.getText().toString()+"/"+
                optimum.getText().toString()+"/"+fruitavailable+"/"+fruightweight+"/"+rejectedbunches+
                "/"+gradingdoneby.getText().toString();
        String qrCodeValue = hashString;
        Log.d("qrCodeValueis", qrCodeValue  + "");
        Barcode barcode = new Barcode(PrinterConstants.BarcodeType.QRCODE, 3, 95, 3, qrCodeValue);

        mPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER);
        mPrinter.setCharacterMultiple(0, 1);

        String space = "-----------------------------------------------";
        String tokenNumber  =  "Token Number";
        String spaceBuilderr = "\n";

        mPrinter.printText(space);
        mPrinter.printText(spaceBuilderr);
        mPrinter.printText(tokenNumber);
        mPrinter.printText(spaceBuilderr);
        mPrinter.printText(tokenCount);
        mPrinter.printText(spaceBuilderr);
        mPrinter.printText(space);
        mPrinter.printText(spaceBuilderr);


        if(CommonConstants.PrinterName.contains("AMIGOS")){
            Log.d(LOG_TAG,"########### NEW ##############");
            print_qr_code(mPrinter,qrCodeValue);
        }else{
            Log.d(LOG_TAG,"########### OLD ##############");
            mPrinter.printBarCode(barcode);
        }
        mPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER);
        mPrinter.setCharacterMultiple(0, 1);
        //mPrinter.printText(qrCodeValue);

        String spaceBuilder = "\n" +
                " " +
                "\n" +
                " " +
                "\n" +
                "\n" +
                " " +
                "\n" +
                "\n";
        mPrinter.printText(spaceBuilder);

        boolean printSuccess = false;
        try {
            mPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2);
            printSuccess = true;
        } catch (Exception e) {
            Log.v(LOG_TAG, "@@@ printing failed " + e.getMessage());
            UiUtils.showCustomToastMessage("Printing failes due to " + e.getMessage(), GradingActivity.this, 1);
            printSuccess = false;
        } finally {
            if (printSuccess) {
                Toast.makeText(GradingActivity.this, "Print Success", Toast.LENGTH_SHORT).show();
//                if (CommonUtils.isNetworkAvailable(GradingActivity.this)) {
//                    DataSyncHelper.performRefreshTransactionsSync(GradingActivity.this, new ApplicationThread.OnComplete() {
//                        @Override
//                        public void execute(boolean success, Object result, String msg) {
//                            if (success) {
//                                ApplicationThread.uiPost(LOG_TAG, "transactions sync message", new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        UiUtils.showCustomToastMessage("Successfully data sent to server", GradingActivity.this, 0);
//                                        startActivity(new Intent(GradingActivity.this, MainActivity.class));
//                                    }
//                                });
//                            } else {
//                                ApplicationThread.uiPost(LOG_TAG, "transactions sync failed message", new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        UiUtils.showCustomToastMessage("Data sync failed", GradingActivity.this, 1);
//                                    }
//                                });
//                            }
//                        }
//                    });
//                } else {
//                    startActivity(new Intent(GradingActivity.this, MainActivity.class));
//                }
            }
        }

    }

    public void print_qr_code(PrinterInstance mPrinter,String qrdata)
    {
        int store_len = qrdata.length() + 3;
        byte store_pL = (byte) (store_len % 256);
        byte store_pH = (byte) (store_len / 256);


        // QR Code: Select the modelc
        //              Hex     1D      28      6B      04      00      31      41      n1(x32)     n2(x00) - size of model
        // set n1 [49 x31, model 1] [50 x32, model 2] [51 x33, micro qr code]
        // https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=140
        byte[] modelQR = {(byte)0x1d, (byte)0x28, (byte)0x6b, (byte)0x04, (byte)0x00, (byte)0x31, (byte)0x41, (byte)0x32, (byte)0x00};

        // QR Code: Set the size of module
        // Hex      1D      28      6B      03      00      31      43      n
        // n depends on the printer
        // https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=141


        byte[] sizeQR = {(byte)0x1d, (byte)0x28, (byte)0x6b, (byte)0x03, (byte)0x00, (byte)0x31, (byte)0x43, (byte)0x08};


        //          Hex     1D      28      6B      03      00      31      45      n
        // Set n for error correction [48 x30 -> 7%] [49 x31-> 15%] [50 x32 -> 25%] [51 x33 -> 30%]
        // https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=142
        byte[] errorQR = {(byte)0x1d, (byte)0x28, (byte)0x6b, (byte)0x03, (byte)0x00, (byte)0x31, (byte)0x45, (byte)0x31};


        // QR Code: Store the data in the symbol storage area
        // Hex      1D      28      6B      pL      pH      31      50      30      d1...dk
        // https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=143
        //                        1D          28          6B         pL          pH  cn(49->x31) fn(80->x50) m(48->x30) d1…dk
        byte[] storeQR = {(byte)0x1d, (byte)0x28, (byte)0x6b, store_pL, store_pH, (byte)0x31, (byte)0x50, (byte)0x30};


        // QR Code: Print the symbol data in the symbol storage area
        // Hex      1D      28      6B      03      00      31      51      m
        // https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=144
        byte[] printQR = {(byte)0x1d, (byte)0x28, (byte)0x6b, (byte)0x03, (byte)0x00, (byte)0x31, (byte)0x51, (byte)0x30};

        // flush() runs the print job and clears out the print buffer
//        flush();

        // write() simply appends the data to the buffer
        mPrinter.sendByteData(modelQR);

        mPrinter.sendByteData(sizeQR);
        mPrinter.sendByteData(errorQR);
        mPrinter.sendByteData(storeQR);
        mPrinter.sendByteData(qrdata.getBytes());
        mPrinter.sendByteData(printQR);

    }

    public String firstEight(String str) {
        return str.length() < 8 ? str : str.substring(0, 8);
    }

    public void showDialog(Activity activity, String msg) {
        final Dialog dialog = new Dialog(activity, R.style.DialogSlideAnim);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog);
        final ImageView img = dialog.findViewById(R.id.img_cross);

        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        text.setText(msg);
        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((Animatable) img.getDrawable()).start();
            }
        }, 500);
    }

}