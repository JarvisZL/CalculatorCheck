package com.example.projectone;

import android.graphics.Bitmap;
import android.graphics.Color;

public class PictureHandle {

    public static final int FILTER_OSTU = 1;
    public static final int FILTER_ONLY = 2;
    public static final int FILTER_ITERATOR = 3;
    public static final int FILTER_MAXTRIX = 4;


    public  static Bitmap getBinaryImage(Bitmap bitmap,int type) throws Exception{
          int width = bitmap.getWidth() ;
          int height = bitmap.getHeight();
          int[] pixels = new int[width*height];
          bitmap.getPixels(pixels,0,width,0,0,width,height);
          int[] gray = pixels;

          switch(type){
              case FILTER_ONLY:
                  gray = filter_only(width,height,pixels);
                  break;
              case FILTER_OSTU:
                  gray = filter_ostu(width,height,pixels);
                  break;
              case FILTER_ITERATOR:
                  gray = filter_iterator(width,height,pixels);
                  break;
              case FILTER_MAXTRIX:
               //   gray = filter_matrix(width,height,pixels);
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

    private static int[] filter_only(int width, int height, int[] inputs) {
        int[] newpixel = new int[width*height];

        for(int i = 0; i < height; ++i){
            for(int j = 0; j < width; ++j){
                int argb = inputs[width*i+j];
                int alpha = argb & 0xff000000;
                int red = (argb >> 16) & 0xff;
                int green = (argb >> 8) & 0xff;
                int blue = (argb) & 0xff;
                int gray = (int) (0.3*red+0.59*green+0.11*blue);
                if(gray <= 95){
                    gray = 0;
                }else{
                    gray = 255;
                }
                newpixel[width*i+j] = gray;
            }
        }
        return newpixel ;
    }


    public static int[] filter_iterator(int width, int height, int[] inputs){
          int[] grayArr = new int[width*height];
          int[] newpixel = new int[width*height];

          for(int i = 0; i < height; ++i){
              for(int j = 0; j < width; ++j){
                  int argb = inputs[width*i+j];
                  int red = (argb >> 16) & 0xff;
                  int green = (argb >> 8) & 0xff;
                  int blue = (argb) & 0xff;
                  int gray = (int) (0.3*red+0.59*green+0.11*blue);
                  grayArr[width*i+j] = gray;
              }
          }

          // find the maximal and minimal grayscale value zmax and zmin
          int Gmax = grayArr[0], Gmin = grayArr[0];
          for (int index = 0; index < width * height; index++) {
              if (grayArr[index] > Gmax) {
                  Gmax = grayArr[index];
              }
              if (grayArr[index] < Gmin) {
                  Gmin = grayArr[index];
              }
          }

          //获取灰度直方图
          int i, j, t, count1 = 0, count2 = 0, sum1 = 0, sum2 = 0;
          int bp, fp;
          int[] histogram = new int[256];
          for (t = Gmin; t <= Gmax; t++) {
              for (int index = 0; index < width * height; index++) {
                  if (grayArr[index] == t)
                      histogram[t]++;
              }
          }

          /*
           * 迭代法求出最佳分割阈值
           * */
          int T = 0;
          int newT = (Gmax + Gmin) / 2;//初始阈值
          while (T != newT)
          //求出背景和前景的平均灰度值bp和fp
          {
              for (i = 0; i < T; i++) {
                  count1 += histogram[i];//背景像素点的总个数
                  sum1 += histogram[i] * i;//背景像素点的灰度总值
              }
              bp = (count1 == 0) ? 0 : (sum1 / count1);//背景像素点的平均灰度值

              for (j = i; j < histogram.length; j++) {
                  count2 += histogram[j];//前景像素点的总个数
                  sum2 += histogram[j] * j;//前景像素点的灰度总值
              }
              fp = (count2 == 0) ? 0 : (sum2 / count2);//前景像素点的平均灰度值
              T = newT;
              newT = (bp + fp) / 2;
          }
          int finestYzt = newT; //最佳阈值

          //二值化
          for (int index = 0; index < width * height; index++) {
              if (grayArr[index] > finestYzt)
                  newpixel[index] = Color.WHITE;
              else newpixel[index] = Color.BLACK;
          }
          return newpixel;
      }

}
