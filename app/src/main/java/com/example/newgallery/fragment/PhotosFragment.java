package com.example.newgallery.fragment;


import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_MODIFIED;
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
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.newgallery.Adapters.PhotosAdapter;
import com.example.newgallery.R;
import com.example.newgallery.databinding.FragmentPhotosBinding;
import com.example.newgallery.modelclass.Directory;
import com.example.newgallery.modelclass.ImageFiles;
import com.example.newgallery.utils.Util;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class PhotosFragment extends Fragment {
    FragmentPhotosBinding binding ;
    PhotosAdapter photosAdapter ;
    ArrayList<ImageFiles>allImage = new ArrayList<>();
    public ArrayList<String> dateList = new ArrayList<>();
    GridLayoutManager gridLayoutManager;
    private boolean isSet = false;
    private static final String TAG = PhotosFragment.class.getSimpleName();
    private MyReceiver myReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myReceiver = new MyReceiver();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(myReceiver,
                new IntentFilter("TAG_REFRESH"));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photos, container, false);
        gridLayoutManager = new GridLayoutManager( requireContext(), Util.COLUMN_TYPE);
        binding.pRecycler.setLayoutManager(gridLayoutManager);
        binding.pRecycler.setScrollContainer(true);
        binding.pRecycler.setLayoutAnimation(null);
        binding.pRecycler.setItemAnimator(null);

        photosAdapter = new PhotosAdapter(new ArrayList<>(), getActivity());
        binding.pRecycler.setAdapter(photosAdapter);


        return binding.getRoot() ;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean isGranted = EasyPermissions.hasPermissions(getContext(), "android.permission.READ_EXTERNAL_STORAGE");
        if (isGranted) {
//            isSet = true;
            new LoadImages(getActivity()).execute();
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(myReceiver);
    }

    void startAnim() {
        binding.progressbar.setVisibility(View.VISIBLE);
    }

    void stopAnim() {
        binding.progressbar.setVisibility(View.GONE);
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
            allImage.clear();
            dateList.clear();
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
                    DATE_MODIFIED,
                    MediaStore.Images.Media.ORIENTATION
            };

            String selection = MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=? or " + MediaStore.MediaColumns.MIME_TYPE + "=?";

            String[] selectionArgs;
            selectionArgs = new String[]{"image/jpeg", "image/png", "image/jpg", "image/gif"};

            Cursor data = requireActivity().getContentResolver().query(MediaStore.Files.getContentUri("external"),
                    FILE_PROJECTION,
                    selection,
                    selectionArgs,
                    DATE_MODIFIED + " DESC");

            List<Directory<ImageFiles>> directories = new ArrayList<>();

            if (data.getPosition() != -1) {
                data.moveToPosition(-1);
            }

            int i = 0;
            int position = 0;

            while (data.moveToNext()) {
                //Create a File instance
                if (data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME)) != null) {
                    ImageFiles img = new ImageFiles();

                    i = data.getPosition();

                    if (!data.getString(data.getColumnIndexOrThrow(TITLE)).startsWith(".")) {
                        img.setId(data.getLong(data.getColumnIndexOrThrow(_ID)));
                        img.setName(data.getString(data.getColumnIndexOrThrow(TITLE)));
                        img.setPath(data.getString(data.getColumnIndexOrThrow(DATA)));
                        img.setSize(data.getLong(data.getColumnIndexOrThrow(SIZE)));
                        img.setBucketId(data.getString(data.getColumnIndexOrThrow(BUCKET_ID)));
                        img.setBucketName(data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME)));
                        img.setDate(Util.convertTimeDateModified(data.getLong(data.getColumnIndexOrThrow(DATE_MODIFIED))));
                        img.setOrientation(data.getInt(data.getColumnIndexOrThrow(ORIENTATION)));
                        img.setDateTitle(img.getDate());

                        if (!dateList.contains(img.getDate())) {
                            dateList.add(img.getDate());
                            img.setDirectory(true);
                            data.moveToPosition(i - 1);
                            img.setPosition(position);
                            position = position + 1;
                        } else {
                            img.setDirectory(false);
                        }

                        allImage.add(img);
                    }
                }
            }

            data.close();

            return directories;
        }

        @Override
        protected void onPostExecute(List<Directory<ImageFiles>> directories) {
            super.onPostExecute(directories);
            Log.d(TAG, "onPostExecute: " + "done");

            fragmentActivity.runOnUiThread(() -> {

                photosAdapter.addAll(allImage);
                photosAdapter.notifyDataSetChanged();

                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (photosAdapter != null) {
                            if (photosAdapter.getItemViewType(position) == PhotosAdapter.DATE_TYPE) {
                                return Util.COLUMN_TYPE;
                            }
                        }
                        return 1;
                    }
                });
                binding.pRecycler.setLayoutManager(gridLayoutManager);
            });
            stopAnim();

        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            gridLayoutManager = new GridLayoutManager(requireContext(), Util.COLUMN_TYPE);
            binding.pRecycler.setLayoutManager(gridLayoutManager);
            binding.pRecycler.setScrollContainer(true);
            binding.pRecycler.setLayoutAnimation(null);
            binding.pRecycler.setItemAnimator(null);

            photosAdapter = new PhotosAdapter(new ArrayList<>(), getActivity());
            binding.pRecycler.setAdapter(photosAdapter);
            new LoadImages(getActivity()).execute();
        }
    }


}