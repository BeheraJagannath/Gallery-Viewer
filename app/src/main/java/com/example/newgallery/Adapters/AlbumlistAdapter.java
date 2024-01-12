package com.example.newgallery.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.content.Intent;
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
import com.example.newgallery.activity.AlbumActivity;
import com.example.newgallery.R;
import com.example.newgallery.databinding.AlbumViewlayoutBinding;
import com.example.newgallery.modelclass.Directory;
import com.example.newgallery.modelclass.ImageFiles;

import java.util.List;

public class AlbumlistAdapter extends RecyclerView .Adapter<AlbumlistAdapter.ViewHolder> {
    List<Directory<ImageFiles>> files;
    Activity activity;

    public AlbumlistAdapter(List<Directory<ImageFiles>> files, Activity activity) {
        this.files = files;
        this.activity = activity;
    }
    @NonNull
    @Override
    public AlbumlistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(activity).inflate(R.layout.album_viewlayout, parent, false);
        AlbumViewlayoutBinding albumViewlayoutBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.album_viewlayout, parent, false);
        return new ViewHolder(albumViewlayoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumlistAdapter.ViewHolder holder, int position) {

        Directory<ImageFiles> DateName = files.get(position);

        holder.albumViewlayoutBinding.setDirectory(DateName);

        ImageFiles file = DateName.getFiles().get(0);


        holder.albumViewlayoutBinding.imgAlbum.setClipToOutline(true);
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
                    .into(holder.albumViewlayoutBinding.imgAlbum);
        } else {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.albumViewlayoutBinding.imgAlbum);
        }

        holder.albumViewlayoutBinding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(activity, AlbumActivity.class);
            intent.putExtra("FileName", DateName.getName());
            activity.startActivity(intent);
        });

    }
    public void addAll(List<Directory<ImageFiles>> imgMain1DownloadList) {
        this.files.addAll(imgMain1DownloadList);
        if (this.files.size() >= 10)
            notifyItemRangeChanged(this.files.size() - 10, this.files.size());
        else
            notifyDataSetChanged();
    }

    public void clearData() {
        this.files.clear();
    }

    @Override
    public int getItemCount() {

        return files.size() ;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AlbumViewlayoutBinding albumViewlayoutBinding;

        public ViewHolder(AlbumViewlayoutBinding albumViewlayoutBinding) {
            super(albumViewlayoutBinding.getRoot());
            this.albumViewlayoutBinding = albumViewlayoutBinding;
        }
    }
}
