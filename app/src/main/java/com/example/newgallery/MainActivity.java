package com.example.newgallery;

import androidx.annotation.NonNull;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;


import com.example.newgallery.Prefrence.PrefrenceMabager;
import com.example.newgallery.activity.FavouriteActivity;
import com.example.newgallery.activity.RecyclebinActivity;
import com.example.newgallery.databinding.ActivityMainBinding;
import com.example.newgallery.fragment.AlbumFragment;
import com.example.newgallery.fragment.PhotosFragment;
import com.example.newgallery.fragment.PrivacyFragment;
import com.example.newgallery.utils.PermissioinUtils;
import com.example.newgallery.utils.Util;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding ;
    MyClickHandlers myClickHandlers;
    private AppUpdateManager appUpdateManager;
    private static final int UPDATE_REQUEST_CODE = 101;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    PrefrenceMabager prefrenceMabager ;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil .setContentView(this ,R.layout.activity_main);

        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(result -> {
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(result, MainActivity.this,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).setAllowAssetPackDeletion(true).build(),
                            UPDATE_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });



        myClickHandlers = new MyClickHandlers(MainActivity.this);
        binding.setMoreBtnClick(myClickHandlers);

    }
    private  void startApp(){


        binding.tabLayout.getTabAt(0).setText("Photos");
        binding.tabLayout.getTabAt(1).setText("Albums");
        binding.tabLayout.getTabAt(2).setText("Private");

        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final TabsLayoutAdapter tabsLayoutAdapter = new TabsLayoutAdapter(this,getSupportFragmentManager(), binding.tabLayout.getTabCount());
        binding.content.viewPager.setAdapter(tabsLayoutAdapter);

        binding .content.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding .tabLayout));

        if (binding.content.viewPager.getCurrentItem() == 0) {
            binding.rlViewType.setVisibility(View.GONE);
        } else {
            binding.rlViewType.setVisibility(View.VISIBLE);
        }


        binding .tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding .content.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        binding.content.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (binding.cvMainMenu.getVisibility() == View.VISIBLE)
                    binding.cvMainMenu.setVisibility(View.GONE);
                if (binding.cvViewType.getVisibility() == View.VISIBLE)
                    binding.cvViewType.setVisibility(View.GONE);
                if (binding.cvColumn.getVisibility() == View.VISIBLE)
                    binding.cvColumn.setVisibility(View.GONE);
                layGone();
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    binding.rlSorting.setVisibility(View.VISIBLE);
                } else {
                    binding.rlSorting.setVisibility(View.GONE);
                }
                if (position == 0) {
                    binding.rlViewType.setVisibility(View.GONE);
                } else {
                    binding.rlViewType.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



    }
    @SuppressLint("NewApi")
    private void runtimePermission(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PermissioinUtils.neverAskAgainSelected(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    displayNeverAskAgainDialog();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE );
                }
            }

        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            startApp();

        }
    }
    private void displayNeverAskAgainDialog() {

        Intent intent = new Intent();
        intent.setAction( Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               startApp();
            } else {
                PermissioinUtils.setShouldShowStatus(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void onResume() {
        super.onResume();

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(result -> {
            if (result.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // If an in-app update is already running, resume the update.
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            result,
                            AppUpdateType.IMMEDIATE,
                            MainActivity.this,
                            UPDATE_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
       runtimePermission();



    }


    private void layVisible() {
        binding.rlOne.setVisibility(View.VISIBLE);
        binding.content.rlTwo.setVisibility(View.VISIBLE);
    }

    private void layGone() {
        binding.rlOne.setVisibility(View.GONE);
        binding.content.rlTwo.setVisibility(View.GONE);
    }

    private void sortingPopUp() {
        String[] radio1 = {getResources().getString(R.string.no_of_photos),
                getResources().getString(R.string.last_modified_date),
                getResources().getString(R.string.name)};

        String[] radio2 = {getResources().getString(R.string.ascending),
                getResources().getString(R.string.descending)};

        final Dialog dial = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dial.requestWindowFeature(1);
        dial.setContentView(R.layout.sorting_dialog);
        dial.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dial.setCanceledOnTouchOutside(true);

        RadioGroup rg_sorting1 = dial.findViewById(R.id.rg_sorting1);
        RadioGroup rg_sorting2 = dial.findViewById(R.id.rg_sorting2);

        for (int i = 0; i < radio1.length; i++) {
            String sorting1 = radio1[i];
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30, 30, 7, 30);
            radioButton.setText(sorting1);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            radioButton.setTextColor(getResources().getColor(R.color.textview_color));
            radioButton.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.radio_button)));
            radioButton.setId(i);

            if (Util.SORTING_TYPE2.equals("")) {
                if (i == 1) {
                    radioButton.setChecked(true);
                    Util.SORTING_TYPE = radioButton.getText().toString();
                }
            } else if (radioButton.getText().equals(Util.SORTING_TYPE)) {
                radioButton.setChecked(true);
            }

            rg_sorting1.addView(radioButton);

        }

        //set listener to radio button group
        rg_sorting1.setOnCheckedChangeListener((group, checkedId) -> {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton radioBtn = dial.findViewById(checkedRadioButtonId);
            Util.SORTING_TYPE = radioBtn.getText().toString();
            Log.e("LLLLL_Refill_time: ", Util.SORTING_TYPE);
        });

        for (int i = 0; i < radio2.length; i++) {
            String sorting2 = radio2[i];
            RadioButton radioButton = new RadioButton(this);
            radioButton.setPadding(30, 30, 7, 30);
            radioButton.setText(sorting2);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
            radioButton.setTextColor(getResources().getColor(R.color.textview_color));
            radioButton.setButtonTintList(ColorStateList.valueOf(getResources().getColor(R.color.radio_button)));
            radioButton.setId(i + radio1.length);

            if (Util.SORTING_TYPE2.equals("")) {
                if (i == 0) {
                    radioButton.setChecked(true);
                    Util.SORTING_TYPE2 = radioButton.getText().toString();
                }
            } else if (radioButton.getText().equals(Util.SORTING_TYPE2)) {
                radioButton.setChecked(true);
            }

            rg_sorting2.addView(radioButton);

        }

        //set listener to radio button group
        rg_sorting2.setOnCheckedChangeListener((group, checkedId) -> {
            int checkedRadioButtonId = group.getCheckedRadioButtonId();
            RadioButton radioBtn = dial.findViewById(checkedRadioButtonId);
            Util.SORTING_TYPE2 = radioBtn.getText().toString();
            Log.e("LLLLL_Refill_time: ", Util.SORTING_TYPE2);
        });

        dial.findViewById(R.id.tv_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dial.dismiss();
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
                Intent localIn = new Intent("TAG_REFRESH");
                lbm.sendBroadcast(localIn);
            }
        });

        dial.show();
    }
    @Override
    public void onBackPressed() {
        if (binding.cvMainMenu.getVisibility() == View.VISIBLE)
            binding.cvMainMenu.setVisibility(View.GONE);
        else if (binding.cvViewType.getVisibility() == View.VISIBLE)
            binding.cvViewType.setVisibility(View.GONE);
        else if (binding.cvColumn.getVisibility() == View.VISIBLE)
            binding.cvColumn.setVisibility(View.GONE);
        else
            super.onBackPressed();
        layGone();
    }

    public class MyClickHandlers {
        Context context;

        public MyClickHandlers(Context context) {
            this.context = context;
        }

        public void onMoreBtnClicked(View view) {
            if (binding.cvMainMenu.getVisibility() == View.VISIBLE)
                binding.cvMainMenu.setVisibility(View.GONE);
            else
                binding.cvMainMenu.setVisibility(View.VISIBLE);
            layVisible();

        }

        public void onViewType(View view) {
            if (binding.cvMainMenu.getVisibility() == View.VISIBLE)
                binding.cvMainMenu.setVisibility(View.GONE);
            if (binding.cvViewType.getVisibility() == View.GONE)
                binding.cvViewType.setVisibility(View.VISIBLE);
            layVisible();
        }

        public void onColumnType(View view) {
            if (binding.cvMainMenu.getVisibility() == View.VISIBLE)
                binding.cvMainMenu.setVisibility(View.GONE);
            if (binding.cvColumn.getVisibility() == View.GONE)
                binding.cvColumn.setVisibility(View.VISIBLE);
            layVisible();
        }

        public void onColumn2Click(View view) {
            if (binding.cvColumn.getVisibility() == View.VISIBLE)
                binding.cvColumn.setVisibility(View.GONE);
            layGone();
            Util.COLUMN_TYPE = 2;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
            Intent localIn = new Intent("TAG_REFRESH");
            lbm.sendBroadcast(localIn);
        }

        public void onColumn3Click(View view) {
            if (binding.cvColumn.getVisibility() == View.VISIBLE)
                binding.cvColumn.setVisibility(View.GONE);
            layGone();
            Util.COLUMN_TYPE = 3;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
            Intent localIn = new Intent("TAG_REFRESH");
            lbm.sendBroadcast(localIn);
        }

        public void onColumn4Click(View view) {
            if (binding.cvColumn.getVisibility() == View.VISIBLE)
                binding.cvColumn.setVisibility(View.GONE);
            layGone();
            Util.COLUMN_TYPE = 4;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
            Intent localIn = new Intent("TAG_REFRESH");
            lbm.sendBroadcast(localIn);
        }

        public void listClick(View view) {
            if (binding.cvViewType.getVisibility() == View.VISIBLE)
                binding.cvViewType.setVisibility(View.GONE);
            layGone();
            Util.isList = true;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
            Intent localIn = new Intent("TAG_REFRESH");
            lbm.sendBroadcast(localIn);
        }

        public void gridClick(View view) {
            if (binding.cvViewType.getVisibility() == View.VISIBLE)
                binding.cvViewType.setVisibility(View.GONE);
            layGone();
            Util.isList = false;
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(MainActivity.this);
            Intent localIn = new Intent("TAG_REFRESH");
            lbm.sendBroadcast(localIn);
        }

        public void onFavouriteClick(View view) {
            if (binding.cvMainMenu.getVisibility() == View.VISIBLE)
                binding.cvMainMenu.setVisibility(View.GONE);
            layGone();
            Intent intent = new Intent(MainActivity.this, FavouriteActivity.class);
            startActivity(intent);
        }


        public void onRecycleBinClick(View view) {
            if (binding.cvMainMenu.getVisibility() == View.VISIBLE)
                binding.cvMainMenu.setVisibility(View.GONE);
            layGone();
            Intent intent = new Intent(MainActivity.this, RecyclebinActivity.class);
            startActivity(intent);

        }

        public void onSortingClick(View view) {
            if (binding.cvMainMenu.getVisibility() == View.VISIBLE)
                binding.cvMainMenu.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                sortingPopUp();
                layGone();
            }
        }
    }

    public class TabsLayoutAdapter extends FragmentPagerAdapter {

        private Context myContext;
        int totalTabs;

        public TabsLayoutAdapter(Context context, FragmentManager fm, int totalTabs) {
            super(fm);
            myContext = context;
            this.totalTabs = totalTabs;
        }

        // this is for fragment tabs
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    PhotosFragment photosFragment = new PhotosFragment();
                    return photosFragment;
                case 1:
                    AlbumFragment albumFragment = new AlbumFragment();
                    return albumFragment;
                case 2:
                    PrivacyFragment privacyFragment = new PrivacyFragment();
                    return privacyFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return totalTabs;
        }
    }


}