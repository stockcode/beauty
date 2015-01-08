package cn.nit.beauty.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by gengke on 2015/1/8.
 */
public class SPUtils {
    private static SharedPreferences sp;

    private static SharedPreferences getSharedPreferences(Context context) {
        if (sp == null) {
            sp = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sp;
    }

    public static void putString(Context context, String key, String value) {
        getSharedPreferences(context).edit().putString(key, value).commit();
    }

    public static String getString(Context context, String key) {
        return getSharedPreferences(context).getString(key, "");
    }

    public static boolean exists(Context context, String key) {
        return getSharedPreferences(context).contains(key);
    }
}
