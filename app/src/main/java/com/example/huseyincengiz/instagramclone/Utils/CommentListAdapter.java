package com.example.huseyincengiz.instagramclone.Utils;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.huseyincengiz.instagramclone.Models.Comment;
import com.example.huseyincengiz.instagramclone.Models.UserAccountSettings;
import com.example.huseyincengiz.instagramclone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HuseyinCengiz on 18.04.2018.
 */

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mInflater;
    private int LayoutResource;
    private Context mContext;

    public CommentListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) LayoutInflater.from(context);
        mContext = context;
        LayoutResource = resource;
    }

    private static class ViewHolder {
        TextView comment, username, timestamp, reply, likes;
        CircleImageView profileImage;
        ImageView like;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(LayoutResource, parent, false);
            holder = new ViewHolder();

            holder.comment = convertView.findViewById(R.id.comment);
            holder.username = convertView.findViewById(R.id.comment_username);
            holder.timestamp = convertView.findViewById(R.id.comment_time_posted);
            holder.reply = convertView.findViewById(R.id.comment_reply);
            holder.like = convertView.findViewById(R.id.comment_like);
            holder.likes = convertView.findViewById(R.id.comment_likes);
            holder.profileImage = convertView.findViewById(R.id.comment_profile_image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.comment.setText(getItem(position).getComment());
        String timeStampDifference = getTimeStampDifference(getItem(position));
        if (!timeStampDifference.equals("0")) {
            holder.timestamp.setText(timeStampDifference + "d");
        } else {
            holder.timestamp.setText("today");
        }
        //set the username and profile image
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        //.getreferance("/user_account_settings/"+userid.tostring())
        Query query = databaseReference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(), holder.profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });

        if(position==0){
            holder.like.setVisibility(View.GONE);
            holder.likes.setVisibility(View.GONE);
            holder.reply.setVisibility(View.GONE);
        }

        return convertView;
    }

    private String getTimeStampDifference(Comment comment) {
        Log.d(TAG, "getTimeStampDifference: getting TimeSpan Difference");
        String difference = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Turkey"));

        Calendar c = Calendar.getInstance();
        Date today = c.getTime();
        Date photoCreatedTime;
        try {
            photoCreatedTime = sdf.parse(comment.getDate_created());
            difference = String.valueOf(Math.round((today.getTime() - photoCreatedTime.getTime()) / 1000 / 60 / 60 / 24));

        } catch (ParseException e) {
            Log.e(TAG, "getTimeStampDifference: ParseException" + e.getMessage());
            difference = "0";
        }


        return difference;
    }
}
