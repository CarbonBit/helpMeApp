package com.helpme.android.helpmesender;
/*
    Programmer: Vitaly Simonovich
    ID: 309398311
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.BoringLayout;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vitaly on 05/16/2015.
 */
public class SessionManagement {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "MyPref";
    private static final String IS_LOGIN = "IsLogedIn";


    public static final String KEY_NAME = "name";
    public static final String KEY_PASS = "password";
    public static final String KEY_PIN = "pincode";

    public static final String KEY_GPS = "gps";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_NUMPHOTO = "numPhotos";



    public SessionManagement(Context context){
        this.context = context;
        pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public  void createLoginSession(int id,String name,String password,String pin,HashMap<String,Integer> userSensors){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_PASS, password);
        editor.putString(KEY_PIN, pin);

        updateLoginSessionSensors(userSensors, false);
        editor.commit();
    }

    public HashMap<String,String> getUserDetails(){
        HashMap<String,String> user = new HashMap<String,String>();

        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_PASS, pref.getString(KEY_PASS, null));
        user.put(KEY_PIN, pref.getString(KEY_PIN, null));
        user.put(KEY_GPS, pref.getString(KEY_GPS, "true"));
        user.put(KEY_PHOTO, pref.getString(KEY_PHOTO, "true"));
        user.put(KEY_NUMPHOTO, pref.getString(KEY_NUMPHOTO, "5"));

        return user;

    }

    public void checkLogin(){

        if(this.isLoggedIn()){
            Intent i = new Intent(this.context, SetupActivity.class);
            //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.context.startActivity(i);

        }

    }

    public void updateLoginSessionSensors(HashMap<String,Integer> userSensors, Boolean commit){
        Map<String, Integer> map = userSensors;
        for (Map.Entry<String, Integer> entry : userSensors.entrySet()) {
            String key = entry.getKey();
            switch (key){
                case KEY_GPS:
                    editor.putString(KEY_GPS, convertIntString(entry.getValue()));
                    break;
                case KEY_PHOTO:
                    editor.putString(KEY_PHOTO, convertIntString(entry.getValue()));
                    break;
                case KEY_NUMPHOTO:
                    editor.putString(KEY_NUMPHOTO, String.valueOf(entry.getValue()));
                    break;
            }

        }
        if(commit) editor.commit();
    }
    public void updateLoginSessionPincode(String pincode){
        editor.putString(KEY_PIN, pincode);
        editor.commit();
    }


    public void logoutUser(){

        editor.clear();
        editor.commit();
        Intent i = new Intent(this.context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        this.context.startActivity(i);
    }
    private String convertIntString(int i){
        if(i == 0) return "false";
        else return "true";
    }
    public boolean isLoggedIn(){

        return pref.getBoolean(IS_LOGIN, false);
    }

}
