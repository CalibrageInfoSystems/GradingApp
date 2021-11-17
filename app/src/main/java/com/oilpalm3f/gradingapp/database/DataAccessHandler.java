package com.oilpalm3f.gradingapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.oilpalm3f.gradingapp.cloudhelper.ApplicationThread;
import com.oilpalm3f.gradingapp.common.CommonUtils;
import com.oilpalm3f.gradingapp.dbmodels.GradingFileRepository;
import com.oilpalm3f.gradingapp.dbmodels.GradingReportModel;
import com.oilpalm3f.gradingapp.dbmodels.UserDetails;
import com.oilpalm3f.gradingapp.utils.ImageUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;


public class DataAccessHandler <T> {

    private static final String LOG_TAG = DataAccessHandler.class.getName();

    private Context context;
    private SQLiteDatabase mDatabase;
    private String var = "";
    String queryForLookupTable = "select Name from LookUp where id=" + var;
    private int value;

    public DataAccessHandler() {

    }

    SimpleDateFormat simpledatefrmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    String currentTime = simpledatefrmt.format(new Date());


    public DataAccessHandler(final Context context) {
        this.context = context;
        try {
            mDatabase = Palm3FoilDatabase.openDataBaseNew();
            DataBaseUpgrade.upgradeDataBase(context, mDatabase);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public DataAccessHandler(final Context context, boolean firstTime) {
        this.context = context;
        try {
            mDatabase = Palm3FoilDatabase.openDataBaseNew();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //To Get Only One Int value from DB
    public Integer getOnlyOneIntValueFromDb(String query) {
        Log.v(LOG_TAG, "@@@ query " + query);
        Cursor mOprQuery = null;
        try {
            mOprQuery = mDatabase.rawQuery(query, null);
            if (mOprQuery != null && mOprQuery.moveToFirst()) {
                return mOprQuery.getInt(0);
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != mOprQuery)
                mOprQuery.close();

            closeDataBase();
        }
        return null;
    }
    //To Get Only One value from DB
    public String getOnlyOneValueFromDb(String query) {
        Log.v(LOG_TAG, "@@@ query " + query);
        Cursor mOprQuery = null;
        try {
            mOprQuery = mDatabase.rawQuery(query, null);
            if (mOprQuery != null && mOprQuery.moveToFirst()) {
                return mOprQuery.getString(0);
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != mOprQuery)
                mOprQuery.close();

            closeDataBase();
        }
        return "";
    }


    //To insert Data received from Server
    public synchronized void insertData(boolean fromMaster, String tableName, List<LinkedHashMap> mapList, final ApplicationThread.OnComplete<String> oncomplete) {
        int checkCount = 0;
        try {
            List<ContentValues> values1 = new ArrayList<>();
            for (int i = 0; i < mapList.size(); i++) {
                checkCount++;
                List<LinkedHashMap.Entry> entryList = new ArrayList<>((mapList.get(i)).entrySet());

                ContentValues contentValues = new ContentValues();
                for (LinkedHashMap.Entry temp : entryList) {
                    String keyToInsert = temp.getKey().toString();
                    if (keyToInsert.equalsIgnoreCase("ServerUpdatedStatus")) {
                        contentValues.put(keyToInsert, "1");
                    } else {
                        contentValues.put(temp.getKey().toString(), temp.getValue().toString());
                    }
                }
                values1.add(contentValues);
            }
            Log.v(LOG_TAG, "@@@@ log check " + checkCount + " here " + values1.size());
            boolean hasError = bulkinserttoTable(values1, tableName);
            if (hasError) {
                Log.v(LOG_TAG, "@@@ Error while inserting data ");
                if (null != oncomplete) {
                    oncomplete.execute(false, "failed to insert data", "");
                }
            } else {
                Log.v(LOG_TAG, "@@@ data inserted successfully for table :" + tableName);
                if (null != oncomplete) {
                    oncomplete.execute(true, "data inserted successfully", "");
                }
            }
        } catch (Exception e) {
            checkCount++;
            e.printStackTrace();
            Log.v(LOG_TAG, "@@@@ exception log check " + checkCount + " here " + mapList.size());
            if (checkCount == mapList.size()) {
                if (null != oncomplete)
                    oncomplete.execute(false, "data insertion failed", "" + e.getMessage());
            }
        } finally {
            closeDataBase();
        }
    }

    //Save Data into the table
    public synchronized void savedata(boolean fromMaster, String tableName, List<LinkedHashMap> mapList, final ApplicationThread.OnComplete<String> oncomplete) {
        int checkCount = 0;
        try {
            List<ContentValues> values1 = new ArrayList<>();
            for (int i = 0; i < mapList.size(); i++) {
                checkCount++;
                List<LinkedHashMap.Entry> entryList = new ArrayList<>((mapList.get(i)).entrySet());

                ContentValues contentValues = new ContentValues();
                for (LinkedHashMap.Entry temp : entryList) {
                    String keyToInsert = temp.getKey().toString();
                    if (keyToInsert.equalsIgnoreCase("ServerUpdatedStatus")) {
                        contentValues.put(keyToInsert, "0");
                    } else {
                        contentValues.put(temp.getKey().toString(), temp.getValue().toString());
                    }
                }
                values1.add(contentValues);
            }
            Log.v(LOG_TAG, "@@@@ log check " + checkCount + " here " + values1.size());
            boolean hasError = bulkinserttoTable(values1, tableName);
            if (hasError) {
                Log.v(LOG_TAG, "@@@ Error while inserting data ");
                if (null != oncomplete) {
                    oncomplete.execute(false, "failed to insert data", "");
                }
            } else {
                Log.v(LOG_TAG, "@@@ data inserted successfully for table :" + tableName);
                if (null != oncomplete) {
                    oncomplete.execute(true, "data inserted successfully", "");
                }
            }
        } catch (Exception e) {
            checkCount++;
            e.printStackTrace();
            Log.v(LOG_TAG, "@@@@ exception log check " + checkCount + " here " + mapList.size());
            if (checkCount == mapList.size()) {
                if (null != oncomplete)
                    oncomplete.execute(false, "data insertion failed", "" + e.getMessage());
            }
        } finally {
            closeDataBase();
        }
    }

    //Save Data
    public synchronized void saveData(String tableName, List<LinkedHashMap> mapList, final ApplicationThread.OnComplete<String> oncomplete) {
        savedata(false, tableName, mapList, oncomplete);
    }

    //Insert Data
    public synchronized void insertData(String tableName, List<LinkedHashMap> mapList, final ApplicationThread.OnComplete<String> oncomplete) {
        insertData(false, tableName, mapList, oncomplete);
    }

    //Delete Row in the Tables
    public synchronized void deleteRow(String tableName, String columnName, String value, boolean isWhere, final ApplicationThread.OnComplete<String> onComplete) {
        boolean isDataDeleted = true;
//        if (!ApplicationThread.dbThreadCheck())
//            Log.e(LOG_TAG, "called on non-db thread", new RuntimeException());

        try {
//            mDatabase = palm3FoilDatabase.getWritableDatabase();
            String query = "delete from " + tableName;
            if (isWhere) {
                query = query + " where " + columnName + " = '" + value + "'";
            }
            mDatabase.execSQL(query);
        } catch (Exception e) {
            isDataDeleted = false;
            Log.e(LOG_TAG, "@@@ master data deletion failed for " + tableName + " error is " + e.getMessage());
            onComplete.execute(false, null, "master data deletion failed for " + tableName + " error is " + e.getMessage());
        } finally {
            closeDataBase();

            if (isDataDeleted) {
                Log.v(LOG_TAG, "@@@ master data deleted successfully for " + tableName);
                onComplete.execute(true, null, "master data deleted successfully for " + tableName);
            }

        }
    }

    //To get the count from table
    public String getCountValue(String query) {
//        mDatabase = palm3FoilDatabase.getWritableDatabase();
        Cursor mOprQuery = null;
        try {
            mOprQuery = mDatabase.rawQuery(query, null);
            if (mOprQuery != null && mOprQuery.moveToFirst()) {
                return mOprQuery.getString(0);
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mOprQuery.close();
            closeDataBase();
        }
        return "";
    }


    public void closeDataBase() {
//        if (mDatabase != null)
//            mDatabase.close();
    }

    public void executeRawQuery(String query) {
        try {
            if (mDatabase != null) {
                mDatabase.execSQL(query);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }


    //Bulk Insert to the Table
    public boolean bulkinserttoTable(List<ContentValues> cv, final String tableName) {
        boolean isError = false;
        mDatabase.beginTransaction();
        try {
            for (int i = 0; i < cv.size(); i++) {
                ContentValues stockResponse = cv.get(i);
                long id = mDatabase.insert(tableName, null, stockResponse);
                if (id < 0) {
                    isError = true;
                }
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
        return isError;
    }

    //To Get the Datya from the Table
    public void getGradingReportDetails(final String query, final ApplicationThread.OnComplete onComplete) {
        List<GradingReportModel> gradingReportDetails = new ArrayList<>();
        Cursor cursor = null;
//        String query = Queries.getInstance().getCollectionReportDetails();
        Log.v(LOG_TAG, "@@@@ collection reports query " + query);
        try {
            cursor = mDatabase.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {

                do {

                    GradingReportModel grading_details = new GradingReportModel();
                    grading_details.setTokenNumber(cursor.getString(cursor.getColumnIndex("TokenNumber")));
                    grading_details.setCCCode(cursor.getString(cursor.getColumnIndex("CCCode")));
                    grading_details.setFruitType(cursor.getString(cursor.getColumnIndex("FruitType")));
                    grading_details.setGrossWeight(cursor.getString(cursor.getColumnIndex("GrossWeight")));
                    grading_details.setTokenDate(cursor.getString(cursor.getColumnIndex("TokenDate")));
                    grading_details.setUnRipen(cursor.getInt(cursor.getColumnIndex("UnRipen")));
                    grading_details.setUnderRipe(cursor.getInt(cursor.getColumnIndex("UnderRipe")));
                    grading_details.setRipen(cursor.getInt(cursor.getColumnIndex("Ripen")));
                    grading_details.setOverRipe(cursor.getInt(cursor.getColumnIndex("OverRipe")));
                    grading_details.setDiseased(cursor.getInt(cursor.getColumnIndex("Diseased")));
                    grading_details.setEmptyBunches(cursor.getInt(cursor.getColumnIndex("EmptyBunches")));
                    grading_details.setFFBQualityLong(cursor.getInt(cursor.getColumnIndex("FFBQualityLong")));
                    grading_details.setFFBQualityMedium(cursor.getInt(cursor.getColumnIndex("FFBQualityMedium")));
                    grading_details.setFFBQualityShort(cursor.getInt(cursor.getColumnIndex("FFBQualityShort")));
                    grading_details.setFFBQualityOptimum(cursor.getInt(cursor.getColumnIndex("FFBQualityOptimum")));
                    grading_details.setLooseFruit(cursor.getString(cursor.getColumnIndex("LooseFruit")));
                  grading_details.setLooseFruitWeight(cursor.getString(cursor.getColumnIndex("LooseFruitWeight")));
                    grading_details.setGraderName(cursor.getString(cursor.getColumnIndex("GraderName")));
                    grading_details.setRejectedBunches(cursor.getString(cursor.getColumnIndex("RejectedBunches")));
                    grading_details.setCreatedDatewithtime(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                    grading_details.setCreatedDate(cursor.getString(cursor.getColumnIndex("date")));

                    gradingReportDetails.add(grading_details);
                } while (cursor.moveToNext());
            }



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            closeDataBase();

            onComplete.execute(true, gradingReportDetails, "getting data");
        }
    }



    //To get user details
    public T getUserDetails(final String query, int dataReturnType) {
        UserDetails userDetails = null;
        Cursor cursor = null;
        List userDataList = new ArrayList();
        Log.v(LOG_TAG, "@@@ user details query " + query);
        try {
            cursor = mDatabase.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    userDetails = new UserDetails();
                    userDetails.setUserId(cursor.getString(0));
                    userDetails.setUserName(cursor.getString(1));
                    userDetails.setPassword(cursor.getString(2));
                    userDetails.setRoleId(cursor.getInt(3));
                    userDetails.setManagerId(cursor.getInt(4));
                    userDetails.setId(cursor.getString(5));
                    userDetails.setFirstName(cursor.getString(6));
                    userDetails.setTabName(cursor.getString(7));
                    userDetails.setUserCode(cursor.getString(8));
//                    userDetails.setTabletId(cursor.getInt(5));
//                    userDetails.setUserVillageId(cursor.getString(6));
                    if (dataReturnType == 1) {
                        userDataList.add(userDetails);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "@@@ getting user details " + e.getMessage());
        }
        return (T) ((dataReturnType == 0) ? userDetails : userDataList);
    }

    //To get the Grading Repository Details
    public T getGradingRepoDetails(final String query, final int type) {
        List<GradingFileRepository> gradingrepolist = new ArrayList<>();
        GradingFileRepository mgradingrepository = null;
        Cursor cursor = null;
        Log.v(LOG_TAG, "@@@ GradingRepo details query " + query);
        try {
            cursor = mDatabase.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {


                    mgradingrepository = new GradingFileRepository();

                    String filelocation = cursor.getString(cursor.getColumnIndex("FileLocation"));

                    File imagefile = new File(filelocation);
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(imagefile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    Bitmap bm = BitmapFactory.decodeStream(fis);
                    bm = ImageUtility.rotatePicture(90, bm);
                    String base64string = ImageUtility.convertBitmapToString(bm);
                    mgradingrepository.setImageString(base64string);

//
//                    if(filelocation != null){
//                        try{
//                            mgradingrepository.setImageString(CommonUtils.encodeFileToBase64Binary(new File(filelocation)));
//
//                        }catch (Exception exc){
//
//                        }
//                    }

                    mgradingrepository.setTokenNumber(cursor.getString(cursor.getColumnIndex("TokenNumber")));
                    mgradingrepository.setCCCode(cursor.getString(cursor.getColumnIndex("CCCode")));
                    mgradingrepository.setFruitType(cursor.getInt(cursor.getColumnIndex("FruitType")));
                    mgradingrepository.setGrossWeight(cursor.getDouble(cursor.getColumnIndex("GrossWeight")));
                    mgradingrepository.setFileName(cursor.getString(cursor.getColumnIndex("FileName")));
                    //mgradingrepository.setFileLocation(cursor.getString(cursor.getColumnIndex("FileLocation")));
                    mgradingrepository.setFileExtension(cursor.getString(cursor.getColumnIndex("FileExtension")));
                    mgradingrepository.setCreatedByUserId(cursor.getInt(cursor.getColumnIndex("CreatedByUserId")));
                    mgradingrepository.setCreatedDate(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                    mgradingrepository.setServerUpdatedStatus(0);
                    if (type == 1) {
                        gradingrepolist.add(mgradingrepository);
                        mgradingrepository = null;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "@@@ getting GradingRepo details " + e.getMessage());
        }
        return (T) ((type == 0) ? mgradingrepository : gradingrepolist);
    }



}
