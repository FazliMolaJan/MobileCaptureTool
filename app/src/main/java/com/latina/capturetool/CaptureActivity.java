package com.latina.capturetool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.android.graphics.CanvasView;

import java.io.File;

//이미지 수신 액티비티
public class CaptureActivity extends AppCompatActivity {
    ImageView imageView;
    private CanvasView canvas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        String filePath = getIntent().getStringExtra("image");
        //String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/image2.jpg";
        File file = new File(filePath);
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        canvas = findViewById(R.id.canvas);
        canvas.drawBitmap(myBitmap);
        canvas.setLineCap(Paint.Cap.ROUND);
        canvas.setPaintStrokeWidth(5F);
    }
    public void onClick(View v) {
        while(true) {
            if(!canvas.undo())
                break;
        }
    }
}
