package com.example.projectone;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

public class PictureHandle {
    private static final int min_tresh = 2; //波峰最小幅度
    private static final int min_range = 10;//波峰最小间隔

    private static final int VPRO = 0;//segmode
    private static final int HPRO = 1;

    private static final int BLACK = 0;//color
    private static final int WITHE = 255;

    private static final int CWidth = 400;//customsize
    private static final int CHeight = 400 ;

    private static final int SWidth = 28;//standard
    private static final int SHeight = 28;

    private static final String TAG = "JARVIS in handle";
    private static final String filepath = Environment.getExternalStorageDirectory().getPath()+ "/ZLYTEST";
    private static int dcntrow = 0, dcntcol = 0;
    private static int cnt = 0;

    public static Bitmap BinarizationWithDenoising_Opencv(Bitmap bitmap,int d){
        Log.i(TAG,"begin to binarizaiton");
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Mat origin = new Mat();
        Mat gray = new Mat();
        Mat bf = new Mat();
        Mat out = new Mat();
        Utils.bitmapToMat(bitmap, origin);
        Imgproc.cvtColor(origin, gray, Imgproc.COLOR_RGBA2GRAY);
        Log.i(TAG,"After cvtColor");

        Imgproc.bilateralFilter(gray, bf, d, (double) (d * 2), (double) (d / 2));
        Log.i(TAG,"After denoising");
        Imgproc.adaptiveThreshold(bf, out, 255.0D, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 2.5D);
        Log.i(TAG,"After adaptiveThreshold");
      //  Imgproc.Canny(bf,out,100,200);
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
        //先扩充成正方形
        Mat smat = new Mat();
        int width = origin.cols(),height = origin.rows();
        Scalar value = new Scalar(WITHE,WITHE,WITHE,WITHE);
        //计算填充后的大小
        int Flength = (int)(max(width,height)*1.2);
        Core.copyMakeBorder(origin,smat,(Flength-height)/2,(Flength-height)/2,(Flength-width)/2,(Flength-width)/2, Core.BORDER_CONSTANT,value);
        //saveImg(filepath+"/afterseg/expand"+(cnt++)+".png",smat);
        //缩放
        Imgproc.resize(smat,out,size,0,0,Imgproc.INTER_AREA);
        Utils.matToBitmap(out,res);
        origin.release();
        out.release();
        return res;
    }

    public static Bitmap resize_28_Opencv_withoutfill(Bitmap bitmap) {
        Bitmap res = Bitmap.createBitmap(SWidth,SHeight,Bitmap.Config.ARGB_8888);
        Mat origin = new Mat();
        Mat out  = new Mat();
        Size size = new Size(SWidth,SHeight);
        Utils.bitmapToMat(bitmap,origin);
        //缩放
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
    public static Bitmap erodeAnddialte_Opencv(Bitmap bitmap,int type){
        Bitmap res = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
        Mat origin = new Mat();
        Mat out = new Mat();
        Mat structImg = Imgproc.getStructuringElement(Imgproc.MARKER_CROSS,new Size(3,3));
        Utils.bitmapToMat(bitmap,origin);
        switch (type){
            case 0:
                Imgproc.morphologyEx(origin,out,Imgproc.MORPH_CLOSE,structImg);
                break;
            case 1:
                Imgproc.morphologyEx(origin,out,Imgproc.MORPH_OPEN,structImg);
                break;
            case 2:
                Imgproc.dilate(origin,out,structImg);
                break;
            case 3:
                Imgproc.erode(origin,out,structImg);
                break;
            default:
                throw new AssertionError("No such a type");
        }
        Utils.matToBitmap(out,res);
        return res;
    }

    public static void savebitmap(String path, Bitmap bitmap){
        Mat origin = new Mat();
        Utils.bitmapToMat(bitmap,origin);
        try{
            Imgcodecs.imwrite(path,origin);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"save failed.");
        }
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
            saveImg(filepath+"/pic/row"+(dcntrow++)+".png",project);
        }
        else if(mode == VPRO){
            int width = pos.length;
            int height = Arrays.stream(pos).max().getAsInt();
            Mat project = Mat.zeros(height,width,CvType.CV_8SC1);
            for(int i = 0; i < project.cols(); ++i){
                for(int j = 0; j < pos[i]; ++j){
                    project.put(j,i, WITHE);
                }
            }
            saveImg(filepath+"/pic/col"+(dcntcol++)+".png",project);
        }

    }

    //水平投影切割后连通域切割
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<Mat> cutImg(Bitmap bitmap){
        Mat origin = new Mat();
        Utils.bitmapToMat(bitmap,origin);
        List<Mat> ret = new ArrayList<>();
        List<Mat> ycutpoint = cutImginmode(origin,HPRO);
        for(int i = 0; i < ycutpoint.size(); ++i){
            Bitmap temp = Bitmap.createBitmap(ycutpoint.get(i).cols(),ycutpoint.get(i).rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(ycutpoint.get(i),temp);
            List<Mat> ss = cutImgbyseedfill(temp);
            ret.addAll(ss);
        }
        return ret;
    }

    //投影切割
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<Mat> cutImgbyprojection(Bitmap bitmap){
       Mat origin = new Mat();
       Utils.bitmapToMat(bitmap,origin);
       List<Mat> ret = new ArrayList<>();
       List<Mat> ycutpoint = cutImginmode(origin,HPRO);//得到了每一行图片的矩阵。
       for(int i = 0; i < ycutpoint.size(); ++i){
          // saveImg(filepath+"/afterseg/line"+i+".png",ycutpoint.get(i));
           List<Mat> xcutpoint = cutImginmode(ycutpoint.get(i), VPRO);
          for(int j = 0; j < xcutpoint.size(); ++j){
               List<Mat> finalcutpoint = cutImginmode(xcutpoint.get(j),HPRO);
               for(int k = 0; k < finalcutpoint.size(); ++k){//always only once
                   saveImg(filepath+"/afterseg/img("+i+","+j+").png",finalcutpoint.get(k));
                   ret.add(finalcutpoint.get(k));
               }
          }
       }
       origin.release();
       return ret;
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
           Drawprojection(xNum,mode);
            //分割
            List<Mat> YMat = new ArrayList<>();
            int begin =-1,end = 0;
            for(int i = 0; i < xNum.length; ++i){
                if(xNum[i] > min_tresh && begin == -1){
                    begin = i;
                }
                else if((xNum[i] < min_tresh && begin != -1)||(i == xNum.length-1 && begin != -1 && end == 0)){
                    end = i;
                    if(end - begin >= min_range){//find a row
                         int height = end - begin;
                         System.out.println("begin:"+begin+" end:"+end);
                         Mat temp = new Mat(origin,new Rect(0,begin,nWidth,height));
                         Mat t = new Mat();
                         temp.copyTo(t);
                         YMat.add(t);
                    }
                    begin =-1;
                    end = 0;

                }
                else if(xNum[i] > min_tresh && begin !=-1){
                    continue;
                }
                else if(xNum[i] < min_tresh || begin == -1) {
                    continue;
                }
            }
          //  System.out.println("fbegin: "+begin+" fend: "+end);
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
            Drawprojection(yNum,mode);
            //分割
            List<Mat> XMat = new ArrayList<>();
            int begin =-1,end = 0;
            for(int i = 0; i < yNum.length; ++i){
                if(yNum[i] > min_tresh && begin == -1){
                    begin = i;
                }
                else if((yNum[i] < min_tresh && begin != -1)||(i == yNum.length-1 && begin != -1 && end == 0)){
                    end = i;
                    if(end - begin >= min_range){//find a column
                        int width = end - begin;
                        System.out.println("begin:"+begin+" end:"+end);
                        Mat temp = new Mat(origin,new Rect(begin,0,width,nHeight));
                        Mat t = new Mat();
                        temp.copyTo(t);
                        XMat.add(t);
                    }
                    begin=-1;
                    end = 0;
                }
                else if(yNum[i] > min_tresh && begin != -1){
                    continue;
                }
                else if(yNum[i] < min_tresh || begin == -1) {
                    continue;
                }
            }
            return XMat;
        }
        else
            return null;
    }

    //全连通域切割
    public static List<Mat> cutImgbyseedfill(Bitmap bitmap){
        Mat origin  = new Mat();
        Utils.bitmapToMat(bitmap,origin);
        int label = 0;
        ArrayList<BOCD> bocds = new ArrayList<>();
        int[][] mmap = new int[origin.rows()][origin.cols()];
        Stack<Point> stack = new Stack<>();
        stack.clear();

        for(int j = 0; j < origin.cols(); ++j){
            for (int i = 0; i < origin.rows(); ++i){
                if(BLACK == (int)origin.get(i, j)[0] && mmap[i][j] == 0){
                        mmap[i][j] = ++label;
                        stack.push(new Point(j,i));
                        bocds.add(new BOCD(label,j,j,i,i));
                        //get the whole connected domain
                        while(!stack.empty()){
                            Point seed = stack.pop();
                            int c = (int)seed.x, r = (int) seed.y;
                            //top
                            if(r - 1 >= 0 && mmap[r-1][c] == 0 && BLACK == (int)origin.get(r-1,c)[0]){
                                mmap[r-1][c] = label;
                                stack.push(new Point(c,r-1));
                                //update top bound
                                BOCD bocd = bocds.get(label-1);//from zero
                                bocd.setTop(min(r-1,bocd.getTop()));
                                bocds.set(label-1,bocd);
                            }
                            //bottom
                            if(r + 1 < origin.rows() && mmap[r+1][c] == 0 && BLACK == (int)origin.get(r+1,c)[0]){
                                mmap[r+1][c] = label;
                                stack.push(new Point(c,r+1));
                                //update bottom bound
                                BOCD bocd = bocds.get(label-1);
                                bocd.setBottom(max(r+1,bocd.getBottom()));
                                bocds.set(label-1,bocd);
                            }
                            //left
                            if(c - 1 >= 0 && mmap[r][c-1] == 0 && BLACK == (int)origin.get(r,c-1)[0]){
                                mmap[r][c-1] = label;
                                stack.push(new Point(c-1,r));
                                //update left bound
                                BOCD bocd = bocds.get(label-1);
                                bocd.setLeft(min(c-1,bocd.getLeft()));
                                bocds.set(label-1,bocd);
                            }
                            //right
                            if(c + 1 < origin.cols() && mmap[r][c+1] == 0 && BLACK == (int)origin.get(r,c+1)[0]){
                                mmap[r][c+1] = label;
                                stack.push(new Point(c+1,r));
                                //update right bound
                                BOCD bocd = bocds.get(label-1);
                                bocd.setRight(max(c+1,bocd.getRight()));
                                bocds.set(label-1,bocd);
                            }
                            //left-top
                            if(c - 1 >= 0 && r - 1 >= 0 && mmap[r-1][c-1] == 0 && BLACK == (int)origin.get(r-1,c-1)[0]){
                                mmap[r-1][c-1] = label;
                                stack.push(new Point(c-1,r-1));
                                //update left and top bound
                                BOCD bocd = bocds.get(label-1);
                                bocd.setLeft(min(c-1,bocd.getLeft()));
                                bocd.setTop(min(r-1,bocd.getTop()));
                                bocds.set(label-1,bocd);
                            }
                            //left-bottom
                            if(c-1 >= 0 && r+1 < origin.rows() && mmap[r+1][c-1] == 0 && BLACK == (int)origin.get(r+1,c-1)[0]){
                                mmap[r+1][c-1] = label;
                                stack.push(new Point(c-1, r+1));
                                //update left and bottom bound
                                BOCD bocd = bocds.get(label-1);
                                bocd.setLeft(min(c-1,bocd.getLeft()));
                                bocd.setBottom(max(r+1,bocd.getBottom()));
                                bocds.set(label-1,bocd);
                            }
                            //right-top
                            if(c+1 < origin.cols() && r-1 >= 0 && mmap[r-1][c+1] == 0 && BLACK == (int)origin.get(r-1,c+1)[0]){
                                mmap[r-1][c+1] = label;
                                stack.push(new Point(c+1,r-1));
                                //update right and top bound
                                BOCD bocd = bocds.get(label-1);
                                bocd.setRight(max(c+1,bocd.getRight()));
                                bocd.setTop(min(r-1,bocd.getTop()));
                                bocds.set(label-1,bocd);
                            }
                            //right-bottom
                            if(c+1 < origin.cols() && r+1 < origin.rows() && mmap[r+1][c+1] == 0 && BLACK == (int)origin.get(r+1,c+1)[0]){
                                mmap[r+1][c+1] = label;
                                stack.push(new Point(c+1,r+1));
                                //update right and bottom bound
                                BOCD bocd = bocds.get(label-1);
                                bocd.setRight(max(c+1,bocd.getRight()));
                                bocd.setBottom(max(r+1,bocd.getBottom()));
                                bocds.set(label-1,bocd);
                            }
                        }
                }
            }
        }

        List<Mat> ret = new ArrayList<>();

        for(int i = 0; i < bocds.size();++i){
            BOCD bocd = bocds.get(i);
          //System.out.println(i+" left: "+bocd.getLeft()+" right: "+bocd.getRight()+" top: "+bocd.getTop()+" bottom: "+bocd.getBottom());
            int width = bocd.getRight()-bocd.getLeft()+1;
            int height = bocd.getBottom()-bocd.getTop()+1;
            Mat res = new Mat(origin,new Rect(bocd.getLeft(),bocd.getTop(),width,height));
            ret.add(res);
        }
        return ret;
    }

    //获取图片时旋转
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
    public  static int getBitmapdgree(String path) {
        int ret = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
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




















    //角度矫正
    public static Bitmap ImgRecifyByHL(Bitmap bitmap){
        Mat origin = new Mat();
        Utils.bitmapToMat(bitmap,origin);
        double degree = calcdegree(origin);
        Mat res = rotateImg(origin,degree);
        Bitmap bitmap1 = Bitmap.createBitmap(res.cols(),res.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(res,bitmap1);
        return bitmap1;
    }

    public static Mat rotateImg(Mat origin, double degree){
        Point center = new Point();
        center.x = (float)(origin.cols()/2.0);
        center.y = (float)(origin.rows()/2.0);
        int length = (int) sqrt(origin.cols()*origin.cols()+origin.rows()*origin.rows());
        Mat M = Imgproc.getRotationMatrix2D(center,degree,1);
        Mat ret = new Mat();
        Imgproc.warpAffine(origin,ret,M, new Size(length,length),1,0, new Scalar(WITHE, WITHE,WITHE,WITHE));
        return ret;
    }

    private static double calcdegree(Mat origin){
        Mat midmat = new Mat();
        Mat gray = new Mat();
        Imgproc.Canny(origin,midmat,50,200,3);
        Imgproc.cvtColor(midmat,gray,Imgproc.COLOR_GRAY2BGRA);
        Mat lines = new Mat();
        int threshold = 351;
        do{
            threshold -= 50;
            Imgproc.HoughLines(midmat,lines,1, PI/180,threshold);
        }while(lines.rows() <= 50 && threshold > 0);

        Log.i(TAG,"threshold:" + threshold);
        Log.i(TAG,"linesc: "+lines.cols());
        Log.i(TAG,"linesr: "+lines.rows());

        //Draw lines
        double[] data;
        double rho, theta;
        Point pt1 = new Point();
        Point pt2 = new Point();
        double a,b,x0,y0;
        double sumangle = 0;

        for(int i = 0; i < lines.rows(); ++i){
            data = lines.get(i,0);
            rho = data[0]; theta = data[1];
            a = cos(theta); b = sin(theta);
            x0 = a*rho; y0 = b*rho;
            pt1.x = round(x0+5000*(-b));
            pt1.y = round(y0+5000*a);
            pt2.x = round(x0-5000*(-b));
            pt2.y = round(y0-5000*a);
            Imgproc.line(gray,pt1,pt2,new Scalar(0,0,255),3);

            sumangle += theta;
        }

        saveImg(filepath+"/pic/line.png",gray);

        double averageangle = sumangle/lines.rows();

        Log.i(TAG,"angle:" +((averageangle/PI*180)-90));

        return (averageangle/PI*180)-90;
    }

    public static Bitmap ImgRecifyByDFT(Bitmap bitmap){
        Mat origin = new Mat();
        Utils.bitmapToMat(bitmap,origin);
        Mat res = DFT(origin);
        Bitmap ret = Bitmap.createBitmap(res.cols(),res.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(res,ret);
        return ret;
    }

    public static Mat DFT(Mat origin){
        Mat gray = new Mat();
        Imgproc.cvtColor(origin, gray, Imgproc.COLOR_RGBA2GRAY);
        final int nrows = gray.rows();
        final int ncols = gray.cols();

        int crows = Core.getOptimalDFTSize(nrows);
        int ccols = Core.getOptimalDFTSize(ncols);
        Mat mat = new Mat();
        Core.copyMakeBorder(gray,mat,0,crows-nrows,0,ccols-ncols,Core.BORDER_CONSTANT,new Scalar(WITHE,WITHE,WITHE,WITHE));

        mat.convertTo(mat,CvType.CV_32F);
        List<Mat> groupMats = new ArrayList<>();
        groupMats.add(mat);
        groupMats.add(Mat.zeros(mat.size(),CvType.CV_32F));
        Mat mergemat = new Mat();
        Core.merge(groupMats,mergemat);
        Core.dft(mergemat,mergemat);
        Core.split(mergemat,groupMats);
        Core.magnitude(groupMats.get(0),groupMats.get(1),groupMats.get(0));

        Mat mat1 = groupMats.get(0).clone();
        Mat mat2 = new Mat(mat1.rows(),mat1.cols(),mat1.type(),new Scalar(1,1,1));
        System.out.println(mat1);
        System.out.println("------------------");
        System.out.println(mat2);
        Core.add(mat1,mat2,mat1);
        Core.log(mat1,mat1);

        Core.normalize(mat1,mat1,0,1,Core.NORM_MINMAX);
        mat1.convertTo(mat1,CvType.CV_8UC1,255,0);
        int cx = mat1.cols()/2;
        int cy = mat1.rows()/2;

        Mat temp = new Mat();
        Mat q0 = new Mat(mat1,new Rect(0,0,cx,cy));
        Mat q1 = new Mat(mat1,new Rect(cx,0,cx,cy));
        Mat q2 = new Mat(mat1,new Rect(0,cy,cx,cy));
        Mat q3 = new Mat(mat1,new Rect(cx,cy,cx,cy));

        q0.copyTo(temp);
        q3.copyTo(q0);
        temp.copyTo(q3);

        q1.copyTo(temp);
        q2.copyTo(q1);
        temp.copyTo(q2);

        saveImg(filepath+"/pic/magnititude.png",mat1);

        Mat binarymat = new Mat();
        Imgproc.threshold(mat1,binarymat,155,255,Imgproc.THRESH_BINARY);
        //Imgproc.adaptiveThreshold(mat1, binarymat, 255.0D, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 4.5D);
        binarymat.convertTo(binarymat,CvType.CV_8UC1,255,0);
        Mat lines = new Mat();
        saveImg(filepath+"/pic/binary.png",binarymat);
        Imgproc.HoughLines(binarymat,lines,1,PI/180,50);

        Mat houghmat = new Mat(binarymat.size(),CvType.CV_8UC1);

        saveImg(filepath+"/pic/line0.png",houghmat);

        double[] data;
        Log.i(TAG,"linesr: "+lines.rows());
        for(int i = 0; i < lines.rows(); ++i){
            data = lines.get(i,0);
            double rho = data[0], theta = data[1];
            double a = cos(theta), b = sin(theta);
            double x0 = a*rho, y0 = b*rho;
            Point pt1 = new Point();
            Point pt2 = new Point();
            pt1.x = round(x0+1000*(-b));
            pt1.y = round(y0+1000*a);
            pt2.x = round(x0-1000*(-b));
            pt2.y = round(y0-1000*a);
            Imgproc.line(houghmat,pt1,pt2,new Scalar(255,0,0),1);
        }
        saveImg(filepath+"/pic/line1.png",houghmat);

        double theta = 0;
        int cnt = 0;
        for(int i = 0; i < lines.rows(); ++i){
            double tmp = lines.get(i,0)[1]*180/PI;
            theta += tmp;
            cnt++;
        }
        theta = theta/(cnt == 0 ? 1 : cnt);

        double angleT = nrows*tan(theta/180*PI)/ncols;
        theta = atan(angleT)*180/PI;
        if(theta < 0)
            theta += 90;
        else
            theta -= 90;

        Point center = new Point(ncols/2,nrows/2);
        Mat warpmat = Imgproc.getRotationMatrix2D(center,theta,1);
        Mat ret = new Mat(origin.size(),origin.type());
        Imgproc.warpAffine(origin,ret,warpmat,ret.size(),1,0,new Scalar(WITHE,WITHE,WITHE,WITHE));
        return ret;
    }

}

class BOCD{
    private int index;
    private int left,right,top,bottom;

    public BOCD(int index){
        this.index = index;
        left = right = top = bottom = 0;
    }
    public BOCD(int index,int left,int right,int top,int bottom){
        this.index = index;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }
    public int getIndex(){
        return index;
    }
    public int getBottom() {
        return bottom;
    }
    public int getLeft() {
        return left;
    }
    public int getRight() {
        return right;
    }
    public int getTop() {
        return top;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public void setBottom(int bottom) {
        this.bottom = bottom;
    }
    public void setLeft(int left) {
        this.left = left;
    }
    public void setTop(int top) {
        this.top = top;
    }
    public void setRight(int right) {
        this.right = right;
    }
}
