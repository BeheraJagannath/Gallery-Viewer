package com.example.newgallery.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.newgallery.Adapters.RecyclerbinGridAdapter;
import com.example.newgallery.Adapters.RecyclerbinListAdapter;
import com.example.newgallery.Prefrence.SharedPrefrence;
import com.example.newgallery.R;
import com.example.newgallery.databinding.ActivityRecyclebinBinding;
import com.example.newgallery.modelclass.RecyclerModel;
import com.example.newgallery.utils.Util;

import java.util.ArrayList;
import java.util.Collections;

public class RecyclebinActivity extends AppCompatActivity {
    ActivityRecyclebinBinding recyclebinBinding ;
    public ArrayList < RecyclerModel > pathList = new ArrayList<>();
    RecyclerbinGridAdapter recyclerbinGridAdapter ;
    RecyclerbinListAdapter recyclerbinListAdapter ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclebin);
        recyclebinBinding = DataBindingUtil .setContentView ( this,R.layout.activity_recyclebin );

        recyclebinBinding.recyclerbinBack.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        pathList = SharedPrefrence.getRecycleBinData(RecyclebinActivity.this );
        Log.e("LLL_size: ", String.valueOf(pathList.size()));

        if ( pathList.size() == 0 ) {
            recyclebinBinding.llNoData.setVisibility( View.VISIBLE );
        } else {
            recyclebinBinding.llNoData.setVisibility( View.GONE );
        }
        if (!Util.isList) {
            final GridLayoutManager layoutManager = new GridLayoutManager(RecyclebinActivity.this, Util.COLUMN_TYPE );
            recyclebinBinding.recyclerBin.setLayoutManager( layoutManager );
            recyclerbinGridAdapter = new RecyclerbinGridAdapter( pathList, RecyclebinActivity.this );
            recyclebinBinding.recyclerBin.setAdapter(recyclerbinGridAdapter);
        } else {
            final LinearLayoutManager layoutManager = new LinearLayoutManager(RecyclebinActivity.this, RecyclerView.VERTICAL, false );
            recyclebinBinding.recyclerBin.setLayoutManager( layoutManager );
            recyclerbinListAdapter = new RecyclerbinListAdapter( pathList, RecyclebinActivity.this );
            recyclebinBinding.recyclerBin.setAdapter( recyclerbinListAdapter );
        }

    }
}