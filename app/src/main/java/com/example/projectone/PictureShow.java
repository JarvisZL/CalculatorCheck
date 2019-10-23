package com.example.projectone;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;

public class PictureShow extends AppCompatActivity {


    private static final String TAG="JARVIS IN PICSHOW";
    public static final String EXTRA_PHOTO = "exphoto";
    private static String datapath;
    ImageView imageView1,imageView2,imageView3;
    TextView textView;
    Bitmap bitmap;
    private Uri imguri;

    private Classifier mclassifier;

    static {
        if(!OpenCVLoader.initDebug()){
            Log.i(TAG,"Opencv faied");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_show);

        imageView1 = findViewById(R.id.afterbinary);
        imageView2 = findViewById(R.id.afterclip);
        imageView3 = findViewById(R.id.afterresize);
        textView = findViewById(R.id.result);
        imguri = (Uri) getIntent().getExtras().get(EXTRA_PHOTO);

        //from Uri to Bitmap
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imguri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG,"from uri to bitmap failed");
        }

        //binary
        try {
            bitmap = PictureHandle.BinarizationWithDenoising_Opencv(bitmap,10);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle binarization failed");
        }

        //erode and bilate
        try {
            bitmap = PictureHandle.erodeAnddialte_Opencv(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle erode and dilate failed");
        }
        imageView1.setImageBitmap(bitmap);


        //Segment
        try {
           Bitmap temp = PictureHandle.cutImg(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"Cut failed");
        }


        //clip
        Log.i(TAG,"before clip, width:"+bitmap.getWidth()+" height:"+bitmap.getHeight());
        try {
            bitmap = PictureHandle.imgClip_Opencv(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle clip failed");
        }
        imageView2.setImageBitmap(bitmap);
        Log.i(TAG,"after clip, width:"+bitmap.getWidth()+" height:"+bitmap.getHeight());

        //resize
        try {
            bitmap = PictureHandle.resize_28_Opencv(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle  resize failed");
        }
        imageView3.setImageBitmap(bitmap);

        //TensorFlow check
        tensorflowinit();
        // Log.i(TAG,"initsuccess");
        tensorflowrun(bitmap);
        // Log.i(TAG,"runsuccess");


        /*tess-two check
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
