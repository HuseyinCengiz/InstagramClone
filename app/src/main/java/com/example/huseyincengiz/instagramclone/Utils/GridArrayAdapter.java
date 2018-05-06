package com.example.huseyincengiz.instagramclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.huseyincengiz.instagramclone.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuseyinCengiz on 2.12.2017.
 */

public class GridArrayAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    public  int layoutResource;
    public String mAppend;
    public List<String> imgURLs;

    public GridArrayAdapter(Context mContext,int layoutResource, List<String> imgURLs,String mAppend) {
        super(mContext, layoutResource, imgURLs);
        this.mContext = mContext;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.layoutResource = layoutResource;
        this.mAppend = mAppend;
        this.imgURLs = imgURLs;
    }

    private static class ViewHolder{
        SquareImageView mPhoto;
        ProgressBar mProgressBar;

        public ViewHolder(SquareImageView mPhoto, ProgressBar mProgressBar) {
            this.mPhoto = mPhoto;
            this.mProgressBar = mProgressBar;
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView==null)
        {
            convertView=mLayoutInflater.inflate(layoutResource,parent,false);
            SquareImageView gridImage=convertView.findViewById(R.id.gridImageView);
            ProgressBar gridProgress=convertView.findViewById(R.id.gridProgressBar);
            holder=new ViewHolder(gridImage,gridProgress);
            convertView.setTag(holder);
        }
        else
        {
            holder= (ViewHolder)convertView.getTag();
        }
        String mUrl=imgURLs.get(position);
        ImageLoader imgLoader=ImageLoader.getInstance();
        imgLoader.displayImage(mAppend + mUrl, holder.mPhoto, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(holder.mProgressBar!=null)
                {
                    holder.mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(holder.mProgressBar!=null)
                {
                    holder.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(holder.mProgressBar!=null)
                {
                    holder.mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(holder.mProgressBar!=null)
                {
                    holder.mProgressBar.setVisibility(View.GONE);
                }
            }
        });
        return convertView;
    }
}
