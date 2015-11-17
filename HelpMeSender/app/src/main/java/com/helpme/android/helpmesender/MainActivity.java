package com.helpme.android.helpmesender;
/*
    Programmer: Vitaly Simonovich
    ID: 309398311
 */
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private TextView tvMain;
    private TextView tvMain2;
    private TextView tvUser;
    private TextView tvPass;
    private TextView tvPin;
    private EditText etUsername;
    private EditText etPass;
    private EditText etPin;
    private SessionManagement session;
    private DatabaseHelper db;
    private int victimID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isNetworkAvailable(getApplicationContext())){
            Toast.makeText(getApplicationContext(),"No internet connection!",Toast.LENGTH_LONG);
            finish();
        }
        //session management
        session = new SessionManagement(getApplicationContext());
        session.checkLogin();
        if(session.isLoggedIn()){
            finish();
        }else{
            setContentView(R.layout.activity_main);
            setFonts();

            etUsername = (EditText) findViewById(R.id.edName);
            etPass = (EditText) findViewById(R.id.edPass);
            etPin = (EditText) findViewById(R.id.edPin);
        }

    }
    //signup using parse.com
    public void signup(View view){
        ParseUser.getCurrentUser().logOut();
        try{
            String username = etUsername.getText().toString().trim();
            String password = etPass.getText().toString().trim();
            int pincode = Integer.parseInt(etPin.getText().toString().trim());
            //Write method for input validation

            final ParseUser user = new ParseUser();
            user.setUsername(username);
            user.setPassword(password);
            user.put("pincode", pincode);
            user.put("gps",0);
            user.put("photo", 0);
            user.put("numPhotos", 5);


            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", username);
            query.findInBackground(new FindCallback<ParseUser>() {
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null ) {
                        user.signUpInBackground(new SignUpCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(getApplicationContext(), "Account was created successfully", Toast.LENGTH_LONG).show();

                                } else {
                                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();

                                }
                            }
                        });

                    } else {
                        Toast.makeText(getApplicationContext(), "User already exist", Toast.LENGTH_LONG).show();
                    }
                }
            });


        }catch (Exception e){
            Toast.makeText(this,"Couldn't sign up, problem with input",Toast.LENGTH_LONG).show();

        }


    }

    //login using parse.com
    public void login(View view){
        final String username = etUsername.getText().toString().trim();
        final String password = etPass.getText().toString().trim();
        try{
            final int pincode = Integer.parseInt(etPin.getText().toString().trim());

            if(username.equals(" ") && !(username != null)){
                Toast.makeText(this,"Enter username",Toast.LENGTH_SHORT).show();
            }else if (password.equals(" ") && !(password != null)){
                Toast.makeText(this,"Enter password",Toast.LENGTH_SHORT).show();
            }else if(pincode < 0){
                Toast.makeText(this,"Enter pincode",Toast.LENGTH_SHORT).show();
            }else{
                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    public void done(final ParseUser user, ParseException e) {
                        if (user != null) {

                            ParseQuery<ParseUser> query = ParseUser.getQuery();
                            query.whereEqualTo("username", username);
                            query.findInBackground(new FindCallback<ParseUser>() {
                                @Override
                                public void done(List<ParseUser> users, ParseException e) {
                                        if (e == null && users.size() == 1) {
                                            Log.e("VICTIM",users.get(0).getInt("gps") + "|" +users.get(0).getInt("photo")+"|"+users.get(0).getInt("numPhotos"));
                                            HashMap<String, Integer> userSensors = new HashMap<String, Integer>();
                                            userSensors.put("gps", users.get(0).getInt("gps"));
                                            userSensors.put("photo", users.get(0).getInt("photo"));
                                            userSensors.put("numPhotos", users.get(0).getInt("numPhotos"));

                                            session.createLoginSession(victimID, username, password, Integer.toString(pincode), userSensors);
                                            Intent intent = new Intent(getApplicationContext(),SetupActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }else{
                                        Toast.makeText(getApplicationContext(),"Wrong credentioals",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                        } else {
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }



        }catch (Exception e){
            Toast.makeText(this,"Pincode must be an integer",Toast.LENGTH_SHORT).show();

        }




    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    //Fonts for main activity
    private void setFonts(){
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/revorioum.ttf");
        Typeface face2 = Typeface.createFromAsset(getAssets(),"fonts/futureforces.ttf");
        Typeface faceUser = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Light.ttf");

        tvMain = (TextView) findViewById(R.id.tvMain);
        tvMain2 = (TextView) findViewById(R.id.tvMain2);
        tvUser = (TextView) findViewById(R.id.tvUser);
        tvPass = (TextView) findViewById(R.id.tvPass);
        tvPin = (TextView)findViewById(R.id.tvPin);

        tvMain.setTypeface(face);
        tvMain2.setTypeface(face2);
        tvPass.setTypeface(faceUser);
        tvUser.setTypeface(faceUser);
        tvPin.setTypeface(faceUser);
    }

    @Override
    public void onBackPressed() {
       finish();
    }

    //Check if network available
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
