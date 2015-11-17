package com.helpme.android.helpmereceiver;
/*
    Programmer: Vitaly Simonovich
    ID: 309398311
 */
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity  {
    private DatabaseHelper db = new DatabaseHelper(this);

    private EditText edUser;
    private String victimName = "";
    private ArrayList<String> users = new ArrayList<String>();
    //Google maps
    private ImageButton ibMap;
    private GoogleMap googleMap;
    //Pictures
    private ImageButton ibPictures;
    private Bitmap bmp;
    private int numOfimages = 0;
    private int parseNum = 0;
    private boolean start = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isNetworkAvailable(getApplicationContext())){
            Toast.makeText(getApplicationContext(),"No internet connection!",Toast.LENGTH_LONG);
            finish();
        }

        edUser = (EditText)findViewById(R.id.edUser);

        deleteAllImages();
        //getAllUsers();
        //Log.e("SERVER", users.size() + "");

        ibMap = (ImageButton)findViewById(R.id.ibMap);
        ibMap.setBackground(null);

        ibMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle id = new Bundle();
                //Spinner selected ID
                String name;
                try {
                    name = edUser.getText().toString().trim();
                } catch (Exception e2) {
                    name = "";
                }
                String selected = String.valueOf(name);
                id.putString("VICTIMNAME", selected);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                MapsFragment googlemaps = new MapsFragment();
                googlemaps.setArguments(id);
                fragmentTransaction.add(R.id.fragHolder, googlemaps, "GOOGLEMAPS");
                fragmentTransaction.commit();
            }
        });

        ibPictures =(ImageButton)findViewById(R.id.ibPicture);
        ibPictures.setBackground(null);
        ibPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!start) {
                    deleteAllImages();

                    start = true;
                    downloadImages();
                }
                if (parseNum == numOfimages && parseNum>0 && numOfimages>0) {
                    numOfimages = 0;
                    Toast.makeText(getApplicationContext(), "Images ready", Toast.LENGTH_LONG).show();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    PicturesFragment pictures = new PicturesFragment();
                    fragmentTransaction.add(R.id.fragHolder, pictures, "PICTURES");
                    fragmentTransaction.commit();
                    start = false;
                } else {
                    Toast.makeText(getApplicationContext(), "Downloading images from the web...click in a few seconds", Toast.LENGTH_LONG).show();
                }

            }
        });


        Thread parseDB = new Thread(new Runnable() {
            @Override
            public void run() {

                db.removeAllGPS();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("victim");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            for (ParseObject temp : objects) {

                                if(db.checkGPSID(temp.getString("victimName"),temp.getString("timestamp")) == 0){
                                    db.addGPSCor(temp.getString("victimName"), temp.getDouble("latitude"),temp.getDouble("longitude"),temp.getString("timestamp"));
                                }

                            }
                        } else {
                            Log.e("SERVER", "FAILED");
                        }
                    }
                });
            }
        });

        parseDB.start();

        ArrayList<String> bb = new ArrayList<>();
        bb.add("test");
        bb.add("vitaly");



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

    //Store image to internal storage
    private void storeImage(Bitmap image) {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String tar = (sdf.format(cal.getTime()));

        File pictureFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),tar+".jpeg");

        if (pictureFile == null) {
            Log.d("SERVER",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            Log.e("SERVER", "BITMAP " + pictureFile.toString());
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("SERVER", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("SERVER", "Error accessing file: " + e.getMessage());
        }
    }


    //Check if network available
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    //Delete all images from picture folder
    private void deleteAllImages(){
        final File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        final File filelist[] =  sdcard.listFiles();
        for (File temp:filelist){
            int i = 0;
            if(temp.isFile()){
                temp.delete();
            }
        }
    }

    //Start downloading...
    private void downloadImages(){
        parseNum =0;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("victimImage");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() != 0) {
                    for (ParseObject temp : objects) {
                        String victimName = temp.getString("victimName");
                        String name;
                        try {
                            name = edUser.getText().toString().trim();
                        }catch (Exception e2){
                             name = "";
                        }
                        if(victimName.equals(name)){
                            ParseFile t = temp.getParseFile("image");
                            parseNum++;
                            Log.e("SERVER", t.getUrl());
                            new LoadImage().execute(t.getUrl());
                        }

                    }
                } else {
                    Log.e("SERVER", "FAILED");
                }
            }
        });
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        protected Bitmap doInBackground(String... args) {
            return downloadBitmap(args[0]);
        }

        protected void onPostExecute(Bitmap image) {

            if(image != null){
                storeImage(image);

            }else{

                Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

            }
        }

        //download photos from parse.com
        private Bitmap downloadBitmap(String url) {
            // initilize the default HTTP client object
            final DefaultHttpClient client = new DefaultHttpClient();

            //forming a HttoGet request
            final HttpGet getRequest = new HttpGet(url);
            try {

                HttpResponse response = client.execute(getRequest);

                //check 200 OK for success
                final int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != HttpStatus.SC_OK) {
                    Log.e("SERVER", "Error " + statusCode +
                            " while retrieving bitmap from " + url);
                    return null;

                }

                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    try {
                        // getting contents from the stream
                        inputStream = entity.getContent();

                        // decoding stream data back into image Bitmap that android understands
                        BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
                        //options.inJustDecodeBounds = true;
                        options.inSampleSize = 4;
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,options);
                        //bitmap.recycle();
                        Log.e("SERVER",bitmap.getByteCount()+"");
                        numOfimages++;
                        return bitmap;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        entity.consumeContent();
                    }
                }
            } catch (Exception e) {
                // You Could provide a more explicit error message for IOException
                getRequest.abort();
                Log.e("SERVER", "Something went wrong while" +
                        " retrieving bitmap from " + url + e.toString());
            }

            return null;
        }
    }
}