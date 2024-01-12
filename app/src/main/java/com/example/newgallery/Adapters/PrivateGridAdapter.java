package com.example.newgallery.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;

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
import com.example.newgallery.databinding.ImageLayoutBinding;
import com.example.newgallery.utils.Util;

import java.util.ArrayList;

public class PrivateGridAdapter extends RecyclerView .Adapter<PrivateGridAdapter .ViewHolser> {
    ArrayList<String> pathList;
    Activity activity;
    public PrivateGridAdapter(ArrayList<String> pathList, Activity activity) {
        this.pathList = pathList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public PrivateGridAdapter.ViewHolser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageLayoutBinding imageLayoutBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.image_layout, parent, false);
        ViewGroup.LayoutParams params = imageLayoutBinding.getRoot().getLayoutParams();
        if (params != null) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            params.height = width / Util.COLUMN_TYPE;
        }
        return new ViewHolser(imageLayoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PrivateGridAdapter.ViewHolser holder, int position) {

        String filePath = pathList.get(position);

        holder.imageLayoutBinding.pImage.setClipToOutline(true);

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
                    .into(holder.imageLayoutBinding.pImage);
        } else {
            Glide.with(activity)
                    .load(filePath)
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.imageLayoutBinding.pImage);
        }

        holder.imageLayoutBinding.getRoot().setOnClickListener(v -> {
//            fireAnalytics(PrivateImageAdapter.class.getSimpleName(), "PrivateImage", "Open");
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

    public void addAll(ArrayList<String> pathList) {
        this.pathList = new ArrayList<>();
        this.pathList = pathList;
        notifyDataSetChanged();
    }

    public void clearData() {
        this.pathList.clear();
        notifyDataSetChanged();
    }

    public class ViewHolser extends RecyclerView.ViewHolder {

        private final ImageLayoutBinding imageLayoutBinding ;

        public ViewHolser(ImageLayoutBinding imageLayoutBinding) {
            super(imageLayoutBinding.getRoot());
            this.imageLayoutBinding = imageLayoutBinding ;
        }
    }
}
