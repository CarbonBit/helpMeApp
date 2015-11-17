package android.helpme.com.helpmestarter;
/*
    Programmer: Vitaly Simonovich
    ID: 309398311
 */
import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VictimPhotos extends Service {
    //Camera variables
    //a surface holder
    private SurfaceHolder sHolder;
    //a variable to control the camera
    private Camera mCamera;

    //the camera parameters
    private Camera.Parameters parameters;

    private final IBinder photobinder =  new PhotosBinder();

    private String victimName;
    public VictimPhotos() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //start back camera
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        SurfaceView sv = new SurfaceView(getApplicationContext());

        try {

            mCamera.setPreviewDisplay(sv.getHolder());
            parameters = mCamera.getParameters();
            mCamera.setParameters(parameters);

        } catch (IOException e) { e.printStackTrace(); }

        sHolder = sv.getHolder();
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }

    //When photo had been taken call mCall
    Camera.PictureCallback mCall = new Camera.PictureCallback()
    {

        public void onPictureTaken(final byte[] data, Camera camera)
        {

            FileOutputStream outStream = null;
            try{

                File sd = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "A");
                if(!sd.exists()) {
                    sd.mkdirs();
                    Log.i("FO", "folder" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
                }

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String tar = (sdf.format(cal.getTime()));

                outStream = new FileOutputStream(sd+tar+".jpg");
                outStream.write(data);  outStream.close();

                Log.i("VICTIM", data.length + " byte written to:" + sd + tar + ".jpg");
                camkapa(sHolder);



            } catch (FileNotFoundException e){
                Log.d("VICTIM", e.getMessage());
            } catch (IOException e){
                Log.d("VICTIM", e.getMessage());
            }

        }


    };

    public void camkapa(SurfaceHolder sHolder) {

        if (null == mCamera)
            return;
        mCamera.stopPreview();

    }

    public void takePicture(){

            mCamera.startPreview();
            mCamera.takePicture(null, null, mCall);
            Log.e("VICTIM", "Picture taken");

    }
    @Override
    public IBinder onBind(Intent intent) {
        victimName = intent.getExtras().getString("victimName");
        return photobinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mCamera.release();
        mCamera = null;
        Log.i("VICTIM", "Camera closed");
        return super.onUnbind(intent);
    }

    public class PhotosBinder extends Binder {
        VictimPhotos getService(){
            return VictimPhotos.this;
        }
    }


}
