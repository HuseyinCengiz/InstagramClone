package com.example.huseyincengiz.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huseyincengiz.instagramclone.Models.User;
import com.example.huseyincengiz.instagramclone.R;
import com.example.huseyincengiz.instagramclone.Utils.FirebaseMethods;
import com.example.huseyincengiz.instagramclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by HuseyinCengiz on 30.03.2018.
 */

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    // Write a message to the database
    private FirebaseDatabase mFirebaseDatabase;//sqlconnection gibi dusun
    private DatabaseReference databaseReference;//buda veritabanından okuma yazma islemlerini yapmamızı saglayan yardımcı class

    //Widgets
    private EditText mCaption;

    //Vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgUrl;
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        Log.d(TAG, "onCreate: Starting NextActivity");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = mFirebaseDatabase.getReference();
        setupFirebaseAuth();
        SetImage();
        firebaseMethods = new FirebaseMethods(this);
        mCaption = (EditText) findViewById(R.id.caption);
        ImageView backArrow = (ImageView) findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });

        TextView share = (TextView) findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Sharing the image");
                //upload the image to firebase
                Toast.makeText(NextActivity.this, "Attempting to upload new photo", Toast.LENGTH_LONG).show();
                String caption = mCaption.getText().toString();
                if (intent.hasExtra(getString(R.string.selected_image))) {
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    firebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgUrl, null);
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    Bitmap bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    firebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, null, bitmap);
                }
            }
        });
    }

    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
    private void SetImage() {
        intent = getIntent();
        ImageView imageView = (ImageView) findViewById(R.id.imageShare);
        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "SetImage: got new image url : " + imgUrl);
            UniversalImageLoader.SetImage(intent.getStringExtra(getString(R.string.selected_image)), imageView, null, mAppend);
        } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            Bitmap bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "SetImage: got new bitmap");
            imageView.setImageBitmap(bitmap);
        }
    }

    /*
     Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");
        Log.d(TAG, "setupFirebaseAuth: image count : " + imageCount);
        mAuth = FirebaseAuth.getInstance();
        /*
        *FirebaseDatabase i sqlconneciton gibi düşünebiliriz bu bizim firebase ile bağlantıyı sağlıyor
        * Database referans ise mesela getReference() diyince bütün verileri kapsayan kök roottaki verileri
        * izler yani referansım oraları kapsar Ama ben getReference("kullanici") dersem  kullanici rootun
        *altındaki verilere ulaşabilirim yani sadece oraları kapsar
        *  */
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                   /* Burası sadece izleyici yani ben burda bir listener koyuyorum veritabanında
                    bir değişiklik olduğunda onDataChange metodu verileri tekrardan alabilmem icin
                    snapshotın örneğini alıp verilerek çalıştırılıyor
                    hata olursa onCancelled metodu databaseerror örneği verilerek çalıştırılıyor

                    addValueEventListener--->Sürekli değişikliği dinliyor
                    addListenerForSingleValueEvent--->İlk değişiklik olana kadar dinliyor 1 kere çalışır
                    myRef.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });*/
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }

        };

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imageCount = firebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count : " + imageCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
