package com.walter.simpleimagecropper;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button buttonCamera, buttonGallery ;
    File file;
    Uri uri;
    Intent CamIntent, GalIntent, CropIntent ;
    public  static final int RequestPermissionCode  = 1 ;
    DisplayMetrics displayMetrics ;
    int width, height;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imgDisplay);
        buttonCamera = (Button)findViewById(R.id.buttonCamera);
        buttonGallery = (Button)findViewById(R.id.buttonGallery);

        EnableRuntimePermission();

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClickImageFromCamera() ;

            }
        });

        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GetImageFromGallery();

            }
        });
    }

    private void EnableRuntimePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA))
        {
            Toast.makeText(MainActivity.this,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();
        } else
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{ Manifest.permission.CAMERA}, RequestPermissionCode);
        }
    }

    private void GetImageFromGallery() {
        GalIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(Intent.createChooser(GalIntent, "Select Image From Gallery"), 2);
    }

    private void ClickImageFromCamera() {
        CamIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        file = new File(Environment.getExternalStorageDirectory(),"file" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        uri = Uri.fromFile(file);

        CamIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);

        CamIntent.putExtra("return-data", true);

        startActivityForResult(CamIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {

            ImageCropFunction();

        }
        else if (requestCode == 2) {

            if (data != null) {

                uri = data.getData();

                ImageCropFunction();

            }
        }
        else if (requestCode == 1) {

            if (data != null) {

                Bundle bundle = data.getExtras();

                Bitmap bitmap = bundle.getParcelable("data");

                imageView.setImageBitmap(bitmap);
                int kbs =byteSizeOf(bitmap)/1024;
                Log.d("BYTE_SIZE", kbs+"KB");
                upload(bitmap);


            }
        }

    }

    public static int byteSizeOf(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }
    private void ImageCropFunction() {
        // Image Crop Code
        try {
            CropIntent = new Intent("com.android.camera.action.CROP");

            CropIntent.setDataAndType(uri, "image/*");

            CropIntent.putExtra("crop", "true");
            CropIntent.putExtra("outputX", 300);
            CropIntent.putExtra("outputY", 180);
            CropIntent.putExtra("aspectX", 5);
            CropIntent.putExtra("aspectY", 3);
            CropIntent.putExtra("scaleUpIfNeeded", true);
            CropIntent.putExtra("return-data", true);

            startActivityForResult(CropIntent, 1);

        } catch (ActivityNotFoundException e) {
           e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();

                }
                break;

    }
  }


    public void upload(Bitmap bitmap){
        AsyncHttpClient client =new AsyncHttpClient();
        RequestParams params=new RequestParams();
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/to";
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, "file.png");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            params.put("fileToUpload",file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e)
        {
           e.printStackTrace();
        }

        client.post("http://192.168.0.32/uploads/upload.php", params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("RESPONSE", "Failed To Load "+statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d("RESPONSE", responseString);
            }
        });

    }
}
