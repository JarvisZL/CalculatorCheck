package com.example.projectone;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class PictureShow extends AppCompatActivity {

    private static final String TAG="JARVIS IN PICSHOW";
    public static final String EXTRA_PHOTO = "exphoto";
    private static String datapath;
    ImageView imageView;
    TextView textView;
    Bitmap bitmap;
    private Uri imguri;

    private Classifier mclassifier;

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
            Log.i(TAG,"from uri to bitmap failed");
        }

        bitmap = PictureHandle.resize_28_Opencv(bitmap);

        //Opencv
        try {
            bitmap = PictureHandle.BinarizationWithDenoising_Opencv(bitmap,20);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle failed");
        }

        //tess-two check
        /*
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
        }*/


        //TensorFlow check
        tensorflowinit();
       // Log.i(TAG,"initsuccess");
        imageView.setImageBitmap(bitmap);
       // Log.i(TAG,"getbitmap");
        tensorflowrun(bitmap);
       // Log.i(TAG,"runsuccess");
    }


    private void tensorflowinit(){
        try{
            mclassifier = new Classifier(PictureShow.this);
        } catch (IOException e) {
            Toast.makeText(this, R.string.failed_to_create_classifier, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void tensorflowrun(Bitmap bitmap){
        if(mclassifier == null){
            Log.i(TAG,"classifier is null");
        }
        Result result = mclassifier.classify(bitmap);
        textView.setText(String.valueOf(result.getNumber()));
    }
}
