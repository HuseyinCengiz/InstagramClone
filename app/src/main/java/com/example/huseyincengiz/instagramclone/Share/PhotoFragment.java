package com.example.huseyincengiz.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.huseyincengiz.instagramclone.Profile.AccountSettingsActivity;
import com.example.huseyincengiz.instagramclone.R;
import com.example.huseyincengiz.instagramclone.Utils.Permissions;

/**
 * Created by HuseyinCengiz on 1.10.2017.
 */

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";

    public static final int GALLERY_FRAGMENT_NUM = 0;
    public static final int PHOTO_FRAGMENT_NUM = 1;
    //it doesn't matter what request code will be.
    //We get photo by using this requet code
    public static final int CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: Started.");

        Button btnLaunchCamera = (Button) view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Launcing the camera.");
                if (((ShareActivity) getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM) {

                    if (((ShareActivity) getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION)) {
                        Log.d(TAG, "onClick: starting camera.");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //We are gonna wait result that a photo taking from camera intent
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    } else {
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        //that will clear activity stack
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //then we know we have a photo
        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: done taking a photo");
            Log.d(TAG, "onActivityResult: attemting to navigate to final share screen");
            //navigate to the final share screen top publish photo

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            //Share Post
            if (isRootTask()) {
                try {
                    Log.d(TAG, "onActivityResult: received new bitmap from camera");
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    startActivity(intent);
                } catch (NullPointerException ex) {
                    Log.d(TAG, "onActivityResult: NullPointerException -> " + ex.getMessage());
                }
            } else //Change Profile Photo
            {
                try {
                    Log.d(TAG, "onActivityResult: received new bitmap from camera");
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                } catch (NullPointerException ex) {
                    Log.d(TAG, "onActivityResult: NullPointerException -> " + ex.getMessage());
                }
            }


        }
    }

    private boolean isRootTask() {
        if (((ShareActivity) getActivity()).getTask() == 0)
            return true;
        else
            return false;
    }

}
