package com.oilpalm3f.gradingapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.oilpalm3f.gradingapp.R;
import com.oilpalm3f.gradingapp.cloudhelper.ApplicationThread;
import com.oilpalm3f.gradingapp.common.CommonUtils;
import com.oilpalm3f.gradingapp.database.DataAccessHandler;
import com.oilpalm3f.gradingapp.database.Queries;
import com.oilpalm3f.gradingapp.datasync.helpers.DataSyncHelper;
import com.oilpalm3f.gradingapp.uihelper.ProgressBar;
import com.oilpalm3f.gradingapp.utils.UiUtils;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class RefreshSyncActivity extends AppCompatActivity {

    private static final String LOG_TAG = RefreshSyncActivity.class.getName();

    private TextView tvgradingfilerepository;
    private Button btnsend, btnmastersync;
    private DataAccessHandler dataAccessHandler;
    private boolean isDataUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_sync);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Sync Screen");
        setSupportActionBar(toolbar);

        dataAccessHandler = new DataAccessHandler(this);
        tvgradingfilerepository = findViewById(R.id.gradingrepcount);
        btnsend = findViewById(R.id.btsynctoserver);
        btnmastersync = findViewById(R.id.btnmastersync);

        bindData();


        btnmastersync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (CommonUtils.isNetworkAvailable(RefreshSyncActivity.this)) {
                    DataSyncHelper.performMasterSync(RefreshSyncActivity.this, false, new ApplicationThread.OnComplete() {
                        @Override
                        public void execute(boolean success, Object result, String msg) {
                            ProgressBar.hideProgressBar();
                            if (success) {
                                if (!msg.equalsIgnoreCase("Sync is up-to-date")) {
                                    Toast.makeText(RefreshSyncActivity.this, "Data synced successfully", Toast.LENGTH_SHORT).show();
                                    // List<UserSync> userSyncList = (List<UserSync>)dataAccessHandler.getUserSyncData(Queries.getInstance().countOfMasterSync());
//                                    List<UserSync> userSyncList = (List<UserSync>) dataAccessHandler.getUserSyncData(Queries.getInstance().countOfSync());
//
//                                    if (userSyncList.size() == 0) {
//                                        Log.v("@@@MM", "mas");
//                                        addUserMasSyncDetails();
//                                    } else {
//                                        dataAccessHandler.updateMasterSync();
//                                    }

                                    // DataAccessHandler dataAccessHandler = new DataAccessHandler(RefreshSyncActivity.this);
                                    // dataAccessHandler.updateMasterSyncDate(false, CommonConstants.USER_ID);
                                } else {
                                    ApplicationThread.uiPost(LOG_TAG, "master sync message", new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RefreshSyncActivity.this, "You have updated data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                Log.v(LOG_TAG, "@@@ Master sync failed " + msg);
                                ApplicationThread.uiPost(LOG_TAG, "master sync message", new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RefreshSyncActivity.this, "Master sync failed. Please try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    UiUtils.showCustomToastMessage("Please check network connection", RefreshSyncActivity.this, 1);
                }
            }
        });


        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (CommonUtils.isNetworkAvailable(RefreshSyncActivity.this)) {

                    btnsend.setVisibility(View.GONE);
                    isDataUpdated = false;
                    DataSyncHelper.performRefreshTransactionsSync(RefreshSyncActivity.this, new ApplicationThread.OnComplete() {
                        @Override
                        public void execute(boolean success, Object result, String msg) {
                            if (success) {
                                ApplicationThread.uiPost(LOG_TAG, "transactions sync message", new Runnable() {
                                    @Override
                                    public void run() {
                                        bindData();
//                                        Toasty.success(RefreshSyncActivity.this,"Successfully data sent to server",10).show();
                                        if (isDataUpdated) {
                                            UiUtils.showCustomToastMessage("Successfully data sent to server", RefreshSyncActivity.this, 0);
                                            ProgressBar.hideProgressBar();
                                            btnsend.setVisibility(View.VISIBLE);
                                            //  dataAccessHandler.updateUserSync();
                                        }

                                    }
                                });
                            } else {
                                ApplicationThread.uiPost(LOG_TAG, "transactions sync failed message", new Runnable() {
                                    @Override
                                    public void run() {
                                        bindData();
                                        Toasty.error(RefreshSyncActivity.this, "Data sending failed", 10).show();
//                                        Toast.makeText(RefreshSyncActivity.this, "Data sending failed", Toast.LENGTH_SHORT).show();
                                        ProgressBar.hideProgressBar();
                                        btnsend.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }
                    });
                } else {
                    UiUtils.showCustomToastMessage("Please check network connection", RefreshSyncActivity.this, 1);
                    btnsend.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void bindData() {
        try {

            tvgradingfilerepository.setText(dataAccessHandler.getCountValue(Queries.getInstance().getRefreshCountQuery("FFBGradingRepository")));
            isDataUpdated = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}