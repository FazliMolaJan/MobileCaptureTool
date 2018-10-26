package com.latina.capturetool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenShot extends AppCompatActivity {
    private final int REQUEST_CODE = 200;
    private MediaProjection mediaProjection;
    private MediaProjectionManager projectionManager;
    private VirtualDisplay virtualDisplay;

    private int deviceWidth;
    private int deviceHeight;
    int screenDensity;

    int resultCode;
    Intent resultData;

    ImageReader imageReader;
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screenshot);
        DisplayMetrics disp = getResources().getDisplayMetrics();
        deviceWidth = disp.widthPixels;
        deviceHeight = disp.heightPixels;
        screenDensity = disp.densityDpi;

        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        startScreenCapture();
    }
    // Capture하기 전 객체 생성 확인 단계
    private void startScreenCapture() {
        if (mediaProjection != null) { // Display만 꺼진상태
            setUpVirtualDisplay();
        } else if (resultCode != 0 && resultData != null) { // Projection 객체 미생성
            setUpMediaProjection(); // Media Projection 생성
            setUpVirtualDisplay(); // 촬영 시작
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // 사용자가 권한을 허용해주었는지에 대한 처리
            if (resultCode != RESULT_OK) {
                // 사용자가 권한을 허용해주지 않았습니다.
                return;
            }
            this.resultCode = resultCode;
            resultData = data;
            finish();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setUpMediaProjection();
                    setUpVirtualDisplay();
                }
            }, 1500);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Media Projection 생성
    private void setUpMediaProjection() {
        mediaProjection = projectionManager.getMediaProjection(resultCode, resultData);
    }

    // Display 촬영 메소드
    private void setUpVirtualDisplay() {
        if(imageReader != null)
            imageReader.close();
        imageReader = ImageReader.newInstance(deviceWidth, deviceHeight, PixelFormat.RGBA_8888, 2); // 이미지 전송을 위한 리더
        virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture",
                deviceWidth, deviceHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null); // 이미지리더의 서피스에 Display를 저장한다.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() { // 이미지 촬영을 위한 핸들러
                stopScreenCapture();
                Image image  = null;
                Bitmap bitmap = null;

                image = imageReader.acquireNextImage();
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * deviceWidth;

                bitmap = Bitmap.createBitmap(deviceWidth+rowPadding/pixelStride, deviceHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                image.close();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                // 파일 저장
                FileOutputStream fos;
                try {
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CaptureTool");
                    if(!dir.exists())
                        dir.mkdir();
                    String current = format.format(new Date());
                    File file = new File(dir + "/screenshot-" + current +".jpg");
                    if(!file.exists())
                        file.createNewFile();
                    fos = new FileOutputStream(file);
                    fos.write(byteArray);
                    fos.close();
                    Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
                    intent.putExtra("image", file.getAbsolutePath());
                    startActivity(intent);
                } catch(Exception e )  { }
            }
        }, 500);
    }
    // Display 촬영 중지 및 MediaProjection 해제
    private void stopScreenCapture() {
        if (virtualDisplay == null)
            return;
        virtualDisplay.release();
        virtualDisplay = null;
        tearDownMediaProjection();
    }
    private void tearDownMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }
}
