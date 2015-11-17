package com.helpme.android.helpmereceiver;
/*
    Programmer: Vitaly Simonovich
    ID: 309398311
 */
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.util.ArrayList;


public class PicturesFragment extends Fragment {
    Context thiscontext;
    private ArrayList<String> filePathStrings;
    private ArrayList<String> fileNameStrings;
    private String[] filePath;
    private String[] fileName;
    private File[] listFile;
    GridView grid;
    File file;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout
        thiscontext = container.getContext();
        View v = inflater.inflate(R.layout.fragment_pictures, container,false);

        grid = (GridView) v.findViewById(R.id.gridview);
        filePathStrings = new ArrayList<>();
        fileNameStrings = new ArrayList<>();

        final File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        final File filelist[] =  sdcard.listFiles();
        for (File temp:filelist){
            int i = 0;
            if(temp.isFile()){
                filePathStrings.add(temp.getAbsolutePath());
                fileNameStrings.add(temp.getName());
            }
        }

        filePath = new String[filePathStrings.size()];
        filePathStrings.toArray(filePath);

        fileName = new String[fileNameStrings.size()];
        fileNameStrings.toArray(fileName);


        grid.setAdapter(new GridViewAdapter(thiscontext,filePath,fileName));

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent i = new Intent(getActivity(),FullScreenImage.class);
                // Pass String arrays FilePathStrings
                i.putExtra("filepath", filePath);
                // Pass String arrays FileNameStrings
                i.putExtra("filename", fileName);
                // Pass click position
                i.putExtra("position", position);
                startActivity(i);
            }
        });
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }



    public class GridViewAdapter extends BaseAdapter {

        // Declare variables
        private Context context;
        private String[] filepath;
        private String[] filename;


        public GridViewAdapter(Context a, String[] fpath, String[] fname) {
            context = a;
            filepath = fpath;
            filename = fname;
;

        }

        public int getCount() {
            return filepath.length;

        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null){
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(185, 185));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(5, 5, 5, 5);

            } else {
            imageView = (ImageView) convertView;
            }

            Bitmap bmp = BitmapFactory.decodeFile(filepath[position]);

            // Set the decoded bitmap into ImageView
            imageView.setImageBitmap(bmp);
            return imageView;
        }
    }
}
