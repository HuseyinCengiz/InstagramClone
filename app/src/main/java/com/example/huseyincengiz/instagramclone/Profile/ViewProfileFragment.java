package com.example.huseyincengiz.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huseyincengiz.instagramclone.Models.Comment;
import com.example.huseyincengiz.instagramclone.Models.Like;
import com.example.huseyincengiz.instagramclone.Models.Photo;
import com.example.huseyincengiz.instagramclone.Models.User;
import com.example.huseyincengiz.instagramclone.Models.UserAccountSettings;
import com.example.huseyincengiz.instagramclone.Models.UserSettings;
import com.example.huseyincengiz.instagramclone.R;
import com.example.huseyincengiz.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.huseyincengiz.instagramclone.Utils.FirebaseMethods;
import com.example.huseyincengiz.instagramclone.Utils.GridArrayAdapter;
import com.example.huseyincengiz.instagramclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HuseyinCengiz on 7.02.2018.
 */

public class ViewProfileFragment extends Fragment {


    public interface OnGridImageSelectedListener {
        public void OnGridImageSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    private static final String TAG = "ViewProfileFragment";
    private static final int ACTIVITY_NUM = 4;
    private static final int GRID_COL_NUM = 3;
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // Write a message to the database
    private FirebaseDatabase database;//sqlconnection gibi dusun
    private DatabaseReference myRef;//buda veritabanından okuma yazma islemlerini yapmamızı saglayan yardımcı class
    private Context mContext;

    //Widgets

    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription, mFollow, mUnfollow, editProfile;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private ImageView mBackArrow;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private int mFollowersCount, mFollowingCount, mPostCount;

    //Vars
    private User mUser;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);

        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mPosts = view.findViewById(R.id.tvPost);
        mFollowers = view.findViewById(R.id.tvFollewers);
        mFollowing = view.findViewById(R.id.tvFollowing);
        mProgressBar = view.findViewById(R.id.profileProgressBar);
        gridView = view.findViewById(R.id.gridView);
        editProfile = view.findViewById(R.id.textEditProfile);
        mFollow = view.findViewById(R.id.follow);
        mUnfollow = view.findViewById(R.id.unfollow);
        mBackArrow = view.findViewById(R.id.backArrow);
        bottomNavigationViewEx = view.findViewById(R.id.BottomNavViewBar);
        mContext = getActivity();
        Log.d(TAG, "onCreateView: started");
        try {
            mUser = getUserFromBundle();
            init();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }
        setupBottomNavigationView();
        setupFirebaseAuth();
        isFollowing();

        getFollowersCount();
        getFollowingCount();
        getPostsCount();

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now following: " + mUser.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.field_user_id))
                        .setValue(mUser.getUser_id());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());


                setFollowing();
            }
        });

        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now unfollowig: " + mUser.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();

                setUnFollowing();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to edit profile fragment");
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to back");
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().finish();
            }
        });

        return view;
    }

    private void getFollowersCount() {
        mFollowersCount = 0;
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_followers))
                .child(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: getting followers count");
                    mFollowersCount++;
                }
                mFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void getFollowingCount() {
        mFollowingCount = 0;
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_following))
                .child(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: getting followings count");
                    mFollowingCount++;
                }
                mFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void getPostsCount() {
        mPostCount = 0;
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: getting posts count");
                    mPostCount++;
                }
                mPosts.setText(String.valueOf(mPostCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void isFollowing() {
        Log.d(TAG, "isFollowing: checks the current user's whether following or not visiting the user");
        setUnFollowing();

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: the user is being followed by current user");
                    setFollowing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void setFollowing() {
        Log.d(TAG, "setFollowing: updating UI for following this user");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        editProfile.setVisibility(View.GONE);
    }

    private void setUnFollowing() {
        Log.d(TAG, "setFollowing: updating UI for unfollowing this user");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.GONE);
    }

    private void setCurrentUserProfile() {
        Log.d(TAG, "setFollowing: updating UI for showing this user their own profile");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.VISIBLE);
    }

    private void init() {
        //set the profile widgets
        Log.d(TAG, "init: setting the profile widgets");
        Query query1 = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mUser.getUser_id());

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: Found user " + ds.getValue(UserAccountSettings.class).toString());
                    UserSettings userSettings = new UserSettings();
                    userSettings.setUser(mUser);
                    userSettings.setUserAccountSettings(ds.getValue(UserAccountSettings.class));
                    setProfileWidgets(userSettings);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
        //get user's profile photos
        Log.d(TAG, "init: getting user's profile photos");

        Query query2 = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Photo> photos = new ArrayList<>();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Photo photo = new Photo();
                    HashMap<String, Object> objectMap = (HashMap<String, Object>) ds.getValue();

                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    List<Like> likeList = new ArrayList<Like>();
                    for (DataSnapshot ds1 : ds.child(getString(R.string.field_likes)).getChildren()) {
                        Like like = new Like(ds1.getValue(Like.class).getUser_id());
                        likeList.add(like);
                    }
                    photo.setLikes(likeList);

                    List<Comment> commentList = new ArrayList<Comment>();
                    for (DataSnapshot ds1 : ds.child(getString(R.string.field_comments)).getChildren()) {
                        Comment comment = new Comment();
                        comment.setUser_id(ds1.getValue(Comment.class).getUser_id());
                        comment.setComment(ds1.getValue(Comment.class).getComment());
                        comment.setDate_created(ds1.getValue(Comment.class).getDate_created());
                        commentList.add(comment);
                    }
                    photo.setComments(commentList);

                    photos.add(photo);
                }
                //set up our gridview
                setupGridview(photos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void setupGridview(final ArrayList<Photo> photos) {
        Log.d(TAG, "setupGridview: setting up image grid");

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int columnWidth = screenWidth / GRID_COL_NUM;
        gridView.setColumnWidth(columnWidth);

        ArrayList<String> imgUrls = new ArrayList<String>();
        for (Photo photo : photos) {
            imgUrls.add(photo.getImage_path());
        }
        GridArrayAdapter adapter = new GridArrayAdapter(getActivity(), R.layout.layout_grid_imageview, imgUrls, "");
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOnGridImageSelectedListener.OnGridImageSelected(photos.get(position), ACTIVITY_NUM);
            }
        });

    }

    /*BottomNavigationViewEx setup*/
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    @Nullable
    private User getUserFromBundle() {
        Log.d(TAG, "getUserFromBundle: arguments" + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.intent_user));
        } else {
            return null;
        }
    }

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieveng from the database" + userSettings.toString());
        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getUserAccountSettings();

        UniversalImageLoader.SetImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mProgressBar.setVisibility(View.GONE);

    }

    /*
    ------------------------------------- Firebase ------------------------------------------
     */

    /*
     Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the current user is logged in
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnGridImageSelectedListener = (ProfileActivity) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
