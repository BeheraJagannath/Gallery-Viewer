package com.example.newgallery.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.newgallery.R;
import com.example.newgallery.databinding.ActivityCropBinding;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileOutputStream;

public class CropActivity extends AppCompatActivity {
    ActivityCropBinding cropBinding ;
    private MediaScannerConnection msConn;
    private String FilePath;


    void startAnim() {
        cropBinding.rlLoading.setVisibility(View.VISIBLE);

    }

    void stopAnim() {
        cropBinding.rlLoading.setVisibility(View.GONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cropBinding = DataBindingUtil .setContentView(this,R.layout.activity_crop);
        FilePath = getIntent().getStringExtra("filePath");

        Bitmap bitmap = BitmapFactory.decodeFile(new File(FilePath).getAbsolutePath());

        Glide.with(CropActivity.this)
                .asBitmap()
                .load(bitmap)
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(cropBinding.cropImageview);
        cropBinding.imgBack .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        cropBinding .imgNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropBinding.cropImageview.setCropEnabled(false);
            }
        });
        cropBinding.imgFree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropBinding.cropImageview.setCropEnabled(true);
                cropBinding.cropImageview.setCropMode(CropImageView.CropMode.FREE);

            }
        });
        cropBinding .circle .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropBinding.cropImageview.setCropEnabled(true);
                cropBinding.cropImageview.setCropMode(CropImageView.CropMode.CIRCLE);

            }
        });
        cropBinding .oneOne .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropBinding.cropImageview.setCropEnabled(true);
                cropBinding.cropImageview.setCustomRatio(1, 1);

            }
        });
        cropBinding.threeFour .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropBinding.cropImageview.setCropEnabled(true);
                cropBinding.cropImageview.setCropMode(CropImageView.CropMode.RATIO_3_4);

            }
        });
        cropBinding.FourThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropBinding.cropImageview.setCropEnabled(true);
                cropBinding.cropImageview.setCropMode(CropImageView.CropMode.RATIO_4_3);
            }
        });
        cropBinding.twoThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropBinding.cropImageview.setCropEnabled(true);
                cropBinding.cropImageview.setCustomRatio(2, 3);
            }
        });
        cropBinding.img916.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropBinding.cropImageview.setCropEnabled(true);
                cropBinding.cropImageview.setCropMode(CropImageView.CropMode.RATIO_9_16);

            }
        });
        cropBinding.img169 .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropBinding.cropImageview.setCropEnabled(true);
                cropBinding.cropImageview.setCropMode(CropImageView.CropMode.RATIO_16_9);

            }
        });
        cropBinding.imgDone .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    new LongOperation().execute();
            }
        });
    }
    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/" + getString(R.string.app_name));
        File mySubDir = new File(myDir.getAbsolutePath() + "/.temp");
        String path = "";
        Log.d("Data", "data: " + path);

        if (!myDir.exists()) {
            Log.d("Data", "data: myDir " + myDir.mkdirs());
        }

        if (!mySubDir.exists()) {
            Log.d("Data", "data: mySubDir " + mySubDir.mkdirs());
        }

        File file = new File(mySubDir, "image_temp.jpeg");
        path = file.getAbsolutePath();

        if (file.exists()) file.delete();

        try {
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
//            scanPhoto(file.toString());

            Intent intent = new Intent(CropActivity.this, EditActivity.class);
            intent.putExtra("filePath", path);
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


    private final class LongOperation extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

        @Override
        protected String doInBackground(Void... params) {
            SaveImage(cropBinding.cropImageview.getCroppedBitmap());
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            stopAnim();
        }
    }

}