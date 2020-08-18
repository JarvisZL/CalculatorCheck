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
    private List<List<Mat>> listofimgs;

    private ClassifierForNormal classifierfornomal;
    private ClassifierForHnd classifierformnist;
    private static final String filepath = Environment.getExternalStorageDirectory().getPath()+ "/HmwkCheck/PicShow/";

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

        //通过URI获取在mainactivity中选择或者拍摄的图片
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imguri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG,"from uri to bitmap failed");
        }


        //二值化
        try {
            bitmap = PictureHandle.BinarizationWithDenoising_Opencv(bitmap,9);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"opencv handle binarization failed");
        }

        PictureHandle.savebitmap(filepath+"binary.png",bitmap);
        imageView.setImageBitmap(bitmap);

        //将整个图片分割成单个横式算式
        pieces = PictureHandle.cutIntopieces(bitmap);

        //保存每一个算式
        int cnt = 0;
        for(Mat piece : pieces){
            PictureHandle.saveImg(filepath+"piece"+cnt+".png",piece);
            cnt++;
        }

        //模型初始化/
        ClassfierForHndInit();
        ClassifierForNomarlInit();

        anss = new ArrayList<>();
        text = "";

//        int mean_row = 0, mean_col = 0;
//        int img_cnt = 0;
        //分割每一个算式为单个字符， 每一个算式中的字符用一个List存储，并存在Listofimgs中。
        listofimgs = new ArrayList<>();
        for(Mat piece : pieces){
            //此处调用函数分割
            imgs = PictureHandle.cuteachpieces(piece);
            listofimgs.add(imgs);
            //统计字符的平均长度和宽度
//            for(Mat img : imgs){
//                mean_row += img.rows();
//                mean_col += img.cols();
//                img_cnt++;
//            }
        }
//        mean_col = mean_col / img_cnt;
//        mean_row = mean_row / img_cnt;

        //求字符长度和宽度的方差
//        int var_col = 0, var_row = 0;
//        for(List<Mat> tmpimgs : listofimgs){
//            for(Mat img : tmpimgs){
//                var_col += (img.cols() - mean_col) * (img.cols() - mean_col);
//                var_row += (img.rows() - mean_row) * (img.rows() - mean_row);
//            }
//        }
//        var_row = (int)Math.sqrt(var_row / img_cnt);
//        var_col = (int)Math.sqrt(var_col / img_cnt);

        //开始识别，第一层循环每次取出一个算式中的字符
        for(List<Mat> tmpimgs : listofimgs){
            //当前的结果
            String line = null;
            //changeflag用于判断是否识别出了'='号
            boolean changeflag = false;
            //少于3个不可能凑成一个算式，直接跳过，a=b
            if(tmpimgs.size() < 3) continue;
            //遍历每一个字符
            for(Mat img : tmpimgs){
                //认为都小于10则是干扰项，但此处不够准确，应该用图片相关数据作为界限。
                if(img.rows() < 10 && img.cols() < 10) continue;
                //Mat to Bitmap
                Bitmap bitmap1 = Bitmap.createBitmap(img.cols(),img.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(img,bitmap1);

                if(!changeflag)
                    //还未识别出'='，认为还在打印体识别，缩放为28*28.
                    bitmap1 = PictureHandle.resize_Opencv(bitmap1, 28);
                else
                    //识别出了'='，目前是手写体识别，缩放为56*56.
                    bitmap1 = PictureHandle.resize_Opencv(bitmap1, 56);
                //腐蚀膨胀
                bitmap1 = PictureHandle.erodeAnddialte_Opencv(bitmap1,3);
                PictureHandle.savebitmap(filepath+"expand"+(savecnt++)+".png",bitmap1);

                String res;
                if(!changeflag){
                    //打印体识别
                    res = Normalrun(bitmap1);
                }
                else
                    //手写体识别
                    res = Hndrun(bitmap1);

                if(res!=null){
                    if(line == null){
                        line = res;
                    }else{
                        line += res;
                    }
                    //识别出了'='号
                    if(res.equals("=")){
                        changeflag = true;
                    }
                }
            }

            //一个算式已经识别出来，调用Calculator模块计算验证。
            if(line == null) continue;
            Log.w("Line: ",line);
            String status = Calculator.Check(line);
            line = line + " (" + status + ")" + "\n";
            anss.add(new Pair<>(line,status));
            //text是最终显示出来的文字
            text = text + line;
        }


        //使用spannablestring来给不同情况显示不同颜色。
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
            else if(pair.second.equals("Not a equation")){
                //gray
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#515A5A")), textindex,
                        textindex + pair.first.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                textindex += pair.first.length();
            }
        }

        textView.setText(spannableString);

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
