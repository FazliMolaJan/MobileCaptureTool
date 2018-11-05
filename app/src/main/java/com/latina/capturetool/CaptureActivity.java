package com.latina.capturetool;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//이미지 수신 액티비티
public class CaptureActivity extends AppCompatActivity {
    private CanvasView canvas;
    FloatingActionButton btn_delete, btn_edit, btn_crop, btn_check;
    Button btn_red, btn_blue, btn_black;
    SeekBar strokeWidth;
    Button btn_undo, btn_redo, btn_clear, btn_back;
    RelativeLayout drawingContainer;
    LinearLayout container;

    String filePath; // 이미지 파일 경로
    private String cachePath;

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
        if(filePath == null) { // 잘못 실행 될 경우 이동
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        File file = new File(filePath);
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        // Canvas View 세팅
        canvas = findViewById(R.id.canvas);
        canvas.drawBitmap(myBitmap);
        canvas.setLineCap(Paint.Cap.ROUND);
        canvas.setPaintStrokeWidth(10F);
        canvas.setDrawable(false);
    }

    // 일반 모드 리스너
    public void onClick(View v) {
        if (v == btn_delete) { // 삭제 버튼
            delete();
        } else if (v == btn_edit) { // 그리기 모드
            canvas.setDrawable(true);
            container.setVisibility(View.GONE);
            drawingContainer.setVisibility(View.VISIBLE);
        } else if (v == btn_crop) { // 사이즈 편집 버튼
            Bitmap bitmap = canvas.getBitmap(); // Bitmap to Uri
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            cachePath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
            CropImage.activity(Uri.parse(cachePath)).start(this); // CropImage Start
        } else if (v == btn_check) { // 확인 후 저장 버튼
            save();
        }
    }

    // 편집 모드 수정 리스너
    public void onDrawClick(View v) {
        if (v == btn_back) { // 뒤로 가기 버튼
            canvas.setDrawable(false);
            container.setVisibility(View.VISIBLE);
            drawingContainer.setVisibility(View.GONE);
        } else if (v == btn_red) { // 빨강 펜
            clearColorButton();
            btn_red.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_red_select));
            canvas.setPaintStrokeColor(Color.RED);
        } else if (v == btn_blue) { // 파랑 펜
            clearColorButton();
            btn_blue.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_blue_select));
            canvas.setPaintStrokeColor(Color.BLUE);
        } else if (v == btn_black) { // 검정 펜
            clearColorButton();
            btn_black.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_black_select));
            canvas.setPaintStrokeColor(Color.BLACK);
        } else if (v == btn_undo) { // Undo 버튼
            canvas.undo();
            if(!canvas.undo())
                canvas.clearChanged();
            else
                canvas.redo();
        } else if (v == btn_redo) { // Redo 버튼
            canvas.redo();
        } else if (v == btn_clear) { // 그렸던 작업 초기화
            while (canvas.undo()) ;
            canvas.clearChanged();
        }
    }

    private void save() { // 이미지 저장(변경이 되었을 경우 동작)
        if (canvas.isChanged()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            else {
                FileOutputStream fos;
                try {
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CaptureTool");
                    if (!dir.exists())
                        dir.mkdir();
                    File file = new File(filePath);
                    if (!file.exists())
                        file.createNewFile();
                    fos = new FileOutputStream(file);
                    Bitmap bitmap = canvas.getBitmap();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                    Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show();
                } catch (IOException e) { e.printStackTrace(); }
                finish();
            }
        } else
            finish();
    }
    private void delete() { // 이미지 삭제
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("삭제");
        builder.setMessage("삭제 하시겠습니까?");
        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(filePath);
                if (file.exists())
                    file.delete();
                finish();
            }
        });
        builder.setNegativeButton("취소", null);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) { // Crop 결과 들어오면 실행
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                try {
                    deleteCacheFile();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri()); // Uri -> Bitmap
                    int canvasWidth = (bitmap.getWidth() > canvas.getWidth()) ? canvas.getWidth() : bitmap.getWidth();
                    int canvasHeight = (bitmap.getHeight() > canvas.getHeight()) ? canvas.getHeight() : bitmap.getHeight();
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(canvasWidth, canvasHeight);
                    params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    canvas.setLayoutParams(params);
                    Toast.makeText(this, "Width : " + canvas.getWidth() + " Height : " + canvas.getHeight(), Toast.LENGTH_SHORT).show();
                    canvas.drawBitmap(bitmap);
                } catch(IOException e) {e.printStackTrace(); }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.getError().printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                save();
            else
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        if (item.getItemId() == android.R.id.home) { // 뒤로 가기
            saveCheck();
        }
        return super.onOptionsItemSelected(item);
    }
    private void saveCheck() { // 뒤로 가기 클릭 시 저장 할지 여부 확인 후 종료
        if (canvas.isChanged()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_menu_save);
            builder.setTitle("저장");
            builder.setMessage("변경 사항이 있습니다. 저장 하시겠습니까?");
            builder.setCancelable(false);
            builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    save();
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.create().show();
        } else
            finish();
    }
    // 선 굵기 이벤트 리스너
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            canvas.setPaintStrokeWidth((float) progress * 2);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void deleteCacheFile() { // Temp 이미지 삭제
        File file = new File(UriManager.getPath(this, Uri.parse(cachePath)));
        if (file.exists()) {
            file.delete();
            Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show();
        }
    }
}
