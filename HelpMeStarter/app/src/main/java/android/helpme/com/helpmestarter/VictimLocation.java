package android.helpme.com.helpmestarter;
/*
    Programmer: Vitaly Simonovich
    ID: 309398311
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VictimLocation extends Service implements LocationListener {


    private LocationManager mLocationManager;
    private static final int INTERVAL = 5000; // minimum time interval between location updates (milliseconds)
    private static final float DISTANCE = 1f; // minimum distance between location updates (meters)

    private final IBinder locationBinder =  new LocationBinder();
    private String msg = "";
    private SimpleDateFormat s;
    private String victimName;

    public VictimLocation() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        if (mLocationManager == null){
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(this);
            } catch (Exception ex) {
                Log.e("GPS", "fail to remove location listeners, ignore", ex);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        victimName = intent.getExtras().getString("victimName");
        return locationBinder;
    }


    @Override
    public void onLocationChanged(Location location) {
         String timestamp = s.format(new Date());
        ParseObject victim = new ParseObject("victim");
        victim.put("victimName", victimName);
        victim.put("timestamp",timestamp);
        victim.put("latitude",location.getLatitude());
        victim.put("longitude", location.getLongitude());
        victim.saveInBackground();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(getBaseContext(), "Gps is turned on!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();
    }



    public class LocationBinder extends Binder {
        VictimLocation getService(){
            return VictimLocation.this;
        }
    }

    private void storeGPSToDB(int victimID,double lan,double lon,String gpsStamp){
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        db.addGPSCor(victimID, lan, lon, gpsStamp);
        db.close();
    }


}
