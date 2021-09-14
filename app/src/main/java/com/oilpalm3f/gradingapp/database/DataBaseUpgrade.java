package com.oilpalm3f.gradingapp.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.oilpalm3f.gradingapp.common.CommonConstants;

import static android.content.Context.MODE_PRIVATE;

public class DataBaseUpgrade {
    private static final String LOG_TAG = DataBaseUpgrade.class.getName();

    static void upgradeDataBase(final Context context, final SQLiteDatabase db) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appprefs", MODE_PRIVATE);
        boolean result = true;
        try {
            boolean isFreshInstall = sharedPreferences.getBoolean(CommonConstants.IS_FRESH_INSTALL, true);
            if (isFreshInstall) {
                upgradeDb1(db);
//

            } else {
                boolean isDbUpgradeFinished = sharedPreferences.getBoolean(String.valueOf(Palm3FoilDatabase.DATA_VERSION), false);
                Log.v(LOG_TAG, "@@@@ database....." + isDbUpgradeFinished);
                if (!isDbUpgradeFinished) {
                    switch (Palm3FoilDatabase.DATA_VERSION) {
                        case 1:
                            upgradeDb1(db);
                            break;

                    }
                } else {
                    Log.v(LOG_TAG, "@@@@ database is already upgraded " + Palm3FoilDatabase.DATA_VERSION);
                }
            }

        } catch (Exception e) {
            result = false;
        } finally {
            if (result) {
                Log.v(LOG_TAG, "@@@@ database is upgraded " + Palm3FoilDatabase.DATA_VERSION);
            } else {
                Log.e(LOG_TAG, "@@@@ database is upgrade failed or already upgraded");
            }
            sharedPreferences.edit().putBoolean(CommonConstants.IS_FRESH_INSTALL, false).apply();
            sharedPreferences.edit().putBoolean(String.valueOf(Palm3FoilDatabase.DATA_VERSION), true).apply();
        }
    }


    public static void upgradeDb1(final SQLiteDatabase db) {
        Log.d(LOG_TAG, "******* upgradeDataBase " + Palm3FoilDatabase.DATA_VERSION);

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
