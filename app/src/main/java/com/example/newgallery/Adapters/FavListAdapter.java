package com.example.newgallery.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.content.Intent;
import android.text.format.DateFormat;
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
import com.example.newgallery.activity.FavouriteViewerActivity;
import com.example.newgallery.databinding.FavListBinding;
import com.example.newgallery.utils.Util;

import java.io.File;
import java.util.ArrayList;

public class FavListAdapter extends RecyclerView . Adapter<FavListAdapter.ViewHolder> {

    ArrayList<String> favChild;
    Activity activity;

    public FavListAdapter(ArrayList<String> favChild, Activity activity) {
        this.favChild = favChild;
        this.activity = activity;
    }
    @NonNull
    @Override
    public FavListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FavListBinding favListBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.fav_list, parent, false);

        return new ViewHolder(favListBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavListAdapter.ViewHolder holder, int position ) {

        String filePath = favChild.get(position);

        holder.favListBinding.favImage.setClipToOutline(true);
        File newFile = new File(filePath);
        String time = DateFormat.format("dd MMM yyyy hh:mm a", newFile.lastModified()).toString();
        holder .favListBinding.favDate .setText(time);

        holder.favListBinding.favName.setText(newFile.getName());
//        holder.favListBinding.favDate.setText(Util.convertTimeDateModifiedShown(newFile.lastModified()));
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
            Intent intent = new Intent(activity, FavouriteViewerActivity.class);
            intent.putExtra("Position", position);
            intent.putExtra("from", "FavImageFragment");
            intent.putExtra("directoryPosition", 0);
            activity.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return favChild.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        FavListBinding favListBinding ;

        public ViewHolder( FavListBinding favListBinding ) {
            super( favListBinding.getRoot());

            this.favListBinding = favListBinding ;
        }
    }
}
