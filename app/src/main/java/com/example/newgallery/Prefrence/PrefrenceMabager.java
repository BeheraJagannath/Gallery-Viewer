package com.example.newgallery.Prefrence;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefrenceMabager {

    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public PrefrenceMabager(Context context) {

        this.context = context;
        sharedPreferences = context.getSharedPreferences("UserPref", Context.MODE_PRIVATE);

    }
    public void isNightMode(boolean nightMode){
        editor = sharedPreferences.edit();
        editor.putBoolean("night_mode",nightMode);
        editor.apply();
    }
    public boolean checkMode(){
        return sharedPreferences.getBoolean("night_mode",false);

    }

}
