package com.latina.capturetool;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by Latina on 2018-10-07.
 */

public class Screenshot {
    public static Bitmap takeScreenshot(View v) {
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return b;
    }
    public static Bitmap takeScreenshotOfRootView(View v) {
        return takeScreenshot(v.getRootView());
    }
}
