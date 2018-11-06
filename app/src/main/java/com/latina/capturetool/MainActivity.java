package com.latina.capturetool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public final static int OVERAY_CODE = 3333;
    Intent alwaysServiceIntent;
    public static final String IMAGE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CaptureTool";

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alwaysServiceIntent = new Intent(this, AlwaysTopService.class);
        startOverlayWindowService(this);

        // 툴바 세팅
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // 상태바 색상 변경
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.addItemDecoration(new GalleryDecoration());
    }

    public void startOverlayWindowService(Context context) { // 앱 위에 표시 권한 신청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERAY_CODE);
        } else {
            Toast.makeText(this, "권한 확인 완료", Toast.LENGTH_SHORT).show();
            initWindowLayout();
        }
    }
    private void initWindowLayout() {
        startService(alwaysServiceIntent);
    } // 서비스 On
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERAY_CODE && resultCode == RESULT_OK) { // 앱 위에 표시 권한
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(MainActivity.this, "오버레이 권한 확인 완료", Toast.LENGTH_SHORT).show();
                    initWindowLayout();
                } else
                    Toast.makeText(MainActivity.this, "오버레이 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == 100 && resultCode != RESULT_OK) { // 쓰기 권한 확인
            Toast.makeText(this, "권한 없음", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void setImageList() { // CaptureTool 내에 있는 이미지 불러와서 리스트에 추가
        List<ImageVO> imgList = new ArrayList<>();
        adapter = new GalleryAdapter(this);
        File file = new File(IMAGE_DIR);
        File[] fileList = file.listFiles();
        if(fileList != null) {
            for (File img : fileList) {
                String path = img.getAbsoluteFile().getAbsolutePath();
                if (path.contains(".jpg") || path.contains(".png"))
                    imgList.add(new ImageVO(path, img.lastModified()));
            }
        }
        adapter.setImgList(imgList);
        recyclerView.setAdapter(adapter);
    }
    public void onClick(View v) {
        stopService(alwaysServiceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "설정");
        menu.add(0, 1, 0, "공유");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        setImageList();
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 0) { // 설정 클릭시

        }
        return super.onOptionsItemSelected(item);
    }
}
