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
import com.example.newgallery.activity.RecycleBinviewActivity;
import com.example.newgallery.databinding.FavListBinding;
import com.example.newgallery.modelclass.RecyclerModel;
import com.example.newgallery.utils.Util;

import java.io.File;
import java.util.ArrayList;

public class RecyclerbinListAdapter extends RecyclerView .Adapter<RecyclerbinListAdapter .ViewHolder> {
    ArrayList<RecyclerModel> pathList;
    Activity activity;

    public RecyclerbinListAdapter(ArrayList<RecyclerModel> pathList, Activity activity) {
        this.pathList = pathList;
        this.activity = activity;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FavListBinding favListBinding =
                DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.fav_list, parent, false);

        return new ViewHolder(favListBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecyclerModel recycleModel = pathList.get(position);
        File file = new File(pathList.get(position).getNewImagePath());

//        holder.favListBinding.favName.setText(recycleModel.getImageName());
        holder .favListBinding . favName .setText(file .getName());
        String time = DateFormat.format("dd MMM yyyy hh:mm a", file.lastModified()).toString();
        holder .favListBinding.favDate.setText(time);

        String filePath = recycleModel.getNewImagePath();

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
            Intent intent = new Intent(activity, RecycleBinviewActivity.class);
            intent.putExtra("Position", position);
            activity.startActivity(intent);

        });


    }

    @Override
    public int getItemCount() {
        return pathList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        FavListBinding favListBinding ;

        public ViewHolder( FavListBinding favListBinding ) {
            super( favListBinding.getRoot());

            this.favListBinding = favListBinding ;
        }
    }
}
