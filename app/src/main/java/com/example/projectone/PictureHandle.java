package com.example.projectone;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import static java.lang.Math.min;

public class PictureHandle {

    public static final int CWidth = 250;
    public static final int CHeight = 250;
    public static final int SWidth = 28;
    public static final int SHeight = 28;
    private static  final String TAG = "JARVIS in handle";

    public static Bitmap BinarizationWithDenoising_Opencv(Bitmap bitmap,int d){
        Log.i(TAG,"begin to binarizaiton");
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Mat origin = new Mat();
        Mat gray = new Mat();
        Mat bf = new Mat();
        Mat out = new Mat();
        Utils.bitmapToMat(bitmap, origin);
        Imgproc.cvtColor(origin, gray, Imgproc.COLOR_RGB2GRAY);
        Log.i(TAG,"After cvtColor");
        // 去燥
        Imgproc.bilateralFilter(gray, bf, d, (double) (d * 2), (double) (d / 2));
        Log.i(TAG,"After denoising");
        Imgproc.adaptiveThreshold(bf, out, 255.0D, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 4.5D);
        Log.i(TAG,"After adaptiveThreshold");
        Utils.matToBitmap(out, result);
        origin.release();
        gray.release();
        bf.release();
        out.release();
        return result;
    }


    public static Bitmap resize_28_Opencv(Bitmap bitmap) {
        Bitmap res = Bitmap.createBitmap(SWidth,SHeight,Bitmap.Config.ARGB_8888);
        Mat origin = new Mat();
        Mat out  = new Mat();
        Size size = new Size(SWidth,SHeight);
        Utils.bitmapToMat(bitmap,origin);
        Imgproc.resize(origin,out,size,0,0,Imgproc.INTER_AREA);
        Utils.matToBitmap(out,res);
        origin.release();
        out.release();
        return res;
    }


    public static Bitmap imgClip_Opencv(Bitmap bitmap){
        if(bitmap.getHeight() <= CHeight && bitmap.getWidth() <= CWidth){
            return bitmap;
        }
        else{
            int newH = min(CHeight,bitmap.getHeight());
            int newW = min(CWidth,bitmap.getWidth());
            Bitmap res = Bitmap.createBitmap(newW,newH,Bitmap.Config.ARGB_8888);
            Mat origin = new Mat();
            int x_start,y_start;
            x_start = bitmap.getWidth()/2-newW/2;
            y_start = bitmap.getHeight()/2-newH/2;
            Rect rect = new Rect(x_start,y_start,newW,newH);
            Utils.bitmapToMat(bitmap,origin);
            Mat out  = new Mat(origin,rect);
            Utils.matToBitmap(out,res);
            return res;
        }
    }

    public  static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        Bitmap ret = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            ret = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(ret == null){
            ret = bitmap;
        }
        if(ret != bitmap){
            bitmap.recycle();
        }
        return ret;
    }

    public  static int getBitmapdgree(String filepath) {
        int ret = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filepath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    ret = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    ret = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    ret = 270;
                    break;
                default:
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, String.valueOf(ret));
        return ret;
    }







}
