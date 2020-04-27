package com.example.projectone;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PictureShow extends AppCompatActivity {

    private static final String TAG="Picture Show";
    public static final String EXTRA_PHOTO = "exphoto";
    private static String datapath;
    ImageView imageView,imageView1,imageView2,imageView3,imageView4,imageView5,imageView6,imageView7,imageView8,imageView9,imageView10;
    ImageView imageView11,imageView12,imageView13,imageView14,imageView15,imageView16,imageView17,imageView18,imageView19,imageView20;
    ImageView imageView21,imageView22,imageView23,imageView24,imageView25,imageView26,imageView27,imageView28,imageView29,imageView30;
    TextView textView;
    Bitmap bitmap;
    private List<Mat> imgs;
    private List<Mat> pieces;
    private List<Pair<String,String>> anss;
    private String text;
    private int savecnt;

    private ClassifierForNormal classifierfornomal;
    private ClassifierForHnd classifierformnist;
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
        savecnt = 0;
        imageView = findViewById(R.id.afterbinary);
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
            bitmap = PictureHandle.BinarizationWithDenoising_Opencv(bitmap,8);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle binarization failed");
        }

        PictureHandle.savebitmap(filepath+"binary.png",bitmap);

        imageView.setImageBitmap(bitmap);

        //Cut into pieces
        pieces = PictureHandle.cutIntopieces(bitmap);
//        for(int i = 0; i < pieces.size(); ++i){
//            PictureHandle.saveImg(filepath+"piece"+i+".png",pieces.get(i));
//        }

        ClassfierForHndInit();
        ClassifierForNomarlInit();
        //Cut each piece
        anss = new ArrayList<>();
        text = "";

        for(Mat piece : pieces){
            imgs = PictureHandle.cuteachpieces(piece);
            String line = null;
            boolean changeflag = false;
            for(Mat img : imgs){
                if(img.rows() < 15 && img.cols() < 15) continue;
                Bitmap bitmap1 = Bitmap.createBitmap(img.cols(),img.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(img,bitmap1);
                if(!changeflag)
                    bitmap1 = PictureHandle.resize_Opencv(bitmap1, 28);
                else
                    bitmap1 = PictureHandle.resize_Opencv(bitmap1, 56);
                PictureHandle.savebitmap(filepath+"expand"+(savecnt++)+".png",bitmap1);

                String res;
                if(!changeflag){
                    res = Normalrun(bitmap1);
                }
                else
                    res = Hndrun(bitmap1);
                    //res = Normalrun(bitmap1);

                if(res!=null){
                    if(line == null){
                        line = res;
                    }else{
                        line += res;
                    }
                    if(res.equals("=")){
                        changeflag = true;
                    }
                }
            }
            //check one line
            if(line == null) continue;
            Log.w("Line: ",line);
            String status = Calculator.Check(line);
            line = line + " (" + status + ")" + "\n";
            anss.add(new Pair<>(line,status));
            text = text + line;
    }

        SpannableString spannableString = new SpannableString(text);
        int textindex = 0;
        for(Pair<String,String> pair : anss){
            if(pair.second.equals("Right")){
                //green
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#138D75")), textindex,
                        textindex + pair.first.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textindex += pair.first.length();
            }
            else if(pair.second.equals("Wrong")) {
                //red
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#CB4335")), textindex,
                        textindex + pair.first.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textindex += pair.first.length();
            }
            else if(pair.second.equals("Missing answer")) {
                //yellow
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#F1C40F")), textindex,
                        textindex + pair.first.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textindex += pair.first.length();
            }
            else if(pair.second.equals("Unqualified")){
                //gray
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#515A5A")), textindex,
                        textindex + pair.first.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textindex += pair.first.length();
            }
        }

        textView.setText(spannableString);


        //Cutimg
//        try {
//            imgs = PictureHandle.cutImgbyprojection(bitmap,"characters");
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.i(TAG,"Cut failed");
//        }

        //resize and recognize
//        int judge = 0;
//        int cnt = 0;
//        for(int i = 0; i < imgs.size(); ++i){
//            if(imgs.get(i).cols() < 15 && imgs.get(i).rows() < 15 ) continue;
//            Bitmap bitmap1 = Bitmap.createBitmap(imgs.get(i).cols(),imgs.get(i).rows(),Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(imgs.get(i),bitmap1);
//            //闭运算：消除内部杂点
//            //bitmap1 = PictureHandle.erodeAnddialte_Opencv(bitmap1,0);
//            bitmap1 = PictureHandle.resize_28_Opencv(bitmap1);
//            //膨胀: 填充中间空白
//            //bitmap1 = PictureHandle.erodeAnddialte_Opencv(bitmap1,3);
//            PictureHandle.savebitmap(filepath+"expand"+(savecnt++)+".png",bitmap1);
//            Mat mat = new Mat();
//            Utils.bitmapToMat(bitmap1,mat);
//
//            switch (judge){
//                case 0: switch (cnt){
//                    case 0: imageView1.setImageBitmap(bitmap1); break;
//                    case 1: imageView2.setImageBitmap(bitmap1); break;
//                    case 2: imageView3.setImageBitmap(bitmap1); break;
//                    case 3: imageView4.setImageBitmap(bitmap1); break;
//                    case 4: imageView5.setImageBitmap(bitmap1); break;
//                    case 5: imageView6.setImageBitmap(bitmap1); break;
//                    case 6: imageView7.setImageBitmap(bitmap1); break;
//                    case 7: imageView8.setImageBitmap(bitmap1); break;
//                    case 8: imageView9.setImageBitmap(bitmap1); break;
//                    case 9: imageView10.setImageBitmap(bitmap1); break;
//                    case 10: imageView11.setImageBitmap(bitmap1); break;
//                    case 11: imageView12.setImageBitmap(bitmap1); break;
//                    case 12: imageView13.setImageBitmap(bitmap1); break;
//                    case 13: imageView14.setImageBitmap(bitmap1); break;
//                    case 14: imageView15.setImageBitmap(bitmap1); break;
//                    case 15: imageView16.setImageBitmap(bitmap1); break;
//                    case 16: imageView17.setImageBitmap(bitmap1); break;
//                    case 17: imageView18.setImageBitmap(bitmap1); break;
//                    case 18: imageView19.setImageBitmap(bitmap1); break;
//                    case 19: imageView20.setImageBitmap(bitmap1); break;
//                    case 20: imageView21.setImageBitmap(bitmap1); break;
//                    case 21: imageView22.setImageBitmap(bitmap1); break;
//                    case 22: imageView23.setImageBitmap(bitmap1); break;
//                    case 23: imageView24.setImageBitmap(bitmap1); break;
//                    case 24: imageView25.setImageBitmap(bitmap1); break;
//                    case 25: imageView26.setImageBitmap(bitmap1); break;
//                    case 26: imageView27.setImageBitmap(bitmap1); break;
//                    case 27: imageView28.setImageBitmap(bitmap1); break;
//                    case 28: imageView29.setImageBitmap(bitmap1); break;
//                    case 29: imageView30.setImageBitmap(bitmap1); break;
//                    default: break;
//                } break;
//                case 1:  switch (cnt){
//                    case 30: imageView1.setImageBitmap(bitmap1); break;
//                    case 31: imageView2.setImageBitmap(bitmap1); break;
//                    case 32: imageView3.setImageBitmap(bitmap1); break;
//                    case 33: imageView4.setImageBitmap(bitmap1); break;
//                    case 34: imageView5.setImageBitmap(bitmap1); break;
//                    case 35: imageView6.setImageBitmap(bitmap1); break;
//                    case 36: imageView7.setImageBitmap(bitmap1); break;
//                    case 37: imageView8.setImageBitmap(bitmap1); break;
//                    case 38: imageView9.setImageBitmap(bitmap1); break;
//                    case 39: imageView10.setImageBitmap(bitmap1); break;
//                    case 40: imageView11.setImageBitmap(bitmap1); break;
//                    case 41: imageView12.setImageBitmap(bitmap1); break;
//                    case 42: imageView13.setImageBitmap(bitmap1); break;
//                    case 43: imageView14.setImageBitmap(bitmap1); break;
//                    case 44: imageView15.setImageBitmap(bitmap1); break;
//                    case 45: imageView16.setImageBitmap(bitmap1); break;
//                    case 46: imageView17.setImageBitmap(bitmap1); break;
//                    case 47: imageView18.setImageBitmap(bitmap1); break;
//                    case 48: imageView19.setImageBitmap(bitmap1); break;
//                    case 49: imageView20.setImageBitmap(bitmap1); break;
//                    case 50: imageView22.setImageBitmap(bitmap1); break;
//                    case 51: imageView23.setImageBitmap(bitmap1); break;
//                    case 52: imageView24.setImageBitmap(bitmap1); break;
//                    case 53: imageView25.setImageBitmap(bitmap1); break;
//                    case 54: imageView26.setImageBitmap(bitmap1); break;
//                    case 55: imageView27.setImageBitmap(bitmap1); break;
//                    case 56: imageView28.setImageBitmap(bitmap1); break;
//                    case 57: imageView29.setImageBitmap(bitmap1); break;
//                    case 58: imageView30.setImageBitmap(bitmap1); break;
//                    default: break;
//                } break;
//                default: break;
//            }
//
//            cnt++;
//            String res = tensorflowrun(bitmap1);
//            if(res!=null){
//                if(ans == null){
//                    ans = res;
//                    ans+= " ";
//                }else{
//                    ans += res+" ";
//                }
//            }
//        }


        //textView.setText(ans);
    }


    private void ClassfierForHndInit(){
        try {
            classifierformnist = new ClassifierForHnd(PictureShow.this);
        } catch (IOException e){
            Toast.makeText(this, R.string.failed_to_create_classifier, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void ClassifierForNomarlInit(){
        try{
            classifierfornomal = new ClassifierForNormal(PictureShow.this);
        } catch (IOException e) {
            Toast.makeText(this, R.string.failed_to_create_classifier, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    private String Hndrun(Bitmap bitmap){
        if(classifierformnist == null){
            throw new AssertionError("classifier for Hnd is null");
        }
        Result result = classifierformnist.classify(bitmap);
        float prob = result.getProbability();
        Log.i("Mnist prob: ",prob+" mnum: "+result.getmNumber());
        return result.getchar();
    }

    private String  Normalrun(Bitmap bitmap){
        if(classifierfornomal == null){
            throw new AssertionError("classifier for normal is null");
        }
        Result result = classifierfornomal.classify(bitmap);
        float prob = result.getProbability();
        Log.i("Normal prob: ",prob+" mnum: "+result.getmNumber());
        return result.getchar();
    }

}
