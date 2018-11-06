package com.latina.capturetool;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.internal.NavigationMenu;

import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

import io.github.yavski.fabspeeddial.FabSpeedDial;

//Always On Top View 떠있는 서비스
public class AlwaysTopService extends Service {
    private View mView; // AOT View
    private WindowManager mManager; // 서비스 매니저
    private WindowManager.LayoutParams mParams;

    private float mTouchX, mTouchY;
    private int mViewX, mViewY;

    private boolean isMove = false;
    FabSpeedDial fabSpeedDial;
    FloatingActionButton fab;

    public static final String SERVICE_RECEIVER = "com.latina.capturetool.receiver";

    BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SERVICE_RECEIVER))
                if (intent.getBooleanExtra("hide", false))
                    fab.hide();
                else
                    fab.show();
        }
    };

    private OnTouchListener mViewTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: // 마우스 누른경우 현재 위치 지정
                    isMove = false;
                    mTouchX = event.getRawX();
                    mTouchY = event.getRawY();
                    mViewX = mParams.x;
                    mViewY = mParams.y;
                    fab.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255,92,209,229)));
                    break;
                case MotionEvent.ACTION_UP:
                    if(!fabSpeedDial.isMenuOpen())
                        fab.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255,178,235,244)));
                    if (!isMove) { // 실제 클릭했을때 이 부분 구현
                        if(fabSpeedDial.isMenuOpen()) {
                            fabSpeedDial.closeMenu();
                            fab.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, 178, 235, 244)));
                        }
                        else {
                            fabSpeedDial.animate();
                            fabSpeedDial.openMenu();
                            fab.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255,92,209,229)));
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    isMove = true;
                    int x = (int) (event.getRawX() - mTouchX);
                    int y = (int) (event.getRawY() - mTouchY);
                    final int num = 5; // 이동한 거리가 매우 짧은 경우 클릭으로 취급
                    if ((x > -num && x < num) && (y > -num && y < num)) {
                        isMove = false;
                        break;
                    }
                    mParams.x = mViewX + x;
                    mParams.y = mViewY + y;
                    mManager.updateViewLayout(mView, mParams);
                    break;
            }
            return true;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme); // FAB 가 ATV에서 구동하기 위해 설정
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mView = mInflater.inflate(R.layout.always_top, null);
        fabSpeedDial = mView.findViewById(R.id.fab_main_dial);
        fab = mView.findViewById(R.id.fab);
        fab.setSize(1);
        fab.setOnTouchListener(mViewTouchListener);

        registerReceiver(serviceReceiver, new IntentFilter(SERVICE_RECEIVER));

        fabSpeedDial.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.menu_capture : // 촬영 버튼
                        startActivity(new Intent(getApplicationContext(), ScreenShot.class));
                        break;
                    case R.id.menu_capture_edit :
                        Intent intent = new Intent(getApplicationContext(), ScreenShot.class);
                        intent.putExtra("mode", true);
                        startActivity(intent);
                        break;
                }
                return true;
            }
            @Override
            public void onMenuClosed() {
            }
        });
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // PHONE 타입으로하면 에러남
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        mManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mManager.addView(mView, mParams);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(serviceReceiver);
        if (mView != null) {
            mManager.removeView(mView);
            mView = null;
        }
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
