package com.latina.capturetool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public final static int OVERAY_CODE = 3333;
    Intent alwaysServiceIntent;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alwaysServiceIntent = new Intent(this, AlwaysTopService.class);
        startOverlayWindowService(this);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
        button = findViewById(R.id.screenshot);
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
        } else if(requestCode == 100 && resultCode != RESULT_OK) // 쓰기 권한 확인
            finish();
    }
    public void onClick(View v) {
        stopService(alwaysServiceIntent);
    }
}
