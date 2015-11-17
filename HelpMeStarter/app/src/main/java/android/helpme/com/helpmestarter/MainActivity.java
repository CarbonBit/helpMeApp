package android.helpme.com.helpmestarter;
/*
    Programmer: Vitaly Simonovich
    ID: 309398311
 */
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.helpme.com.helpmestarter.VictimLocation.LocationBinder;
import android.helpme.com.helpmestarter.VictimPhotos.PhotosBinder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final  String FILE_NAME = "victim.txt";
    private EditText etPin;
    private Button btnStop;
    private int imageCounter = 0;
    private ImageButton imHelp;
    //Location service
    private VictimLocation locService;
    private boolean isBoundLocation = false;

    //Photo service
    private VictimPhotos photoService;
    private boolean isBoundPhoto = false;

    //victim data
    private int gps;
    private int camera;
    private int numOfPhotos;
    private int pincode;
    private String victimName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isNetworkAvailable(getApplicationContext())){
            Toast.makeText(getApplicationContext(),"No internet connection!",Toast.LENGTH_LONG);
            finish();
        }
        //Get victim name from local file
        victimName = read(FILE_NAME);
        if (victimName.isEmpty() || victimName == null){
            Toast.makeText(getApplicationContext(),"No victim is logged in",Toast.LENGTH_LONG).show();
            finish();
        }else{
            //get victim info
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("username", victimName);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> users, ParseException e) {
                    if (e == null && users.size() == 1) {
                        ParseUser temp = users.get(0);
                        gps = temp.getInt("gps");
                        camera = temp.getInt("photo");
                        numOfPhotos = temp.getInt("numPhotos");
                        pincode = temp.getInt("pincode");
                        }
                }
            });
        }

        etPin = (EditText)findViewById(R.id.etPin);
        btnStop = (Button)findViewById(R.id.btnStop);
        //stop all services
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int i = Integer.parseInt(etPin.getText().toString());
                    if (i == pincode){
                        Intent intentLoc = new Intent(getApplicationContext(), VictimLocation.class);
                        stopService(intentLoc);
                        Toast.makeText(getApplicationContext(),"Location service unbinded!!!",Toast.LENGTH_LONG).show();
                        Intent intentPhto = new Intent(getApplicationContext(), VictimPhotos.class);
                        stopService(intentPhto);
                        Toast.makeText(getApplicationContext(),"Camera service unbinded!!!",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Wrong pincode",Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e){

                }

            }

        });
        imHelp = (ImageButton)findViewById(R.id.imHelp);
        imHelp.setBackground(null);
        //start selected services
        imHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gps == 1){
                    Log.e("VICTIM", "Location started");
                    Toast.makeText(getApplicationContext(),"Location capturing started",Toast.LENGTH_LONG).show();
                    Thread locationThread= new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent locationIntent = new Intent(getApplicationContext(),VictimLocation.class);
                            locationIntent.putExtra("victimName",victimName);
                            bindService(locationIntent, locationConnection, Context.BIND_AUTO_CREATE);
                        }
                    });

                    locationThread.start();
                }
                if(camera == 1){
                    Log.e("VICTIM", "Photo started");
                    deleteAllImages();
                    Toast.makeText(getApplicationContext(),"Camera capturing started",Toast.LENGTH_LONG).show();
                    Thread photoThread= new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Intent photoIntent = new Intent(getApplicationContext(),VictimPhotos.class);
                            photoIntent.putExtra("victimName",victimName);
                            bindService(photoIntent, photoConnection, Context.BIND_AUTO_CREATE);
                        }
                    });

                    photoThread.start();
                    //take photos
                    try{
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    for (int i = 0;i<numOfPhotos;i++){
                                        Thread.sleep(1000);
                                        photoService.takePicture();
                                        imageCounter++;

                                        if (imageCounter == numOfPhotos){
                                            uploadPhotos();
                                        }
                                    }

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        t.start();


                    }catch (Exception e){

                    }


            }


            }
        });

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


    //Location connection
    private ServiceConnection locationConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            LocationBinder binder = (LocationBinder) iBinder;
            locService = binder.getService();
            isBoundLocation = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBoundLocation = false;
        }
    };

    //Photo connection
    private ServiceConnection photoConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            PhotosBinder binder = (PhotosBinder) iBinder;
            photoService = binder.getService();
            isBoundPhoto = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBoundPhoto = false;
        }
    };

    //Upload photos to parse.com
    private void uploadPhotos(){
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        final File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File filelist[] =  sdcard.listFiles();
        for (File temp:filelist){
            if(temp.isFile()){
                String imageURI = sdcard.getAbsolutePath()+"/"+temp;
                Log.e("VICTIM",temp.length()+"");
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(temp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inSampleSize = 2;
                Bitmap bm = BitmapFactory.decodeFile(temp.getAbsolutePath(), bmOptions);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, out);

                byte[] byteArray = out.toByteArray();
                Log.e("VICTIM",byteArray.length+"");

                ParseFile file = new ParseFile(temp.getName(),byteArray);
                file.saveInBackground(new ProgressCallback() {
                    @Override
                    public void done(Integer integer) {
                        Log.e("VICTIM","uploaded");
                    }
                });
                String timestamp = s.format(new Date());
                ParseObject image = new ParseObject("victimImage");
                image.put("victimName",victimName);
                image.put("timestamp",timestamp);
                image.put("image",file);
                Log.e("VICTIM", victimName + "");
                Log.e("VICTIM",timestamp+"");


                image.saveInBackground();

            }

        }
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

    //Read logged victim name
    public String read(String fname){

        BufferedReader br = null;
        String response = null;

        try {

            StringBuffer output = new StringBuffer();
            String fpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/"+fname;

            br = new BufferedReader(new FileReader(fpath));
            String line = "";
            while ((line = br.readLine()) != null) {
                output.append(line);
            }
            response = output.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
        return response;

    }

    //Check if network available
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
