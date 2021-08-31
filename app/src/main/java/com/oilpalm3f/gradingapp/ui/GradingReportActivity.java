package com.oilpalm3f.gradingapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.oilpalm3f.gradingapp.R;
import com.oilpalm3f.gradingapp.cloudhelper.ApplicationThread;
import com.oilpalm3f.gradingapp.cloudhelper.Log;
import com.oilpalm3f.gradingapp.common.CommonConstants;
import com.oilpalm3f.gradingapp.common.CommonUtils;
import com.oilpalm3f.gradingapp.database.DataAccessHandler;
import com.oilpalm3f.gradingapp.database.Queries;
import com.oilpalm3f.gradingapp.dbmodels.GradingReportModel;
import com.oilpalm3f.gradingapp.uihelper.ProgressBar;
import com.oilpalm3f.gradingapp.utils.UiUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GradingReportActivity extends AppCompatActivity implements onPrintOptionSelected {
    private static final String LOG_TAG = GradingReportActivity.class.getName();
    private GradingReportAdapter gradingReportRecyclerAdapter;
    private RecyclerView gradingReportsList;
    private List<GradingReportModel> mReportsList = new ArrayList<>();
    private TextView tvNorecords, totalNetWeightSum;
    private DataAccessHandler dataAccessHandler;
    private EditText fromDateEdt, toDateEdt;
    private Calendar myCalendar = Calendar.getInstance();
    private Button searchBtn;
    private String searchQuery = "";
    public static String SearchCollectionwithoutPlotQuery = "";
    private String fromDateStr = "";
    private String toDateStr = "";
    private GradingReportModel selectedReport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grading_report);
        dataAccessHandler = new DataAccessHandler(this);
        initUI();

        String currentDate = CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMMYYYY);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        fromDateEdt.setText(sdf.format(new Date()));
        toDateEdt.setText(sdf.format(new Date()));
//
       searchQuery = Queries.getInstance().getGradingReports(currentDate, currentDate);
//        SearchCollectionwithoutPlotQuery = Queries.getInstance().getCollectionCenterReportsWithOutPlot(currentDate, currentDate);
        updateLabel(0);
        updateLabel(1);
   getCollectionCenterReports(searchQuery);
        CommonUtils.currentActivity = this;
        gradingReportRecyclerAdapter = new GradingReportAdapter(GradingReportActivity.this);
        gradingReportRecyclerAdapter.setonPrintSelected((this));
        gradingReportsList.setLayoutManager(new LinearLayoutManager(GradingReportActivity.this, LinearLayoutManager.VERTICAL, false));
        gradingReportsList.setAdapter(gradingReportRecyclerAdapter);

//        String CollectionNetWeight = dataAccessHandler.getOnlyOneValueFromDb(Queries.getInstance().getCollectionNetSum(currentDate, currentDate));
//        String CollectionWithOutPlotNetWeight = dataAccessHandler.getOnlyOneValueFromDb(Queries.getInstance().getCollectionWithOutPlotNetSum(currentDate, currentDate));
//
//        if (CollectionNetWeight == null){
//            CollectionNetWeight = "0.0";
//        }
//        if (CollectionWithOutPlotNetWeight == null){
//            CollectionWithOutPlotNetWeight = "0.0";
//        }
//        Float totalNetWeight = Float.valueOf(CollectionNetWeight) +Float.valueOf(CollectionWithOutPlotNetWeight);
//        totalNetWeightSum.setText(" "+totalNetWeight + " Kgs");
    }

    public void getCollectionCenterReports(final String searchQuery) {
        ProgressBar.showProgressBar(this, "Please wait...");
        ApplicationThread.bgndPost(LOG_TAG, "getting reports data", new Runnable() {
            @Override
            public void run() {
                dataAccessHandler.getGradingReportDetails(searchQuery, new ApplicationThread.OnComplete<List<GradingReportModel>>() {
                    @Override
                    public void execute(boolean success, final List<GradingReportModel> reports, String msg) {
                        ProgressBar.hideProgressBar();
                        if (success) {
                            if (reports != null && reports.size() > 0) {
                                mReportsList.clear();
                                mReportsList = reports;
                                ApplicationThread.uiPost(LOG_TAG, "update ui", new Runnable() {
                                    @Override
                                    public void run() {
                                        int recordsSize = reports.size();
                                        Log.v(LOG_TAG, "data size " + recordsSize);
                                        if (recordsSize > 0) {
                                            gradingReportRecyclerAdapter.updateAdapter(reports);
                                            tvNorecords.setVisibility(View.GONE);
                                            gradingReportsList.setVisibility(View.VISIBLE);
                                          //  setTile(getString(R.string.collection_report) + " ("+recordsSize+")");
                                        } else {
                                            tvNorecords.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            } else {
                                ApplicationThread.uiPost(LOG_TAG, "updating ui", new Runnable() {
                                    @Override
                                    public void run() {
                                        tvNorecords.setVisibility(View.VISIBLE);
                                        Log.v(LOG_TAG, "@@@ No records found");
                                        gradingReportsList.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } else {
                            ApplicationThread.uiPost(LOG_TAG, "updating ui", new Runnable() {
                                @Override
                                public void run() {
                                    tvNorecords.setVisibility(View.VISIBLE);
                                    Log.v(LOG_TAG, "@@@ No records found");
                                    gradingReportsList.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
            }
        });
    }


    private void initUI() {
        gradingReportsList = (RecyclerView) findViewById(R.id.grading_reports_list);
        searchBtn = (Button) findViewById(R.id.searchBtn);
        totalNetWeightSum = (TextView) findViewById(R.id.totalNetWeightSum);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(fromDateStr) && TextUtils.isEmpty(toDateStr)) {
                    UiUtils.showCustomToastMessage("Please select from or to dates", GradingReportActivity.this, 0);
                } else {
//                    String CollectionNetWeight = dataAccessHandler.getOnlyOneValueFromDb(Queries.getInstance().getCollectionNetSum(fromDateStr, toDateStr));
//                    String CollectionWithOutPlotNetWeight = dataAccessHandler.getOnlyOneValueFromDb(Queries.getInstance().getCollectionWithOutPlotNetSum(fromDateStr, toDateStr));
//                    if (CollectionNetWeight == null){
//                        CollectionNetWeight = "0.0";
//                    }
//                    if (CollectionWithOutPlotNetWeight == null){
//                        CollectionWithOutPlotNetWeight = "0.0";
//                    }
//                    Float totalNetWeight = Float.valueOf(CollectionNetWeight) + Float.valueOf(CollectionWithOutPlotNetWeight);
////                    String totalNetWeight = dataAccessHandler.getOnlyOneValueFromDb(Queries.getInstance().getCollectionNetSum(fromDateStr, toDateStr));
//                    if (!TextUtils.isEmpty(String.valueOf(totalNetWeight))) {
//                        totalNetWeightSum.setText(" "+totalNetWeight + " Kgs");
//                    }
                    searchQuery = Queries.getInstance().getGradingReports(fromDateStr, toDateStr);
//                    SearchCollectionwithoutPlotQuery = Queries.getInstance().getCollectionCenterReportsWithOutPlot(fromDateStr, toDateStr);
                    if (null != gradingReportRecyclerAdapter) {
                        mReportsList.clear();
                        gradingReportRecyclerAdapter.notifyDataSetChanged();
                    }
                    getCollectionCenterReports(searchQuery);
                }
            }
        });
        tvNorecords = (TextView) findViewById(R.id.no_records);
        tvNorecords.setVisibility(View.GONE);

        fromDateEdt = (EditText) findViewById(R.id.fromDate);
        toDateEdt = (EditText) findViewById(R.id.toDate);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(0);
            }
        };

        final DatePickerDialog.OnDateSetListener toDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(1);
            }
        };

        toDateEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(GradingReportActivity.this, toDate, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));  //date is dateSetListener as per your code in question
                datePickerDialog.show();
            }
        });

        fromDateEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(GradingReportActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));  //date is dateSetListener as per your code in question
                datePickerDialog.show();
            }
        });
    }
    private void updateLabel(int type) {
        String myFormat = "dd-MM-yyyy";
        String dateFormatter = "yyyy-MM-dd";

        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        SimpleDateFormat sdf2 = new SimpleDateFormat(dateFormatter, Locale.US);

        if (type == 0) {
            fromDateStr = sdf2.format(myCalendar.getTime());
            fromDateEdt.setText(sdf.format(myCalendar.getTime()));
        } else {
            toDateStr = sdf2.format(myCalendar.getTime());
            toDateEdt.setText(sdf.format(myCalendar.getTime()));
        }

    }

    @Override
    public void printOptionSelected(int position) {

    }
}