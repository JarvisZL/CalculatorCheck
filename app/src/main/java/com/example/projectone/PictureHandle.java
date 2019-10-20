package com.example.projectone;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
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

    private static final int BLACK = 0;
    private static final int WITHE = 255;
    private static final int CWidth = 300;
    private static final int CHeight = 300;
    private static final int SWidth = 28;
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
        }

    }

    public static void cutImg(Bitmap bitmap){
       Mat origin = new Mat();
       Utils.bitmapToMat(bitmap,origin);
       List<Mat> ycutpoint = cutImgX(origin);//得到了每一行图片的矩阵。
       for(int i = 0; i < ycutpoint.size(); ++i){
           Mat temp = ycutpoint.get(i);
           saveImg(filepath+"line"+i,temp);
       }
    }


    public static List<Mat> cutImgX(Mat origin){//分割成一行行
        int i, j;
        int nWidth = origin.cols(), nHeight = origin.rows();
        int[] xNum = new int[nHeight], cNum;
        int average = 0;// 记录像素的平均值

        // 统计出每行黑色像素点的个数
        for (i = 0; i < nHeight; i++) {
            for (j = 0; j < nWidth; j++) {
                if (((int)origin.get(i,j)[0]) == BLACK) {
                    xNum[i]++;
                }
            }
        }

        // 经过测试这样得到的平均值最优
        cNum = Arrays.copyOf(xNum, xNum.length);
        Arrays.sort(cNum);
        for (i = 31 * nHeight / 32; i < nHeight; i++) {
            average += cNum[i];
        }
        average /= (nHeight / 32);

        // 把需要切割的y点都存到cutY中
        List<Integer> cutY = new ArrayList<Integer>();
        for (i = 0; i < nHeight; i++) {
            if (xNum[i] > average) {
                cutY.add(i);
            }
        }

        // 优化cutY
        if (cutY.size() != 0) {
            int temp = cutY.get(cutY.size() - 1);
            // 因为线条有粗细,优化cutY
            for (i = cutY.size() - 2; i >= 0; i--) {
                int k = temp - cutY.get(i);
                if (k <= 8) {
                    cutY.remove(i);
                } else {
                    temp = cutY.get(i);
                }
            }
        }
        // 把切割的图片都保存到YMat中
        List<Mat> YMat = new ArrayList<Mat>();
        for (i = 1; i < cutY.size(); i++) {
            // 设置感兴趣的区域
            int startY = cutY.get(i - 1);
            int height = cutY.get(i) - startY;
            Mat temp = new Mat(origin, new Rect(0, startY, nWidth, height));
            Mat t = new Mat();
            temp.copyTo(t);
            YMat.add(t);
        }
        return YMat;
    }

    public static List<Mat> cutImgY(Mat origin){
        int i, j;
        int nWidth = origin.cols(), nHeight = origin.rows();
        int[] xNum = new int[nWidth], cNum;
        int average = 0;// 记录像素的平均值

        // 统计出每列黑色像素点的个数
        for (i = 0; i < nWidth; i++) {
            for (j = 0; j < nHeight; j++) {
                if (((int)origin.get(i,j)[0]) == BLACK) {
                    xNum[i]++;
                }
            }
        }
        // 经过测试这样得到的平均值最优 , 平均值的选取很重要
        cNum = Arrays.copyOf(xNum, xNum.length);
        Arrays.sort(cNum);
        for (i = 31 * nWidth / 32; i < nWidth; i++) {
            average += cNum[i];
        }
        average /= (nWidth / 28);

        // 把需要切割的x点都存到cutX中,
        List<Integer> cutX = new ArrayList<Integer>();
        for (i = 0; i < nWidth; i += 2) {
            if (xNum[i] >= average) {
                cutX.add(i);
            }
        }
        if (cutX.size() != 0) {
            int temp = cutX.get(cutX.size() - 1);
            // 因为线条有粗细,优化cutX
            for (i = cutX.size() - 2; i >= 0; i--) {
                int k = temp - cutX.get(i);
                if (k <= 10) {
                    cutX.remove(i);
                } else {
                    temp = cutX.get(i);
                }
            }
        }
        // 把切割的图片都保存到XMat中
        List<Mat> XMat = new ArrayList<Mat>();
        for (i = 1; i < cutX.size(); i++) {
            // 设置感兴趣的区域
            int startX = cutX.get(i - 1);
            int width = cutX.get(i) - startX;
            Mat temp = new Mat(origin, new Rect(startX, 0, width, nHeight));
            Mat t = new Mat();
            temp.copyTo(t);
            XMat.add(t);
        }
        return XMat;
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
