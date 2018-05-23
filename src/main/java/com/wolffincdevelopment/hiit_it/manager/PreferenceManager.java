package com.wolffincdevelopment.hiit_it.manager;

import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public final class PreferenceManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PreferenceManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.editor = sharedPreferences.edit();
    }

    public PreferenceManager clear() {
        editor.clear().apply();
        return this;
    }

    public PreferenceManager batch(String key, String value) {
        editor.putString(key, value);
        return this;
    }

    public PreferenceManager batch(String key, boolean value) {
        editor.putBoolean(key, value);
        return this;
    }

    public PreferenceManager batch(String key, int value) {
        editor.putInt(key, value);
        return this;
    }

    public PreferenceManager batch(String key, long value) {
        editor.putLong(key, value);
        return this;
    }

    public PreferenceManager batch(String key, float value) {
        editor.putFloat(key, value);
        return this;
    }

    public PreferenceManager batch(String key, Set<String> value) {
        editor.putStringSet(key, value);
        return this;
    }

    public void apply(String key, String value) {
        batch(key, value).applyBatch();
    }

    public void apply(String key, boolean value) {
        batch(key, value).applyBatch();
    }

    public void apply(String key, int value) {
        batch(key, value).applyBatch();
    }

    public void apply(String key, long value) {
        batch(key, value).applyBatch();
    }

    public void apply(String key, float value) {
        batch(key, value).applyBatch();
    }

    public void apply(String key, Set<String> value) {
        batch(key, value).applyBatch();
    }

    public void applyBatch() {
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return sharedPreferences.getStringSet(key, defaultValue);
    }
}
