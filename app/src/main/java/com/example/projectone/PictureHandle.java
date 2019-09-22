package com.example.projectone;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class PictureHandle {

    public static final int FILTER_OSTU = 1;


    public static Bitmap BinarizationWithDenoising(Bitmap bitmap,int d){
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



    public  static Bitmap getBinaryImage(Bitmap bitmap,int type) throws Exception{
          int width = bitmap.getWidth() ;
          int height = bitmap.getHeight();
          int[] pixels = new int[width*height];
          bitmap.getPixels(pixels,0,width,0,0,width,height);
          int[] gray = pixels;

          switch(type){
              case FILTER_OSTU:
                  gray = filter_ostu(width,height,pixels);
                  break;
              default:
                  break;
          }

          Bitmap newbmp = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
          newbmp.setPixels(gray,0,width,0,0,width,height);

          return newbmp;
      }


    private static int[] filter_ostu(int width, int height, int[] inPixels) {
        // 图像灰度化
        int[] outPixels = new int[width * height];
        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index = i * width + j;
                int argb = inPixels[width * i + j];
                int red = (argb >> 16) & 0xff;
                int green = (argb >> 8) & 0xff;
                int blue = argb & 0xff;
                int gray = (int) (0.3 * red + 0.59 * green + 0.11 * blue);
                outPixels[width * i + j] = gray;
            }
        }

        int[] histogram = new int[256];
        for (int row = 0; row < height; row++) {
            int tr = 0;
            for (int col = 0; col < width; col++) {
                index = row * width + col;
                tr = (inPixels[index] >> 16) & 0xff;
                histogram[tr]++;
            }
        }
        // 图像二值化 - OTSU 阈值化方法
        double total = width * height;
        double[] variances = new double[256];
        for (int i = 0; i < variances.length; i++) {
            double bw = 0;
            double bmeans = 0;
            double bvariance = 0;
            double count = 0;
            for (int t = 0; t < i; t++) {
                count += histogram[t];
                bmeans += histogram[t] * t;
            }
            bw = count / total;
            bmeans = (count == 0) ? 0 : (bmeans / count);
            for (int t = 0; t < i; t++) {
                bvariance += (Math.pow((t - bmeans), 2) * histogram[t]);
            }
            bvariance = (count == 0) ? 0 : (bvariance / count);
            double fw = 0;
            double fmeans = 0;
            double fvariance = 0;
            count = 0;
            for (int t = i; t < histogram.length; t++) {
                count += histogram[t];
                fmeans += histogram[t] * t;
            }
            fw = count / total;
            fmeans = (count == 0) ? 0 : (fmeans / count);
            for (int t = i; t < histogram.length; t++) {
                fvariance += (Math.pow((t - fmeans), 2) * histogram[t]);
            }
            fvariance = (count == 0) ? 0 : (fvariance / count);
            variances[i] = bw * bvariance + fw * fvariance;
        }

        // find the minimum within class variance
        double min = variances[0];
        int threshold = 0;
        for (int m = 1; m < variances.length; m++) {
            if (min > variances[m]) {
                threshold = m;
                min = variances[m];
            }
        }
        // 二值化
       // System.out.println("final threshold value : " + threshold);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                index = row * width + col;
                int gray = (inPixels[index] >> 8) & 0xff;
                if (gray > threshold) {
                    gray = 255;
                    outPixels[index] = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
                } else {
                    gray = 0;
                    outPixels[index] = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
                }

            }
        }
        return outPixels;
    }


}
