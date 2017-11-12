package com.agrawroh.ninja.dressly.utility;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Lincoln on 05/05/16.
 */
public class PrefManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    /* Shared Preferences Mode */
    private static final int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "MyPreference";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    /* Preferences Manager */
    public PrefManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(final boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }
}
