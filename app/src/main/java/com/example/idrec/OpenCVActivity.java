package com.example.idrec;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class OpenCVActivity extends AppCompatActivity {
    private static final String TAG = "luge";
    private Bitmap fullImage;
    private ImageView image;
    private Bitmap ResultImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv);
        image = findViewById(R.id.image);
    }

    public void search(View view) {
        Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        intent.setType("image/*");
        //使用选取器并自定义标题
        startActivityForResult(Intent.createChooser(intent, "选择待识别图片"), 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && null != data) {
            getResult(data.getData());
        }
    }

    private void getResult(Uri uri) {
        String imagePath = null;
        if (null != uri) {
            if ("file".equals(uri.getScheme())) {
                Log.i(TAG, "path uri 获得图片");
                imagePath = uri.getPath();
            } else if ("content".equals(uri.getScheme())) {
                Log.i(TAG, "content uri 获得图片");
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(uri, filePathColumns, null, null, null);
                if (null != c) {
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(filePathColumns[0]);
                        imagePath = c.getString(columnIndex);
                    }
                    c.close();
                }
            }
        }
        if (!TextUtils.isEmpty(imagePath)) {
            if (fullImage != null) {
                fullImage.recycle();
            }
            fullImage = toBitmap(imagePath);
            image.setImageBitmap(fullImage);
        }
    }

    public static Bitmap toBitmap(String pathName) {
        if (TextUtils.isEmpty(pathName)) {
            return null;
        }
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, o);
        int widthTmp = o.outWidth, heightTmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (widthTmp <= 640 && heightTmp <= 480) {
                break;
            }
            widthTmp /= 2;
            heightTmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;
        opts.outHeight = heightTmp;
        opts.outWidth = widthTmp;
        return BitmapFactory.decodeFile(pathName, opts);
    }

    /**
     * @param view
     */
    public void cartoonizingimages(View view) {
        Bitmap cartonizedImage = ImageProcess.getCartonizedImage(fullImage, Bitmap.Config.ARGB_8888);
        fullImage.recycle();
        image.setImageBitmap(cartonizedImage);
    }
}
