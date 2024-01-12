package com.example.newgallery.activity;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.newgallery.MainActivity;
import com.example.newgallery.R;
import com.example.newgallery.databinding.ActivityEditBinding;
import com.example.newgallery.utils.Util;

import java.io.File;
import java.io.FileOutputStream;

public class EditActivity extends AppCompatActivity {
    ActivityEditBinding editBinding ;
    Bitmap bitmap;
    private String FilePath;
    private MediaScannerConnection msConn;

    void startAnim() {
        editBinding.rlLoading.setVisibility(View.VISIBLE);

    }

    void stopAnim() {
        editBinding.rlLoading.setVisibility(View.GONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editBinding = DataBindingUtil.setContentView(this ,R.layout.activity_edit);

//
        FilePath = getIntent().getStringExtra("filePath");
        bitmap = BitmapFactory.decodeFile(FilePath);

        RequestOptions options = new RequestOptions();
        if (FilePath.endsWith(".PNG") || FilePath.endsWith(".png")) {
            Glide.with(EditActivity.this)
                    .load(bitmap)
                    .apply(options.skipMemoryCache(true)
                            .priority(Priority.LOW)
                            .format(DecodeFormat.PREFER_ARGB_8888))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(editBinding.imgImages);
        } else {
            Glide.with(EditActivity.this)
                    .load(bitmap)
                    .apply(options.skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(editBinding.imgImages);
        }
        editBinding. imgCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCropListener();

            }
            private void onCropListener() {
                Intent intent = new Intent(EditActivity.this, CropActivity.class);
                intent.putExtra("filePath", FilePath);
                startActivity(intent);

            }
        });
        editBinding.imgRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRotateListener();

            }

            private void onRotateListener() {
//                fireAnalytics(EditActivity.class.getSimpleName(), "Image", "Rotate");
                Intent intent = new Intent(EditActivity.this, RotateActivity.class);
                intent.putExtra("filePath", FilePath);
                startActivity(intent);


            }
        });
        editBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        editBinding.imgDone .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LongOperation(bitmap).execute();

            }
        });
    }



    private void SaveImage(Bitmap finalBitmap) {

        File myDir = new File(Util.FOLDER_NAME);
        File mySubDir = new File(myDir.getAbsolutePath() + "/.temp");
        File file1 = new File(mySubDir, "image_temp.jpeg");
        if (!myDir.exists())
            Log.d("Data", "data: myDir " + myDir.mkdirs());

        if (file1.exists()) file1.delete();

        File file = new File(myDir, System.currentTimeMillis() + ".jpeg");

        if (file.exists()) file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            scanPhoto(file.toString());

            Intent intent = new Intent(EditActivity.this, MainActivity.class);
            startActivity(intent);
            finish();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);
                Log.i("msClient obj", "connection established");
            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();
                Log.i("msClient obj", "scan completed");
            }
        });
        this.msConn.connect();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
    private final class LongOperation extends AsyncTask<Void, Void, String> {

        Bitmap bitmap;

        public LongOperation(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

        @Override
        protected String doInBackground(Void... params) {
            SaveImage(bitmap);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            stopAnim();
        }
    }
}