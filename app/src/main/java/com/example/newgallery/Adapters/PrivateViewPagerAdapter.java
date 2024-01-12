package com.example.newgallery.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.newgallery.R;
import com.example.newgallery.databinding.ImageSlideBinding;
import com.example.newgallery.modelclass.ImageFiles;

import java.util.ArrayList;
import java.util.List;

public class PrivateViewPagerAdapter extends PagerAdapter {

    private final LayoutInflater inflater;
    private final Activity activity;
    ArrayList<String> pathList;
    private String from = "";

    public PrivateViewPagerAdapter(Activity activity, ArrayList<String> pathList, String from) {
        this.activity = activity;
        this.pathList = pathList;
        this.from = from;
        inflater = LayoutInflater.from(activity);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public int getCount() {
        return pathList.size();
    }


    @Override
    public Object instantiateItem(ViewGroup view, int position) {

        ImageSlideBinding imageSlideBinding =
                DataBindingUtil.inflate(inflater, R.layout.image_slide, view, false);

        String filePath = pathList.get(position);

        RequestOptions options = new RequestOptions();
        if (filePath.endsWith(".PNG") || filePath.endsWith(".png")) {
            Glide.with(activity)
                    .load(filePath)
                    .apply(options.skipMemoryCache(true)
                            .priority(Priority.LOW)
                            .format(DecodeFormat.PREFER_ARGB_8888))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(imageSlideBinding.slideImage);
        } else {
            Glide.with(activity)
                    .load(filePath)
                    .apply(options.skipMemoryCache(true)
                            .priority(Priority.LOW))
                    .transition(withCrossFade())
                    .transition(new DrawableTransitionOptions().crossFade(500))
                    .into(imageSlideBinding.slideImage);
        }

        view.addView(imageSlideBinding.getRoot(), 0);
        return imageSlideBinding.getRoot();
    }

    public void addAll(List<ImageFiles> imgMain1DownloadList) {
        notifyDataSetChanged();
    }
}
