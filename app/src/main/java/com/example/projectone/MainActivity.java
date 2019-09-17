package com.example.projectone;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.media.ExifInterface;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    Button button1,button2;
    ImageView image;
    File file;

    private static String filepath;
    private static final int TAKE_PHOTO1 = 100;
    private static final int TAKE_PHOTO2 = 200;
    private static final String TAG="MYJARVIS";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);

        button1 = findViewById(R.id.button1);
        image = findViewById(R.id.image);
        button2 = findViewById(R.id.button2);
        filepath = Environment.getExternalStorageDirectory().getPath();
        filepath = filepath + "/Pictures/" +"temp.png";

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
              //  intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(intent,TAKE_PHOTO1);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                file = new File(filepath);
                try {
                   if(file.exists()) {
                       file.delete();
                   }
                   file.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                Uri  imguri;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    imguri = FileProvider.getUriForFile(MainActivity.this,"com.example.projectone.fileprovider",file);
                }
                else{
                    imguri = Uri.fromFile(file);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imguri);
                startActivityForResult(intent,TAKE_PHOTO2);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode,resultCode,data);
            if(resultCode == RESULT_OK){
                if(requestCode == TAKE_PHOTO1){
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    image.setImageBitmap(bitmap);
                } else if(requestCode == TAKE_PHOTO2){
                    Log.i(TAG,"Onactivityresult");
                    FileInputStream fileInputStream = null;
                    try {
                        fileInputStream = new FileInputStream(filepath);
                        Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                        int degree = getBitmapdgree(filepath);
                        Bitmap bmp = rotateBitmap(bitmap,degree);
                        image.setImageBitmap(bmp);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degree) {
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

    private int getBitmapdgree(String filepath) {
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
