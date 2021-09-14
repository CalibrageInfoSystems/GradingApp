package com.oilpalm3f.gradingapp.cloudhelper;

import android.content.Context;
import android.util.Log;
import com.oilpalm3f.gradingapp.common.CommonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class CloudDataHandler {

    private static final String LOG_TAG = CloudDataHandler.class.getName();


    public static synchronized void placeDataInCloudd(final Context context, final JSONArray values, final String url, final ApplicationThread.OnComplete<String> onComplete) {
        ApplicationThread.bgndPost(CloudDataHandler.class.getName(), "placeDataInCloud..", () -> {
            try {
                HttpClient.postDataToServerjsonn(context,url, values, new ApplicationThread.OnComplete<String>() {
                    @Override
                    public void execute(boolean success, String result, String msg) {
                        if (success) {
                            try {
                                onComplete.execute(true, result, msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                                onComplete.execute(true, result, msg);
                            }
                        } else{
                            onComplete.execute(false, result, msg);
                        }

                    }
                });
            } catch (Exception e) {
                android.util.Log.v(LOG_TAG, "@Error while getting " + e.getMessage());
            }
        });

    }




    public static void getMasterData(final String url, final LinkedHashMap dataMap, final ApplicationThread.OnComplete<HashMap<String, List>> onComplete) {
        ApplicationThread.bgndPost(CloudDataHandler.class.getName(), "getMasterData...", new Runnable() {
            @Override
            public void run() {
                HttpClient.post(url, dataMap, new ApplicationThread.OnComplete<String>() {
                    @Override
                    public void execute(boolean success, String result, String msg) {
                        if (success) {
                            try {

                                JSONObject parentMasterDataObject = new JSONObject(result);

                                Iterator keysToCopyIterator = parentMasterDataObject.keys();
                                List<String> keysList = new ArrayList<>();
                                while (keysToCopyIterator.hasNext()) {
                                    String key = (String) keysToCopyIterator.next();
                                    keysList.add(key);
                                }

                                android.util.Log.v(LOG_TAG, "@@@@ Tables Size " + keysList.size());
                                LinkedHashMap<String, List> masterDataMap = new LinkedHashMap<>();
                                for (String tableName : keysList) {
                                    //if (!tableName.equalsIgnoreCase("KnowledgeZone") && !tableName.equalsIgnoreCase("KRA")) {
                                        masterDataMap.put(tableName, CommonUtils.toList(parentMasterDataObject.getJSONArray(tableName)));
                                   //}
                                }

                                onComplete.execute(success, masterDataMap, msg);

                                Log.v(LOG_TAG, "@@@@ Tables Data " + masterDataMap.size());

                                //getKraData(onComplete, masterDataMap);



                            } catch (Exception e) {
                                e.printStackTrace();
                                onComplete.execute(success, null, msg);
                            }
                        } else
                            onComplete.execute(success, null, msg);
                    }
                });
            }
        });
    }

}


