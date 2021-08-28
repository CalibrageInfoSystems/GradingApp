package com.oilpalm3f.gradingapp.datasync.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.oilpalm3f.gradingapp.FaLogTracking.LocationTracker;
import com.oilpalm3f.gradingapp.cloudhelper.ApplicationThread;
import com.oilpalm3f.gradingapp.cloudhelper.CloudDataHandler;
import com.oilpalm3f.gradingapp.cloudhelper.Config;
import com.oilpalm3f.gradingapp.common.CommonConstants;
import com.oilpalm3f.gradingapp.common.CommonUtils;
import com.oilpalm3f.gradingapp.database.DataAccessHandler;
import com.oilpalm3f.gradingapp.database.DatabaseKeys;
import com.oilpalm3f.gradingapp.database.Queries;
import com.oilpalm3f.gradingapp.dbmodels.GradingFileRepository;
import com.oilpalm3f.gradingapp.uihelper.ProgressBar;
import com.oilpalm3f.gradingapp.utils.UiUtils;

import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class DataSyncHelper {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String LOG_TAG = DataSyncHelper.class.getName();
    public static String PREVIOUS_SYNC_DATE = "previous_sync_date";
    public static LinkedHashMap<String, List> dataToUpdate = new LinkedHashMap<>();
    public static int countCheck, transactionsCheck = 0, imagesCount = 0, reverseSyncTransCount = 0, innerCountCheck = 0;
    public static List<String> refreshtableNamesList = new ArrayList<>();
    public static LinkedHashMap<String, List> refreshtransactionsDataMap = new LinkedHashMap<>();
    private static String IMEINUMBER;
    public static int resetCount;
    public static int FarmerDataCount = 0;
    public static int PlotDataCount = 0;
    public static int AdvanceTourPlan = 0;
    public static int FarmerResetCount;
    public static int PlotResetCount;


    public static synchronized void performMasterSync(final Context context, final boolean firstTimeInsertFinished, final ApplicationThread.OnComplete onComplete) {
        IMEINUMBER = CommonUtils.getIMEInumber(context);
        LinkedHashMap<String, String> syncDataMap = new LinkedHashMap<>();
        syncDataMap.put("LastUpdatedDate", "");
        syncDataMap.put("IMEINumber", IMEINUMBER);
        countCheck = 0;
        final DataAccessHandler dataAccessHandler = new DataAccessHandler(context);
        ProgressBar.showProgressBar(context, "Making data ready for you...");
        CloudDataHandler.getMasterData(Config.live_url + Config.masterSyncUrl, syncDataMap, new ApplicationThread.OnComplete<HashMap<String, List>>() {
            @Override
            public void execute(boolean success, final HashMap<String, List> masterData, String msg) {
                if (success) {
                    if (masterData != null && masterData.size() > 0) {
                        //Log.v(LOG_TAG, "@@@ Master sync is success and data size is " + masterData.size());

                        final Set<String> tableNames = masterData.keySet();
                        masterData.remove("CcRate");
                        for (final String tableName : tableNames) {
                           // Log.v(LOG_TAG, "@@@ Delete Query " + String.format(Queries.getInstance().deleteTableData(), tableName));
                            ApplicationThread.dbPost("Master Data Sync..", "master data", new Runnable() {
                                @Override
                                public void run() {
                                    countCheck++;
                                    if (!firstTimeInsertFinished) {
                                        dataAccessHandler.deleteRow(tableName, null, null, false, new ApplicationThread.OnComplete<String>() {
                                            @Override
                                            public void execute(boolean success, String result, String msg) {
                                                if (success) {
                                                    dataAccessHandler.insertData(true, tableName, masterData.get(tableName), new ApplicationThread.OnComplete<String>() {
                                                        @Override
                                                        public void execute(boolean success, String result, String msg) {
                                                            if (success) {
                                                               //Log.v(LOG_TAG, "@@@ sync success for " + tableName);
                                                            } else {
//                                                                Log.v(LOG_TAG, "@@@ check 1 " + masterData.size() + "...pos " + countCheck);
//                                                                Log.v(LOG_TAG, "@@@ sync failed for " + tableName + " message " + msg);
                                                            }
                                                            if (countCheck == masterData.size()) {
                                                                //Log.v(LOG_TAG, "@@@ Done with master sync " + countCheck);
                                                                ProgressBar.hideProgressBar();
                                                                onComplete.execute(true, null, "Sync is success");
                                                            }
                                                        }
                                                    });
                                                } else {
                                                  //  Log.v(LOG_TAG, "@@@ Master table deletion failed for " + tableName);
                                                }
                                            }
                                        });
                                    } else {
                                        dataAccessHandler.insertData(tableName, masterData.get(tableName), new ApplicationThread.OnComplete<String>() {
                                            @Override
                                            public void execute(boolean success, String result, String msg) {
                                                if (success) {
                                                   // Log.v(LOG_TAG, "@@@ sync success for " + tableName);
                                                } else {
                                                   // Log.v(LOG_TAG, "@@@ check 2 " + masterData.size() + "...pos " + countCheck);
                                                    //Log.v(LOG_TAG, "@@@ sync failed for " + tableName + " message " + msg);
                                                }
                                                if (countCheck == masterData.size()) {
                                                    //Log.v(LOG_TAG, "@@@ Done with master sync " + countCheck);
                                                    ProgressBar.hideProgressBar();
                                                    onComplete.execute(true, null, "Sync is success");
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    } else {
                        ProgressBar.hideProgressBar();
                        Log.v(LOG_TAG, "@@@ Sync is up-to-date");
                        onComplete.execute(true, null, "Sync is up-to-date");
                    }
                } else {
                    ProgressBar.hideProgressBar();
                    onComplete.execute(false, null, "Master sync failed. Please try again");
                }
            }
        });
    }

    public static synchronized void performRefreshTransactionsSync(final Context context, final ApplicationThread.OnComplete onComplete) {
        countCheck = 0;
        transactionsCheck = 0;
        reverseSyncTransCount = 0;
        imagesCount = 0;
        refreshtableNamesList.clear();
        refreshtransactionsDataMap.clear();
        final DataAccessHandler dataAccessHandler = new DataAccessHandler(context);
        ProgressBar.showProgressBar(context, "Sending data to server...");
        ApplicationThread.bgndPost(LOG_TAG, "getting transactions data", new Runnable() {
            @Override
            public void run() {
                getRefreshSyncTransDataMap(context, new ApplicationThread.OnComplete<LinkedHashMap<String, List>>() {
                    @Override
                    public void execute(boolean success, final LinkedHashMap<String, List> transDataMap, String msg) {
                        if (success) {
                            if (transDataMap != null && transDataMap.size() > 0) {
                                Log.v(LOG_TAG, "transactions data size " + transDataMap.size());
                                Set<String> transDataTableNames = transDataMap.keySet();
                                refreshtableNamesList.addAll(transDataTableNames);
                                refreshtransactionsDataMap = transDataMap;
                                sendTrackingData(context, onComplete);
                                postTransactionsDataToCloud(context, refreshtableNamesList.get(transactionsCheck), dataAccessHandler, onComplete);
                            }
                        } else {
                            ProgressBar.hideProgressBar();
                            Log.v(LOG_TAG, "@@@ Transactions sync failed due to data retrieval error");
                            onComplete.execute(false, null, "Transactions sync failed due to data retrieval error");
                        }
                    }
                });
            }
        });

    }

    public static void postTransactionsDataToCloud(final Context context, final String tableName, final DataAccessHandler dataAccessHandler, final ApplicationThread.OnComplete onComplete) {

        List cctransDataList = refreshtransactionsDataMap.get(tableName);

        if (null != cctransDataList && cctransDataList.size() > 0) {
            Type listType = new TypeToken<List>() {
            }.getType();
            Gson gson = new GsonBuilder().serializeNulls().create();

            String dat = gson.toJson(cctransDataList, listType);
            JSONObject transObj = new JSONObject();
            try {
                transObj.put(tableName, new JSONArray(dat));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v(LOG_TAG, "@@@@ check.." + transObj.toString());
            CommonConstants.SyncTableName = tableName;
            CloudDataHandler.placeDataInCloud(context, transObj, Config.live_url + Config.transactionSyncURL, new ApplicationThread.OnComplete<String>() {
                @Override
                public void execute(boolean success, String result, String msg) {
                    if (success) {
                        dataAccessHandler.executeRawQuery(String.format(Queries.getInstance().updateServerUpdatedStatus(), tableName));
                        Log.v(LOG_TAG, "@@@ Transactions sync success for " + tableName);
                        transactionsCheck++;
                        if (transactionsCheck == refreshtransactionsDataMap.size()) {
                            Log.v(LOG_TAG, "@@@ Done with transactions sync " + transactionsCheck);
                            onComplete.execute(true, null, "Sync is success");

                        } else {
                            postTransactionsDataToCloud(context, refreshtableNamesList.get(transactionsCheck), dataAccessHandler, onComplete);
                        }
                    } else {
                        ApplicationThread.uiPost(LOG_TAG, "Sync is failed", new Runnable() {
                            @Override
                            public void run() {
                                UiUtils.showCustomToastMessage("Sync failed for " + tableName, context, 1);
                            }
                        });
                        transactionsCheck++;
                        if (transactionsCheck == refreshtransactionsDataMap.size()) {
                            Log.v(LOG_TAG, "@@@ Done with transactions sync " + transactionsCheck);
//                            final List<ImageDetails> imagesData = dataAccessHandler.getImageDetails();
//                            if (null != imagesData && !imagesData.isEmpty()) {
//                                sendImageDetails(context, imagesData, dataAccessHandler, onComplete);
//                            } else {
//                                ProgressBar.hideProgressBar();
//                                onComplete.execute(true, null, "Sync is success");
//                            }
                        } else {
                            postTransactionsDataToCloud(context, refreshtableNamesList.get(transactionsCheck), dataAccessHandler, onComplete);
                        }
                        Log.v(LOG_TAG, "@@@ Transactions sync failed for " + tableName);
                        Log.v(LOG_TAG, "@@@ Transactions sync due to " + result);

                    }
                }
            });
        } else {
            transactionsCheck++;
            if (transactionsCheck == refreshtransactionsDataMap.size()) {
                Log.v(LOG_TAG, "@@@ Done with transactions sync " + transactionsCheck);
//                final List<ImageDetails> imagesData = dataAccessHandler.getImageDetails();
//                if (null != imagesData && !imagesData.isEmpty()) {
//                    sendImageDetails(context, imagesData, dataAccessHandler, onComplete);
//                } else {
//                    ProgressBar.hideProgressBar();
//                    onComplete.execute(true, null, "Sync is success");
//                    Log.v(LOG_TAG, "@@@ Done with transactions sync " + transactionsCheck);
//
//                }
            } else {
                postTransactionsDataToCloud(context, refreshtableNamesList.get(transactionsCheck), dataAccessHandler, onComplete);
            }
        }
    }

//    public static void sendImageDetails(final Context context, final List<ImageDetails> imagesData, final DataAccessHandler dataAccessHandler, final ApplicationThread.OnComplete onComplete) {
//        Gson gson = new GsonBuilder().serializeNulls().create();
//        String dat = gson.toJson(imagesData.get(imagesCount));
//        JSONObject transObj = null;
//        try {
//            transObj = new JSONObject(dat);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        Log.v(LOG_TAG, "@@@@ check.." + transObj.toString());
//
//        CloudDataHandler.placeDataInCloud(context, transObj, Config.live_url + Config.imageUploadURL, new ApplicationThread.OnComplete<String>() {
//            @Override
//            public void execute(boolean success, String result, String msg) {
//                if (success) {
//                    dataAccessHandler.executeRawQuery(Queries.getInstance().updatedImageDetailsStatus(imagesData.get(imagesCount).getCollectionCode(), imagesData.get(imagesCount).getFarmerCode(),
//                            100));
//                    imagesCount++;
//                    if (imagesCount == imagesData.size()) {
//                        ProgressBar.hideProgressBar();
//                        onComplete.execute(true, "", "sync success");
//                    } else {
//                        sendImageDetails(context, imagesData, dataAccessHandler, onComplete);
//                    }
//                } else {
//                    imagesCount++;
//                    if (imagesCount == imagesData.size()) {
//                        ProgressBar.hideProgressBar();
//                        onComplete.execute(true, "", "sync success");
//                        selectedPlotCode.clear();
//                    } else {
//                        sendImageDetails(context, imagesData, dataAccessHandler, onComplete);
//                    }
//                    onComplete.execute(false, result, "sync failed due to " + msg);
//                }
//            }
//        });
//    }


    private static void getRefreshSyncTransDataMap(final Context context, final ApplicationThread.OnComplete onComplete) {

        final DataAccessHandler dataAccessHandler = new DataAccessHandler(context);

        List<GradingFileRepository> gradingrepoList = (List<GradingFileRepository>) dataAccessHandler.getGradingRepoDetails(Queries.getInstance().getGradingRepoRefresh(), 1);


        LinkedHashMap<String, List> allRefreshDataMap = new LinkedHashMap<>();
        allRefreshDataMap.put(DatabaseKeys.TABLE_Grading_Repository, gradingrepoList);


        onComplete.execute(true, allRefreshDataMap, "here is collection of table transactions data");

    }

//    public static void startTransactionSync(final Context context, final ProgressDialogFragment progressDialogFragment) {
//
//        SharedPreferences sharedPreferences = context.getSharedPreferences("appprefs", MODE_PRIVATE);
//        String date = sharedPreferences.getString(PREVIOUS_SYNC_DATE, null);
//
//        final String finalDate = date;//
//        //final String finalDate = "2021-08-04 18:34:46";//
//        Log.v(LOG_TAG, "@@@ Date " + date);
//        progressDialogFragment.updateText("Getting total records count");
//        final ProgressDialogFragment finalProgressDialogFragment = progressDialogFragment;
//        getCountOfHits(finalDate, new ApplicationThread.OnComplete() {
//            @Override
//            public void execute(boolean success, Object result, String msg) {
//                if (success) {
//                    Log.v(LOG_TAG, "@@@@ count here " + result.toString());
//                    List<DataCountModel> dataCountModelList = (List<DataCountModel>) result;
//                    prepareIndexes(finalDate, dataCountModelList, context, finalProgressDialogFragment);
//                } else {
//                    if (null != finalProgressDialogFragment) {
//                        finalProgressDialogFragment.dismiss();
//                    }
//                    Log.v(LOG_TAG, "Transaction sync failed due to data issue-->" + msg);
//                    UiUtils.showCustomToastMessage("Transaction sync failed due to data issue", context, 1);
//                }
//            }
//        });
//    }


//    public static void prepareIndexes(final String date, List<DataCountModel> countData, final Context context, ProgressDialogFragment progressDialogFragment) {
//        if (!countData.isEmpty()) {
//            reverseSyncTransCount = 0;
//            transactionsCheck = 0;
//            dataToUpdate.clear();
//            final DataAccessHandler dataAccessHandler = new DataAccessHandler(context);
//            new DownLoadData(context, date, countData, 0, 0, dataAccessHandler, progressDialogFragment).execute();
//        } else {
//            ProgressBar.hideProgressBar();
//            if (null != progressDialogFragment) {
//                progressDialogFragment.dismiss();
//            }
//
//            CommonUtils.showMyToast("There is no transactions data to sync", context);
//        }
//    }

//    public static void getCountOfHits(String date, final ApplicationThread.OnComplete onComplete) {
//        String countUrl = "";
//        LinkedHashMap<String, String> syncDataMap = new LinkedHashMap<>();
//        syncDataMap.put("Date", TextUtils.isEmpty(date) ? "null" : date);
//        syncDataMap.put("UserId", CommonConstants.USER_ID);
//        syncDataMap.put("IsUserDataAccess", CommonConstants.migrationSync);
//        countUrl = Config.live_url + Config.getTransCount;
//        CloudDataHandler.getGenericData(countUrl, syncDataMap, new ApplicationThread.OnComplete<List<DataCountModel>>() {
//            @Override
//            public void execute(boolean success, List<DataCountModel> result, String msg) {
//                onComplete.execute(success, result, msg);
//            }
//        });
//    }

//    public static void updateSyncDate(Context context, String date) {
//        Log.v(LOG_TAG, "@@@ saving date into");
//        SharedPreferences sharedPreferences = context.getSharedPreferences("appprefs", MODE_PRIVATE);
//        sharedPreferences.edit().putString(PREVIOUS_SYNC_DATE, date).apply();
//    }
//
//    public static void ableToProceedToTransactionSync(final String password, final ApplicationThread.OnComplete onComplete) {
//        CloudDataHandler.getGenericData(Config.live_url + String.format(Config.validateTranSync, password), new ApplicationThread.OnComplete<String>() {
//            @Override
//            public void execute(boolean success, String result, String msg) {
//                onComplete.execute(success, result, msg);
//            }
//        });
//    }

//    private static void updateOrInsertData(final String tableName, List dataToInsert, String whereCondition, boolean recordExisted, DataAccessHandler dataAccessHandler, final ApplicationThread.OnComplete onComplete) {
//        if (recordExisted) {
//            dataAccessHandler.updateData(tableName, dataToInsert, true, whereCondition, new ApplicationThread.OnComplete<String>() {
//                @Override
//                public void execute(boolean success, String result, String msg) {
//                    onComplete.execute(success, null, "Sync is " + success + " for " + tableName);
//                }
//            });
//        } else {
//            dataAccessHandler.insertData(tableName, dataToInsert, new ApplicationThread.OnComplete<String>() {
//                @Override
//                public void execute(boolean success, String result, String msg) {
//                    onComplete.execute(true, null, "Sync is " + success + " for " + tableName);
//                }
//            });
//        }
//    }

//    private static synchronized void updateDataIntoDataBase(final LinkedHashMap<String, List> transactionsData, final DataAccessHandler dataAccessHandler, final String tableName, final ApplicationThread.OnComplete onComplete) {
//        final List dataList = transactionsData.get(tableName);
//        List dataToInsert = new ArrayList();
//        JSONObject ccData = null;
//        Gson gson = new GsonBuilder().serializeNulls().create();
//
//        boolean recordExisted = false;
//        String whereCondition = null;
//
//        if (dataList.size() > 0) {
//            if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_FILEREPOSITORY)) {
//                FileRepository fileRepository = (FileRepository) dataList.get(innerCountCheck);
//                whereCondition = " where  Code = '" + fileRepository.getFarmercode() + "'";
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "FarmerCode", fileRepository.getFarmercode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_ADDRESS)) {
//                Address addressData = (Address) dataList.get(innerCountCheck);
//                addressData.setServerupdatedstatus(1);
//                whereCondition = " where  Code = '" + addressData.getCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(addressData));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "Code", addressData.getCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_FARMER)) {
//                Farmer farmerData = (Farmer) dataList.get(innerCountCheck);
//                farmerData.setServerupdatedstatus(1);
//                whereCondition = " where  Code = '" + farmerData.getCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(farmerData));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                    recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "Code", farmerData.getCode()));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PLOT)) {
//                Plot plotData = (Plot) dataList.get(innerCountCheck);
//                plotData.setServerupdatedstatus(1);
//                whereCondition = " where  Code= '" + plotData.getCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(plotData));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "Code", plotData.getCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_FARMERHISTORY)) {
//                FarmerHistory farmerHistoryData = (FarmerHistory) dataList.get(innerCountCheck);
//                farmerHistoryData.setServerUpdatedStatus(1);
//                whereCondition = " where  PlotCode = '" + farmerHistoryData.getPlotcode() + "' and StatusTypeId = '" + farmerHistoryData.getStatustypeid() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(farmerHistoryData));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInFarmerHistoryTable(tableName, "PlotCode", farmerHistoryData.getPlotcode(), String.valueOf(farmerHistoryData.getStatustypeid())));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PLANTATION)) {
//                Plantation plantation = (Plantation) dataList.get(innerCountCheck);
//                plantation.setServerUpdatedStatus(1);
//                whereCondition = " where  PlotCode = '" + plantation.getPlotcode() + "' and " +
//                        "  CreatedByUserId = " + plantation.getCreatedbyuserid() + " and " +
//                        "  GFReceiptNumber = '" + plantation.getGFReceiptNumber() + "' and" +
//                        " datetime(CreatedDate) = datetime('" + plantation.getCreateddate() + "')";
//                try {
//                    ccData = new JSONObject(gson.toJson(plantation));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkPlantationRecordStatusInTable(tableName, plantation.getPlotcode(), plantation.getCreatedbyuserid(), plantation.getCreateddate(), plantation.getGFReceiptNumber()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_UPROOTMENT)) {
//                Uprootment uprootment = (Uprootment) dataList.get(innerCountCheck);
//                uprootment.setServerupdatedstatus(1);
//                whereCondition = " where  PlotCode = '" + uprootment.getPlotcode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(uprootment));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "PlotCode", uprootment.getPlotcode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PEST)) {
//                Pest pest = (Pest) dataList.get(innerCountCheck);
//                pest.setServerUpdatedStatus(1);
//                whereCondition = " where  PlotCode = '" + pest.getPlotCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(pest));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "PlotCode", pest.getPlotCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PESTCHEMICALXREF)) {
//                PestChemicalXref pestChemicalXref = (PestChemicalXref) dataList.get(innerCountCheck);
//                pestChemicalXref.setServerUpdatedStatus(1);
//                whereCondition = " where  PestCode = '" + pestChemicalXref.getPestCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(pestChemicalXref));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "PestCode", pestChemicalXref.getPestCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_NUTRIENT)) {
//                Nutrient nutrient = (Nutrient) dataList.get(innerCountCheck);
//                nutrient.setServerUpdatedStatus(1);
//                whereCondition = " where  PlotCode = '" + nutrient.getPlotcode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(nutrient));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "PlotCode", nutrient.getPlotcode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_WEED)) {
//                Weed weed = (Weed) dataList.get(innerCountCheck);
//                weed.setServerupdatedstatus(1);
//                whereCondition = " where  PlotCode = '" + weed.getPlotCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(weed));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "PlotCode", weed.getPlotCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COLLECTION)) {
//                Collection data = (Collection) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where Code = '" + data.getCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "Code", data.getCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COLLECTIONPLOTXREF)) {
//                CollectionPlotXref data = (CollectionPlotXref) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where CollectionCode = '" + data.getCollectionCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "CollectionCode", data.getCollectionCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_HEALTHOFPLANTATIONDETAILS)) {
//                Healthplantation data = (Healthplantation) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where PlotCode = '" + data.getPlotCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "PlotCode", data.getPlotCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_GEOBOUNDARIES)) {
//                GeoBoundaries data = (GeoBoundaries) dataList.get(innerCountCheck);
//                data.setServerupdatedstatus(1);
//                whereCondition = " where PlotCode = '" + data.getPlotcode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "PlotCode", data.getPlotcode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_FARMERBANK)) {
//                FarmerBank data = (FarmerBank) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where FarmerCode = '" + data.getFarmercode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "FarmerCode", data.getFarmercode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_IDENTITYPROOF)) {
//                IdentityProof data = (IdentityProof) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where FarmerCode = '" + data.getFarmercode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "FarmerCode", data.getFarmercode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PLOTLANDLORD)) {
//                PlotLandlord data = (PlotLandlord) dataList.get(innerCountCheck);
//                data.setServerupdatedstatus(1);
//                whereCondition = " where PlotCode = '" + data.getPlotcode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "PlotCode", data.getPlotcode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_LANDLORDBANK)) {
//                LandlordBank data = (LandlordBank) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where PlotCode = '" + data.getPlotcode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "PlotCode", data.getPlotcode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_LANDLORDIDPROOFS)) {
//                LandlordIdProof data = (LandlordIdProof) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where PlotCode = '" + data.getPlotCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "PlotCode", data.getPlotCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_CROPMAINTENANCEHISTORY)) {
//                CropMaintenanceHistory data = (CropMaintenanceHistory) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where Code = '" + data.getCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "Code", data.getCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COMPLAINT)) {
//                Complaints data = (Complaints) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where Code = '" + data.getCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "Code", data.getCode()));
//
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COMPLAINTSTATUSHISTORY)) {
//                ComplaintStatusHistory data = (ComplaintStatusHistory) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where ComplaintCode = '" + data.getComplaintCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "ComplaintCode", "StatusTypeId", "IsActive", data.getComplaintCode(), data.getStatusTypeId(), data.getIsActive()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COMPLAINTREPOSITORY)) {
//                ComplaintRepository data = (ComplaintRepository) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where ComplaintCode = '" + data.getComplaintCode() + "'" + " and FileExtension = " + "'" + data.getFileExtension() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "ComplaintCode", "FileExtension", "FileName", data.getComplaintCode(), data.getFileExtension(), data.getFileName()));
//                if (recordExisted && data.getFileExtension().equalsIgnoreCase(".mp3")) {
//                    CommonUtils.checkAndDeleteFile(CommonUtils.getAudioFilePath(data.getComplaintCode() + ".mp3"));
//                }
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COMPLAINTTYPEXREF)) {
//                ComplaintTypeXref data = (ComplaintTypeXref) dataList.get(innerCountCheck);
//                data.setServerUpdatedStatus(1);
//                whereCondition = " where ComplaintCode = '" + data.getComplaintCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "ComplaintCode", data.getComplaintCode()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_ADVANCED_DETAILS)) {
//                AdvancedDetails data = (AdvancedDetails) dataList.get(innerCountCheck);
//
//                whereCondition = " where PlotCode = '" + data.getPlotCode() + "' and ReceiptNumber = '" + data.getReceiptNumber() + "' and CreatedDate = '" + data.getCreatedDate() + "' ";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
//                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInAdvanceDetailsTable(data.getPlotCode(), data.getReceiptNumber(), data.getCreatedDate()));
//            } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_NURSERYSAPLING_DETAILS)) {
//                NurserySaplingDetails data = (NurserySaplingDetails) dataList.get(innerCountCheck);
//
////                whereCondition = " where ComplaintCode = '" + data.getComplaintCode() + "'";
//                try {
//                    ccData = new JSONObject(gson.toJson(data));
//                    dataToInsert.add(CommonUtils.toMap(ccData));
//                } catch (JSONException e) {
//                    Log.e(LOG_TAG, "####" + e.getLocalizedMessage());
//                }
////                recordExisted = dataAccessHandler.checkValueExistedInDatabase(Queries.getInstance().checkRecordStatusInTable(tableName, "ComplaintCode", data.getComplaintCode()));
//            }
//
//            if (dataList.size() != innerCountCheck) {
//                updateOrInsertData(tableName, dataToInsert, whereCondition, recordExisted, dataAccessHandler, new ApplicationThread.OnComplete() {
//                    @Override
//                    public void execute(boolean success, Object result, String msg) {
//                        innerCountCheck++;
//                        if (innerCountCheck == dataList.size()) {
//                            innerCountCheck = 0;
//                            onComplete.execute(true, "", "");
//                        } else {
//                            updateDataIntoDataBase(transactionsData, dataAccessHandler, tableName, onComplete);
//                        }
//                    }
//                });
//            } else {
//                onComplete.execute(true, "", "");
//            }
//        } else {
//            innerCountCheck++;
//            if (innerCountCheck == dataList.size()) {
//                innerCountCheck = 0;
//                onComplete.execute(true, "", "");
//            } else {
//                updateDataIntoDataBase(transactionsData, dataAccessHandler, tableName, onComplete);
//            }
//        }
//
//    }

//    public static synchronized void updateTransactionData(final LinkedHashMap<String, List> transactionsData, final DataAccessHandler dataAccessHandler, final List<String> tableNames, final ProgressDialogFragment progressDialogFragment, final ApplicationThread.OnComplete onComplete) {
//        progressDialogFragment.updateText("Updating data...");
//        if (transactionsData != null && transactionsData.size() > 0) {
//            Log.v(LOG_TAG, "@@@ Transactions sync is success and data size is " + transactionsData.size());
//            final String tableName = tableNames.get(reverseSyncTransCount);
//            innerCountCheck = 0;
//            updateDataIntoDataBase(transactionsData, dataAccessHandler, tableName, new ApplicationThread.OnComplete() {
//                @Override
//                public void execute(boolean success, Object result, String msg) {
//                    if (success) {
//                        reverseSyncTransCount++;
//                        if (reverseSyncTransCount == transactionsData.size()) {
//                            onComplete.execute(success, "data updated successfully", "");
//                        } else {
//                            updateTransactionData(transactionsData, dataAccessHandler, tableNames, progressDialogFragment, onComplete);
//                        }
//                    } else {
//                        reverseSyncTransCount++;
//                        if (reverseSyncTransCount == transactionsData.size()) {
//                            onComplete.execute(success, "data updated successfully", "");
//                        } else {
//                            updateTransactionData(transactionsData, dataAccessHandler, tableNames, progressDialogFragment, onComplete);
//                        }
//                    }
//                }
//            });
//        } else {
//            onComplete.execute(false, "data not found to save", "");
//        }
//    }

    public static void sendTrackingData(final Context context, final ApplicationThread.OnComplete onComplete) {
        final DataAccessHandler dataAccessHandler = new DataAccessHandler(context);
        List<LocationTracker> gpsTrackingList = (List<LocationTracker>) dataAccessHandler.getGpsTrackingData(Queries.getInstance().getGpsTrackingRefresh(), 1);
        if (null != gpsTrackingList && !gpsTrackingList.isEmpty()) {
            Type listType = new TypeToken<List>() {
            }.getType();
            Gson gson = null;
            gson = new GsonBuilder().serializeNulls().create();
            String dat = gson.toJson(gpsTrackingList, listType);
            JSONObject transObj = new JSONObject();
            try {
                transObj.put(DatabaseKeys.TABLE_Location_TRACKING_DETAILS, new JSONArray(dat));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v(LOG_TAG, "@@@@ check.." + transObj.toString());
            CloudDataHandler.placeDataInCloud(context, transObj, Config.live_url + Config.locationTrackingURL, new ApplicationThread.OnComplete<String>() {
                        @Override
                        public void execute(boolean success, String result, String msg) {
                            if (success) {
                                dataAccessHandler.executeRawQuery(String.format(Queries.getInstance().updateServerUpdatedStatus(), DatabaseKeys.TABLE_Location_TRACKING_DETAILS));
                                Log.v(LOG_TAG, "@@@ Transactions sync success for " + DatabaseKeys.TABLE_Location_TRACKING_DETAILS);
                                onComplete.execute(true, null, "Sync is success");
                            } else {
                                onComplete.execute(false, null, "Sync is failed");
                            }
                        }
                    }
            );

        }
    }

//    public static void getAlertsData(final Context context, final ApplicationThread.OnComplete<String> onComplete) {
//        CloudDataHandler.getGenericData(Config.live_url + Config.GET_ALERTS + CommonConstants.USER_ID, new ApplicationThread.OnComplete<String>() {
//            @Override
//            public void execute(boolean success, String result, String msg) {
//                if (success) {
//                    final DataAccessHandler dataAccessHandler = new DataAccessHandler(context);
//                    dataAccessHandler.executeRawQuery("delete from Alerts");
//                    LinkedHashMap<String, List> dataMap = new LinkedHashMap<>();
//                    JSONArray resultArray = null;
//                    try {
//                        resultArray = new JSONArray(result);
////                        dataMap.put("Alerts", CommonUtils.toList(resultArray));
//                        List dataList = new ArrayList();
//                        dataList.add(CommonUtils.toList(resultArray));
//                        dataAccessHandler.insertData(DatabaseKeys.TABLE_ALERTS, CommonUtils.toList(resultArray), new ApplicationThread.OnComplete<String>() {
//                            @Override
//                            public void execute(boolean success, String result, String msg) {
//                                if (success) {
//                                    onComplete.execute(true, "", "");
//                                } else {
//                                    onComplete.execute(false, "", "");
//                                }
//                            }
//                        });
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        onComplete.execute(false, "", "");
//                    }
//                } else {
//                    onComplete.execute(false, "", "");
//                }
//            }
//        });
//    }

//    public static class DownLoadData extends AsyncTask<String, String, String> {
//
//        private static final MediaType TEXT_PLAIN = MediaType.parse("application/x-www-form-urlencoded");
//        private Context context;
//        private String date;
//        private List<DataCountModel> totalData;
//        private int totalDataCount;
//        private int currentIndex;
//        private DataAccessHandler dataAccessHandler;
//        private ProgressDialogFragment progressDialogFragment;
//
//
//        public DownLoadData(final Context context, final String date, final List<DataCountModel> totalData, int totalDataCount, int currentIndex, DataAccessHandler dataAccessHandler, ProgressDialogFragment progressDialogFragment) {
//            this.context = context;
//            this.totalData = totalData;
//            this.date = date;
//            this.totalDataCount = totalDataCount;
//            this.currentIndex = currentIndex;
//            this.dataAccessHandler = dataAccessHandler;
//            this.progressDialogFragment = progressDialogFragment;
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            String resultMessage = null;
//            Response response = null;
//            String countUrl = Config.live_url + String.format(Config.getTransData, totalData.get(totalDataCount).getMethodName());
//            Log.v(LOG_TAG, "@@@ data sync url " + countUrl);
//            final String tableName = totalData.get(totalDataCount).getTableName();
//
//            progressDialogFragment.updateText("Downloading " + tableName + " (" + currentIndex + "/" + totalData.get(totalDataCount).getCount() + ")" + " data");
//            if (currentIndex == 0) {
//                if (tableName.equalsIgnoreCase("Farmer")) {
//                    FarmerDataCount = totalData.get(totalDataCount).getCount();
//                } else if (tableName.equalsIgnoreCase("Plot")) {
//                    PlotDataCount = totalData.get(totalDataCount).getCount();
//                }
//            }
//            try {
//                URL obj = new URL(countUrl);
//                Map<String, String> syncDataMap = new LinkedHashMap<>();
//                syncDataMap.put("Date", TextUtils.isEmpty(date) ? "null" : date);
//                syncDataMap.put("UserId", CommonConstants.USER_ID);
//                syncDataMap.put("IsUserDataAccess", CommonConstants.migrationSync);
//                syncDataMap.put("Index", String.valueOf(currentIndex));
//                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//                con.setRequestMethod("POST");
//                con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setRequestProperty("User-Agent", USER_AGENT);
//
//                final StringBuilder sb = new StringBuilder();
//                boolean first = true;
//                RequestBody requestBody = null;
//                for (Map.Entry<String, String> entry : syncDataMap.entrySet()) {
//                    if (first) first = false;
//                    else sb.append("&");
//
//                    sb.append(URLEncoder.encode(entry.getKey(), HTTP.UTF_8)).append("=")
//                            .append(URLEncoder.encode(entry.getValue().toString(), HTTP.UTF_8));
//
//                    Log.d(LOG_TAG, "\nposting key: " + entry.getKey() + " -- value: " + entry.getValue());
//                }
//                requestBody = RequestBody.create(TEXT_PLAIN, sb.toString());
//
//                Request request = HttpClient.buildRequest(countUrl, "POST", (requestBody != null) ? requestBody : RequestBody.create(TEXT_PLAIN, "")).build();
//                OkHttpClient client = getOkHttpClient();
//                response = client.newCall(request).execute();
//                int statusCode = response.code();
//
//                final String strResponse = response.body().string();
//
//
//                Log.d(LOG_TAG, " ############# POST RESPONSE ################ (" + statusCode + ")\n\n" + strResponse + "\n\n");
//                JSONArray dataArray = new JSONArray(strResponse);
//
//                if (statusCode == HttpURLConnection.HTTP_OK) {
//
//                    if (TextUtils.isEmpty(date)) {
//                        if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COMPLAINTTYPEXREF)) {
//                            Log.v(LOG_TAG, "@@@@ Data insertion status comp ");
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COMPLAINTREPOSITORY)) {
//                            Log.v(LOG_TAG, "@@@@ Data insertion status comp2 ");
//                        }
//                        List dataToInsert = new ArrayList();
//                        for (int i = 0; i < dataArray.length(); i++) {
//                            JSONObject eachDataObject = dataArray.getJSONObject(i);
//                            dataToInsert.add(CommonUtils.toMap(eachDataObject));
//                        }
//                        dataAccessHandler.insertData(tableName, dataToInsert, new ApplicationThread.OnComplete<String>() {
//                            @Override
//                            public void execute(boolean success, String result, String msg) {
//                                if (success) {
//                                    Log.v(LOG_TAG, "@@@@ Data insertion status " + result);
//                                    if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_FARMER)) {
//                                        if (currentIndex == 0) {
//                                            FarmerResetCount = 1;
//                                        } else {
//                                            FarmerResetCount = FarmerResetCount + 1;
//                                        }
//                                    } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PLOT)) {
//                                        if (currentIndex == 0) {
//                                            PlotResetCount = 1;
//                                        } else {
//                                            PlotResetCount = PlotResetCount + 1;
//                                        }
//                                    }
//                                } else {
//                                    Log.v(LOG_TAG, "@@@@ Data insertion Failed In Table-" + tableName + "Due to" + result);
//                                }
//
//                            }
//                        });
//                    } else {
//                        if (tableName.equalsIgnoreCase("FileRepository")) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<FileRepository>>() {
//                            }.getType();
//                            List<FileRepository> fileRepositoryInnerList = gson.fromJson(dataArray.toString(), type);
//                            if (null != fileRepositoryInnerList && fileRepositoryInnerList.size() > 0)
//                                dataToUpdate.put(tableName, fileRepositoryInnerList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_ADDRESS)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Address>>() {
//                            }.getType();
//                            List<Address> addressDataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != addressDataList && addressDataList.size() > 0)
//                                dataToUpdate.put(tableName, addressDataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_FARMER)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Farmer>>() {
//                            }.getType();
//                            List<Farmer> farmerDataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != farmerDataList && farmerDataList.size() > 0)
//                                dataToUpdate.put(tableName, farmerDataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PLOT)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Plot>>() {
//                            }.getType();
//                            List<Plot> plotDataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != plotDataList && plotDataList.size() > 0)
//                                dataToUpdate.put(tableName, plotDataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_FARMERHISTORY)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<FarmerHistory>>() {
//                            }.getType();
//                            List<FarmerHistory> farmerHistoryDataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != farmerHistoryDataList && farmerHistoryDataList.size() > 0)
//                                dataToUpdate.put(tableName, farmerHistoryDataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_HEALTHPLANTATION)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Healthplantation>>() {
//                            }.getType();
//                            List<Healthplantation> healthplantationList = gson.fromJson(dataArray.toString(), type);
//                            if (null != healthplantationList && healthplantationList.size() > 0)
//                                dataToUpdate.put(tableName, healthplantationList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PEST)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Pest>>() {
//                            }.getType();
//                            List<Pest> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PESTCHEMICALXREF)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<PestChemicalXref>>() {
//                            }.getType();
//                            List<PestChemicalXref> dataList = gson.fromJson(dataArray.toString(), type);
//                            dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_NUTRIENT)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Nutrient>>() {
//                            }.getType();
//                            List<Nutrient> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_DISEASE)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Disease>>() {
//                            }.getType();
//                            List<Disease> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_WEED)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Weed>>() {
//                            }.getType();
//                            List<Weed> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COLLECTION)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Collection>>() {
//                            }.getType();
//                            List<Collection> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COLLECTIONPLOTXREF)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<CollectionPlotXref>>() {
//                            }.getType();
//                            List<CollectionPlotXref> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_GEOBOUNDARIES)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<GeoBoundaries>>() {
//                            }.getType();
//                            List<GeoBoundaries> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PLANTATION)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Plantation>>() {
//                            }.getType();
//                            List<Plantation> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_FARMERBANK)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<FarmerBank>>() {
//                            }.getType();
//                            List<Plantation> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_IDENTITYPROOF)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<IdentityProof>>() {
//                            }.getType();
//                            List<Plantation> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_PLOTLANDLORD)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<PlotLandlord>>() {
//                            }.getType();
//                            List<Plantation> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_LANDLORDBANK)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<LandlordBank>>() {
//                            }.getType();
//                            List<Plantation> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_LANDLORDIDPROOFS)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<LandlordIdProof>>() {
//                            }.getType();
//                            List<Plantation> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_CROPMAINTENANCEHISTORY)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<CropMaintenanceHistory>>() {
//                            }.getType();
//                            List<CropMaintenanceHistory> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COMPLAINT)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<Complaints>>() {
//                            }.getType();
//                            List<Complaints> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COMPLAINTSTATUSHISTORY)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<ComplaintStatusHistory>>() {
//                            }.getType();
//                            List<ComplaintStatusHistory> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COMPLAINTREPOSITORY)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<ComplaintRepository>>() {
//                            }.getType();
//                            List<ComplaintRepository> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_COMPLAINTTYPEXREF)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<ComplaintTypeXref>>() {
//                            }.getType();
//                            List<ComplaintTypeXref> dataList = gson.fromJson(dataArray.toString(), type);
//                            if (null != dataList && dataList.size() > 0)
//                                dataToUpdate.put(tableName, dataList);
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_RECOMMND_FERTLIZER)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<RecommndFertilizer>>() {
//                            }.getType();
//                            List<RecommndFertilizer> recommndFertilizerList = gson.fromJson(dataArray.toString(), type);
//                            if (null != recommndFertilizerList && recommndFertilizerList.size() > 0)
//                                dataToUpdate.put(tableName, recommndFertilizerList);
//
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_ADVANCED_DETAILS)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<AdvancedDetails>>() {
//                            }.getType();
//                            List<AdvancedDetails> advancedDtails = gson.fromJson(dataArray.toString(), type);
//                            if (null != advancedDtails && advancedDtails.size() > 0)
//                                dataToUpdate.put(tableName, advancedDtails);
//
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_NURSERYSAPLING_DETAILS)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<NurserySaplingDetails>>() {
//                            }.getType();
//                            List<NurserySaplingDetails> advancedDtails = gson.fromJson(dataArray.toString(), type);
//                            if (null != advancedDtails && advancedDtails.size() > 0)
//                                dataToUpdate.put(tableName, advancedDtails);
//
//                        } else if (tableName.equalsIgnoreCase(DatabaseKeys.TABLE_visit_Details)) {
//                            Gson gson = new Gson();
//                            Type type = new TypeToken<List<VisitRequests>>() {
//                            }.getType();
//                            List<VisitRequests> visitDtails = gson.fromJson(dataArray.toString(), type);
//                            if (null != visitDtails && visitDtails.size() > 0)
//                                dataToUpdate.put(tableName, visitDtails);
//
//                        }
//                    }
//                    resultMessage = "success";
//                } else {
//                    resultMessage = "failed";
//                }
//            } catch (Exception e) {
//                resultMessage = e.getMessage();
//                Log.e(LOG_TAG, "@@@ data sync failed for " + tableName);
//            }
//            return resultMessage;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            currentIndex++;
//            if (currentIndex == totalData.get(totalDataCount).getCount()) {
//                currentIndex = 0;
//                totalDataCount++;
//                if (totalDataCount == totalData.size()) {
//                    Log.v(LOG_TAG, "@@@ done with data syncing");
//                    if (TextUtils.isEmpty(date)) {
//                        ProgressBar.hideProgressBar();
//                        if (null != progressDialogFragment && !CommonUtils.currentActivity.isFinishing()) {
//                            progressDialogFragment.dismiss();
//
//                        }
//                        Integer resetFarmerCount = Integer.parseInt(dataAccessHandler.getOnlyOneValueFromDb(Queries.getInstance().getFarmerCount()));
//                        Integer resetPlotCount = Integer.parseInt(dataAccessHandler.getOnlyOneValueFromDb(Queries.getInstance().getPlotCount()));
//
//                        if ((FarmerDataCount == FarmerResetCount) && (PlotDataCount == PlotResetCount)) {
//                            if (resetFarmerCount != null && resetPlotCount != null) {
//
//                                UiUtils.showCustomToastMessage("Data synced successfully", context, 0);
//                                updateSyncDate(context, CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMMYYYY_HHMMSS));
//
//                            } else {
//                                UiUtils.showCustomToastMessage("Data is not Synced Properly Again its DownLoading the Data ", context, 1);
//                                if (CommonUtils.isNetworkAvailable(context)) {
//                                    updateSyncDate(context, null);
//                                    for (String s : RefreshSyncActivity.allRefreshDataMap) {
//                                        dataAccessHandler.executeRawQuery("DELETE FROM " + s);
//                                        Log.v(LOG_TAG, "delete table" + s);
//                                    }
//                                    progressDialogFragment = new ProgressDialogFragment();
//                                    startTransactionSync(context, progressDialogFragment);
//                                } else {
//                                    UiUtils.showCustomToastMessage("Please check network connection", context, 1);
//                                }
//
//                            }
//
//                        } else {
//                            UiUtils.showCustomToastMessage("Data is not Synced Properly Again its DownLoading the Data", context, 1);
//                            if (CommonUtils.isNetworkAvailable(context)) {
//                                updateSyncDate(context, null);
//                                for (String s : RefreshSyncActivity.allRefreshDataMap) {
//                                    dataAccessHandler.executeRawQuery("DELETE FROM " + s);
//                                    Log.v(LOG_TAG, "delete table" + s);
//                                }
//                                progressDialogFragment = new ProgressDialogFragment();
//                                startTransactionSync(context, progressDialogFragment);
//
//                            } else {
//                                UiUtils.showCustomToastMessage("Please check network connection", context, 1);
//                            }
//                        }
//
//                    } else {
//                        reverseSyncTransCount = 0;
//                        Set tableNames = dataToUpdate.keySet();
//                        List<String> tableNamesList = new ArrayList();
//                        tableNamesList.addAll(tableNames);
//                        updateTransactionData(dataToUpdate, dataAccessHandler, tableNamesList, progressDialogFragment, new ApplicationThread.OnComplete() {
//                            @Override
//                            public void execute(boolean success, Object result, String msg) {
//                                if (success) {
//                                    updateSyncDate(context, CommonUtils.getcurrentDateTime(CommonConstants.DATE_FORMAT_DDMMYYYY_HHMMSS));
//                                    UiUtils.showCustomToastMessage("Data synced successfully", context, 0);
//                                } else {
//                                    UiUtils.showCustomToastMessage(msg, context, 1);
//                                }
//                                if (null != progressDialogFragment && !CommonUtils.currentActivity.isFinishing()) {
//                                    progressDialogFragment.dismiss();
//                                }
//                            }
//                        });
//                    }
//                } else {
//                    Log.v(LOG_TAG, "@@@ data downloading next count " + currentIndex + " out of " + totalData.size());
//                    new DownLoadData(context, date, totalData, totalDataCount, currentIndex, dataAccessHandler, progressDialogFragment).execute();
//                }
//            } else {
//                Log.v(LOG_TAG, "@@@ data downloading next count " + currentIndex + " out of " + totalData.size());
//                new DownLoadData(context, date, totalData, totalDataCount, currentIndex, dataAccessHandler, progressDialogFragment).execute();
//            }
//        }
//    }
}
