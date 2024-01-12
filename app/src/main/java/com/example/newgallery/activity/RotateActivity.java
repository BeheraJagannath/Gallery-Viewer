package com.example.newgallery.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.newgallery.R;
import com.example.newgallery.databinding.ActivityRotateBinding;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.FileOutputStream;

public class RotateActivity extends AppCompatActivity {
    ActivityRotateBinding rotateBinding ;
    private String FilePath;
    void startAnim() {
        rotateBinding.rlLoading.setVisibility(View.VISIBLE);

    }

    void stopAnim() {
        rotateBinding.rlLoading.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rotateBinding = DataBindingUtil .setContentView(this ,R.layout.activity_rotate) ;

        FilePath = getIntent().getStringExtra("filePath");

        Bitmap bitmap = BitmapFactory.decodeFile(new File(FilePath).getAbsolutePath());

        Glide.with(RotateActivity.this)
                .asBitmap()
                .load(bitmap)
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(rotateBinding.imgImages);
        rotateBinding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onBackPressed();
            }
        });
        rotateBinding. rotateNone.setOnClickListener(v->{
            rotateBinding.imgImages.setCropEnabled(false);

        });
        rotateBinding.LeftRotate .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateBinding.imgImages.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);

            }
        });
        rotateBinding.RightRotate .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotateBinding.imgImages.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);

            }
        });
        rotateBinding.VerticalRotate .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bInput = rotateBinding.imgImages.getCroppedBitmap();
                Matrix matrix = new Matrix();
                matrix.preScale(1.0f, -1.0f);
                Bitmap bOutput = Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);

                Glide.with(RotateActivity.this)
                        .asBitmap()
                        .load(bOutput)
                        .thumbnail(0.5f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(rotateBinding.imgImages);

            }
        });
        rotateBinding.HorizontalRotate.setOnClickListener(v->{
            Bitmap bInput1 = rotateBinding.imgImages.getCroppedBitmap();
            Matrix matrix1 = new Matrix();
            matrix1.preScale(-1.0f, 1.0f);
            Bitmap bOutput1 = Bitmap.createBitmap(bInput1, 0, 0, bInput1.getWidth(), bInput1.getHeight(), matrix1, true);

            Glide.with(RotateActivity.this)
                    .asBitmap()
                    .load(bOutput1)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(rotateBinding.imgImages);

        });
        rotateBinding.imgDone.setOnClickListener(new View.OnClickListener() {
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

            Intent intent = new Intent(RotateActivity.this, EditActivity.class);
            intent.putExtra("filePath", path);
            startActivity(intent);
            finish();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private final class LongOperation extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

        @Override
        protected String doInBackground(Void... params) {
            SaveImage(rotateBinding.imgImages.getCroppedBitmap());
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            stopAnim();
        }
    }
}