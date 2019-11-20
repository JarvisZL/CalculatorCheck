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

import java.io.IOException;
import java.util.List;


public class PictureShow extends AppCompatActivity {

    private static final String TAG="JARVIS IN PICSHOW";
    public static final String EXTRA_PHOTO = "exphoto";
    private static String datapath;
    ImageView imageView,imageView1,imageView2,imageView3,imageView4,imageView5,imageView6,imageView7,imageView8,imageView9,imageView10;
    ImageView imageView11,imageView12,imageView13,imageView14,imageView15,imageView16,imageView17,imageView18,imageView19,imageView20;
    ImageView imageView21,imageView22,imageView23,imageView24,imageView25,imageView26,imageView27,imageView28,imageView29,imageView30;
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

        ans = null;
        imageView = findViewById(R.id.afterbinary);
        imageView1 = findViewById(R.id.temp1);
        imageView2 = findViewById(R.id.temp2);
        imageView3 = findViewById(R.id.temp3);
        imageView4 = findViewById(R.id.temp4);
        imageView5 = findViewById(R.id.temp5);
        imageView6 = findViewById(R.id.temp6);
        imageView7 = findViewById(R.id.temp7);
        imageView8 = findViewById(R.id.temp8);
        imageView9 = findViewById(R.id.temp9);
        imageView10 = findViewById(R.id.temp10);
        imageView11 = findViewById(R.id.temp11);
        imageView12 = findViewById(R.id.temp12);
        imageView13 = findViewById(R.id.temp13);
        imageView14 = findViewById(R.id.temp14);
        imageView15 = findViewById(R.id.temp15);
        imageView16 = findViewById(R.id.temp16);
        imageView17 = findViewById(R.id.temp17);
        imageView18 = findViewById(R.id.temp18);
        imageView19 = findViewById(R.id.temp19);
        imageView20 = findViewById(R.id.temp20);
        imageView21 = findViewById(R.id.temp21);
        imageView22 = findViewById(R.id.temp22);
        imageView23 = findViewById(R.id.temp23);
        imageView24 = findViewById(R.id.temp24);
        imageView25 = findViewById(R.id.temp25);
        imageView26 = findViewById(R.id.temp26);
        imageView27 = findViewById(R.id.temp27);
        imageView28 = findViewById(R.id.temp28);
        imageView29 = findViewById(R.id.temp29);
        imageView30 = findViewById(R.id.temp30);
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

        //correctAngle
        bitmap = PictureHandle.ImgRecifyByHL(bitmap);


        //erode and bilate
        /*
        try {
            bitmap = PictureHandle.erodeAnddialte_Opencv(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle erode and dilate failed");
        }
         */
        imageView.setImageBitmap(bitmap);


        //Cutimg
        try {
            imgs = PictureHandle.cutImg(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"Cut failed");
        }



        //without save

        tensorflowinit();
        int judge = 0;
        int cnt = 0;
        for(int i = 0; i < imgs.size(); ++i){
            if(imgs.get(i).cols() < 40 && imgs.get(i).rows() < 40) continue;
            Bitmap bitmap1 = Bitmap.createBitmap(imgs.get(i).cols(),imgs.get(i).rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgs.get(i),bitmap1);
            bitmap1 = PictureHandle.erodeAnddialte_Opencv(bitmap1);
            bitmap1 = PictureHandle.resize_28_Opencv(bitmap1);
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap1,mat);

            switch (judge){
                case 0: switch (cnt){
                    case 0: imageView1.setImageBitmap(bitmap1); break;
                    case 1: imageView2.setImageBitmap(bitmap1); break;
                    case 2: imageView3.setImageBitmap(bitmap1); break;
                    case 3: imageView4.setImageBitmap(bitmap1); break;
                    case 4: imageView5.setImageBitmap(bitmap1); break;
                    case 5: imageView6.setImageBitmap(bitmap1); break;
                    case 6: imageView7.setImageBitmap(bitmap1); break;
                    case 7: imageView8.setImageBitmap(bitmap1); break;
                    case 8: imageView9.setImageBitmap(bitmap1); break;
                    case 9: imageView10.setImageBitmap(bitmap1); break;
                    case 10: imageView11.setImageBitmap(bitmap1); break;
                    case 11: imageView12.setImageBitmap(bitmap1); break;
                    case 12: imageView13.setImageBitmap(bitmap1); break;
                    case 13: imageView14.setImageBitmap(bitmap1); break;
                    case 14: imageView15.setImageBitmap(bitmap1); break;
                    case 15: imageView16.setImageBitmap(bitmap1); break;
                    case 16: imageView17.setImageBitmap(bitmap1); break;
                    case 17: imageView18.setImageBitmap(bitmap1); break;
                    case 18: imageView19.setImageBitmap(bitmap1); break;
                    case 19: imageView20.setImageBitmap(bitmap1); break;
                    case 20: imageView21.setImageBitmap(bitmap1); break;
                    case 21: imageView22.setImageBitmap(bitmap1); break;
                    case 22: imageView23.setImageBitmap(bitmap1); break;
                    case 23: imageView24.setImageBitmap(bitmap1); break;
                    case 24: imageView25.setImageBitmap(bitmap1); break;
                    case 25: imageView26.setImageBitmap(bitmap1); break;
                    case 26: imageView27.setImageBitmap(bitmap1); break;
                    case 27: imageView28.setImageBitmap(bitmap1); break;
                    case 28: imageView29.setImageBitmap(bitmap1); break;
                    case 29: imageView30.setImageBitmap(bitmap1); break;
                    default: break;
                } break;
                case 1:  switch (cnt){
                    case 30: imageView1.setImageBitmap(bitmap1); break;
                    case 31: imageView2.setImageBitmap(bitmap1); break;
                    case 32: imageView3.setImageBitmap(bitmap1); break;
                    case 33: imageView4.setImageBitmap(bitmap1); break;
                    case 34: imageView5.setImageBitmap(bitmap1); break;
                    case 35: imageView6.setImageBitmap(bitmap1); break;
                    case 36: imageView7.setImageBitmap(bitmap1); break;
                    case 37: imageView8.setImageBitmap(bitmap1); break;
                    case 38: imageView9.setImageBitmap(bitmap1); break;
                    case 39: imageView10.setImageBitmap(bitmap1); break;
                    case 40: imageView11.setImageBitmap(bitmap1); break;
                    case 41: imageView12.setImageBitmap(bitmap1); break;
                    case 42: imageView13.setImageBitmap(bitmap1); break;
                    case 43: imageView14.setImageBitmap(bitmap1); break;
                    case 44: imageView15.setImageBitmap(bitmap1); break;
                    case 45: imageView16.setImageBitmap(bitmap1); break;
                    case 46: imageView17.setImageBitmap(bitmap1); break;
                    case 47: imageView18.setImageBitmap(bitmap1); break;
                    case 48: imageView19.setImageBitmap(bitmap1); break;
                    case 49: imageView20.setImageBitmap(bitmap1); break;
                    case 50: imageView22.setImageBitmap(bitmap1); break;
                    case 51: imageView23.setImageBitmap(bitmap1); break;
                    case 52: imageView24.setImageBitmap(bitmap1); break;
                    case 53: imageView25.setImageBitmap(bitmap1); break;
                    case 54: imageView26.setImageBitmap(bitmap1); break;
                    case 55: imageView27.setImageBitmap(bitmap1); break;
                    case 56: imageView28.setImageBitmap(bitmap1); break;
                    case 57: imageView29.setImageBitmap(bitmap1); break;
                    case 58: imageView30.setImageBitmap(bitmap1); break;
                    default: break;
                } break;
                default: break;
            }

            cnt++;

            String res = tensorflowrun(bitmap1);
            if(res!=null){
                if(ans == null){
                    ans = res;
                    ans+= " ";
                }else{
                    ans += res+" ";
                }
            }
        }



        //clip
        /*
        Log.i(TAG,"before clip, width:"+bitmap.getWidth()+" height:"+bitmap.getHeight());
        try {
            bitmap = PictureHandle.imgClip_Opencv(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle clip failed");
        }
        Log.i(TAG,"after clip, width:"+bitmap.getWidth()+" height:"+bitmap.getHeight());

         */


        //Save img and rec
        /*
        tensorflowinit();
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
            //debug
            if(i == 0) imageView2.setImageBitmap(bitmap1);
            if(i == 1) imageView3.setImageBitmap(bitmap1);
            if(i == 2) imageView4.setImageBitmap(bitmap1);

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
*/
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
