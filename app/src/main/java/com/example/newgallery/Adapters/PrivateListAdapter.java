package com.example.newgallery.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.newgallery.R;
import com.example.newgallery.activity.PrivateViewActivity;
import com.example.newgallery.databinding.FavListBinding;
import com.example.newgallery.utils.Util;

import java.io.File;
import java.util.ArrayList;

public class PrivateListAdapter  extends RecyclerView .Adapter<PrivateListAdapter.ViewHolder> {

    ArrayList<String> pathList;
    Activity activity;
    public PrivateListAdapter(ArrayList<String> pathList, Activity activity) {
        this.pathList = pathList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PrivateListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FavListBinding favListBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.fav_list, parent, false);
        return new ViewHolder(favListBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull PrivateListAdapter.ViewHolder holder, int position) {
        String filePath = pathList.get(position);

        holder.favListBinding.favImage.setClipToOutline(true);
        File newFile = new File(filePath);
        holder.favListBinding.favName.setText(newFile.getName());
        holder.favListBinding.favDate.setText(Util.convertTimeDateModifiedShown(newFile.lastModified()));
        holder.favListBinding.favSize.setText(Util.getFileSize(newFile.length()));

        RequestOptions options = new RequestOptions();
        if (filePath.endsWith(".PNG") || filePath.endsWith(".png")) {
            Glide.with(activity)
                    .load(filePath)
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW)
                            .format(DecodeFormat.PREFER_ARGB_8888))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.favListBinding.favImage);
        } else {
            Glide.with(activity)
                    .load(filePath)
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.favListBinding.favImage);
        }

        holder.itemView.setOnClickListener(v -> {
//            fireAnalytics(PrivateListImageAdapter.class.getSimpleName(), "ListPrivateImage", "Open");
            Intent intent = new Intent(activity, PrivateViewActivity.class);
            intent.putExtra("Position", position);
            intent.putExtra("from", "PrivateImageFragment");
            intent.putExtra("directoryPosition", 0);
            activity.startActivity(intent);
            activity.finish();
        });

    }

    @Override
    public int getItemCount() {
        return pathList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

         FavListBinding favListBinding;

        public ViewHolder(FavListBinding favListBinding) {
            super(favListBinding.getRoot());

            this.favListBinding = favListBinding;
        }
    }

}
