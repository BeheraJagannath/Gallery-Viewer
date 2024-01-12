package com.example.newgallery.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.newgallery.R;
import com.example.newgallery.databinding.AlbumItemBinding;
import com.example.newgallery.databinding.AlbumListBinding;
import com.example.newgallery.modelclass.Directory;
import com.example.newgallery.modelclass.ImageFiles;
import com.example.newgallery.modelclass.ItemClicklistener;
import com.example.newgallery.utils.Util;

import java.util.List;

public class CreateAlbumAdapter extends RecyclerView.Adapter<CreateAlbumAdapter.MyClassView> {

    private static final String TAG = CreateAlbumAdapter.class.getSimpleName();
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private final ItemClicklistener mItemClickListener;
    List<Directory<ImageFiles>> files;
    Activity activity;

    public CreateAlbumAdapter(List<Directory<ImageFiles>> files, Activity activity, ItemClicklistener itemClickListener) {
        this.files = files;
        this.activity = activity;
        this.mItemClickListener = itemClickListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        Log.e("LLLL_Name: ", files.get(position).getName());
        Directory<ImageFiles> filename = files.get(position);
        holder.albumItemBinding.setDirectory(filename);

        ImageFiles file = filename.getFiles().get(0);

        holder.albumItemBinding.imgAlbum.setClipToOutline(true);
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
                    .into(holder.albumItemBinding.imgAlbum);
        } else {
            Glide.with(activity)
                    .load(file.getPath())
                    .apply(options.centerCrop()
                            .skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(holder.albumItemBinding.imgAlbum);
        }

        holder.itemView.setOnClickListener(v -> {

            if (mItemClickListener != null) {
                mItemClickListener.onItemclick(position);
            }
        });
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AlbumItemBinding albumItemBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.album_item, parent, false);
        return new MyClassView(albumItemBinding);
    }

    @Override
    public int getItemCount() {
        Log.e("LLL_Size: ", String.valueOf(files.size()));
        return files.size();
    }

    public void addAll(List<Directory<ImageFiles>> imgMain1DownloadList) {
        this.files.addAll(imgMain1DownloadList);
        activity.runOnUiThread(() -> notifyDataSetChanged());
    }

    public void clearData() {
        this.files.clear();
        notifyDataSetChanged();
    }

//    Define your Interface method here
//    public interface ItemClickListener {
//        void onItemClick(int position);
//    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final AlbumItemBinding albumItemBinding;

        public MyClassView(AlbumItemBinding albumItemBinding) {
            super(albumItemBinding.getRoot());

            this.albumItemBinding = albumItemBinding;
        }
    }
}
