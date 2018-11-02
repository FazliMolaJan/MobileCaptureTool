package com.latina.capturetool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.android.graphics.CanvasView;
import java.io.File;

//이미지 수신 액티비티
public class CaptureActivity extends AppCompatActivity {

    private CanvasView canvas;
    FloatingActionButton btn_delete, btn_edit, btn_crop, btn_check;
    Button btn_red, btn_blue, btn_black;
    SeekBar strokeWidth;
    Button btn_undo, btn_redo, btn_clear, btn_back;
    RelativeLayout drawingContainer;
    LinearLayout container;

    String filePath;
    boolean isChanged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        // 툴바 세팅
        Toolbar toolbar = findViewById(R.id.capture_toolbar);
        setSupportActionBar(toolbar);

        // 상태바 색상 변경
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);

        btn_delete = findViewById(R.id.delete);
        btn_edit = findViewById(R.id.edit);
        btn_crop = findViewById(R.id.crop);
        btn_check = findViewById(R.id.check);
        container = findViewById(R.id.container);
        drawingContainer = findViewById(R.id.drawContainer);
        btn_red = findViewById(R.id.pen_red);
        btn_blue = findViewById(R.id.pen_blue);
        btn_black = findViewById(R.id.pen_black);
        strokeWidth = findViewById(R.id.widthSeekbar);
        btn_undo = findViewById(R.id.undoButton);
        btn_redo = findViewById(R.id.redoButton);
        btn_clear = findViewById(R.id.clearButton);
        btn_back = findViewById(R.id.backPress);
        strokeWidth.setOnSeekBarChangeListener(seekBarChangeListener);

        // 스크린샷된 이미지 경로로부터 가져오기
        filePath = getIntent().getStringExtra("image");
        File file = new File(filePath);
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        // Canvas View 세팅
        canvas = findViewById(R.id.canvas);
        canvas.drawBitmap(myBitmap);
        canvas.setLineCap(Paint.Cap.ROUND);
        canvas.setPaintStrokeWidth(5F);
    }
    public void onClick(View v) {
        if(v == btn_delete) {
            // Dialog로 삭제 확인 필요
            File file = new File(filePath);
            if(file.exists())
                file.delete();
        } else if(v == btn_edit) {
            container.setVisibility(View.GONE);
            drawingContainer.setVisibility(View.VISIBLE);
        } else if(v == btn_crop) {

        } else if(v == btn_check) {
            save();
        }
    }
    private void save() {
        if(isChanged) {

        }
    }

    // 편집 모드 수정 리스너
    public void onDrawClick(View v) {
        if(v == btn_back) {
            container.setVisibility(View.VISIBLE);
            drawingContainer.setVisibility(View.GONE);
        } else if(v == btn_red) {
            clearColorButton();
            btn_red.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_red_select));
            canvas.setPaintStrokeColor(Color.RED);
        } else if(v == btn_blue) {
            clearColorButton();
            btn_blue.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_blue_select));
            canvas.setPaintStrokeColor(Color.BLUE);
        } else if(v == btn_black) {
            clearColorButton();
            btn_black.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_black_select));
            canvas.setPaintStrokeColor(Color.BLACK);
        } else if(v == btn_undo) {
            canvas.undo();
        } else if(v == btn_redo) {
            canvas.redo();
        } else if(v == btn_clear) {
            while(canvas.undo());
        }
    }
    // 색상 버튼 이미지 초기화
    private void clearColorButton() {
        btn_red.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_red));
        btn_blue.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_blue));
        btn_black.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_black));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_capture, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) { // 뒤로 가기
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    // 선 굵기 이벤트 리스너
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            canvas.setPaintStrokeWidth((float)progress);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
