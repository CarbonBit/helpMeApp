package com.helpme.android.helpmesender;
/*
    Programmer: Vitaly Simonovich
    ID: 309398311
 */
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SetupActivity extends ActionBarActivity {
    private final int PHOTO_NUM = 5;
    private final  String FILE_NAME = "victim.txt";
    private TextView tvWelcome;
    private EditText etPin;

    private EditText etNum;

    //Checkboxes
    private CheckBox ckbGPS;
    private CheckBox ckbVideo;
    private CheckBox ckbPhoto;
    private CheckBox ckbMic;
    private CheckBox ckbPhone;
    private DatabaseHelper db;
    private SessionManagement session;
    private  HashMap<String, String> user;
    private String victimName;
    private int pincode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);



        tvWelcome = (TextView) findViewById(R.id.tvWelcome);
        etPin = (EditText)findViewById(R.id.etPin);


        etNum = (EditText)findViewById(R.id.etNum);
        ckbGPS = (CheckBox)findViewById(R.id.chbGPS);
        ckbPhoto = (CheckBox)findViewById(R.id.chbPhoto);



        session = new SessionManagement(getApplicationContext());

        //session.checkLogin();
        user = session.getUserDetails();
        try{
            victimName = user.get(SessionManagement.KEY_NAME);
            boolean s = write(FILE_NAME,victimName);
            if(s) Toast.makeText(getApplicationContext(),"Victim name was saved!!!",Toast.LENGTH_SHORT).show();
            else Toast.makeText(getApplicationContext(),"Problem saving victim name",Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            Log.e("VICTIM_ID",e.getMessage());
        }


        String name = user.get(SessionManagement.KEY_NAME);
        String pin = user.get(SessionManagement.KEY_PIN);
        Boolean gps = convertString(user.get(SessionManagement.KEY_GPS));
        Boolean photo = convertString(user.get(SessionManagement.KEY_PHOTO));
        String numPhtos = user.get(SessionManagement.KEY_NUMPHOTO);


        tvWelcome.setText("Hello," + name);
        etPin.setText(pin);
        pincode = Integer.parseInt(etPin.getText().toString());
        etNum.setText(numPhtos);

        ckbGPS.setChecked(gps);

        ckbPhoto.setChecked(photo);



    }

    public void logout(View view){
        session.logoutUser();
        ParseUser.getCurrentUser().logOut();
        finish();
    }



    @Override
    protected void onResume() {
        super.onResume();
        user = session.getUserDetails();
        String name = user.get(SessionManagement.KEY_NAME);
        tvWelcome.setText("Hello," + name);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private boolean convertString(String s){
        if(s.contains("true")) return true;
        else return false;
    }
    @Override
    public void onBackPressed() {
       // super.onBackPressed();


        HashMap<String,Integer> hmSensors = new HashMap<String,Integer>();

        if(ckbGPS.isChecked()) hmSensors.put("gps",1);
        else hmSensors.put("gps",0);

        if(ckbPhoto.isChecked()) hmSensors.put("photo",1);
        else hmSensors.put("photo",0);

        if(etNum.getText().toString().equals("") || etNum.getText() == null)hmSensors.put("numPhotos", PHOTO_NUM);
        else hmSensors.put("numPhotos", Integer.parseInt(etNum.getText().toString()));


        //Update user session
        session.updateLoginSessionSensors(hmSensors, true);
        //Update parse
        updateSensors(victimName,hmSensors);
        session.updateLoginSessionPincode(etPin.getText().toString());

        finish();
    }


    public void updatePin(View view){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", victimName);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null && users.size() == 1) {
                    ParseUser temp = users.get(0);
                    temp.put("pincode", Integer.parseInt(etPin.getText().toString()));
                    temp.saveInBackground();
                    Toast.makeText(getApplicationContext(), "Pincode was updated!", Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    private void updateSensors(String username, final HashMap<String,Integer> hmSensors){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", username);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null && users.size() == 1) {
                    ParseUser temp = users.get(0);
                    for (Map.Entry<String,Integer> entry : hmSensors.entrySet()) {
                        temp.put(entry.getKey(), entry.getValue());
                        Log.e("VICTIM",entry.getKey()+"-"+entry.getValue());
                    }
                    temp.saveInBackground();
                }

            }
        });
    }

    //Save logged victim name for HelpMeStarter app
    public Boolean write(String fname, String fcontent){
        try {

            String fpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/"+fname;
            Log.e("VICTIM",fpath);

            File file = new File(fpath);

            // If file does not exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fcontent);
            bw.close();

            Log.d("Suceess","Sucess");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}
