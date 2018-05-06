package com.example.huseyincengiz.instagramclone.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by HuseyinCengiz on 30.03.2018.
 */

public class ImageManager {
    private static final String TAG = "ImageManager";

    public static Bitmap getBitmap(String imgUrl){
        File file=new File(imgUrl);
        FileInputStream fis=null;
        Bitmap bmp=null;
        try{
            fis=new FileInputStream(file);
            bmp= BitmapFactory.decodeStream(fis);

        }catch (FileNotFoundException e)
        {
            Log.d(TAG, "getBitmap: FileNotFoundException: "+e.getMessage());
        }finally {
            try{
                fis.close();
            }catch (IOException ex){
                Log.d(TAG, "getBitmap: "+ex.getMessage());
            }
        }

        return bmp;
    }

    /**
     * return byte array from bitmap
     * quality is greater than 0 but less than 100
     * @param bm
     * @param quality
     * @return
     */
    public static byte[] getBytesFromBitmap(Bitmap bm,int quality){
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
    }
}
