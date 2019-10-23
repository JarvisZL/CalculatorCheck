package com.example.projectone;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;

public class PictureHandle {
    private static final int min_tresh = 5;//波峰最小幅度
    private static final int min_range = 5;//波峰最小间隔

    private static final int VPRO = 0;//segmode
    private static final int HPRO = 1;

    private static final int BLACK = 0;//color
    private static final int WITHE = 255;

    private static final int CWidth = 300;//clipsize
    private static final int CHeight = 300;

    private static final int SWidth = 28;//standard
    private static final int SHeight = 28;

    private static final String TAG = "JARVIS in handle";
    private static final String filepath = Environment.getExternalStorageDirectory().getPath()+ "/ZLYTEST/afterseg/";

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

        Imgproc.bilateralFilter(gray, bf, d, (double) (d * 2), (double) (d / 2));
        Log.i(TAG,"After denoising");
        Imgproc.adaptiveThreshold(bf, out, 255.0D, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 4.5D);
        Log.i(TAG,"After adaptiveThreshold");
        /*
        Imgproc.adaptiveThreshold(gray, bf, 255.0D, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 4.5D);
        Log.i(TAG,"After adaptiveThreshold");
        Imgproc.bilateralFilter(bf, out, d, (double) (d * 2), (double) (d / 2));
        Log.i(TAG,"After denoising");
        */
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

    //腐蚀和膨胀
    public static Bitmap erodeAnddialte_Opencv(Bitmap bitmap){
        Bitmap res = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
        Mat origin = new Mat();
        Mat out = new Mat();
        Mat structImg = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(5,2));
        Utils.bitmapToMat(bitmap,origin);
        //腐蚀
        Imgproc.erode(origin,out,structImg,new Point(-1,-1),2);
        origin = out;
        //膨胀
        Imgproc.dilate(origin,out,structImg,new Point(-1,-1),2);
        Utils.matToBitmap(out,res);
        return res;
    }

    public static void saveImg(String path,Mat img){
        try{
            Imgcodecs.imwrite(path,img);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"save failed.");
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)//android 7
    public static void Drawprojection(int[] pos, int mode){
        if(mode == HPRO){
            int width = Arrays.stream(pos).max().getAsInt();
            int height = pos.length;
            Mat project = Mat.zeros(height,width, CvType.CV_8SC1);//黑底图
            for(int i = 0; i < project.rows(); ++i){
                for(int j = 0; j < pos[i]; ++j){
                    project.put(i,j,WITHE);
                }
            }
            saveImg(filepath+"line.png",project);
        }
        else if(mode == VPRO){

        }

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Bitmap cutImg(Bitmap bitmap){
       Mat origin = new Mat();
       Utils.bitmapToMat(bitmap,origin);
       List<Mat> ycutpoint = cutImginmode(origin,HPRO);//得到了每一行图片的矩阵。
       for(int i = 0; i < ycutpoint.size(); ++i){
          // saveImg(filepath+"line"+i+".png",ycutpoint.get(i));
           List<Mat> xcutpoint = cutImginmode(ycutpoint.get(i), VPRO);
          for(int j = 0; j < xcutpoint.size(); ++j){
               saveImg(filepath+"img("+i+j+").png",xcutpoint.get(j));
           }

       }

       Bitmap res = Bitmap.createBitmap(ycutpoint.get(0).cols(),ycutpoint.get(0).rows(),Bitmap.Config.ARGB_8888);
       Utils.matToBitmap(ycutpoint.get(0),res);
       return res;
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<Mat> cutImginmode(Mat origin,int mode){
        if(mode == HPRO){
            int nWidth = origin.cols(), nHeight = origin.rows();
            int[] xNum = new int[nHeight];
            // 统计出每行黑色像素点的个数
            for (int i = 0; i < nHeight; i++) {
                for (int j = 0; j < nWidth; j++) {
                    if (((int)origin.get(i,j)[0]) == BLACK) {
                        xNum[i]++;
                    }
                }
            }
            //画图
           // Drawprojection(xNum,mode);
            //分割
            List<Mat> YMat = new ArrayList<>();
            int begin =0,end = 0;
            for(int i = 0; i < xNum.length; ++i){
                if(xNum[i] > min_tresh && begin == 0){
                    begin = i;
                }
                else if(xNum[i] > min_tresh && begin != 0){
                    continue;
                }
                else if(xNum[i] < min_tresh && begin != 0){
                    end = i;
                    if(end - begin >= min_range){//find a row
                         int height = end - begin;
                         System.out.println("height:" + height);
                         Mat temp = new Mat(origin,new Rect(0,begin,nWidth,height));
                         Mat t = new Mat();
                         temp.copyTo(t);
                         YMat.add(t);
                    }
                    begin = end = 0;
                }
                else if(xNum[i] < min_tresh || begin == 0) {
                    continue;
                }
            }
            return YMat;
        }
        else if(mode == VPRO){
            int nWidth = origin.cols(), nHeight = origin.rows();
            int[] yNum = new int[nWidth];

            // 统计出每列黑色像素点的个数
            for (int i = 0; i < nWidth; i++) {
                for (int j = 0; j < nHeight; j++) {
                    if (((int)origin.get(j,i)[0]) == BLACK) {
                        yNum[i]++;
                    }
                }
            }

            //画图
            // Drawprojection(yNum,mode);

            //分割
            List<Mat> XMat = new ArrayList<>();
            int begin =0,end = 0;
            for(int i = 0; i < yNum.length; ++i){
                if(yNum[i] > min_tresh && begin == 0){
                    begin = i;
                }
                else if(yNum[i] > min_tresh && begin != 0){
                    continue;
                }
                else if(yNum[i] < min_tresh && begin != 0){
                    end = i;
                    if(end - begin >= min_range){//find a column
                        int width = end - begin;
                        System.out.println("width:" + width);
                        Mat temp = new Mat(origin,new Rect(begin,0,width,nHeight));
                        Mat t = new Mat();
                        temp.copyTo(t);
                        XMat.add(t);
                    }
                    begin = end = 0;
                }
                else if(yNum[i] < min_tresh || begin == 0) {
                    continue;
                }
            }

            return XMat;
        }
        else
            return null;
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
