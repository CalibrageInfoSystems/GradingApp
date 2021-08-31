package com.oilpalm3f.gradingapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.oilpalm3f.gradingapp.FaLogTracking.LocationTracker;
import com.oilpalm3f.gradingapp.cloudhelper.ApplicationThread;
import com.oilpalm3f.gradingapp.common.CommonUtils;
import com.oilpalm3f.gradingapp.dbmodels.GradingFileRepository;
import com.oilpalm3f.gradingapp.dbmodels.GradingReportModel;
import com.oilpalm3f.gradingapp.dbmodels.UserDetails;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    public LinkedHashMap<String, String> getGenericData(final String query) {
        Log.v(LOG_TAG, "@@@ Generic Query " + query);
        LinkedHashMap<String, String> mGenericData = new LinkedHashMap<>();
        Cursor genericDataQuery = null;
        try {
            genericDataQuery = mDatabase.rawQuery(query, null);
            if (genericDataQuery.moveToFirst()) {
                do {
                    mGenericData.put(genericDataQuery.getString(0), genericDataQuery.getString(1));
                } while (genericDataQuery.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (null != genericDataQuery)
                genericDataQuery.close();

            closeDataBase();
        }
        return mGenericData;
    }

    public LinkedHashMap<String, String> getMoreGenericData(final String query) {
        Log.v(LOG_TAG, "@@@ Generic Query " + query);
        LinkedHashMap<String, String> mGenericData = new LinkedHashMap<>();
        Cursor genericDataQuery = mDatabase.rawQuery(query, null);
        try {
            if (genericDataQuery.moveToFirst()) {
                do {
                    mGenericData.put(genericDataQuery.getString(0), genericDataQuery.getString(1) + "-" + genericDataQuery.getString(2) + "-" + genericDataQuery.getString(3));
                } while (genericDataQuery.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (null != genericDataQuery)
                genericDataQuery.close();

            closeDataBase();
        }
        return mGenericData;
    }


    public LinkedHashMap<String, String> getFarmerDetailsData(String query) {
        LinkedHashMap linkedHashMap = new LinkedHashMap<String, String>();
        Cursor cursor = mDatabase.rawQuery(query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String key = cursor.getString(cursor.getColumnIndex("Code"));
                    String value = cursor.getString(cursor.getColumnIndex("FirstName"))
                            + "-" + cursor.getString(cursor.getColumnIndex("ContactNumber"));

                    linkedHashMap.put(key, value);


                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linkedHashMap;

    }









    public String getTwoValues(final String query) {
        Log.v(LOG_TAG, "@@@ Generic Query " + query);
        String mGenericData = "";
        Cursor genericDataQuery = mDatabase.rawQuery(query, null);
        try {
            if (genericDataQuery.moveToFirst()) {
                do {
                    mGenericData = CommonUtils.twoDForm.format(genericDataQuery.getDouble(0)) + "-" + CommonUtils.twoDForm.format(genericDataQuery.getDouble(1));
                } while (genericDataQuery.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (null != genericDataQuery)
                genericDataQuery.close();

            closeDataBase();
        }
        return mGenericData;
    }



    public LinkedHashMap<String, Pair> getPairData(final String query) {
        Log.v(LOG_TAG, "@@@ Generic Query " + query);
        LinkedHashMap<String, Pair> mGenericData = new LinkedHashMap<>();
        Cursor genericDataQuery = null;
        try {
            genericDataQuery = mDatabase.rawQuery(query, null);
            if (genericDataQuery.moveToFirst()) {
                do {
                    mGenericData.put(genericDataQuery.getString(0), Pair.create(genericDataQuery.getString(1), genericDataQuery.getString(2)));
                } while (genericDataQuery.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (null != genericDataQuery)
                genericDataQuery.close();

            closeDataBase();
        }
        return mGenericData;
    }


    public boolean checkValueExistedInDatabase(final String query) {
        Cursor mOprQuery = mDatabase.rawQuery(query, null);
        try {
            if (mOprQuery != null && mOprQuery.moveToFirst()) {
                return (mOprQuery.getInt(0) > 0);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != mOprQuery)
                mOprQuery.close();

            closeDataBase();
        }
        return false;
    }

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

    public String getLastVistCodeFromDb(String query) {
        Log.v(LOG_TAG, "@@@ query " + query);
        Cursor mOprQuery = null;
        try {
            mOprQuery = mDatabase.rawQuery(query, null);
            if (mOprQuery != null && mOprQuery.moveToFirst()) {
                return mOprQuery.getString(mOprQuery.getColumnIndex("Code"));
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

    public String getOnlyTwoValueFromDb(String query) {
        Log.v(LOG_TAG, "@@@ query " + query);
        Cursor mOprQuery = null;
        try {
            mOprQuery = mDatabase.rawQuery(query, null);
            if (mOprQuery != null && mOprQuery.moveToFirst()) {
                return mOprQuery.getString(0) + "@" + mOprQuery.getString(1);
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

    public synchronized void insertDataOld(String tableName, List<LinkedHashMap> mapList, final ApplicationThread.OnComplete<String> oncomplete) {
//        if (!ApplicationThread.dbThreadCheck())
//            Log.e(LOG_TAG, "called on non-db thread", new RuntimeException());
        int checkCount = 0;
        boolean errorMessageSent = false;
        try {
            for (int i = 0; i < mapList.size(); i++) {
                checkCount++;
                List<LinkedHashMap.Entry> entryList = new ArrayList<>((mapList.get(i)).entrySet());
                String query = "insert into " + tableName;
                String namestring, valuestring;
                StringBuffer values = new StringBuffer();
                StringBuffer columns = new StringBuffer();
                for (LinkedHashMap.Entry temp : entryList) {
//                    if (temp.getKey().equals("Id"))
//                        continue;
                    columns.append(temp.getKey());
                    columns.append(",");
                    values.append("'");
                    values.append(temp.getValue());
                    values.append("'");
                    values.append(",");
                }
                namestring = "(" + columns.deleteCharAt(columns.length() - 1).toString() + ")";
                valuestring = "(" + values.deleteCharAt(values.length() - 1).toString() + ")";
                query = query + namestring + "values" + valuestring;
                Log.v(getClass().getSimpleName(), "query.." + query);
                Log.v(LOG_TAG, "@@@@ log check " + checkCount + " here " + mapList.size());
                try {
                    mDatabase.execSQL(query);
                } catch (Exception e) {
                    Log.v(LOG_TAG, "@@@ Error while inserting data " + e.getMessage());
                    if (checkCount == mapList.size()) {
                        errorMessageSent = true;
                        if (null != oncomplete)
                            oncomplete.execute(false, "failed to insert data", "");
                    }
                }
                if (checkCount == mapList.size() && !errorMessageSent) {
                    if (null != oncomplete)
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
                    if (!fromMaster) {
                        if (keyToInsert.equalsIgnoreCase("Id") && !tableName.equalsIgnoreCase(DatabaseKeys.TABLE_ALERTS))
                            continue;
                    }
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
                    if (!fromMaster) {
                        if (keyToInsert.equalsIgnoreCase("Id") && !tableName.equalsIgnoreCase(DatabaseKeys.TABLE_ALERTS))
                            continue;
                    }
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

    public synchronized void saveData(String tableName, List<LinkedHashMap> mapList, final ApplicationThread.OnComplete<String> oncomplete) {
        savedata(false, tableName, mapList, oncomplete);
    }
    public synchronized void insertData(String tableName, List<LinkedHashMap> mapList, final ApplicationThread.OnComplete<String> oncomplete) {
        insertData(false, tableName, mapList, oncomplete);
    }

    /**
     * Updating database records
     *
     * @param tableName      ---> Table name to update
     * @param list           ---> List which contains data values
     * @param isClaues       ---> Checking where condition availability
     * @param whereCondition ---> condition
     */
    public synchronized void updateData(String tableName, List<LinkedHashMap> list, Boolean isClaues, String whereCondition, final ApplicationThread.OnComplete<String> oncomplete) {
        boolean isUpdateSuccess = false;
        int checkCount = 0;
        try {
            for (int i = 0; i < list.size(); i++) {
                checkCount++;
                List<Map.Entry> entryList = new ArrayList<Map.Entry>((list.get(i)).entrySet());
                String query = "update " + tableName + " set ";
                String namestring = "";

                System.out.println("\n==> Size of Entry list: " + entryList.size());
                StringBuffer columns = new StringBuffer();
                for (Map.Entry temp : entryList) {
                    columns.append(temp.getKey());
                    columns.append("='");
                    columns.append(temp.getValue());
                    columns.append("',");
                }

                namestring = columns.deleteCharAt(columns.length() - 1).toString();
                query = query + namestring + "" + whereCondition;
                mDatabase.execSQL(query);
                isUpdateSuccess = true;
                Log.v(LOG_TAG, "@@@ query for Plantation " + query);
            }
        } catch (Exception e) {
            checkCount++;
            e.printStackTrace();
            isUpdateSuccess = false;
        } finally {
            closeDataBase();
            if (checkCount == list.size()) {
                if (isUpdateSuccess) {
                    Log.v(LOG_TAG, "@@@ data updated successfully for " + tableName);
                    oncomplete.execute(true, null, "data updated successfully for " + tableName);
                } else {
                    oncomplete.execute(false, null, "data updation failed for " + tableName);
                }
            }
        }
    }

    /**
     * Deleting records from database table
     *
     * @param tableName  ---> Table name
     * @param columnName ---> Column name to deleting
     * @param value      ---> Value for where condition
     * @param isWhere    ---> Checking where condition is required or not
     */
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

    public ArrayList<String> getListOfCodes(final String query) {
        ArrayList<String> plotCodes = new ArrayList<>();
        Cursor paCursor = null;
        try {
            paCursor = mDatabase.rawQuery(query, null);
            if (paCursor.moveToFirst()) {
                do {
                    plotCodes.add(paCursor.getString(0).trim());
                } while (paCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (null != paCursor)
                paCursor.close();

            closeDataBase();
        }
        return plotCodes;
    }

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

    public String getdeleteDuplicateValue(String query) {
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

    public List<Pair> getOnlyPairData(final String query) {
        Log.v(LOG_TAG, "@@@ Generic Query " + query);
        List<Pair> mGenericData = new ArrayList<>();
//        mDatabase = palm3FoilDatabase.getWritableDatabase();
        Cursor genericDataQuery = null;
        try {
            genericDataQuery = mDatabase.rawQuery(query, null);
            if (genericDataQuery.moveToFirst()) {
                do {
                    mGenericData.add(Pair.create(genericDataQuery.getString(0), genericDataQuery.getString(1)));
                } while (genericDataQuery.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (null != genericDataQuery)
                genericDataQuery.close();

            closeDataBase();
        }
        return mGenericData;
    }

    public List<String> getSingleListData(final String query) {
        Log.v(LOG_TAG, "@@@ Generic Query " + query);
        List<String> mGenericData = new ArrayList<>();
        Cursor genericDataQuery = null;
        try {
            genericDataQuery = mDatabase.rawQuery(query, null);
            if (genericDataQuery.moveToFirst()) {
                do {
                    String plotCode = genericDataQuery.getString(0);
                    mGenericData.add(plotCode);
                } while (genericDataQuery.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (null != genericDataQuery)
                genericDataQuery.close();
            closeDataBase();
        }
        return mGenericData;
    }

    public List<String> getZombiData(final String query) {
        Log.v(LOG_TAG, "@@@ Generic Query " + query);
        List<String> mGenericData = new ArrayList<>();
//        mDatabase = palm3FoilDatabase.getWritableDatabase();
        Cursor genericDataQuery = null;
        try {
            genericDataQuery = mDatabase.rawQuery(query, null);
            if (genericDataQuery.moveToFirst()) {
                do {
                    String plotCode = genericDataQuery.getString(0);
                    if (!TextUtils.isEmpty(plotCode)) {
                        mGenericData.add("'" + plotCode + "'");
                    }
                } while (genericDataQuery.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (null != genericDataQuery)
                genericDataQuery.close();

            closeDataBase();
        }
        return mGenericData;
    }

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

    public boolean bulkUpdateToTable(List<ContentValues> cv, final String tableName) {
        boolean isError = false;
        mDatabase.beginTransaction();
        try {
            for (int i = 0; i < cv.size(); i++) {
                ContentValues stockResponse = cv.get(i);
                long id = mDatabase.replaceOrThrow(tableName, null, stockResponse);
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
    //Falog_Tracking...
    public T getGpsTrackingData(final String query, final int type) {
        LocationTracker mGpsBoundaries = null;
        List<LocationTracker> mGpsBoundariesList = new ArrayList<>();
        Cursor cursor = null;
        Log.v(LOG_TAG, "@@@ GeoBoundaries query " + query);
        try {
            cursor = mDatabase.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    mGpsBoundaries = new LocationTracker();

                    mGpsBoundaries.setUserId(cursor.getInt(1));
                    mGpsBoundaries.setLatitude(cursor.getDouble(2));
                    mGpsBoundaries.setLongitude(cursor.getDouble(3));
                    mGpsBoundaries.setAddress(cursor.getString(4));
                    mGpsBoundaries.setLogDate(cursor.getString(5));
                    //mGpsBoundaries.setServerUpdatedStatus(cursor.getInt(6));
                    if (type == 1) {
                        mGpsBoundariesList.add(mGpsBoundaries);
                    }

                    Log.v(LOG_TAG, "Lat@Log" + String.valueOf(mGpsBoundariesList));


                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "@@@ getting user details " + e.getMessage());
        }
        return (T) ((type == 0) ? mGpsBoundaries : mGpsBoundariesList);

    }

    public String getFalogLatLongs(final String query) {
        Log.v(LOG_TAG, "@@@ Generic Query " + query);
        String latlongData = "";
        Cursor genericDataQuery = mDatabase.rawQuery(query, null);
        try {
            if (genericDataQuery.getCount() > 0 && genericDataQuery.moveToFirst()) {
                do {
                    latlongData = (genericDataQuery.getDouble(0) + "-" + genericDataQuery.getDouble(1));
                } while (genericDataQuery.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (null != genericDataQuery)
                genericDataQuery.close();

            closeDataBase();
        }
        return latlongData;
    }

    public String getLatLongs(final String query) {
        Log.v(LOG_TAG, "@@@ Generic Query " + query);
        String latlongData = "";
        Cursor genericDataQuery = mDatabase.rawQuery(query, null);
        try {
            if (genericDataQuery.getCount() > 0 && genericDataQuery.moveToFirst()) {
                do {
                    latlongData = (genericDataQuery.getDouble(0) + "-" + genericDataQuery.getDouble(1));
                } while (genericDataQuery.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            if (null != genericDataQuery)
                genericDataQuery.close();

            closeDataBase();
        }
        return latlongData;
    }




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
                    grading_details.setRejectedBunches(cursor.getInt(cursor.getColumnIndex("RejectedBunches")));

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



    private String doFileUpload(String selectedPath) {
        byte[] videoBytes;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            File AudioFile = new File(selectedPath);
            if (AudioFile.exists()) {
                FileInputStream fis = new FileInputStream(AudioFile);

                byte[] buf = new byte[1024];
                int n;
                while (-1 != (n = fis.read(buf)))
                    baos.write(buf, 0, n);

                videoBytes = baos.toByteArray();


                String video_str = android.util.Base64.encodeToString(videoBytes, 0);
                System.out.println("video array" + video_str);
                return video_str;
            } else {
                return "";
            }

        } catch (Exception e) {
            return "";
            // TODO: handle exception
        }
    }

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
                    mgradingrepository.setTokenNumber(cursor.getString(1));
                    mgradingrepository.setCCCode(cursor.getString(2));
                    mgradingrepository.setFruitType(cursor.getInt(3));
                    mgradingrepository.setGrossWeight(cursor.getDouble(4));
                    mgradingrepository.setFileName(cursor.getString(5));
                    mgradingrepository.setFileLocation(cursor.getString(6));
                    mgradingrepository.setFileExtension(cursor.getString(7));
                    mgradingrepository.setTokenNumber(cursor.getString(8));
                    mgradingrepository.setCreatedByUserId(cursor.getInt(9));
                    mgradingrepository.setCreatedDate(cursor.getString(10));
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
