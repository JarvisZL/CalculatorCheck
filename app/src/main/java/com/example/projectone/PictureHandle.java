package com.example.projectone;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class PictureHandle extends AppCompatActivity {

    private static final String TAG="JARVIS IN PIC";
    public static final String EXTRA_PHOTO = "exphoto";
    private static String filepath;
    ImageView imageView;
    TextView textView;
    Bitmap bitmap;
    Uri imguri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_handle);
        imageView = findViewById(R.id.photo);
        textView = findViewById(R.id.result);
        filepath = Environment.getExternalStorageDirectory().getPath();
        filepath = filepath + "/Pictures/" +"temp.png";

        imguri = (Uri) getIntent().getExtras().get(EXTRA_PHOTO);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imguri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int degree = PictureRotate.getBitmapdgree(filepath);
        bitmap = PictureRotate.rotateBitmap(bitmap,degree);
        imageView.setImageBitmap(bitmap);
        textView.setText(R.string.Testing);
    }
}
