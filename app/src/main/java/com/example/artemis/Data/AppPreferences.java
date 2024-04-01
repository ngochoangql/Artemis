package com.example.artemis.Data;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private static final String APP_SHARED_PREFS = "my_app_preferences"; // Tên file SharedPreferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public AppPreferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
//        this.sharedPreferences = context.getSharedPreferences()
        this.editor = sharedPreferences.edit();
    }

    // Lưu giá trị vào SharedPreferences
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // Đọc giá trị từ SharedPreferences
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
}