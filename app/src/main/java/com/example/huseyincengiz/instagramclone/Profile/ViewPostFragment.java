package com.example.huseyincengiz.instagramclone.Profile;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.huseyincengiz.instagramclone.Models.Comment;
import com.example.huseyincengiz.instagramclone.Models.Like;
import com.example.huseyincengiz.instagramclone.Models.Photo;
import com.example.huseyincengiz.instagramclone.Models.User;
import com.example.huseyincengiz.instagramclone.Models.UserAccountSettings;
import com.example.huseyincengiz.instagramclone.R;
import com.example.huseyincengiz.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.huseyincengiz.instagramclone.Utils.FirebaseMethods;
import com.example.huseyincengiz.instagramclone.Utils.Heart;
import com.example.huseyincengiz.instagramclone.Utils.SquareImageView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by HuseyinCengiz on 3.04.2018.
 */

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // Write a message to the database
    private FirebaseDatabase database;//sqlconnection gibi dusun
    private DatabaseReference myRef;//buda veritabanından okuma yazma islemlerini yapmamızı saglayan yardımcı class
    private FirebaseMethods firebaseMethods;

    //Widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private TextView mBackLabel, mCaption, mUsername, mTimestamp, mLikes, mCommentLink;
    private ImageView mBackArrow, mMorevert, mHeartRed, mHeartWhite, mProfileImage, mComment;
    //vars
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mLikedUsers;
    private String mLikesString = "";
    private User mCurrentUser;

    public interface OnCommentThreadSelectedListener {
        void CommentThreadSelectedListener(Photo photo);
    }

    private OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        initWidgets(view);
        setupFirebaseAuth();
        setupBottomNavigationView();
        return view;
    }

    private void init() {
        try {
            UniversalImageLoader.SetImage(getPhotoFromBundle().getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumberFromBundle();

            String photo_id = getPhotoFromBundle().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference().
                    child(getString(R.string.dbname_photos))
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        Photo newphoto = new Photo();
                        HashMap<String, Object> objectMap = (HashMap<String, Object>) ds.getValue();

                        newphoto.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        newphoto.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        newphoto.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        newphoto.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        newphoto.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        newphoto.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        List<Like> likeList = new ArrayList<Like>();
                        for (DataSnapshot ds1 : ds.child(getString(R.string.field_likes)).getChildren()) {
                            Like like = new Like(ds1.getValue(Like.class).getUser_id());
                            likeList.add(like);
                        }
                        newphoto.setLikes(likeList);

                        List<Comment> commentList = new ArrayList<Comment>();
                        for (DataSnapshot ds1 : ds.child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(ds1.getValue(Comment.class).getUser_id());
                            comment.setComment(ds1.getValue(Comment.class).getComment());
                            comment.setDate_created(ds1.getValue(Comment.class).getDate_created());
                            commentList.add(comment);
                        }
                        newphoto.setComments(commentList);

                        mPhoto = newphoto;

                        getCurrentUser();
                        getPhotoDetails();
                        // getLikesString();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: query cancelled");
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            init();
        }
    }

    private void initWidgets(View view) {
        Log.d(TAG, "initWidgets: initializing widgets in the fragment");
        mPostImage = (SquareImageView) view.findViewById(R.id.post_image);
        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.BottomNavViewBar);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.image_caption);
        mUsername = (TextView) view.findViewById(R.id.username);
        mTimestamp = (TextView) view.findViewById(R.id.image_time_posted);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mMorevert = (ImageView) view.findViewById(R.id.ivEllipses);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_photo);
        mLikes = (TextView) view.findViewById(R.id.image_likes);
        mComment = (ImageView) view.findViewById(R.id.speech_bubble);
        mCommentLink = (TextView) view.findViewById(R.id.image_comments_link);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        mHeart = new Heart(mHeartRed, mHeartWhite);
    }

    private void setupWidgets() {
        String timeStampDiff = getTimeStampDifference();
        if (timeStampDiff.equals("0")) {
            mTimestamp.setText("TODAY");
        } else {
            mTimestamp.setText(timeStampDiff + " DAYS AGO");
        }
        UniversalImageLoader.SetImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
        mUsername.setText(mUserAccountSettings.getUsername());
        mLikes.setText(mLikesString);
        mCaption.setText(mPhoto.getCaption());

        if (mPhoto.getComments().size() > 0) {
            mCommentLink.setText("View All " + mPhoto.getComments().size() + " comments");
        } else {
            mCommentLink.setText("There is no comments");
        }

        mCommentLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread");
                mOnCommentThreadSelectedListener.CommentThreadSelectedListener(mPhoto);
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread");
                mOnCommentThreadSelectedListener.CommentThreadSelectedListener(mPhoto);
            }
        });

        if (mLikedByCurrentUser) {
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: touched red heart");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        } else {
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: touched white Heart");
                    return mGestureDetector.onTouchEvent(event);
                }
            });
        }


    }

    private void getCurrentUser() {
        Log.d(TAG, "getCurrentUser: getting the current user");

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mCurrentUser = singleSnapshot.getValue(User.class);
                }
                getLikesString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void getLikesString() {
        Log.d(TAG, "getLikesString: getting likes string");

        DatabaseReference databaseReferenceLikes = FirebaseDatabase.getInstance().getReference();

        Query query = databaseReferenceLikes
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLikedUsers = new StringBuilder();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference();
                    Query query = databaseReferenceUser
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                Log.d(TAG, "onDataChange: found like: " + singleSnapshot.getValue(User.class).getUsername());

                                mLikedUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                mLikedUsers.append(",");
                            }

                            String[] splitUsers = mLikedUsers.toString().split(",");

                            if (mLikedUsers.toString().contains(mCurrentUser.getUsername() + ",")) {
                                mLikedByCurrentUser = true;
                            } else {
                                mLikedByCurrentUser = false;
                            }

                            int length = splitUsers.length;

                            if (length == 1) {
                                mLikesString = "Liked by " + splitUsers[0];
                            } else if (length == 2) {
                                mLikesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                            } else if (length == 3) {
                                mLikesString = "Liked by " + splitUsers[0] + " , " + splitUsers[1] + " and " + splitUsers[2];
                            } else if (length == 4) {
                                mLikesString = "Liked by " + splitUsers[0] + "," + splitUsers[1] + " , " + splitUsers[2] + " and " + splitUsers[3];
                            } else if (length > 4) {
                                mLikesString = "Liked by " + splitUsers[0] + "," + splitUsers[1] + " , " + splitUsers[2] + " and " + (splitUsers.length - 3) + " others";
                            }
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled: query cancelled");
                        }
                    });
                }
                if (!dataSnapshot.exists()) {
                    mLikesString = "There is no likes";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }




    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: Double Tap detected.");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            Query query = databaseReference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        String keyID = singleSnapshot.getKey();
                        //case1: The user has already liked the photo
                        if (mLikedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            mHeart.toggleLike();
                            getLikesString();
                        }
                        //case2: The user hasn't liked the photo
                        else if (!mLikedByCurrentUser) {
                            //add new like
                            addNewLike();
                            break;
                        }
                    }
                    if (!dataSnapshot.exists()) {
                        //add new like
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: query cancelled");
                }
            });

            return true;
        }
    }

    private void addNewLike() {
        Log.d(TAG, "addNewLike: adding new like to post");

        String newKeyID = myRef.push().getKey();

        Like like = new Like(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newKeyID)
                .setValue(like);

        myRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newKeyID)
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();
    }

    private void getPhotoDetails() {
        Log.d(TAG, "getPhotoDetails: retrieving photo details");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        //.getreferance("/user_account_settings/"+userid.tostring())
        Query query = databaseReference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    /**
     * getting the photo from the incoming bundle from ProfileActivity
     *
     * @return
     */
    @Nullable
    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: getting photo from bundle");
        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }

    /**
     * getting the calling activity number from the incoming bundle from ProfileActivity
     *
     * @return
     */
    private int getActivityNumberFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: getting calling activity number from bundle");
        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        } else {
            return 0;
        }
    }

    /**
     * return a string representing the number of days ago the post was made
     *
     * @return
     */
    private String getTimeStampDifference() {
        Log.d(TAG, "getTimeStampDifference: getting TimeSpan Difference");
        String difference = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Turkey"));

        Calendar c = Calendar.getInstance();
        Date today = c.getTime();
        Date photoCreatedTime;
        try {
            photoCreatedTime = sdf.parse(mPhoto.getDate_created());
            difference = String.valueOf(Math.round((today.getTime() - photoCreatedTime.getTime()) / 1000 / 60 / 60 / 24));

        } catch (ParseException e) {
            Log.e(TAG, "getTimeStampDifference: ParseException" + e.getMessage());
            difference = "0";
        }


        return difference;
    }

    /*BottomNavigationViewEx setup*/
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(), bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
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

        firebaseMethods = new FirebaseMethods(getActivity());

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException : " + e.getMessage());
        }
    }
}
