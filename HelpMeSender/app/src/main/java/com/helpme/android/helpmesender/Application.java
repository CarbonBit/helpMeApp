package com.helpme.android.helpmesender;
/*
    Programmer: Vitaly Simonovich
    ID: 309398311
 */
import com.parse.Parse;
import com.parse.ParseUser;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();


        Parse.initialize(this, "bI2hZEosysqAG6QGcB4E9RagVIpiAnMShYxhpYzQ", "NZGXjSFSqUkXER8k5mrSYF2uiiTYPcuEhe9zaMVK");
        ParseUser.enableRevocableSessionInBackground();
    }
}