package com.example.projectone;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    Button check,camera,choose, rotation;
    ImageView image;
    File file;

    private static String filepath;
    private static final int TAKE_PHOTO = 200;
    private static final int CHOOSE_PHOTO = 100;
    private static final String TAG="HwmkCheck IN MAIN";

    static {
        if(!OpenCVLoader.initDebug()){
            Log.i(TAG,"Opencv faied");
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)//android4.3
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        // Dynamic Permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);

        choose  = findViewById(R.id.choose);
        check  = findViewById(R.id.button3);
        image = findViewById(R.id.image);
        camera = findViewById(R.id.button2);
        //相关图片存储的位置
        filepath = Environment.getExternalStorageDirectory().getPath();
        filepath = filepath + "/HmwkCheck/" +"temp.png";

        //拍照按钮
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                //将拍摄的图片存入filepath中
                file = new File(filepath);
                try {
                   if(file.exists()) {
                       file.delete();
                   }
                   file.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }

                //将图片转化为imguri
                Uri imguri;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){//android 7
                    imguri = FileProvider.getUriForFile(MainActivity.this,"com.example.projectone.fileprovider",file);
                }
                else{
                    imguri = Uri.fromFile(file);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imguri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });

        //从相册中选择图片
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,CHOOSE_PHOTO);
            }
        });

        //开始执行识别
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(image.getDrawable() == null){
                    Log.i(TAG,"null");
                }
                else{
                    Log.i(TAG,"not null");
                    Intent intent = new Intent(MainActivity.this,PictureShow.class);
                    //from file to Uri
                    //  intent.putExtra(PictureShow.EXTRA_PHOTO,FileProvider.getUriForFile(MainActivity.this,"com.example.projectone.fileprovider",file));
                    //from imageview to Uri
                    Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                    Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,null,null));
                    intent.putExtra(PictureShow.EXTRA_PHOTO,uri);
                    startActivity(intent);
                }
            }
        });

        //旋转目标图片
        rotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(image.getDrawable() == null){
                    Log.i(TAG,"null");
                }
                else{
                    Log.i(TAG,"not null");
                    Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                    bitmap = PictureHandle.rotateBitmap(bitmap,90);
                    image.setImageBitmap(bitmap);
                }
            }
        });
    }


    //camera和choose按钮返回后相关处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode,resultCode,data);
            if(resultCode == RESULT_OK){
               if(requestCode == TAKE_PHOTO){
                    Log.i(TAG,"Onactivityresult");
                    FileInputStream fileInputStream = null;
                    try {
                        fileInputStream = new FileInputStream(filepath);
                        Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                        int degree = PictureHandle.getBitmapdgree(filepath);
                        bitmap = PictureHandle.rotateBitmap(bitmap,degree);
                        image.setImageBitmap(bitmap);
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
               else if(requestCode == CHOOSE_PHOTO){
                   Bitmap bitmap = null;
                   try {
                       Uri uri = data.getData();
                      try {
                         bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                      }catch (FileNotFoundException e) {
                          e.printStackTrace();
                      }
                      image.setImageBitmap(bitmap);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }
            }

    }

}
