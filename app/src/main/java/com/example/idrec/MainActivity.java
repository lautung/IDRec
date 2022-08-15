package com.example.idrec;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private TessBaseAPI baseApi;
    private String language = "cn";

    private TextView tesstext;
    private ImageView idCard;
    private ProgressDialog progressDialog;
    private Bitmap ResultImage;
    private Bitmap fullImage;


    private void showProgress() {
        if (null == progressDialog) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("请稍候...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void dismissProgress() {
        if (null != progressDialog) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        idCard = findViewById(R.id.idcard);
        tesstext = findViewById(R.id.tesstext);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {//未获取权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //当用户第一次申请拒绝时，再次申请该权限调用
                Toast.makeText(this, "读写sdcard的权限", Toast.LENGTH_SHORT).show();
            }
            //申请权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x01);
        } else {//已经获得了权限
            Toast.makeText(this, "已经获取读写sdcard的权限", Toast.LENGTH_SHORT).show();
            initTess();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @SuppressLint("StaticFieldLeak")
    private void initTess() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                showProgress();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    dismissProgress();
                } else {
                    Toast.makeText(MainActivity.this, "load trainedData failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                baseApi = new TessBaseAPI();
                try {
                    InputStream is = null;
                    is = getAssets().open(language + ".traineddata");
                    File file = new File("/sdcard/tess/tessdata/" + language + ".traineddata");
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[2048];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                    is.close();
                    return baseApi.init("/sdcard/tess", language);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
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

    public void searchId(View view) {
        tesstext.setText(null);
        ResultImage = null;
        Bitmap bitmapResult = ImageProcess.getIdNumber(fullImage, Bitmap.Config.ARGB_8888);
        fullImage.recycle();
        ResultImage = bitmapResult;
        //tesseract-ocr
        idCard.setImageBitmap(bitmapResult);
    }

    public void recognition(View view) {
        // 识别Bitmap中的图片
        baseApi.setImage(ResultImage);
        tesstext.setText(baseApi.getUTF8Text());
        baseApi.clear();
    }

    private void getResult(Uri uri) {
        String imagePath = null;
        if (null != uri) {
            //在我们的魅族测试手机上发现有一个相册管家 从这里选取图片会得到类似
            //file:///storage/emulated/0/tencent/MicroMsg/WeiXin/mmexport1474966179606.jpg的uri
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
            tesstext.setText(null);
            idCard.setImageBitmap(fullImage);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && null != data) {
            getResult(data.getData());
        }
    }

    public static Bitmap toBitmap(String pathName) {
        if (TextUtils.isEmpty(pathName)) {
            return null;
        }
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, o);
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp <= 640 && height_tmp <= 480) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;
        opts.outHeight = height_tmp;
        opts.outWidth = width_tmp;
        return BitmapFactory.decodeFile(pathName, opts);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgress();
        baseApi.end();
    }


    public void start(View view) {
        Intent intent =  new Intent(MainActivity.this,OpenCVActivity.class);
        startActivity(intent);
    }
}
