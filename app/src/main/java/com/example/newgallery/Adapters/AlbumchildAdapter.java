package com.example.newgallery.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.example.newgallery.activity.AlbumviewActivity;
import com.example.newgallery.databinding.ImageLayoutBinding;
import com.example.newgallery.modelclass.ImageFiles;
import com.example.newgallery.utils.Util;

import java.util.List;

public class AlbumchildAdapter extends RecyclerView.Adapter<AlbumchildAdapter . viewHolder> {

    List<ImageFiles> images;
    Activity activity;
    int direPos = 0;

    public AlbumchildAdapter(List<ImageFiles> images, Activity activity, int i) {
        this.images = images;
        this.activity = activity;
        this.direPos = i;
    }
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageLayoutBinding imageLayoutBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.image_layout, parent, false);
        ViewGroup.LayoutParams params = imageLayoutBinding.getRoot().getLayoutParams();
        if (params != null) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            params.height = width / Util.COLUMN_TYPE;
        }
        return new viewHolder(imageLayoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        ImageFiles file = images.get(position);

        holder.imageLayoutBinding.pImage.setClipToOutline(true);

        RequestOptions options = new RequestOptions();
        if (file.getPath().endsWith(".PNG") || file.getPath().endsWith(".png")) {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW)
                            .format(DecodeFormat.PREFER_ARGB_8888))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.imageLayoutBinding.pImage);
        } else {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.imageLayoutBinding.pImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(activity, AlbumviewActivity.class);
            intent.putExtra("Position", position);
            intent.putExtra("DirPos", direPos);
            intent.putExtra("from", "ImageFragment");
            activity.startActivity(intent);

        });

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        private final ImageLayoutBinding imageLayoutBinding;

        public viewHolder(ImageLayoutBinding imageLayoutBinding) {
            super(imageLayoutBinding.getRoot());
            this.imageLayoutBinding = imageLayoutBinding;
        }
    }
}
