package com.example.projectone;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.IOException;

public class PictureShow extends AppCompatActivity {

    private static final String TAG="JARVIS IN PICSHOW";
    public static final String EXTRA_PHOTO = "exphoto";
    private static String datapath;
    ImageView imageView;
    TextView textView;
    Bitmap bitmap;
    Uri imguri;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_show);

        imageView = findViewById(R.id.photo);
        textView = findViewById(R.id.result);

        imguri = (Uri) getIntent().getExtras().get(EXTRA_PHOTO);

        //from Uri to Bitmap
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imguri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Opencv
        try {
            bitmap = PictureHandle.BinarizationWithDenoising(bitmap,20);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Binaryimge
/*
        try {
            bitmap = PictureHandle.getBinaryImage(bitmap,PictureHandle.FILTER_OSTU);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
        //tess-two check
        datapath = Environment.getExternalStorageDirectory().getPath();
        datapath = datapath + "/ZLYTEST";
        imageView.setImageBitmap(bitmap);
        try {
            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.init(datapath,"eng");
            tessBaseAPI.setImage(bitmap);
            textView.setText(tessBaseAPI.getUTF8Text());
        } catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }


        //TensorFlow check



    }
}
