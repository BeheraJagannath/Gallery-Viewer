package com.example.newgallery.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.newgallery.Adapters.FavListAdapter;
import com.example.newgallery.Adapters.FavouriteGridAdapter;
import com.example.newgallery.MainActivity;
import com.example.newgallery.Prefrence.SharedPrefrence;
import com.example.newgallery.R;
    import com.example.newgallery.databinding.ActivityFavouriteBinding;
    import com.example.newgallery.utils.Util;

import java.util.ArrayList;

public class FavouriteActivity extends AppCompatActivity {
    public ArrayList<String> pathList = new ArrayList<>();

    ActivityFavouriteBinding favouriteBinding ;
    FavouriteGridAdapter favouriteGridAdapter ;
    FavListAdapter favListAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        favouriteBinding = DataBindingUtil .setContentView(this , R.layout.activity_favourite);

        favouriteBinding . favBack .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity ( new Intent(FavouriteActivity .this, MainActivity .class ));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        pathList = SharedPrefrence.getFavouriteFileList(FavouriteActivity.this );

        if (pathList.size() == 0) {
            favouriteBinding.llNoData.setVisibility(View.VISIBLE);
        } else {
            favouriteBinding.llNoData.setVisibility(View.GONE);
        }

        if (!Util.isList) {
            final GridLayoutManager layoutManager = new GridLayoutManager(FavouriteActivity.this, Util.COLUMN_TYPE);
            favouriteBinding.favRecycler.setLayoutManager(layoutManager);
            favouriteGridAdapter = new FavouriteGridAdapter(pathList, FavouriteActivity.this);
            favouriteBinding.favRecycler.setAdapter(favouriteGridAdapter);
        } else {
            final LinearLayoutManager layoutManager = new LinearLayoutManager(FavouriteActivity.this, RecyclerView.VERTICAL, false);
            favouriteBinding.favRecycler.setLayoutManager(layoutManager);
            favListAdapter = new FavListAdapter(pathList, FavouriteActivity.this);
            favouriteBinding.favRecycler.setAdapter(favListAdapter);
        }
    }
}