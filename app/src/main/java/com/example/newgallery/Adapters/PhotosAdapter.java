package com.example.newgallery.Adapters;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.newgallery.R;

import com.example.newgallery.activity.ImageViewActivity;
import com.example.newgallery.databinding.DateLayoutBinding;
import com.example.newgallery.databinding.ImageLayoutBinding;
import com.example.newgallery.modelclass.ImageFiles;
import java.util.Calendar;
import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int DATE_TYPE = 1;
    public static final int ITEM = 0;

    public List<ImageFiles> objects;
    Activity activity;

    public PhotosAdapter(List<ImageFiles> files, Activity activity) {
        this.objects = files;
        this.activity = activity;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        if (viewType == DATE_TYPE) {
            DateLayoutBinding layoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),

                    R.layout.date_layout, parent, false);
            holder = new MyClassView(layoutBinding);
        } else {
            ImageLayoutBinding imageLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.image_layout, parent, false);
            holder = new ImageClassView(imageLayoutBinding);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vholder, int position) {
        ImageFiles directory = objects.get(position);
        if (vholder instanceof MyClassView) {

            MyClassView holder = (MyClassView) vholder;

            String dateString = directory.getDateTitle();
            holder.layoutBinding.date.setText(dateString);

        }else{
            ImageClassView holder = (ImageClassView) vholder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.imageLayoutBinding.pImage.setClipToOutline(true);
            }

            RequestOptions options = new RequestOptions();
            if (directory.getPath().endsWith(".PNG") || directory.getPath().endsWith(".png")) {
                Glide.with(activity)
                        .load(directory.getPath())
                        .apply(options.centerCrop()
                                .skipMemoryCache(true)
                                .priority(Priority.LOW)
                                .format(DecodeFormat.PREFER_ARGB_8888))
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(0.5f)
                        .centerCrop()
                        .into(holder.imageLayoutBinding.pImage);
            } else {
                Glide.with(activity)
                        .load(directory.getPath())
                        .apply(options.centerCrop()
                                .skipMemoryCache(true)
                                .priority(Priority.LOW))
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(0.5f)
                        .centerCrop()
                        .into(holder.imageLayoutBinding.pImage);
            }

                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(activity, ImageViewActivity.class);
                    intent.putExtra("Position", position);
                    intent.putExtra("from", "ImageFragment");
                    intent.putExtra("directoryPosition", directory.getPosition());
                    activity.startActivity(intent);
                    activity.finish();
                });



        }

    }

    @Override
    public int getItemCount() {

        return objects.size();
    }

    @Override
    public int getItemViewType(int position) {
        ImageFiles directory = objects.get(position);
        if (!directory.isDirectory()) {
            return ITEM;
        } else {
            return DATE_TYPE;
        }
    }

    public void addAll(List<ImageFiles> imgMain1DownloadList) {
        this.objects.addAll(imgMain1DownloadList);
    }

    public void clear() {
        objects.clear();
        notifyDataSetChanged();
    }
    public String getFormattedDate(long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "dd MMM yyyy";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return "Today ";
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return "Yesterday ";
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("dd MMM yyyy", smsTime).toString();
        }
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        private final DateLayoutBinding layoutBinding;

        public MyClassView(DateLayoutBinding layoutBinding) {
            super(layoutBinding.getRoot());

            this.layoutBinding = layoutBinding;
        }
    }

    public class ImageClassView extends RecyclerView.ViewHolder {

        private final ImageLayoutBinding imageLayoutBinding;

        public ImageClassView(ImageLayoutBinding imageLayoutBinding) {
            super(imageLayoutBinding.getRoot());

            this.imageLayoutBinding = imageLayoutBinding;
        }
    }
}
