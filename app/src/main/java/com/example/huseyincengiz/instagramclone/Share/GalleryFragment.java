package com.example.huseyincengiz.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.huseyincengiz.instagramclone.Profile.AccountSettingsActivity;
import com.example.huseyincengiz.instagramclone.R;
import com.example.huseyincengiz.instagramclone.Utils.FilePaths;
import com.example.huseyincengiz.instagramclone.Utils.FileSearch;
import com.example.huseyincengiz.instagramclone.Utils.GridArrayAdapter;
import com.example.huseyincengiz.instagramclone.Utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by HuseyinCengiz on 1.10.2017.
 */

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";

    //Widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;

    //Constant
    private static final int NUM_GRID_COL = 3;

    //Vars
    private ArrayList<String> directories;
    private String mAppend = "file:/";
    private String mSelectedImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        galleryImage = (ImageView) view.findViewById(R.id.galleryImageView);
        gridView = (GridView) view.findViewById(R.id.gridView);
        directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        directories = new ArrayList<>();
        Log.d(TAG, "onCreateView: Started.");
        init();

        ImageView closeShare = (ImageView) view.findViewById(R.id.ivCloseShare);

        closeShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the gallery fragment");
                getActivity().finish();
            }
        });

        TextView nextScreen = (TextView) view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share scrren");

                if (isRootTask()) {
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }

            }
        });

        return view;
    }

    private boolean isRootTask() {
        if (((ShareActivity) getActivity()).getTask() == 0)
            return true;
        else
            return false;
    }

    private void init() {
        FilePaths filePaths = new FilePaths();
        //check for other folder inside "/storage/emulated/0/pictures"
        if (FileSearch.getDirectoryPaths(filePaths.PICTURES) != null) {
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }
        directories.add(filePaths.CAMERA);

        ArrayList<String> directoryNames = new ArrayList<>();
        for (int i = 0; i < directories.size(); i++) {
            int index = directories.get(i).lastIndexOf("/") + 1;
            String name = directories.get(i).substring(index);
            directoryNames.add(name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: selected directory :" + directories.get(position));
                //setup our image grid for the directory chosen
                setupGridview(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupGridview(String selectedDirectory) {
        Log.d(TAG, "setupGridview: chosen directory :" + selectedDirectory);

        final ArrayList<String> imgUrls = FileSearch.getFilePaths(selectedDirectory);

        //set the grid column width
        int screenwidth = getResources().getDisplayMetrics().widthPixels;
        int columnwidth = screenwidth / NUM_GRID_COL;
        gridView.setColumnWidth(columnwidth);

        //use the grid adapter to adapter the images to gridview
        GridArrayAdapter adapter = new GridArrayAdapter(getActivity(), R.layout.layout_grid_imageview, imgUrls, mAppend);
        gridView.setAdapter(adapter);

        //set the first image to be displayed when the activity fragment view is inflated
        try {
            UniversalImageLoader.SetImage(imgUrls.get(0), galleryImage, mProgressBar, mAppend);
            mSelectedImage = imgUrls.get(0);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "setupGridview: IndexOutOfBoundsException" + e.getMessage());
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected an image :" + imgUrls.get(position));
                UniversalImageLoader.SetImage(imgUrls.get(position), galleryImage, mProgressBar, mAppend);
                mSelectedImage = imgUrls.get(position);
            }
        });

    }
}
