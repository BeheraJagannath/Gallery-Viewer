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
import com.example.newgallery.activity.AlbumActivity;
import com.example.newgallery.R;
import com.example.newgallery.databinding.AlbumLayoutBinding;
import com.example.newgallery.modelclass.Directory;
import com.example.newgallery.modelclass.ImageFiles;
import com.example.newgallery.utils.Util;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>{

    private static final String TAG = AlbumAdapter.class.getSimpleName();
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    List<Directory<ImageFiles>> albumFiles;
    Activity activity;

    public AlbumAdapter(List<Directory<ImageFiles>> albumFiles, Activity activity) {
        this.albumFiles = albumFiles;
        this.activity = activity;
    }
    @NonNull
    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AlbumLayoutBinding albumLayoutBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.album_layout, parent, false);
        ViewGroup.LayoutParams params = albumLayoutBinding.getRoot().getLayoutParams();
        if (params != null) {
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            params.height = width / Util.COLUMN_TYPE;
        }
        return new ViewHolder ( albumLayoutBinding ) ;
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapter.ViewHolder holder, int position) {
        Directory<ImageFiles> DateName = albumFiles.get(position);
        holder.albumLayoutBinding.setDirectory(DateName);

        ImageFiles file = DateName.getFiles().get(0);

        holder.albumLayoutBinding.albumImage.setClipToOutline(true);
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
                    .into(holder.albumLayoutBinding.albumImage);
        } else {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.albumLayoutBinding.albumImage);
        }

        holder.albumLayoutBinding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(activity, AlbumActivity.class);
            intent.putExtra("FileName", DateName.getName());
            activity.startActivity(intent);
        });

    }

    public void addAll(List<Directory<ImageFiles>> imgMain1DownloadList) {
        this.albumFiles.addAll(imgMain1DownloadList);
        if (this.albumFiles.size() >= 10)
            notifyItemRangeChanged(this.albumFiles.size() - 10, this.albumFiles.size());
        else
            notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }
    public void clearData() {
        this.albumFiles.clear();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AlbumLayoutBinding albumLayoutBinding;

        public ViewHolder(AlbumLayoutBinding albumLayoutBinding) {
            super(albumLayoutBinding.getRoot());
            this.albumLayoutBinding = albumLayoutBinding;
        }
    }
}
