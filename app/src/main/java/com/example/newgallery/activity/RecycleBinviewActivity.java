package com.example.newgallery.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newgallery.Adapters.RecycleviewAdapter;
import com.example.newgallery.Prefrence.SharedPrefrence;
import com.example.newgallery.R;
import com.example.newgallery.databinding.ActivityRecycleBinviewBinding;
import com.example.newgallery.modelclass.RecyclerModel;
import com.example.newgallery.utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class RecycleBinviewActivity extends AppCompatActivity {
    ActivityRecycleBinviewBinding recycleBinviewBinding ;

    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42;
    private static final String TAG = RecycleBinviewActivity.class.getSimpleName();
    private static boolean isDeleteImg = false;
    public ArrayList<RecyclerModel> privateList = new ArrayList<>();
    private int finalPosition = 0;
    RecycleviewAdapter recycleviewAdapter ;
    private MediaScannerConnection msConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recycleBinviewBinding = DataBindingUtil .setContentView(this,R.layout .activity_recycle_binview) ;

        finalPosition = getIntent().getIntExtra("Position", finalPosition);
        new LongOperation().execute();


        recycleBinviewBinding . imgDelete .setOnClickListener(view -> {
            isDeleteImg = true;
            File sampleFile = Environment.getExternalStorageDirectory();
            File file1 = new File(privateList.get(recycleBinviewBinding.imgViewPager.getCurrentItem()).getOldImagePath());

            File sourceFile = new File(privateList.get(recycleBinviewBinding.imgViewPager.getCurrentItem()).getNewImagePath());

            if (sourceFile.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                if (sourceFile.exists()) {
                    deleteFile(sourceFile);
                }
            } else {
                if (!SharedPrefrence.getSharedPreference(RecycleBinviewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
                    Intent intent = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                    }

                    intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                    startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                } else {
                    if (sourceFile.exists()) {
                        deleteFile(sourceFile);
                    }
                }
            }

        });
        recycleBinviewBinding  . imgRestore. setOnClickListener(view -> {
            isDeleteImg = false;
            File sampleFile = Environment.getExternalStorageDirectory();
            File file1 = new File(privateList.get(recycleBinviewBinding.imgViewPager.getCurrentItem()).getOldImagePath());

            File sourceFile = new File(privateList.get(recycleBinviewBinding.imgViewPager.getCurrentItem()).getNewImagePath());
            File destinationfile = new File(privateList.get(recycleBinviewBinding.imgViewPager.getCurrentItem()).getOldImagePath());

            if (destinationfile.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                if (sourceFile.exists()) {
                    moveImageFromRecycleBin ( sourceFile, destinationfile );
                }
            } else {
                if (!SharedPrefrence.getSharedPreference(RecycleBinviewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                    intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                    startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                } else {
                    if ( sourceFile.exists() ) {
                        moveSDImageFromRecycleBin ( sourceFile, destinationfile );
                    }
                }
            }

        });
        recycleBinviewBinding . imgBack .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
    private void moveImageFromRecycleBin(File sendFile, File sourceFile) {
        try {

            Bitmap finalBitmap = BitmapFactory.decodeFile(sendFile.getAbsolutePath());

            FileOutputStream out = new FileOutputStream(sourceFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ArrayList<String> favFileList = SharedPrefrence.getFavouriteFileList(RecycleBinviewActivity.this);
            if (favFileList.contains(sourceFile.getAbsolutePath())) {
                favFileList.remove(sourceFile.getAbsolutePath());
                SharedPrefrence.setFavouriteFileList(RecycleBinviewActivity.this, new ArrayList<>());
                SharedPrefrence.setFavouriteFileList(RecycleBinviewActivity.this, favFileList);
            }

            if (sendFile.exists()) {
                boolean isDelete = sendFile.delete();
                if (!isDelete) {
                    isDelete = Util.delete(RecycleBinviewActivity.this, sendFile);
                }
                Log.e("LLLL_del: ", String.valueOf(isDelete));
            }

            scanPhoto(sourceFile.toString());

            ArrayList<RecyclerModel> recycleList = SharedPrefrence.getRecycleBinData(RecycleBinviewActivity.this);

            if (recycleList == null)
                recycleList = new ArrayList<>();

            Log.e("LLLL_size: ", String.valueOf(recycleBinviewBinding.imgViewPager.getCurrentItem()));

            recycleList.remove(recycleBinviewBinding.imgViewPager.getCurrentItem());
            SharedPrefrence.setRecycleBinData(RecycleBinviewActivity.this, new ArrayList<>());
            SharedPrefrence.setRecycleBinData(RecycleBinviewActivity.this, recycleList);

            Toast.makeText(this, "Restore image in " + sourceFile.getParentFile().getAbsolutePath(), Toast.LENGTH_LONG).show();

            if (recycleBinviewBinding.imgViewPager.getCurrentItem() == privateList.size() - 1) {
                privateList.remove(recycleBinviewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                privateList.remove(recycleBinviewBinding.imgViewPager.getCurrentItem());
                recycleviewAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            File destinationfile = new File(privateList.get(recycleBinviewBinding.imgViewPager.getCurrentItem()).getOldImagePath());
            if (!SharedPrefrence .getSharedPreference (RecycleBinviewActivity.this ).contains ( destinationfile.getParentFile().getAbsolutePath() )) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
            } else {
                if (sourceFile.exists()) {
                    moveSDImageFromRecycleBin(sourceFile, destinationfile);
                }
            }
            e.printStackTrace();
        }
    }

    private void moveSDImageFromRecycleBin(File sentFile, File sourceFile) {

        OutputStream fileOutupStream = null;
        File file2 = new File(sourceFile.getParentFile().getParentFile().getAbsolutePath(), sourceFile.getName());
        DocumentFile targetDocument = getDocumentFile(file2, false);

        try {
            fileOutupStream = getContentResolver().openOutputStream(targetDocument.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(sentFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutupStream);
            fileOutupStream.flush();
            fileOutupStream.close();

            ArrayList<String> favFileList = SharedPrefrence.getFavouriteFileList(RecycleBinviewActivity.this);
            if (favFileList.contains(sourceFile.getAbsolutePath())) {
                favFileList.remove(sourceFile.getAbsolutePath());
                SharedPrefrence.setFavouriteFileList(RecycleBinviewActivity.this, new ArrayList<>());
                SharedPrefrence.setFavouriteFileList(RecycleBinviewActivity.this, favFileList);
            }

            if (sentFile.exists()) {
                boolean isDelete = sentFile.delete();
                if (!isDelete) {
                    isDelete = Util.delete(RecycleBinviewActivity.this, sentFile);
                }
                Log.e("LLLL_del: ", String.valueOf(isDelete));
            }

            scanPhoto(sourceFile.toString());


            ArrayList<RecyclerModel> recycleList = SharedPrefrence.getRecycleBinData(RecycleBinviewActivity.this);

            if (recycleList == null)
                recycleList = new ArrayList<>();

            recycleList.remove(recycleBinviewBinding.imgViewPager.getCurrentItem());
            SharedPrefrence.setRecycleBinData(RecycleBinviewActivity.this, new ArrayList<>());
            SharedPrefrence.setRecycleBinData(RecycleBinviewActivity.this, recycleList);

            Toast.makeText(this, "Restore image in " + sourceFile.getParentFile().getAbsolutePath(), Toast.LENGTH_LONG).show();

            if (recycleBinviewBinding.imgViewPager.getCurrentItem() == privateList.size() - 1) {
                privateList.remove(recycleBinviewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                privateList.remove(recycleBinviewBinding.imgViewPager.getCurrentItem());
                recycleviewAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void deleteFile(File sourceFile) {
        final Dialog dial = new Dialog(RecycleBinviewActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.delete_dialog);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        TextView title = dial.findViewById(R.id.tvTitleDel);
        TextView del = dial.findViewById(R.id.delete_yes);

        del.findViewById(R.id.delete_yes).setOnClickListener(view -> {

            isDeleteImg = true;
            if (sourceFile.exists()) {
                boolean isDelete = sourceFile.delete();
                if (!isDelete) {
                    isDelete = Util.delete(RecycleBinviewActivity.this, sourceFile);
                }
                Log.e("LLLL_del: ", String.valueOf(isDelete));
            }

            ArrayList<RecyclerModel> recycleList = SharedPrefrence.getRecycleBinData(RecycleBinviewActivity.this);

            if (recycleList == null)
                recycleList = new ArrayList<>();

            recycleList.remove(recycleBinviewBinding.imgViewPager.getCurrentItem());
            SharedPrefrence.setRecycleBinData(RecycleBinviewActivity.this, new ArrayList<>());
            SharedPrefrence.setRecycleBinData(RecycleBinviewActivity.this, recycleList);

            Toast.makeText(this, "Image deleted permanently.", Toast.LENGTH_SHORT).show();

            if (recycleBinviewBinding.imgViewPager.getCurrentItem() == privateList.size() - 1) {
                privateList.remove(recycleBinviewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                privateList.remove(recycleBinviewBinding.imgViewPager.getCurrentItem());
                recycleviewAdapter.notifyDataSetChanged();
            }
            dial.dismiss();
        });
        dial.findViewById(R.id.delete_no).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dial.dismiss();
            }
        });
        dial.show();
    }

    public DocumentFile getDocumentFile(final File file, final boolean isDirectory) {
        String baseFolder = getExtSdCardFolder(file);

        if (baseFolder == null) {
            return null;
        }

        String relativePath = null;
        try {
            String fullPath = file.getCanonicalPath();
            relativePath = fullPath.substring(baseFolder.length() + 1);
        } catch (IOException e) {
            return null;
        }

        Uri treeUri = Uri.parse(SharedPrefrence.getSharedPreferenceUri(RecycleBinviewActivity.this));

        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(RecycleBinviewActivity.this, treeUri);

        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            DocumentFile nextDocument = document.findFile(parts[i]);

            if (nextDocument == null) {
                if ((i < parts.length - 1) || isDirectory) {
                    nextDocument = document.createDirectory(parts[i]);
                } else {
                    nextDocument = document.createFile("image", parts[i]);
                }
            }
            document = nextDocument;
        }

        return document;
    }

    public String getExtSdCardFolder(final File file) {
        String[] extSdPaths = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            extSdPaths = getExtSdCardPaths();
        }
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String[] getExtSdCardPaths() {
        List<String> paths = new ArrayList<>();
        for (File file : getExternalFilesDirs("external")) {
            if (file != null && !file.equals(getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w(TAG, "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK) {

            File file1 = new File(privateList.get(recycleBinviewBinding.imgViewPager.getCurrentItem()).getNewImagePath());
            String[] parts = (file1.getAbsolutePath()).split("/");

            DocumentFile documentFile = DocumentFile.fromTreeUri(this, resultData.getData());
            documentFile = documentFile.findFile(parts[parts.length - 1]);
            File sourceFile = new File(privateList.get(recycleBinviewBinding.imgViewPager.getCurrentItem()).getNewImagePath());

            if (documentFile == null) {

                DocumentFile documentFile1 = DocumentFile.fromTreeUri(RecycleBinviewActivity.this, resultData.getData());

                SharedPrefrence.setSharedPreferenceUri(RecycleBinviewActivity.this, documentFile1.getUri());
                File file = new File(privateList.get(recycleBinviewBinding.imgViewPager.getCurrentItem()).getOldImagePath());
                SharedPrefrence.setSharedPreference(RecycleBinviewActivity.this, file.getParentFile().getAbsolutePath());
                if (!isDeleteImg) {
                    isDeleteImg = false;
                    if (sourceFile.exists()) {

                        if (!file.exists())
                            file.mkdirs();

                        File destinationfile = new File(file, sourceFile.getName());

                        moveSDImageFromRecycleBin(sourceFile, destinationfile);
                    }
                } else {
                    deleteFile(sourceFile);
                }
            } else {

                Log.e("LLL_Data: ", String.valueOf(documentFile.getUri()));

                SharedPrefrence.setSharedPreferenceUri(RecycleBinviewActivity.this, documentFile.getUri());
                SharedPrefrence.setSharedPreference(RecycleBinviewActivity.this, file1.getParentFile().getAbsolutePath());
                File file = new File(privateList.get(recycleBinviewBinding.imgViewPager.getCurrentItem()).getOldImagePath());

                if (isDeleteImg) {
                    isDeleteImg = false;
                    if (sourceFile.exists()) {

                        if (!file.exists())
                            file.mkdirs();

                        File destinationfile = new File(file, sourceFile.getName());

                        moveSDImageFromRecycleBin(sourceFile, destinationfile);
                    }

                } else {
                    deleteFile(sourceFile);
                }
            }


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
    private final class LongOperation extends AsyncTask<Void, Void, ArrayList<RecyclerModel>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<RecyclerModel> doInBackground(Void... params) {
            ArrayList<RecyclerModel> pathList;
            pathList = SharedPrefrence.getRecycleBinData(RecycleBinviewActivity.this);
            return pathList;
        }

        @Override
        protected void onPostExecute(ArrayList<RecyclerModel> result) {
            privateList = result;
            int finalPosition1 = finalPosition;
            runOnUiThread(() -> {
                recycleviewAdapter = new RecycleviewAdapter(RecycleBinviewActivity.this, result, "");
                recycleBinviewBinding.imgViewPager.setAdapter(recycleviewAdapter);
                recycleBinviewBinding.imgViewPager.setCurrentItem(finalPosition1);

                ArrayList<String> favList = SharedPrefrence.getFavouriteFileList(RecycleBinviewActivity.this);

                File file = new File(result.get(finalPosition1).getNewImagePath());
                recycleBinviewBinding.tvImgName.setText(file.getName());
                String time = DateFormat.format("dd MMM yyyy hh:mm a", file.lastModified()).toString();
                recycleBinviewBinding.tvDateTime.setText(time);

                recycleBinviewBinding.imgViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        File file = new File(result.get(position).getNewImagePath());
                        recycleBinviewBinding.tvImgName.setText(file.getName());
                        String time = DateFormat.format("dd MMM yyyy hh:mm a", file.lastModified()).toString();
                        recycleBinviewBinding.tvDateTime.setText(time);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

            });
        }
    }


}