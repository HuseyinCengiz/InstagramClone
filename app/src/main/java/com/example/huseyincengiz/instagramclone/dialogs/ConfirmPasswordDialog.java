package com.example.huseyincengiz.instagramclone.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huseyincengiz.instagramclone.R;

/**
 * Created by HuseyinCengiz on 28.03.2018.
 */

public class ConfirmPasswordDialog extends DialogFragment {

    private static final String TAG = "ConfirmPasswordDialog";

    //vars
    EditText mPassword;

    public interface OnConfirmPasswordDialogListener{
        public void OnConfirmPassword(String password);
    }
    //We need to use interface to pass value where we use that value

    OnConfirmPasswordDialogListener mOnConfirmPasswordDialogListener;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.dialog_confirm_password,null,false);
        Log.d(TAG, "onCreateView: started.");
        mPassword=view.findViewById(R.id.confirm_password);

        TextView confirmDialog=view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Captured Password and Confirming.");
                //We are passing the password to profileFragment
                String password=mPassword.getText().toString();
                if(!password.equals("")){
                    mOnConfirmPasswordDialogListener.OnConfirmPassword(password);
                    getDialog().dismiss();
                }else {
                    Toast.makeText(getActivity(),"You must enter a password",Toast.LENGTH_LONG).show();
                }
            }
        });

        TextView cancelDialog=view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Closing The Dialog.");
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnConfirmPasswordDialogListener=(OnConfirmPasswordDialogListener)getTargetFragment();
        }catch (ClassCastException ex)
        {
            Log.d(TAG, "onAttach: ClassCastException :"+ex.getMessage());
        }
    }
}
