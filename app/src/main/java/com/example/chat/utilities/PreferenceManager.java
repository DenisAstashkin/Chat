package com.example.chat.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager
{
    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context)
    {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void putString(String key, String value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key)
    {
        return sharedPreferences.getString(key, null);
    }

    public void clear()
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}
