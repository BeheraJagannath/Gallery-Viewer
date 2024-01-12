package com.example.newgallery.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
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
import com.example.newgallery.activity.AlbumviewActivity;

import com.example.newgallery.databinding.AlbumListBinding;
import com.example.newgallery.modelclass.ImageFiles;
import com.example.newgallery.utils.Util;

import java.util.List;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.viewHolders> {
    List<ImageFiles> images;
    Activity activity;
    int direPos = 0;

    public AlbumDetailsAdapter(List<ImageFiles> images, Activity activity, int direPos) {
        this.images = images;
        this.activity = activity;
        this.direPos = direPos;
    }

    @NonNull
    @Override
    public AlbumDetailsAdapter.viewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AlbumListBinding listBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.album_list, parent, false);
        return new viewHolders(listBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumDetailsAdapter.viewHolders holder, int position) {
        ImageFiles file = images.get(position);

        holder.listBinding.setImageFile(file);

        holder.listBinding.imgAlbum.setClipToOutline(true);
        holder.listBinding.tvImageSize.setText(Util.getFileSize(file.getSize()));

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
                    .into(holder.listBinding.imgAlbum);
        } else {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.listBinding.imgAlbum);
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

    public class viewHolders extends RecyclerView.ViewHolder {

        private final AlbumListBinding listBinding;

        public viewHolders(AlbumListBinding listBinding) {
            super(listBinding.getRoot());

            this.listBinding = listBinding;
        }
    }
}
