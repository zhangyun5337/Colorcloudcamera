package com.zhangyun.colorcloudcamera.utils;

/**
 * Created by ZhangYun on 2018/2/21.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedDataUtil {
    private static final String FILE_NAME = "SharedDataFromAlex";

    public static void saveData(Context context, String key, Object data) {
        String type = data.getClass().getSimpleName();
        Editor editor = context.getSharedPreferences(FILE_NAME, 0).edit();
        if ("Integer".equals(type)) {
            editor.putInt(key, ((Integer) data).intValue());
        } else if ("Boolean".equals(type)) {
            editor.putBoolean(key, ((Boolean) data).booleanValue());
        } else if ("String".equals(type)) {
            editor.putString(key, (String) data);
        } else if ("Float".equals(type)) {
            editor.putFloat(key, ((Float) data).floatValue());
        } else if ("Long".equals(type)) {
            editor.putLong(key, ((Long) data).longValue());
        }
        editor.commit();
    }

    public static Object getData(Context context, String key, Object defValue) {
        String type = defValue.getClass().getSimpleName();
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, 0);
        if ("Integer".equals(type)) {
            return Integer.valueOf(sharedPreferences.getInt(key, ((Integer) defValue).intValue()));
        }
        if ("Boolean".equals(type)) {
            return Boolean.valueOf(sharedPreferences.getBoolean(key, ((Boolean) defValue).booleanValue()));
        }
        if ("String".equals(type)) {
            return sharedPreferences.getString(key, (String) defValue);
        }
        if ("Float".equals(type)) {
            return Float.valueOf(sharedPreferences.getFloat(key, ((Float) defValue).floatValue()));
        }
        if ("Long".equals(type)) {
            return Long.valueOf(sharedPreferences.getLong(key, ((Long) defValue).longValue()));
        }
        return null;
    }
}
