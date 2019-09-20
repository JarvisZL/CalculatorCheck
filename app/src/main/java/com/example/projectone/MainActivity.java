package com.example.projectone;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    Button check,camera;
    ImageView image;
    File file;

    private static String filepath;
    private static final int TAKE_PHOTO = 200;
    private static final String TAG="JARVIS IN MAIN";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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


        check  = findViewById(R.id.button3);
        image = findViewById(R.id.image);
        camera = findViewById(R.id.button2);
        //Inner Storage in mobile
        filepath = Environment.getExternalStorageDirectory().getPath();
        filepath = filepath + "/Pictures/" +"temp.png";

        camera.setOnClickListener(new View.OnClickListener() {
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
                Uri imguri;
                //Android version
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    imguri = FileProvider.getUriForFile(MainActivity.this,"com.example.projectone.fileprovider",file);
                }
                else{
                    imguri = Uri.fromFile(file);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT,imguri);
                startActivityForResult(intent,TAKE_PHOTO);
            }
        });


        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(image.getDrawable() == null){
                    Log.i(TAG,"null");
                }
                else{
                    Log.i(TAG,"not null");
                    Intent intent = new Intent(MainActivity.this,PictureHandle.class);
                    intent.putExtra(PictureHandle.EXTRA_PHOTO,FileProvider.getUriForFile(MainActivity.this,"com.example.projectone.fileprovider",file));
                    startActivity(intent);
                }
            }
        });
    }

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
                        int degree = PictureRotate.getBitmapdgree(filepath);
                        Bitmap bmp = PictureRotate.rotateBitmap(bitmap,degree);
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

}
