package com.example.huseyincengiz.instagramclone.Utils;

import android.os.Environment;

/**
 * Created by HuseyinCengiz on 29.03.2018.
 */

public class FilePaths {

    //"storage/emulated/0"
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/Pictures";

    public String CAMERA = ROOT_DIR + "/DCIM/camera";

    //we need to specify where in the storage location is going to be stored
    public String FIREBASE_IMAGE_STORAGE = "/photos/users";
}
