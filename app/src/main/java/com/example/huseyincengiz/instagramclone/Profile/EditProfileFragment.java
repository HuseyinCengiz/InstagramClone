package com.example.huseyincengiz.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huseyincengiz.instagramclone.Models.User;
import com.example.huseyincengiz.instagramclone.Models.UserAccountSettings;
import com.example.huseyincengiz.instagramclone.Models.UserSettings;
import com.example.huseyincengiz.instagramclone.R;
import com.example.huseyincengiz.instagramclone.Share.ShareActivity;
import com.example.huseyincengiz.instagramclone.Utils.FirebaseMethods;
import com.example.huseyincengiz.instagramclone.Utils.UniversalImageLoader;
import com.example.huseyincengiz.instagramclone.dialogs.ConfirmPasswordDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HuseyinCengiz on 1.12.2017.
 */

public class EditProfileFragment extends Fragment implements ConfirmPasswordDialog.OnConfirmPasswordDialogListener {

    private static final String TAG = "EditProfileFragment";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // Write a message to the database
    private FirebaseDatabase database;//sqlconnection gibi dusun
    private DatabaseReference myRef;//buda veritabanından okuma yazma islemlerini yapmamızı saglayan yardımcı class


    //EditProfile Fragments Widgets
    private EditText mUsername, mDisplayName, mWebsite, mDescription, mPhoneNumber, mEmail;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;

    //Variables
    private FirebaseMethods firebaseMethods;
    private String userID;
    private UserSettings mUserSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mEmail = view.findViewById(R.id.email);
        mPhoneNumber = view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = view.findViewById(R.id.changeProfilePhoto);

        firebaseMethods = new FirebaseMethods(getActivity());
        //back arrow for navigating back to "ProfileActivity"
        ImageView backarrow = view.findViewById(R.id.backarrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating to ProfileActivity");
                //getActivity gets Activity which including this fragment
                getActivity().finish();
            }
        });

        ImageView checkmark = view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attemting to save changes");
                saveProfileSettings();
            }
        });
        setupFirebaseAuth();
        //setProfileImage();
        return view;
    }

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieveng from the database" + userSettings.toString());
        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getUserAccountSettings();

        mUserSettings = userSettings;

        UniversalImageLoader.SetImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(user.getEmail());
        mPhoneNumber.setText(String.valueOf(user.getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ShareActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

    }

    @Override
    public void OnConfirmPassword(String password) {
        Log.d(TAG, "OnConfirmPassword: got the password :" + password);

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(mUserSettings.getUser().getEmail(), password);

        // Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated.");
                            //////Check to see if the email is not already present to database
                            //////Check to see whether email in use or not
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                    //the process completed succesfully
                                    if (task.isSuccessful()) {
                                        try {
                                            if (task.getResult().getProviders().size() == 1)///that means the email in use
                                            {
                                                Log.d(TAG, "onComplete: that email is already in use");
                                                Toast.makeText(getActivity(), "That Email Is Already In Use", Toast.LENGTH_LONG).show();
                                            } else ///it was unable to find any email so the email is available
                                            {
                                                Log.d(TAG, "onComplete: That email is available");

                                                //the email is available so update it
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User email address updated.");
                                                                    Toast.makeText(getActivity(), "Email Updated", Toast.LENGTH_LONG).show();
                                                                    firebaseMethods.updateEmail(mEmail.getText().toString());
                                                                }
                                                            }
                                                        });
                                            }
                                        } catch (NullPointerException ex) {
                                            Log.d(TAG, "onComplete: "+ex.getMessage());
                                        }
                                    }
                                }
                            });

                        } else {
                            Log.d(TAG, "onComplete: re-authenticated failed");
                        }
                    }
                });


    }

    /**
     * Retrieves the data contained in the widgets and submits it to database
     * Before doing so it checks to make sure the username chosen is unique
     */
    private void saveProfileSettings() {

        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final Long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());

        //case 1:the user changed their username therefore we need to check for uniqueness
        if (!mUserSettings.getUser().getUsername().equals(username)) {
            checkIfUsernameExists(username);
        }
        //case 2:if the user made a change their username
        if (!mUserSettings.getUser().getEmail().equals(email)) {
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.setTargetFragment(this, 1);
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
        }

        if(!mUserSettings.getUserAccountSettings().getDisplay_name().equals(displayName)){
            firebaseMethods.updateUserAccountSettings(displayName,R.string.dbname_user_account_settings,R.string.field_display_name);
        }
        if(!mUserSettings.getUserAccountSettings().getDescription().equals(description)){
            firebaseMethods.updateUserAccountSettings(description,R.string.dbname_user_account_settings,R.string.field_description);
        }
        if(!mUserSettings.getUserAccountSettings().getWebsite().equals(website)){
            firebaseMethods.updateUserAccountSettings(website,R.string.dbname_user_account_settings,R.string.field_website);
        }
        if(!(mUserSettings.getUser().getPhone_number()==phoneNumber)){
            firebaseMethods.updateUserAccountSettings(phoneNumber,R.string.dbname_users,R.string.field_phone_number);
        }
    }

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

                //if dataSnapshot doesn't exist so our username is unique
                if (!dataSnapshot.exists()) {
                    //add username
                    firebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Saved Username.", Toast.LENGTH_LONG).show();
                } else {
                    //we get user's fields
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        if (singleSnapshot.exists()) {
                            Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                            Toast.makeText(getActivity(), "That username is already exists.", Toast.LENGTH_LONG).show();
                        }
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        userID = mAuth.getCurrentUser().getUid();
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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database

                setProfileWidgets(firebaseMethods.getUserSettings(dataSnapshot));

                //retrieve images for the user
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
