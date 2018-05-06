package com.example.huseyincengiz.instagramclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.huseyincengiz.instagramclone.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by HuseyinCengiz on 1.12.2017.
 */

public class UniversalImageLoader {
    private final static int defaultImage= R.drawable.ic_android;
    private Context mContext;

    public UniversalImageLoader(Context mContext) {
        this.mContext = mContext;
    }

    /*We set up General ImageLoader Options*/
    public ImageLoaderConfiguration getConfig()
    {
        DisplayImageOptions defaultOptions=new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultImage)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .considerExifParams(true)
                .cacheOnDisk(true).cacheInMemory(true)
                .resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();
        ImageLoaderConfiguration configuration=new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100*1024*1024).build();
        return  configuration;
    }

    /**
     * Bu metod statik olan resimleri gostermek icin kullanılır.Eğer resimler aktivitede ve ya
     * Fragmentte değiştiriliyorsa ya da Liste ve ya Grid olarak basılıyorsa bu metot kullanılmaz.
     *
     * This method can be used to set images that are static. It can't be used if the images are
     * being changed in Activity/Fragment or They are being set in a list or a grid
     * @param imgURL
     * @param imageview
     * @param progressBar
     * @param append
     */
    public static void SetImage(String imgURL, ImageView imageview, final ProgressBar progressBar,String append)
    {
        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, imageview, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(progressBar!=null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(progressBar!=null) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(progressBar!=null) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(progressBar!=null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
