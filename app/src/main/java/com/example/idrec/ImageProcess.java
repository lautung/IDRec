package com.example.idrec;

import android.graphics.Bitmap;

/**
 *
 * @author xiang
 * @date 2017/7/23
 */

public class ImageProcess {

    static {
        System.loadLibrary("native-lib");
    }

    public static native Bitmap getIdNumber(Bitmap src, Bitmap.Config config);

    public static native Bitmap getCartonizedImage(Bitmap src, Bitmap.Config config);
}
