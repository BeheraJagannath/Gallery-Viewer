package com.example.newgallery.activity;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.DATE_MODIFIED;
import static android.provider.MediaStore.MediaColumns.ORIENTATION;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newgallery.Adapters.AlbumViewPager;
import com.example.newgallery.Adapters.CreateAlbumAdapter;
import com.example.newgallery.MainActivity;
import com.example.newgallery.Prefrence.SharedPrefrence;
import com.example.newgallery.R;
import com.example.newgallery.databinding.ActivityImageViewBinding;
import com.example.newgallery.modelclass.Directory;
import com.example.newgallery.modelclass.ImageFiles;
import com.example.newgallery.modelclass.ItemClicklistener;
import com.example.newgallery.modelclass.RecyclerModel;
import com.example.newgallery.utils.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageViewActivity extends AppCompatActivity  {
    ActivityImageViewBinding imageViewBinding;
    MyClickHandlers myClickHandlers;
    private static boolean isDeleteImg = false;
    private static boolean isCopyImg = false;
    private static int position = 0;
    private static int directoryPosition = 0;
    private static int createAlbumPosition = 0;
    private static String ALBUMNAME = "";
    private static boolean isCreateAlbum = false;
    public ArrayList<String> dateList = new ArrayList<>();
    private final List<ImageFiles> imageFiles = new ArrayList<>();
    public ArrayList<String> privateList = new ArrayList<>();
    public List<Directory<ImageFiles>> dateFiles = new ArrayList<>();
    private static final String TAG = ImageViewActivity.class.getSimpleName();
    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE = 42;
    AlbumViewPager albumViewPager;

    CreateAlbumAdapter createAlbumAdapter;
    BottomSheetBehavior mainPopUpBehaviour;
    BottomSheetBehavior albumPopUpBehaviour;
    BottomSheetBehavior albumCreatePopUpBehaviour;
    BottomSheetBehavior moveAlbumPopUpBehaviour;
    BottomSheetBehavior imageInfoUpBehaviour;
    BottomSheetBehavior imageRenameBehaviour;
    MediaScannerConnection msConn;

    private PdfDocument myPdfDocument;
    private int pageHeight;
    private boolean startSlideShow = false;
    private int pageWidth;
    void startAnim() {
        imageViewBinding.rlLoading.setVisibility(View.VISIBLE);
    }

    void stopAnim() {
        imageViewBinding.rlLoading.setVisibility(View.GONE);
    }
    void startSlideShow() {
        imageViewBinding.llBottom.setVisibility(View.GONE);
        imageViewBinding.rlToolbar.setVisibility(View.GONE);
        int pos = imageViewBinding.imgViewPager.getCurrentItem();
        startSlideShow = true;
        calendarUpdater.run();
    }

    void stopSlideShow() {
        imageViewBinding.llBottom.setVisibility(View.VISIBLE);
        imageViewBinding.rlToolbar.setVisibility(View.VISIBLE);
        startSlideShow = false;
        handler.removeCallbacks(calendarUpdater);
    }
    Handler handler = new Handler();
    public Runnable calendarUpdater = new Runnable() {
        @Override
        public void run() {
            Animation fadein = AnimationUtils.loadAnimation(ImageViewActivity.this, R.anim.fade_in);
            Log.e("current item:", String.valueOf(imageViewBinding.imgViewPager.getCurrentItem()));
            if (imageViewBinding.imgViewPager.getCurrentItem() == imageFiles.size() - 1) {
                imageViewBinding.imgViewPager.setCurrentItem(0);
            } else {
                imageViewBinding.imgViewPager.setCurrentItem(imageViewBinding.imgViewPager.getCurrentItem() + 1);
            }
            imageViewBinding.imgViewPager.startAnimation(fadein);
            if (Util.SLIDE_TIME.equals(getString(R.string._1_sec)))
                handler.postDelayed(calendarUpdater, 1000);
            else if (Util.SLIDE_TIME.equals(getString(R.string._2_sec)))
                handler.postDelayed(calendarUpdater, 2000);
            else if (Util.SLIDE_TIME.equals(getString(R.string._3_sec)))
                handler.postDelayed(calendarUpdater, 3000);
            else if (Util.SLIDE_TIME.equals(getString(R.string._4_sec)))
                handler.postDelayed(calendarUpdater, 4000);
            else if (Util.SLIDE_TIME.equals(getString(R.string._5_sec)))
                handler.postDelayed(calendarUpdater, 5000);
            else
                handler.postDelayed(calendarUpdater, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        imageViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_image_view);

        position = getIntent().getIntExtra("Position", 0);
        directoryPosition = getIntent().getIntExtra("directoryPosition", 0);

        imageViewBinding.rvListView.setLayoutManager(new LinearLayoutManager(ImageViewActivity.this, RecyclerView.VERTICAL, false));
        new LoadImages(ImageViewActivity.this).execute();
        new LoadAlbumImages(ImageViewActivity.this).execute();

        createAlbumAdapter = new CreateAlbumAdapter(dateFiles, ImageViewActivity.this, position -> {
            Log.e("LLLLL_PositionPath: ", position + "   Path: " + dateFiles.get(position).getPath());
            ALBUMNAME = new File(dateFiles.get(position).getPath()).getName();
            isCreateAlbum = false;
            createAlbumPosition = position;

            albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        imageViewBinding.rvListView.setAdapter(createAlbumAdapter);
        myClickHandlers = new MyClickHandlers(ImageViewActivity.this);
        imageViewBinding.setOnClickListener(myClickHandlers);

        mainPopUpBehaviour = BottomSheetBehavior.from(imageViewBinding.llMainPopup);
        albumPopUpBehaviour = BottomSheetBehavior.from(imageViewBinding.llCreateAlbum);
        albumCreatePopUpBehaviour = BottomSheetBehavior.from(imageViewBinding.llCreateAnAlbum);
        moveAlbumPopUpBehaviour = BottomSheetBehavior.from(imageViewBinding.llMoveAlbum);
        imageInfoUpBehaviour = BottomSheetBehavior.from(imageViewBinding.rlImageInfo);
        imageRenameBehaviour = BottomSheetBehavior.from(imageViewBinding.llRename);



    }


    @Override
    public void onBackPressed() {
        if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (imageInfoUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            imageInfoUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (imageRenameBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            imageRenameBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            if (startSlideShow)
                stopSlideShow();
            else {
            Intent intent = new Intent(ImageViewActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            }
        }
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onShareListener(View view) {
            String path ;
            File file ;
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            Uri uri;
            path =imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath();
            file = new File(path) ;
            uri =Uri .fromFile(file) ;
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, "share via"));
        }

        public void onImageEditListener(View view) {
            File file = new File(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath()).getParentFile().getAbsoluteFile();
            Util.FOLDER_NAME = file.getAbsolutePath();


            Intent intent = new Intent(ImageViewActivity.this, EditActivity.class);
            intent.putExtra("filePath", imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());
            startActivity(intent);

        }

        public void onImageDeleteListener(View view) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                deleteFile();
            }
        }

        public void onProtectListener(View view) {
            if (!SharedPrefrence.getPasswordProtect(ImageViewActivity.this).equals("")) {
                File sampleFile = Environment.getExternalStorageDirectory();
                File file1 = new File(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());

                if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        SaveImage(bitmap, file1);
                    }
                } else {
                    if (!SharedPrefrence.getSharedPreference(ImageViewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            getData(file1);
                        }
                    }
                }
            } else {
                Util.isPrivate = true;
                Toast.makeText(ImageViewActivity.this, "Please set first password.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ImageViewActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }

        public void onPrivateListener(View view) {
            if (!SharedPrefrence.getPasswordProtect(ImageViewActivity.this).equals("")) {
                if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }

                File sampleFile = Environment.getExternalStorageDirectory();
                File file1 = new File(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());

                if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        SaveImage(bitmap, file1);
                    }
                } else {
                    if (!SharedPrefrence.getSharedPreference(ImageViewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            getData(file1);
                        }
                    }
                }
            } else {
                Util.isPrivate = true;
                Toast.makeText(ImageViewActivity .this , "Please set first password .", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ImageViewActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }

        public void onMoreListener(View view) {
            if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

        }
        public void onAddAlbumListener(View view) {
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        }

        public void onAlbumViewCancelListener(View view) {
            if (albumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        }

        public void onCreateAlbumListener(View view) {
            albumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            isCreateAlbum = true;
            createAlbumPosition = 0;
            if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        }

        public void onOkListener(View view) {
//            fireAnalytics(ImageViewActivity.class.getSimpleName(), "ImageAlbum", "Create New Album");
            if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            if (imageViewBinding.etAlbumName.length() > 0) {
                ALBUMNAME = imageViewBinding.etAlbumName.getText().toString();
            }
        }

        public void onCancelListener(View view) {
            if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (albumCreatePopUpBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                albumCreatePopUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
        public void onMoveAlbumListener(View view) {
            File sourceFile = new File(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());

            File sampleFile = Environment.getExternalStorageDirectory();
            File file1;
            if (isCreateAlbum)
                file1 = new File(sampleFile, ALBUMNAME);
            else
                file1 = new File(dateFiles.get(createAlbumPosition).getPath());

            if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    moveImage(bitmap, sourceFile, isCreateAlbum, createAlbumPosition);
                }
            } else {
                if (!SharedPrefrence.getSharedPreference(ImageViewActivity .this).contains(file1.getParentFile().getAbsolutePath())) {
                    Intent intent = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                    }

                    intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                    startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT_TREE);
                } else {
                    moveSDImage(sourceFile, isCreateAlbum, createAlbumPosition);
                }
            }


            if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
        public void onCopyListener(View view) {
            File sourceFile = new File(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());

            isCopyImg = true;

            File sampleFile = Environment.getExternalStorageDirectory();
            File file1;
            if (isCreateAlbum)
                file1 = new File(sampleFile, ALBUMNAME);
            else
                file1 = new File(dateFiles.get(createAlbumPosition).getPath());

            if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                copyImage(bitmap, sourceFile, isCreateAlbum, createAlbumPosition);
            } else {
                if (!SharedPrefrence.getSharedPreference(ImageViewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
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
                    File file = new File(dateFiles.get(position).getPath());
                    copySDImage(sourceFile, isCreateAlbum, createAlbumPosition);
                }
            }

            if (moveAlbumPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                moveAlbumPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
        public void onSlideShowListener(View view) {
            slideShowPopUp();
        }
        public void onDetailsListener(View view) {
            if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (imageInfoUpBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                imageInfoUpBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

        }
        public void onSetWallpaperListener(View  view ) {
            if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            startAnim();
            SetWallpaper(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath()) ;
        }
        private void SetWallpaper(String path) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
            try {
                manager.setBitmap(bitmap);
                Toast.makeText(ImageViewActivity . this, "Wallpaper set!", Toast.LENGTH_SHORT).show();
                stopAnim();
            } catch (IOException e) {
                Toast.makeText(ImageViewActivity .this , "Error!", Toast.LENGTH_SHORT).show();
            }
        }
        public void onRenameListener(View view) {
            if (imageRenameBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                imageRenameBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

        }
        public void onRenameOkListener(View view) {
            if (imageRenameBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                imageRenameBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (imageViewBinding.etRename.getText() != null) {
                File file = new File(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());
                String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                File sdcard = new File(file.getParentFile().getAbsolutePath());
                File newFileName = new File(sdcard, imageViewBinding.etRename.getText().toString() + extension);
                boolean isRename = file.renameTo(newFileName);

                if (isRename) {
                    ContentResolver resolver = getApplicationContext().getContentResolver();
                    resolver.delete(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA +
                                    " =?", new String[]{file.getAbsolutePath()});
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(newFileName));
                    getApplicationContext().sendBroadcast(intent);
                }

                ArrayList<String> hideFileList = SharedPrefrence.getHideFileList(ImageViewActivity.this);
                if (hideFileList.contains(file.getAbsolutePath())) {
                    hideFileList.remove(file.getAbsolutePath());
                    hideFileList.add(newFileName.getAbsolutePath());
                    SharedPrefrence.setHideFileList(ImageViewActivity.this, new ArrayList<>());
                    SharedPrefrence.setHideFileList(ImageViewActivity.this, hideFileList);
                }

                ArrayList<String> favFileList = SharedPrefrence.getFavouriteFileList(ImageViewActivity.this);
                if (favFileList.contains(file.getAbsolutePath())) {
                    favFileList.remove(file.getAbsolutePath());
                    favFileList.add(newFileName.getAbsolutePath());
                    SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, new ArrayList<>());
                    SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, favFileList);
                }

                Log.e("LLL_Name: ", imageViewBinding.etAlbumName.getText().toString() + "." + extension + "    " + newFileName.getAbsolutePath() + "  " + isRename);
                imageViewBinding.tvImgName.setText(newFileName.getName());
            }
        }
        public void onRenameCanListener(View view) {
            if (imageRenameBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                imageRenameBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

        }
        public void onPrintListener(View view) {
            if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            File printFile = new File(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());
            ArrayList<String> printFileLsit = new ArrayList<>();
            printFileLsit.add(printFile.getAbsolutePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                printDocument(printFileLsit);
            }
        }
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void printDocument(ArrayList<String> totalDoc) {
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

            String jobName = getString(R.string.app_name) +
                    " Document";

            printManager.print(jobName, new MyPrintDocumentAdapter(ImageViewActivity.this, totalDoc),
                    null);
        }
        public void onBackListener(View view) {
            onBackPressed();
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getData(File file1) {

        File file2 = new File(file1.getParentFile().getParentFile().getAbsolutePath(), "." + file1.getName());
        File file3 = new File(file1.getParentFile().getAbsolutePath(), "." + file1.getName());

        OutputStream fileOutputStream = null;
        DocumentFile targetDocument = getDocumentFile(file2, false);

        try {
            fileOutputStream = getContentResolver().openOutputStream(targetDocument.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            ArrayList<String> hideFileList = SharedPrefrence.getHideFileList(ImageViewActivity.this);
            hideFileList.add(file3.getAbsolutePath());
            SharedPrefrence.setHideFileList(ImageViewActivity.this, hideFileList);

            if (file1.exists()) {
                Util.delete(ImageViewActivity.this, file1);
            }

            imageFiles.remove(imageViewBinding.imgViewPager.getCurrentItem());
            albumViewPager.notifyDataSetChanged();
            if (imageFiles.isEmpty())
                onBackPressed();
            Toast.makeText(this, "Hide " + file1.getName(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void moveSDImage(File sourceFile, boolean isCreateAlbum, int position) {

        File file;
        if (!isCreateAlbum) {
            file = new File(dateFiles.get(position).getPath(), sourceFile.getName());
        } else {
            File mainDir = Environment.getExternalStorageDirectory();
            File destinationFile = new File(mainDir, ALBUMNAME);

            if (!destinationFile.exists()) {
                destinationFile.mkdirs();
            }
            Log.e("LLL_Name: ", destinationFile.getAbsolutePath());

            file = new File(destinationFile, sourceFile.getName());
        }

        OutputStream fileOutupStream = null;
        File file2 = new File(file.getParentFile().getParentFile().getAbsolutePath(), sourceFile.getName());
        DocumentFile targetDocument = getDocumentFile(file2, false);

        try {
            fileOutupStream = getContentResolver().openOutputStream(targetDocument.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutupStream);
            fileOutupStream.flush();
            fileOutupStream.close();

            ArrayList<String> favFileList = SharedPrefrence.getFavouriteFileList(ImageViewActivity.this);
            favFileList.remove(sourceFile.getAbsolutePath());
            favFileList.add(file.getAbsolutePath());
            SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, new ArrayList<>());
            SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, favFileList);

            if (sourceFile.exists()) {
                boolean isDelete = Util.delete(ImageViewActivity.this, sourceFile);
                Log.e("LLLL_Del: ", String.valueOf(isDelete));
            }

            scanPhoto(file.toString());


            if (imageViewBinding.imgViewPager.getCurrentItem() == imageFiles.size() - 1) {
                imageFiles.remove(imageViewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                imageFiles.remove(imageViewBinding.imgViewPager.getCurrentItem());
                albumViewPager.notifyDataSetChanged();
            }
            if (imageFiles.size() == 0)
                onBackPressed();
            Toast.makeText(this, "Move " + sourceFile.getName(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void copySDImage(File sourceFile, boolean isCreateAlbum, int position) {

        File file;
        if (!isCreateAlbum) {
            file = new File(dateFiles.get(position).getPath(), sourceFile.getName());
        } else {
            File mainDir = Environment.getExternalStorageDirectory();
            File destinationFile = new File(mainDir, ALBUMNAME);

            if (!destinationFile.exists()) {
                destinationFile.mkdirs();
            }
            Log.e("LLL_Name: ", destinationFile.getAbsolutePath());

            file = new File(destinationFile, sourceFile.getName());
        }

        OutputStream fileOutputStream = null;
        File file2 = new File(file.getParentFile().getParentFile().getAbsolutePath(), sourceFile.getName());
        DocumentFile targetDocument = getDocumentFile(file2, false);

        try {
            fileOutputStream = getContentResolver().openOutputStream(targetDocument.getUri());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            scanPhoto(file.toString());

            Toast.makeText(this, "Copy " + sourceFile.getName(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    public DocumentFile getDocumentFile(final File file, final boolean isDirectory) {
        String baseFolder = getExtSdCardFolder(file);

        if (baseFolder == null) {
            return null;
        }

        String relativePath;
        try {
            String fullPath = file.getCanonicalPath();
            relativePath = fullPath.substring(baseFolder.length() + 1);
        } catch (IOException e) {
            return null;
        }

        Uri treeUri = Uri.parse(SharedPrefrence.getSharedPreferenceUri(ImageViewActivity.this));

        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(ImageViewActivity.this, treeUri);

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
        String[] extSdPaths = getExtSdCardPaths();
        try {
            for (String extSdPath : extSdPaths) {
                if (file.getCanonicalPath().startsWith(extSdPath)) {
                    return extSdPath;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
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



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void deleteFile() {
        final Dialog dial = new Dialog(ImageViewActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.delete_dialog);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        TextView title = dial.findViewById(R.id.tvTitleDel);
        title.setText("Move 1 image to the Recycle bin?");

        TextView del = dial.findViewById(R.id.delete_yes);
        del.setText("Move to Recycle bin");

        del.findViewById(R.id.delete_yes).setOnClickListener(view -> {

            isDeleteImg = true;
            File sourceFile = new File(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());

            File file = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/.nomedia");

            if (!file.exists())
                file.mkdirs();

            File destinations = new File(file, sourceFile.getName());

            File sampleFile = Environment.getExternalStorageDirectory();
            File file1 = new File(dateFiles.get(createAlbumPosition).getPath());

            ArrayList<String> favFileList = SharedPrefrence.getFavouriteFileList(ImageViewActivity.this);
            favFileList.remove(sourceFile.getAbsolutePath());
            SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, new ArrayList<>());
            SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, favFileList);

            if (file1.getAbsolutePath().contains(sampleFile.getAbsolutePath())) {
                if (sourceFile.exists()) {
                    moveImageToRecycleBin(sourceFile, destinations);
                }
            } else {
                if (!SharedPrefrence.getSharedPreference(ImageViewActivity.this).contains(file1.getParentFile().getAbsolutePath())) {
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
                        moveSDImageToRecycleBin(sourceFile, destinations);
                    }
                }
            }

            if (imageFiles.size() == 0)
                onBackPressed();

            dial.dismiss();
        });
        dial.findViewById(R.id.delete_no).setOnClickListener(view -> dial.dismiss());
        dial.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void moveSDImageToRecycleBin(File sentFile, File sourceFile) {
        isDeleteImg = false;

        try {

            Bitmap finalBitmap = BitmapFactory.decodeFile(sentFile.getAbsolutePath());

            FileOutputStream out = new FileOutputStream(sourceFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ArrayList<String> favFileList = SharedPrefrence.getFavouriteFileList(ImageViewActivity.this);
            if (favFileList.contains(sourceFile.getAbsolutePath())) {
                favFileList.remove(sourceFile.getAbsolutePath());
                SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, new ArrayList<>());
                SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, favFileList);
            }

            Toast.makeText(this, "Move File to Recycle Bin", Toast.LENGTH_LONG).show();
            ArrayList<RecyclerModel> recycleList = SharedPrefrence.getRecycleBinData(ImageViewActivity.this);
            if (recycleList == null)
                recycleList = new ArrayList<>();

            RecyclerModel recycleModel = new RecyclerModel();
            recycleModel.setImageName(sourceFile.getName());
            recycleModel.setOldImagePath(sentFile.getAbsolutePath());
            recycleModel.setNewImagePath(sourceFile.getAbsolutePath());

            recycleList.add(recycleModel);
            SharedPrefrence.setRecycleBinData(ImageViewActivity.this, new ArrayList<>());
            SharedPrefrence.setRecycleBinData(ImageViewActivity.this, recycleList);

            if (sentFile.exists()) {
                boolean isDelete = sentFile.delete();
                if (!isDelete) {
                    isDelete = Util.delete(ImageViewActivity.this, sentFile);
                }
            }

            scanPhoto(sentFile.toString());

            if (imageViewBinding.imgViewPager.getCurrentItem() == imageFiles.size() - 1) {
                imageFiles.remove(imageViewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                imageFiles.remove(imageViewBinding.imgViewPager.getCurrentItem());
                albumViewPager.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Toast.makeText(this, "something went wrong" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void SaveImage(Bitmap finalBitmap, File samplefile) {

        File file = new File(samplefile.getParentFile().getAbsolutePath(), "." + samplefile.getName());
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ArrayList<String> hideFileList = SharedPrefrence.getHideFileList(ImageViewActivity.this);
            hideFileList.add(file.getAbsolutePath());
            SharedPrefrence.setHideFileList(ImageViewActivity.this, hideFileList);

            Toast.makeText(this, "Hide " + samplefile.getName(), Toast.LENGTH_LONG).show();
            if (samplefile.exists()) {
                Util.delete(ImageViewActivity.this, samplefile);
            }

            scanPhoto(samplefile.toString());

            imageFiles.remove(imageViewBinding.imgViewPager.getCurrentItem());
            albumViewPager.notifyDataSetChanged();
            if (imageFiles.isEmpty())
                onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void moveImageToRecycleBin(File sendFile, File sourceFile) {
        isDeleteImg = false;
        try {

            Bitmap finalBitmap = BitmapFactory.decodeFile(sendFile.getAbsolutePath());

            FileOutputStream out = new FileOutputStream(sourceFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ArrayList<String> favFileList = SharedPrefrence.getFavouriteFileList(ImageViewActivity.this);
            if (favFileList.contains(sourceFile.getAbsolutePath())) {
                favFileList.remove(sourceFile.getAbsolutePath());
                SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, new ArrayList<>());
                SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, favFileList);
            }

            Toast.makeText(this, "Move File to Recycle Bin", Toast.LENGTH_LONG).show();
            ArrayList<RecyclerModel> recycleList = SharedPrefrence.getRecycleBinData(ImageViewActivity.this);

            RecyclerModel recycleModel = new RecyclerModel();
            recycleModel.setImageName(sourceFile.getName());
            recycleModel.setOldImagePath(sendFile.getAbsolutePath());
            recycleModel.setNewImagePath(sourceFile.getAbsolutePath());

            recycleList.add(recycleModel);
            SharedPrefrence.setRecycleBinData(ImageViewActivity.this, new ArrayList<>());
            SharedPrefrence.setRecycleBinData(ImageViewActivity.this, recycleList);

            if (sendFile.exists()) {
                boolean isDelete = sendFile.delete();
                if (!isDelete) {
                    isDelete = Util.delete(ImageViewActivity.this, sendFile);
                }
            }

            scanPhoto(sendFile.toString());

            if (imageViewBinding.imgViewPager.getCurrentItem() == imageFiles.size() - 1) {
                imageFiles.remove(imageViewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                imageFiles.remove(imageViewBinding.imgViewPager.getCurrentItem());
                albumViewPager.notifyDataSetChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void moveImage(Bitmap finalBitmap, File sourceFile, boolean isCreateAlbum, int position) {

        String[] parts = (sourceFile.getAbsolutePath()).split("/");

        File file;
        if (!isCreateAlbum) {
            file = new File(dateFiles.get(position).getPath(), sourceFile.getName());
        } else {
            File mainDir = Environment.getExternalStorageDirectory();
            File destinationFile = new File(mainDir, ALBUMNAME);

            if (!destinationFile.exists()) {
                destinationFile.mkdirs();
            }
            Log.e("LLL_Name: ", destinationFile.getAbsolutePath());

            file = new File(destinationFile, sourceFile.getName());
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            ArrayList<String> favFileList = SharedPrefrence.getFavouriteFileList(ImageViewActivity.this);
            favFileList.remove(sourceFile.getAbsolutePath());
            favFileList.add(file.getAbsolutePath());
            SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, new ArrayList<>());
            SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, favFileList);

            Toast.makeText(this, "Move File to " + file.getParentFile().getAbsolutePath(), Toast.LENGTH_LONG).show();
            if (sourceFile.exists()) {
                boolean isDelete = Util.delete(ImageViewActivity.this, sourceFile);
            }

            scanPhoto(file.toString());

            if (imageViewBinding.imgViewPager.getCurrentItem() == imageFiles.size() - 1) {
                imageFiles.remove(imageViewBinding.imgViewPager.getCurrentItem());
                onBackPressed();
            } else {
                imageFiles.remove(imageViewBinding.imgViewPager.getCurrentItem());
                albumViewPager.notifyDataSetChanged();
            }

            if (imageFiles.size() == 0)
                onBackPressed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void slideShowPopUp() {
        String[] radio1 = {getResources().getString(R.string._1_sec),
                getResources().getString(R.string._2_sec),
                getResources().getString(R.string._3_sec),
                getResources().getString(R.string._4_sec),
                getResources().getString(R.string._5_sec)};

        final Dialog dial = new Dialog(ImageViewActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.slideshow_dialog);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        RadioGroup rg_sorting1 = dial.findViewById(R.id.rg_sorting1);

        for (int i = 0; i < radio1.length; i++) {
            String sorting1 = radio1[i];
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30, 30, 7, 30);
            radioButton.setText(sorting1);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            radioButton.setTextColor(getResources().getColor(R.color.textview_color));
            radioButton.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.radio_button)));
            radioButton.setId(i);

            if (Util.SLIDE_TIME.equals("")) {
                if (i == 0) {
                    radioButton.setChecked(true);
                    Util.SLIDE_TIME = radioButton.getText().toString();
                }
            } else if (radioButton.getText().equals(Util.SLIDE_TIME)) {
                radioButton.setChecked(true);
            }

            rg_sorting1.addView(radioButton);
        }

        //set listener to radio button group
        rg_sorting1.setOnCheckedChangeListener((group, checkedId) -> {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton radioBtn = dial.findViewById(checkedRadioButtonId);
            Util.SLIDE_TIME = radioBtn.getText().toString();
            Log.e("LLLLL_Refill_time: ", Util.SLIDE_TIME);
            startSlideShow();


            dial.dismiss();
        });

        if (mainPopUpBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mainPopUpBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        dial.show();

    }

    private void copyImage(Bitmap finalBitmap, File sourceFile, boolean isCreateAlbum, int position) {

        File file;
        if (!isCreateAlbum) {
            file = new File(dateFiles.get(position).getPath(), sourceFile.getName());
        } else {
            File mainDir = Environment.getExternalStorageDirectory();
            File destinationFile = new File(mainDir, ALBUMNAME);

            if (!destinationFile.exists()) {
                destinationFile.mkdirs();
            }
            Log.e("LLL_Name: ", destinationFile.getAbsolutePath());

            file = new File(destinationFile, sourceFile.getName());
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            Toast.makeText(this, "Copy File to " + file.getParentFile().getAbsolutePath(), Toast.LENGTH_LONG).show();

            scanPhoto(file.toString());

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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK) {

            File file1 = new File(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());
            String[] parts = (file1.getAbsolutePath()).split("/");

            DocumentFile documentFile = DocumentFile.fromTreeUri(this, resultData.getData());
            documentFile = documentFile.findFile(parts[parts.length - 1]);

            if (documentFile == null) {
                File sourceFile = new File(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());
                DocumentFile documentFile1 = DocumentFile.fromTreeUri(this, resultData.getData());

                SharedPrefrence.setSharedPreferenceUri(ImageViewActivity.this, documentFile1.getUri());
                SharedPrefrence.setSharedPreference(ImageViewActivity.this, sourceFile.getParentFile().getAbsolutePath());

                if (isCopyImg) {
                    isCopyImg = false;
                    copySDImage(sourceFile, isCreateAlbum, createAlbumPosition);
                } else if (isDeleteImg) {
                    isDeleteImg = false;
                    if (sourceFile.exists()) {
                        File file = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/.nomedia");

                        if (!file.exists())
                            file.mkdirs();

                        File destinations = new File(file, sourceFile.getName());

                        moveSDImageToRecycleBin(sourceFile, destinations);
                    }

                } else {
                    moveSDImage(sourceFile, isCreateAlbum, createAlbumPosition);
                }
            } else {

                Log.e("LLL_Data: ", String.valueOf(documentFile.getUri()));

                SharedPrefrence.setSharedPreferenceUri(ImageViewActivity.this, documentFile.getUri());
                SharedPrefrence.setSharedPreference(ImageViewActivity.this, file1.getParentFile().getAbsolutePath());

                getData(file1);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public class MyPrintDocumentAdapter extends PrintDocumentAdapter {
        Context context;
        ArrayList<String> path;

        public MyPrintDocumentAdapter(Context context, ArrayList<String> path) {
            this.context = context;
            this.path = path;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes,
                             PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback,
                             Bundle metadata) {

            myPdfDocument = new PrintedPdfDocument(context, newAttributes);

            pageHeight =
                    newAttributes.getMediaSize().getHeightMils() / 1000 * 72;
            pageWidth =
                    newAttributes.getMediaSize().getWidthMils() / 1000 * 72;

            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            if (path.size() > 0) {
                PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                        .Builder("print_output.pdf").setContentType(
                                PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(path.size());

                PrintDocumentInfo info = builder.build();
                callback.onLayoutFinished(info, true);
            } else {
                callback.onLayoutFailed("Page count is zero.");
            }

        }

        @Override
        public void onWrite(final PageRange[] pageRanges,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal
                                    cancellationSignal,
                            final WriteResultCallback callback) {

            for (int i = 0; i < path.size(); i++) {
                if (pageInRange(pageRanges, i)) {
                    android.graphics.pdf.PdfDocument.PageInfo newPage = new android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth,
                            pageHeight, i).create();

                    PdfDocument.Page page =
                            myPdfDocument.startPage(newPage);

                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        myPdfDocument.close();
                        myPdfDocument = null;
                        return;
                    }
                    drawPage(page, path.get(i));
                    myPdfDocument.finishPage(page);
                }
            }

            try {
                myPdfDocument.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));
            } catch (IOException e) {
                callback.onWriteFailed(e.toString());
                return;
            } finally {
                myPdfDocument.close();
                myPdfDocument = null;
            }

            callback.onWriteFinished(pageRanges);

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean pageInRange(PageRange[] pageRanges, int page) {
        for (PageRange pageRange : pageRanges) {
            if ((page >= pageRange.getStart()) &&
                    (page <= pageRange.getEnd()))
                return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void drawPage(PdfDocument.Page page, String Path) {

        File directory = new File(Path);
        Uri uri = Uri.parse("file://" + directory.getAbsolutePath());
        File file1 = new File(uri.getPath());
        Bitmap bitmap1 = BitmapFactory.decodeFile(file1.getAbsolutePath());
        Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap1,
                page.getCanvas().getWidth(),
                page.getCanvas().getHeight(), true
        );
        int centreX = (page.getCanvas().getWidth() - bitmap2.getWidth()) >> 1;
        int centreY = (page.getCanvas().getWidth() - bitmap2.getWidth()) >> 1;
        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(bitmap2, centreX, centreY, null);
    }



    class LoadImages extends AsyncTask<Void, Void, List<Directory<ImageFiles>>> {

        @SuppressLint("StaticFieldLeak")
        FragmentActivity fragmentActivity;

        public LoadImages(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageFiles.clear();
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

            Cursor data = fragmentActivity.getContentResolver().query(MediaStore.Files.getContentUri("external"),
                    FILE_PROJECTION,
                    selection,
                    selectionArgs,
                    DATE_MODIFIED + " DESC");

            List<Directory<ImageFiles>> directories = new ArrayList<>();
            List<Directory> directories1 = new ArrayList<>();

            if (data.getPosition() != -1) {
                data.moveToPosition(-1);
            }

            int i = 0;
            int position = 0;

            while (data.moveToNext()) {
                //Create a File instance
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

                    imageFiles.add(img);
                }
            }

            return directories;
        }

        @Override
        protected void onPostExecute(List<Directory<ImageFiles>> directories) {
            super.onPostExecute(directories);

            privateList = SharedPrefrence.getHideFileList(ImageViewActivity.this);

            /*imageFiles.clear();
            int finalPosition = 0;
            int count = -1;
            for (int i = 0; i < directories.size(); i++) {
                for (int j = 0; j < directories.get(i).getFiles().size(); j++) {
                    count++;
                    if (directories.get(directoryPosition).getFiles().get(position).getPath().equals(directories.get(i).getFiles().get(j).getPath())) {
                        Log.e("LLLL_Path: ", directories.get(directoryPosition).getFiles().get(position).getPath() + "  " + directories.get(i).getFiles().get(j).getPath() + " j " + j + " count: " + count);
                        finalPosition = count;
                    }
                }
                imageFiles.addAll(directories.get(i).getFiles());
            }*/
//
            int finalPosition1 = position;
            Log.e(TAG, "LLL_onPostExecute: " + "done " + imageFiles.size() + "  " + finalPosition1);
            fragmentActivity.runOnUiThread(() -> {
                albumViewPager = new AlbumViewPager(ImageViewActivity.this, imageFiles, directoryPosition);

                imageViewBinding.imgViewPager.setAdapter(albumViewPager);
                imageViewBinding.imgViewPager.setCurrentItem(finalPosition1);

                ImageFiles imageFile = imageFiles.get(finalPosition1);

                imageViewBinding.setImageFiles(imageFile);

                imageViewBinding.tvDate.setText(imageFile.getDate());

                // File Information
                imageViewBinding.tvDate.setText(imageFile.getDate());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imageFile.getPath(), options);
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;
                imageViewBinding.tvResolution.setText(imageWidth + " x " + imageHeight);
                imageViewBinding.tvSize.setText(Util.getFileSize(imageFile.getSize()));

                ArrayList<String> favList = SharedPrefrence.getFavouriteFileList(ImageViewActivity.this);
                imageViewBinding.imgFav.setSelected(favList.contains(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath()));

                imageViewBinding.imgFav.setOnClickListener(v -> {
                    ArrayList<String> favList1 = SharedPrefrence.getFavouriteFileList(ImageViewActivity.this);
                    if (v.isSelected()) {
                        imageViewBinding.imgFav.setSelected(false);
                        favList1.remove(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());
                    } else {
                        imageViewBinding.imgFav.setSelected(true);
                        favList1.add(imageFiles.get(imageViewBinding.imgViewPager.getCurrentItem()).getPath());
                    }
                    SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, new ArrayList<>());
                    SharedPrefrence.setFavouriteFileList(ImageViewActivity.this, favList1);
                });

                imageViewBinding.imgViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        ImageFiles imageFile = imageFiles.get(position);

                        imageViewBinding.setImageFiles(imageFile);

                        imageViewBinding.tvDateTime.setText(imageFile.getDate());

                        imageViewBinding.imgFav.setSelected(SharedPrefrence.getFavouriteFileList(ImageViewActivity.this).contains(imageFile.getPath()));

                        // File Information
                        imageViewBinding.tvDate.setText(imageFile.getDate());
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(imageFile.getPath(), options);
                        int imageHeight = options.outHeight;
                        int imageWidth = options.outWidth;
                        imageViewBinding.tvResolution.setText(imageWidth + " x " + imageHeight);
                        imageViewBinding.tvSize.setText(Util.getFileSize(imageFile.getSize()));
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

            });
        }

    }

    class LoadAlbumImages extends AsyncTask<Void, Void, List<Directory<ImageFiles>>> {

        @SuppressLint("StaticFieldLeak")
        FragmentActivity fragmentActivity;

        public LoadAlbumImages(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dateFiles.clear();
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
            }

            return directories;
        }

        @Override
        protected void onPostExecute(List<Directory<ImageFiles>> directories) {
            super.onPostExecute(directories);
            Log.d(TAG, "onPostExecute: " + "done");

            fragmentActivity.runOnUiThread(() -> {
                createAlbumAdapter.clearData();
                dateFiles = directories;
                createAlbumAdapter.addAll(directories);
            });
        }
    }
}
