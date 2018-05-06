package com.example.huseyincengiz.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.huseyincengiz.instagramclone.Home.HomeActivity;
import com.example.huseyincengiz.instagramclone.Models.Comment;
import com.example.huseyincengiz.instagramclone.Models.Like;
import com.example.huseyincengiz.instagramclone.Models.Photo;
import com.example.huseyincengiz.instagramclone.Models.User;
import com.example.huseyincengiz.instagramclone.Models.UserAccountSettings;
import com.example.huseyincengiz.instagramclone.Profile.ProfileActivity;
import com.example.huseyincengiz.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HuseyinCengiz on 26.04.2018.
 */

public class MainfeedListAdapter extends ArrayAdapter<Photo> {

    private static final String TAG = "MainfeedListAdapter";

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int layoutResource;
    private DatabaseReference mReference;
    private String currentUsername = "";

    public interface OnLoadMoreItemsListener {
        void onLoadMoretems();
    }

    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    public MainfeedListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        layoutResource = resource;
        mReference = FirebaseDatabase.getInstance().getReference();
    }

    static class ViewHolder {
        CircleImageView mProfileImage;
        TextView username, timeDelta, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likedByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image = (SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) (ImageView) convertView.findViewById(R.id.image_heart);
            holder.comment = (ImageView) (ImageView) convertView.findViewById(R.id.speech_bubble);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comments_link);
            holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.timeDelta = (TextView) convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //get the current username (need for checking likes string)
        getCurrentUsername();

        //get likes string
        getLikesString(holder);

        //set the comments
        List<Comment> comments = getItem(position).getComments();

        holder.caption.setText(getItem(position).getCaption());
        if (comments.size() > 0) {
            holder.comments.setText("View All " + comments.size() + " comments");
        } else {
            holder.comments.setText("There is no comments");
        }
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread for: " + getItem(position).getPhoto_id());
                ((HomeActivity) mContext).onCommentThreadSelected(getItem(position), mContext.getString(R.string.home_activity));
                ((HomeActivity) mContext).hideLayout();
            }
        });

        String timeStampDifference = getTimeStampDifference(getItem(position));
        if (!timeStampDifference.equals("0")) {
            holder.timeDelta.setText(timeStampDifference + " Days Ago");
        } else {
            holder.timeDelta.setText("Today");
        }
        //set the photo
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);

        //get the profile image and username
        Query query = mReference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    holder.username.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of" + holder.username.getText());
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity), mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(), holder.mProfileImage);
                    holder.mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to profile of" + holder.username.getText());
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity), mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);
                        }
                    });
                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "onClick: navigating to comments thread for: " + getItem(position).getPhoto_id());
                            ((HomeActivity) mContext).onCommentThreadSelected(getItem(position), mContext.getString(R.string.home_activity));
                            ((HomeActivity) mContext).hideLayout();
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //get the user object

        Query userQuery = mReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(User.class).getUsername());
                    holder.user = singleSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(reachedEndOfList(position)){
            loadMoreData();
        }
        return convertView;
    }

    private boolean reachedEndOfList(int position) {
        return position == getCount() - 1;
    }

    private void loadMoreData() {
        try {
            mOnLoadMoreItemsListener = ((OnLoadMoreItemsListener) getContext());
        } catch (ClassCastException e) {
            Log.e(TAG, "loadMoreData: ClassCastException: " + e.getMessage());
        }

        try {
            mOnLoadMoreItemsListener.onLoadMoretems();
        } catch (NullPointerException e) {
            Log.e(TAG, "loadMoreData: NullPointerException: " + e.getMessage());
        }
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private ViewHolder mHolder;

        GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: Double Tap detected.");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            Query query = databaseReference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        String keyID = singleSnapshot.getKey();
                        //case1: The user has already liked the photo
                        if (mHolder.likedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }
                        //case2: The user hasn't liked the photo
                        else if (!mHolder.likedByCurrentUser) {
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if (!dataSnapshot.exists()) {
                        //add new like
                        addNewLike(mHolder);
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

    private void addNewLike(ViewHolder holder) {
        Log.d(TAG, "addNewLike: adding new like to post");

        String newKeyID = mReference.push().getKey();

        Like like = new Like(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newKeyID)
                .setValue(like);

        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newKeyID)
                .setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: getting likes string");

        try {
            DatabaseReference databaseReferenceLikes = FirebaseDatabase.getInstance().getReference();

            Query query = databaseReferenceLikes
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.users = new StringBuilder();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference();
                        Query query = databaseReferenceUser
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(singleSnapshot.getValue(Like.class).getUser_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: found like: " + singleSnapshot.getValue(User.class).getUsername());

                                    holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                    holder.users.append(",");
                                }

                                String[] splitUsers = holder.users.toString().split(",");

                                if (holder.users.toString().contains(currentUsername + ",")) {
                                    holder.likedByCurrentUser = true;
                                } else {
                                    holder.likedByCurrentUser = false;
                                }

                                int length = splitUsers.length;

                                if (length == 1) {
                                    holder.mLikesString = "Liked by " + splitUsers[0];
                                } else if (length == 2) {
                                    holder.mLikesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                                } else if (length == 3) {
                                    holder.mLikesString = "Liked by " + splitUsers[0] + " , " + splitUsers[1] + " and " + splitUsers[2];
                                } else if (length == 4) {
                                    holder.mLikesString = "Liked by " + splitUsers[0] + "," + splitUsers[1] + " , " + splitUsers[2] + " and " + splitUsers[3];
                                } else if (length > 4) {
                                    holder.mLikesString = "Liked by " + splitUsers[0] + "," + splitUsers[1] + " , " + splitUsers[2] + " and " + (splitUsers.length - 3) + " others";
                                }
                                setupString(holder);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: query cancelled");
                            }
                        });
                    }
                    if (!dataSnapshot.exists()) {
                        holder.mLikesString = "There is no likes";
                        holder.likedByCurrentUser = false;
                        setupString(holder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: query cancelled");
                }
            });
        } catch (NullPointerException e) {
            Log.e(TAG, "getLikesString: NullPointerException: " + e.getMessage());
            holder.mLikesString = "There is no likes";
            holder.likedByCurrentUser = false;
            setupString(holder);
        }
    }

    private void setupString(final ViewHolder holder) {
        Log.d(TAG, "setupString: likes string: " + holder.mLikesString);

        if (holder.likedByCurrentUser) {
            Log.d(TAG, "setupString: photo is liked by current user");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        } else {
            Log.d(TAG, "setupString: photo is not liked by current user");
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        holder.likes.setText(holder.mLikesString);
    }

    private void getCurrentUsername() {
        Log.d(TAG, "getCurrentUsername: retrieving user account settings");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Query query = databaseReference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    currentUsername = singleSnapshot.getValue(UserAccountSettings.class).getUsername();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * @return a string representing the number of days ago the post was made
     */
    private String getTimeStampDifference(Photo photo) {
        Log.d(TAG, "getTimeStampDifference: getting TimeSpan Difference");
        String difference = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Turkey"));

        Calendar c = Calendar.getInstance();
        Date today = c.getTime();
        Date photoCreatedTime;
        try {
            photoCreatedTime = sdf.parse(photo.getDate_created());
            difference = String.valueOf(Math.round((today.getTime() - photoCreatedTime.getTime()) / 1000 / 60 / 60 / 24));

        } catch (ParseException e) {
            Log.e(TAG, "getTimeStampDifference: ParseException" + e.getMessage());
            difference = "0";
        }
        return difference;
    }
}
