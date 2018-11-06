package com.latina.capturetool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * MobileCaptureTool
 * Class: GalleryAdapter
 * Created by Yoon on 2018-11-06.
 * <p>
 * Description:
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {
    List<ImageVO> imgList = new ArrayList<>();
    Context context;
    int deviceWidth;

    public GalleryAdapter(Context context) {
        this.context = context;
        deviceWidth = context.getResources().getDisplayMetrics().widthPixels;
    }
    @Override
    public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_image, parent, false);
        return new GalleryHolder(v);
    }

    @Override
    public void onBindViewHolder(GalleryHolder holder, int position) {
        final ImageVO image = imgList.get(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(deviceWidth / 3 - 30, deviceWidth / 3 - 30);
        holder.imageView.setLayoutParams(params);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CaptureActivity.class);
                intent.putExtra("image", image.path);
                context.startActivity(intent);
            }
        });
        Glide.with(context).load(image.path)
                .apply(new RequestOptions()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(deviceWidth / 3, deviceWidth / 4))
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }
    public void setImgList(List<ImageVO> imgList) {
        for(int i=imgList.size() - 1; i>=0; i--)
            this.imgList.add(imgList.get(i));
    }
}

class GalleryHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    public GalleryHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
    }
}
class GalleryDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(10,10,10,10);
        ViewCompat.setElevation(view, 25.0f);
    }
}
