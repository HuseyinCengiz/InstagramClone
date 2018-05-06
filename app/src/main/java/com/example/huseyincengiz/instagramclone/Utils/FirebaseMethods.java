package com.example.huseyincengiz.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.huseyincengiz.instagramclone.Home.HomeActivity;
import com.example.huseyincengiz.instagramclone.Models.Photo;
import com.example.huseyincengiz.instagramclone.Models.User;
import com.example.huseyincengiz.instagramclone.Models.UserAccountSettings;
import com.example.huseyincengiz.instagramclone.Models.UserSettings;
import com.example.huseyincengiz.instagramclone.Profile.AccountSettingsActivity;
import com.example.huseyincengiz.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by HuseyinCengiz on 28.01.2018.
 */

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private Context mContext;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference mfStorageReference;

    private String mUserID;

    //Vars
    private double mPhotoUploadProgress = 0;


    public FirebaseMethods(Context context) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = mFirebaseDatabase.getReference();
        mfStorageReference = FirebaseStorage.getInstance().getReference();
        if (mAuth.getCurrentUser() != null) {
            mUserID = mAuth.getCurrentUser().getUid();
        }
    }

   /* public boolean checkUsernameIsAlreadyExists(String username, DataSnapshot dataSnapshot) {
        Log.d(TAG, "checkUsernameIsAlreadyExists: checking if " + username + " already exists");

        User user = new User();

        for (DataSnapshot ds : dataSnapshot.child(mContext.getString(R.string.dbname_users)).getChildren()) {
            Log.d(TAG, "checkUsernameIsAlreadyExists: " + ds);
            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "checkUsernameIsAlreadyExists: userName " + user.getUsername());

            if (StringManipulation.expandUsername(user.getUsername()).equals(username)) {
                Log.d(TAG, "checkUsernameIsAlreadyExists: Match Found " + user.getUsername());
                return true;
            }
        }
        return false;
    }*/

    /**
     * Register a new user to Firebase Authentication
     *
     * @param email
     * @param password
     * @param username
     */
    public void registerNewUser(final String email, final String password, final String username) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user.
                        // If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(mContext, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.isSuccessful()) {
                            sendVerificationEmail();
                            mUserID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: AuthState Changed " + mUserID);
                        }
                    }
                });
    }


    public void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(mContext, "Couldn't send verification email", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * add information to users node
     * add information to user_account_settings_node
     *
     * @param email
     * @param username
     * @param description
     * @param webSite
     * @param profilePhoto
     */
    public void addNewUser(String email, String username, String description, String webSite, String profilePhoto) {

        User user = new User(mUserID, email, 1, StringManipulation.condenseUsername(username));

        databaseReference.child(mContext.getString(R.string.dbname_users)).child(mUserID).setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                "",
                StringManipulation.condenseUsername(username),
                webSite,
                mUserID
        );

        databaseReference.child(mContext.getString(R.string.dbname_user_account_settings)).child(mUserID).setValue(settings);
    }

    /**
     * Retrieves the account settings for the user currently logged in
     * Database:user account settings node
     *
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from database");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            Log.d(TAG, "getUserAccountSettings: datasnapshot" + ds);

            //user account settings node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                try {
                    settings.setDisplay_name(ds.child(mUserID).getValue(UserAccountSettings.class).getDisplay_name());
                    settings.setUsername(ds.child(mUserID).getValue(UserAccountSettings.class).getUsername());
                    settings.setWebsite(ds.child(mUserID).getValue(UserAccountSettings.class).getWebsite());
                    settings.setDescription(ds.child(mUserID).getValue(UserAccountSettings.class).getDescription());
                    settings.setFollowers(ds.child(mUserID).getValue(UserAccountSettings.class).getFollowers());
                    settings.setFollowing(ds.child(mUserID).getValue(UserAccountSettings.class).getFollowing());
                    settings.setPosts(ds.child(mUserID).getValue(UserAccountSettings.class).getPosts());
                    settings.setProfile_photo(ds.child(mUserID).getValue(UserAccountSettings.class).getProfile_photo());
                    Log.d(TAG, "getUserAccountSettings: retrieved user account settings information" + settings.toString());
                } catch (NullPointerException ex) {
                    Log.d(TAG, "getUserAccountSettings: NullPointerException" + ex.getMessage());
                }
            }

            //user node
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                user.setEmail(ds.child(mUserID).getValue(User.class).getEmail());
                user.setUsername(ds.child(mUserID).getValue(User.class).getUsername());
                user.setPhone_number(ds.child(mUserID).getValue(User.class).getPhone_number());
                user.setUser_id(ds.child(mUserID).getValue(User.class).getUser_id());
                Log.d(TAG, "getUserAccountSettings: retrieved user ınformation" + user.toString());
            }
        }
        return new UserSettings(user, settings);
    }

    /**
     * update username in the 'user' node and 'user_account_setting' node
     *
     * @param username
     */
    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: updating username to :" + username);


        databaseReference.child(mContext.getString(R.string.dbname_users))
                .child(mUserID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
        databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(mUserID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    /**
     * update email in the 'user' node and 'user_account_setting' node
     *
     * @param email
     */
    public void updateEmail(String email) {
        Log.d(TAG, "updateUsername: updating email to :" + email);
        databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(mUserID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }

    public void updateUserAccountSettings(String value, int dbname, int dbfield) {
        Log.d(TAG, "updateUserAccountSettings: updating user account settings " + value);
        if (value != null) {
            databaseReference.child(mContext.getString(dbname))
                    .child(mUserID)
                    .child(mContext.getString(dbfield))
                    .setValue(value);
        }
    }

    public void updateUserAccountSettings(Long value, int dbname, int dbfield) {
        Log.d(TAG, "updateUserAccountSettings: updating user account settings " + value);
        if (value != null) {
            databaseReference.child(mContext.getString(dbname))
                    .child(mUserID)
                    .child(mContext.getString(dbfield))
                    .setValue(value);
        }
    }

    public int getImageCount(DataSnapshot dataSnapshot) {
        int imageCount = 0;
        for (DataSnapshot ds : dataSnapshot.child(mContext.getString(R.string.dbname_user_photos)).child(mUserID).getChildren()) {
            imageCount++;
        }
        return imageCount;
    }

    public void uploadNewPhoto(String photoType, final String caption, int imageCount, String imgUrl, Bitmap bm) {
        Log.d(TAG, "uploadNewPhoto: attemting to upload new photo");

        FilePaths filePaths = new FilePaths();
        //case-1) new photo
        if (photoType.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new photo");
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //we are specifying where in database photos are going to be storaged.
            StorageReference storageReference = mfStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (imageCount + 1));

            //convert image uri to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "onFailure: Photo Uploading Failed");
                    Toast.makeText(mContext, "Photo Uploading Failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests")
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                    Uri photoPathInFirebase = taskSnapshot.getDownloadUrl();

                    Toast.makeText(mContext, "Photo Uploaded Succesfully", Toast.LENGTH_SHORT).show();

                    //add the new photo to 'photos' node and 'user_photos' node

                    addPhotoToDatabase(caption, photoPathInFirebase.toString());

                    //navigate to the main feed so the user can see their photos

                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests")
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progres = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progres - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "Photo Upload Progress: " + String.format("%.0f", progres), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progres;
                    }
                }
            });

        }
        //case-2) new profile
        else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new profile photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //we are specifying where in database photos are going to be storaged.
            StorageReference storageReference = mfStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert image uri to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "onFailure: Photo Uploading Failed");
                    Toast.makeText(mContext, "Photo Uploading Failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests")
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                    Uri photoPathInFirebase = taskSnapshot.getDownloadUrl();

                    Toast.makeText(mContext, "Photo Uploaded Succesfully", Toast.LENGTH_SHORT).show();

                    //insert into 'user_account_settings' node
                    setProfilePhoto(photoPathInFirebase.toString());

                    ((AccountSettingsActivity) mContext).setViewPager(
                            ((AccountSettingsActivity) mContext).pagerAdapter.getFragmentNumber(
                                    mContext.getString(R.string.edit_profile_fragment)));

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @SuppressWarnings("VisibleForTests")
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progres = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progres - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "Photo Upload Progress: " + String.format("%.0f", progres), Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progres;
                    }
                }
            });
        }
    }

    private void setProfilePhoto(String firebaseUrl) {
        Log.d(TAG, "setProfilePhoto: setting new profile photo");

        databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(mAuth.getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(firebaseUrl);

    }

    private void addPhotoToDatabase(String caption, String firebaseUrl) {
        Log.d(TAG, "addPhotoToDatabase: adding photo to database");

        String tags = StringManipulation.getTags(caption);
        String newPhotoId = databaseReference.child(mContext.getString(R.string.dbname_user_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setPhoto_id(newPhotoId);
        photo.setImage_path(firebaseUrl);
        photo.setUser_id(mAuth.getCurrentUser().getUid());
        photo.setTags(tags);
        photo.setDate_created(getTimeStamp());

        //insert into database
        databaseReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(mAuth.getCurrentUser().getUid())
                .child(newPhotoId).setValue(photo);
        databaseReference.child(mContext.getString(R.string.dbname_photos))
                .child(newPhotoId).setValue(photo);

    }

    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("Turkey"));
        return sdf.format(new Date());
    }

}
