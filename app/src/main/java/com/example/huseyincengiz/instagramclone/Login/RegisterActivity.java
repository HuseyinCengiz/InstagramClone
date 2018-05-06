package com.example.huseyincengiz.instagramclone.Login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huseyincengiz.instagramclone.Models.User;
import com.example.huseyincengiz.instagramclone.R;
import com.example.huseyincengiz.instagramclone.Utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by HuseyinCengiz on 14.12.2017.
 */

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private Context mContext;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    // Write a message to the database
    private FirebaseDatabase database;//sqlconnection gibi dusun
    private DatabaseReference myRef;//buda veritabanından okuma yazma islemlerini yapmamızı saglayan yardımcı class

    //Widgets
    private String email, username, password;
    private EditText mEmail, mUsername, mPassword;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    private TextView loadingPleaseWait;

    private String append = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate:started");
        InitWidgets();
        setupFirebaseAuth();
        InitRegisterButtonClickListener();
    }

    /*
    ------------------------------------- Firebase ------------------------------------------
     */
    private void InitRegisterButtonClickListener() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                username = mUsername.getText().toString();
                if (checkInputs(email, password, username)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);
                    firebaseMethods.registerNewUser(email, password, username);
                }
            }
        });
    }

    private boolean checkInputs(String email, String password, String username) {
        if (email.equals("") || password.equals("") || username.equals("")) {
            Toast.makeText(mContext, "All fields must be filled out", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
    /*
        Initialize the activity widgets
     */
    private void InitWidgets() {
        Log.d(TAG, "InitWidgets: Initializing widgets");
        mContext = RegisterActivity.this;
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        mUsername = (EditText) findViewById(R.id.input_username);
        mProgressBar = (ProgressBar) findViewById(R.id.registerRequestLoadingProgressBar);
        loadingPleaseWait = (TextView) findViewById(R.id.loadingPleaseWait);
        btnRegister = (Button) findViewById(R.id.btn_register);
        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);
        firebaseMethods = new FirebaseMethods(mContext);
    }

    private boolean isStringNull(String value) {
        if (value.equals(""))
            return true;
        else
            return false;
    }
    /*
     Setup the firebase auth object
     */

    /**
     * Checking if @param username already exists in the database
     *
     * @param username
     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if " + username + " already exists");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //this is going to return datasnapshot only if a match is found

                //we have got same username in here
                //eğer aynı username de kullanıcı varsa isme random ek string ekliyoruz
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                        //push random string bir key oluşturur bunuda getKey ile alırız c# da Guid gibi
                        append = myRef.push().getKey().substring(3, 10);
                        Log.d(TAG, "onDataChange: username already exists Appending random string to name" + append);
                    }
                }

                String mUsername = "";
                mUsername = username + append;

                //add new user to database
                //add new user_account_settings to database
                firebaseMethods.addNewUser(email, mUsername, "", "", "");
                Toast.makeText(mContext, "Signup Successfull. Sending verify email", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");
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
                    checkIfUsernameExists(username);
                    finish();
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
}
