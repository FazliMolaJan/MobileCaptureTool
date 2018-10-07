package com.latina.capturetool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public final static int REQUEST_CODE = 3333;
    Intent alwaysServiceIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alwaysServiceIntent = new Intent(this, AlwaysTopService.class);

        startOverlayWindowService(this);
    }

    public void startOverlayWindowService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            Toast.makeText(this, "권한 확인 완료", Toast.LENGTH_SHORT).show();
            initWindowLayout();
        }
    }
    private void initWindowLayout() {
        startService(alwaysServiceIntent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(MainActivity.this, "오버레이 권한 확인 완료", Toast.LENGTH_SHORT).show();
                    initWindowLayout();
                } else {
                    Toast.makeText(MainActivity.this, "오버레이 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    public void onClick(View v) {
        stopService(alwaysServiceIntent);
    }
}
