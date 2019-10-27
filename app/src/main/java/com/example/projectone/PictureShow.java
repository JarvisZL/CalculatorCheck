package com.example.projectone;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;
import java.util.List;

public class PictureShow extends AppCompatActivity {

    private static final String TAG="JARVIS IN PICSHOW";
    public static final String EXTRA_PHOTO = "exphoto";
    private static String datapath;
    ImageView imageView1,imageView2,imageView3;
    TextView textView;
    Bitmap bitmap;
    private List<Mat> imgs;
    private String ans;

    private Classifier mclassifier;
    private static final String filepath = Environment.getExternalStorageDirectory().getPath()+ "/ZLYTEST/afterseg/";

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
   //     imageView2 = findViewById(R.id.afterclip);
    //    imageView3 = findViewById(R.id.afterresize);
        textView = findViewById(R.id.result);
        Uri imguri = (Uri) getIntent().getExtras().get(EXTRA_PHOTO);

        //from Uri to Bitmap
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imguri);
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


        //Cutimg
        try {
           imgs = PictureHandle.cutImg(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"Cut failed");
        }

        //testing
/*
        Bitmap bitmap1 = Bitmap.createBitmap(imgs.get(1).cols(),imgs.get(1).rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgs.get(1),bitmap1);
        bitmap1 = PictureHandle.resize_28_Opencv(bitmap1);
        Mat mat1 = new Mat();
        Utils.bitmapToMat(bitmap1,mat1);

        System.out.println("----------------------------------------------");

        Mat mat2 = Imgcodecs.imread(filepath+"expand0.jpg");
        Bitmap bitmap2 = Bitmap.createBitmap(mat2.cols(),mat2.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2,bitmap2);
        bitmap2 = PictureHandle.resize_28_Opencv_simple(bitmap2);
        Utils.bitmapToMat(bitmap2,mat2);

        tensorflowinit();

        ans = tensorflowrun(bitmap1);
        ans += " ";
        ans += tensorflowrun(bitmap2);


        System.out.println("----------------------------------------------");
        System.out.println(mat1.dump());
        System.out.println("----------------------------------------------");
        System.out.println(mat2.dump());

*/
        //clip
        /*Log.i(TAG,"before clip, width:"+bitmap.getWidth()+" height:"+bitmap.getHeight());
        try {
            bitmap = PictureHandle.imgClip_Opencv(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle clip failed");
        }
        imageView2.setImageBitmap(bitmap);
        Log.i(TAG,"after clip, width:"+bitmap.getWidth()+" height:"+bitmap.getHeight());
        */


        tensorflowinit();

        //fill and save
        try{
            PictureHandle.fillandsave(imgs);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle fillandsave failed.");
        }

        //get and resize
        for(int i = 0 ; i<imgs.size(); ++i){
            if(imgs.get(i).rows() < 50 && imgs.get(i).cols() < 50) continue;
            Mat mat = Imgcodecs.imread(filepath+"expand"+i+".jpg");
            Bitmap bitmap1 = Bitmap.createBitmap(mat.cols(),mat.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat,bitmap1);
            bitmap1 = PictureHandle.resize_28_Opencv_withoutfill(bitmap1);

            String res = tensorflowrun(bitmap1);
            if(res!=null){
                if(ans == null){
                    ans = res;
                    ans += " ";
                }else{
                    ans += res;
                    ans += " ";
                }
            }
        }
        textView.setText("The digits are "+ans);


    }

    private void tensorflowinit(){
        try{
            mclassifier = new Classifier(PictureShow.this);
        } catch (IOException e) {
            Toast.makeText(this, R.string.failed_to_create_classifier, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private String  tensorflowrun(Bitmap bitmap){
        if(mclassifier == null){
            Log.i(TAG,"classifier is null");
        }
        Result result = mclassifier.classify(bitmap);
        float prob = result.getProbability();
        Log.i(TAG,"prob : "+prob);
        if(prob < 0.1){
            return null;
        }
        else{
            String ret = String.valueOf(result.getNumber());
            return ret;
        }

    }
}
