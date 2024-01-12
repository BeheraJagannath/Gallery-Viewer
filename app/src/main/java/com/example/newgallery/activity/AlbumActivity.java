package com.example.newgallery.activity;

import static android.content.ContentValues.TAG;
import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.ORIENTATION;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.example.newgallery.Adapters.AlbumDetailsAdapter;
import com.example.newgallery.Adapters.AlbumchildAdapter;

import com.example.newgallery.R;
import com.example.newgallery.databinding.ActivityAlbumBinding;
import com.example.newgallery.modelclass.Directory;
import com.example.newgallery.modelclass.ImageFiles;
import com.example.newgallery.utils.Util;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class AlbumActivity extends AppCompatActivity {
    ActivityAlbumBinding albumBinding;
    AlbumDetailsAdapter albumDetailsAdapter ;
    private List<ImageFiles> imageFiles = new ArrayList<>();
    AlbumchildAdapter albumChildAdapter ;
    private String pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        albumBinding = DataBindingUtil.setContentView(this, R.layout.activity_album);

        albumBinding.setDirName(getIntent().getStringExtra("FileName"));
        pos = getIntent().getStringExtra("FileName") ;
        albumBinding.tvAlbumName.setText(pos);


        albumBinding.imgBack.setOnClickListener(v -> onBackPressed());


    }
    @Override
    protected void onResume() {
        super.onResume();
        boolean isGranted = EasyPermissions.hasPermissions(AlbumActivity.this, "android.permission.READ_EXTERNAL_STORAGE");
        if (isGranted)
            new LoadImages(AlbumActivity.this).execute();
    }

    void startAnim() {
        albumBinding.rlLoading.setVisibility(View.VISIBLE);
    }

    void stopAnim() {
        albumBinding.rlLoading.setVisibility(View.GONE);
    }


    class LoadImages extends AsyncTask<Void, Void, List<Directory<ImageFiles>>> {

        @SuppressLint("StaticFieldLeak")
        FragmentActivity fragmentActivity;
        List<ImageFiles> list1 = new ArrayList<>();

        public LoadImages(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

        @Override
        protected List<Directory<ImageFiles>> doInBackground(Void... voids) {

            String[] FILE_PROJECTION = {
                    //Base File
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.TITLE,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    DATE_ADDED,
                    MediaStore.Images.Media.ORIENTATION
            };

            String selection = MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=?";

            String[] selectionArgs;
            selectionArgs = new String[]{"image/jpeg", "image/png", "image/jpg", "image/gif"};

            Cursor data = fragmentActivity.getContentResolver().query(MediaStore.Files.getContentUri("external"),
                    FILE_PROJECTION,
                    selection,
                    selectionArgs,
                    DATE_ADDED + " DESC");

            List<Directory<ImageFiles>> directories = new ArrayList<>();
            List<Directory> directories1 = new ArrayList<>();

            if (data.getPosition() != -1) {
                data.moveToPosition(-1);
            }

            while (data.moveToNext()) {
                //Create a File instance
                ImageFiles img = new ImageFiles();

                img.setId(data.getLong(data.getColumnIndexOrThrow(_ID)));
                img.setName(data.getString(data.getColumnIndexOrThrow(TITLE)));
                img.setPath(data.getString(data.getColumnIndexOrThrow(DATA)));
                img.setSize(data.getLong(data.getColumnIndexOrThrow(SIZE)));
                img.setBucketId(data.getString(data.getColumnIndexOrThrow(BUCKET_ID)));
                img.setBucketName(data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME)));
                img.setDate(Util.convertTimeDateModified(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED))));
                img.setOrientation(data.getInt(data.getColumnIndexOrThrow(ORIENTATION)));

                //Create a Directory
                Directory<ImageFiles> directory = new Directory<>();
                directory.setId(img.getBucketId());
                directory.setName(img.getBucketName());
                directory.setPath(Util.extractPathWithoutSeparator(img.getPath()));

                if (!directories1.contains(directory)) {
                    directory.addFile(img);
                    directories.add(directory);
                    directories1.add(directory);
                } else {
                    directories.get(directories.indexOf(directory)).addFile(img);
                }
                imageFiles.add(img);
            }

            return directories;
        }

        @Override
        protected void onPostExecute(List<Directory<ImageFiles>> directories) {
            super.onPostExecute(directories);
            Log.d(TAG, "onPostExecute: " + "done");
            imageFiles.clear();
            for (int i = 0; i < directories.size(); i++) {
                if (albumBinding.getDirName().equals(directories.get(i).getName())) {
                    imageFiles = directories.get(i).getFiles();
                    int finalI = i;
                    fragmentActivity.runOnUiThread(() -> {
                        Log.e("LLL_Date: ", albumBinding.getDirName() + "  Directory: " + directories.get(finalI).getName() + " Size: " + directories.get(finalI).getFiles().size());
                        if (!Util.isList) {
                            albumBinding.rvAlbumImage.setLayoutManager(new GridLayoutManager(AlbumActivity.this, Util.COLUMN_TYPE));
                            albumChildAdapter = new AlbumchildAdapter(imageFiles, AlbumActivity.this, finalI);
                            albumBinding.rvAlbumImage.setAdapter(albumChildAdapter);
                        } else {
                            albumBinding.rvAlbumImage.setLayoutManager(new LinearLayoutManager(AlbumActivity.this, RecyclerView.VERTICAL, false));
                            albumDetailsAdapter = new AlbumDetailsAdapter(imageFiles, AlbumActivity.this, finalI);
                            albumBinding.rvAlbumImage.setAdapter(albumDetailsAdapter);
                        }
                    });
                }
            }

            stopAnim();

        }

    }
}