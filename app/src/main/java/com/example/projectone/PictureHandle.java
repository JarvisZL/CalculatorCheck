package com.example.projectone;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class PictureHandle {

    public static final int FILTER_OSTU = 1;
    public static final int Width = 28;
    public static final int Height = 28;
    private static  final String TAG = "JARVIS in handle";

    public static Bitmap BinarizationWithDenoising_Opencv(Bitmap bitmap,int d){
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Mat origin = new Mat();
        Mat gray = new Mat();
        Mat bf = new Mat();
        Mat out = new Mat();
        Utils.bitmapToMat(bitmap, origin);
        Imgproc.cvtColor(origin, gray, Imgproc.COLOR_RGB2GRAY);
        // 去燥
        Imgproc.bilateralFilter(gray, bf, d, (double) (d * 2), (double) (d / 2));
        Imgproc.adaptiveThreshold(bf, out, 255.0D, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, 10.0D);
        Utils.matToBitmap(out, result);
        origin.release();
        gray.release();
        bf.release();
        out.release();
        return result;
    }


    public static Bitmap resize_28_Opencv(Bitmap bitmap) {
        Bitmap res = Bitmap.createBitmap(Width,Height,Bitmap.Config.ARGB_8888);
        Mat origin = new Mat();
        Mat out  = new Mat();
        Size size = new Size(28,28);
        Utils.bitmapToMat(bitmap,origin);
        Imgproc.resize(origin,out,size,0,0,Imgproc.INTER_AREA);
        Utils.matToBitmap(out,res);
        origin.release();
        out.release();
        return res;
    }

    public static Bitmap resize_28(Bitmap bitmap){
          int width = bitmap.getWidth();
          int height = bitmap.getHeight();
          float scalew = ((float)Width/width);
          float scaleh = ((float)Height/height);
          Matrix matrix = new Matrix();
          matrix.postScale(scalew,scaleh);

          bitmap = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
          width = bitmap.getWidth();
          height = bitmap.getHeight();
          Log.i(TAG,"width:"+String.valueOf(width)+" height:"+String.valueOf(height));
          return bitmap;
    }

}
