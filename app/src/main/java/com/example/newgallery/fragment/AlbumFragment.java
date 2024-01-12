package com.example.newgallery.fragment;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.ORIENTATION;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.newgallery.Adapters.AlbumAdapter;
import com.example.newgallery.Adapters.AlbumDetailsAdapter;
import com.example.newgallery.Adapters.AlbumlistAdapter;
import com.example.newgallery.R;
import com.example.newgallery.databinding.AlbumListBinding;
import com.example.newgallery.databinding.FragmentAlbumBinding;
import com.example.newgallery.modelclass.Directory;
import com.example.newgallery.modelclass.ImageFiles;
import com.example.newgallery.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class AlbumFragment extends Fragment {
    FragmentAlbumBinding albumBinding ;
    AlbumAdapter albumAdapter ;
    private static final String TAG = AlbumFragment.class.getSimpleName();
    private final List<ImageFiles> imageFiles = new ArrayList<>();
    public List<Directory<ImageFiles>> dateFiles = new ArrayList<>();
    private MyReceiver myReceiver;
    AlbumlistAdapter albumlistAdapter ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        albumBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_album, container, false);

        albumAdapter = new AlbumAdapter(dateFiles, getActivity());
        albumlistAdapter = new AlbumlistAdapter(dateFiles, getActivity());

        return albumBinding.getRoot();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(myReceiver,
                new IntentFilter("TAG_REFRESH"));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SORTING_TYPE.equals("Name")) {
            new LoadImagesName(getActivity()).execute();
        } else if (Util.SORTING_TYPE.equals("No of Photos")) {
            new LoadImagesNo(getActivity()).execute();
        } else {
            boolean isGranted = EasyPermissions.hasPermissions(getContext(), "android.permission.READ_EXTERNAL_STORAGE");
            if (isGranted)
                new LoadImages(getActivity()).execute();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(myReceiver);
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

                if (!img.getBucketName().startsWith(".")) {
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
            }

            return directories;
        }

        @Override
        protected void onPostExecute(List<Directory<ImageFiles>> directories) {
            super.onPostExecute(directories);
            Log.d(TAG, "onPostExecute: " + "done");

            if (Util.SORTING_TYPE2.equals("Descending")) {
                Collections.reverse(directories);
            }

            fragmentActivity.runOnUiThread(() -> {
                if (!Util.isList) {
                    albumBinding.albumRecycler.setLayoutManager(new GridLayoutManager(getContext(), Util.COLUMN_TYPE));
                    albumBinding.albumRecycler.setLayoutAnimation(null);

                    albumBinding.albumRecycler.setAdapter(albumAdapter);
                    Collections.reverse(dateFiles);
                    albumAdapter.clearData();

                    albumAdapter.addAll(directories);
                } else {
                    albumBinding.albumRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
                    albumBinding.albumRecycler.setLayoutAnimation(null);

                    albumBinding.albumRecycler.setAdapter(albumlistAdapter);
                    Collections.reverse(dateFiles);
                    albumlistAdapter.clearData();

                    albumlistAdapter.addAll(directories);
                }

            });
        }

    }

    class LoadImagesName extends AsyncTask<Void, Void, List<Directory<ImageFiles>>> {

        @SuppressLint("StaticFieldLeak")
        FragmentActivity fragmentActivity;
        List<ImageFiles> list1 = new ArrayList<>();

        public LoadImagesName(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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

            ArrayList<String> arrayList = new ArrayList<>();
            for (int i = 0; i < directories.size(); i++) {
                arrayList.add(String.valueOf(directories.get(i).getFiles().size()));
            }
            Collections.sort(directories, (Comparator<Directory>) (lhs, rhs) -> {
                Log.e("LLLLL_Compare: ", lhs.getName() + "    : " + rhs.getName());
                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
            });

            if (Util.SORTING_TYPE2.equals("Descending")) {
                Collections.reverse(directories);
            }

            fragmentActivity.runOnUiThread(() -> {
                if (!Util.isList) {
                    albumBinding.albumRecycler.setLayoutManager(new GridLayoutManager(getContext(), Util.COLUMN_TYPE));
                    albumBinding.albumRecycler.setLayoutAnimation(null);

                    albumBinding.albumRecycler.setAdapter(albumAdapter);
                    Collections.reverse(dateFiles);
                    albumAdapter.clearData();

                    albumAdapter.addAll(directories);
                } else {
                    albumBinding.albumRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
                    albumBinding.albumRecycler.setLayoutAnimation(null);

                    albumBinding.albumRecycler.setAdapter(albumlistAdapter);
                    Collections.reverse(dateFiles);
                    albumlistAdapter.clearData();

                    albumlistAdapter.addAll(directories);
                }

            });
        }

    }

    class LoadImagesNo extends AsyncTask<Void, Void, List<Directory<ImageFiles>>> {

        @SuppressLint("StaticFieldLeak")
        FragmentActivity fragmentActivity;
        List<ImageFiles> list1 = new ArrayList<>();

        public LoadImagesNo(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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

            ArrayList<Integer> arrayList = new ArrayList<>();
            for (int i = 0; i < directories.size(); i++) {
                arrayList.add(directories.get(i).getFiles().size());
            }

            Collections.sort(directories, new NameNoComparator());

            if (Util.SORTING_TYPE2.equals("Descending")) {
                Collections.reverse(directories);
            }

            fragmentActivity.runOnUiThread(() -> {
                if (!Util.isList) {
                    albumBinding.albumRecycler.setLayoutManager(new GridLayoutManager(getContext(), Util.COLUMN_TYPE));
                    albumBinding.albumRecycler.setLayoutAnimation(null);

                    albumBinding.albumRecycler.setAdapter(albumAdapter);
                    albumAdapter.clearData();

                    albumAdapter.addAll(directories);
                } else {
                    albumBinding.albumRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
                    albumBinding.albumRecycler.setLayoutAnimation(null);

                    albumBinding.albumRecycler.setAdapter(albumlistAdapter);
                    albumlistAdapter.clearData();

                    albumlistAdapter.addAll(directories);
                }

            });
        }

    }

    class NameNoComparator implements Comparator<Directory> {

        @Override
        public int compare(Directory o1, Directory o2) {
            return Integer.compare(o1.getFiles().size(), o2.getFiles().size());
        }

    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    }
}