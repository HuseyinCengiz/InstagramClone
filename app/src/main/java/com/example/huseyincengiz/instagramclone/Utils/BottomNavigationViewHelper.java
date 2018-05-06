package com.example.huseyincengiz.instagramclone.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.huseyincengiz.instagramclone.Home.HomeActivity;
import com.example.huseyincengiz.instagramclone.Likes.LikesActivity;
import com.example.huseyincengiz.instagramclone.Profile.ProfileActivity;
import com.example.huseyincengiz.instagramclone.R;
import com.example.huseyincengiz.instagramclone.Search.SearchActivity;
import com.example.huseyincengiz.instagramclone.Share.ShareActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

/**
 * Created by HuseyinCengiz on 1.10.2017.
 */

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHel";

    /*Bunu yaparak Navigation View Ayarlarını yaptık*/
    public static void setupBottomNavigationView(BottomNavigationViewEx botnavEx) {
        Log.d(TAG, "setupBottomNavigationView: Setting up NavigationView");
        botnavEx.enableAnimation(false);
        botnavEx.enableItemShiftingMode(false);
        botnavEx.enableShiftingMode(false);
        botnavEx.setTextVisibility(false);
    }

    /*Bu metodla butun activitylerdeki navigation viewlere item select listener ekledik
    * Böylelikle butun activitylere yönlendirme yapıyoruz
    * Context almamızın neden bu class activityden kaltım almıyor yani bu helper object class
    * o yüzden intentin 1.parametresinde content istiyor yani nerden yönlendirme olacak
    * sonra startActivity metodunu kullamak için context nesnesi gerekiyor*/
    public static void enableNavigation(final Context context,final Activity callingActivity ,BottomNavigationViewEx botNavEx) {
       
        botNavEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d(TAG, "onNavigationItemSelected: item selected.");
                switch (item.getItemId()) {
                    case R.id.ic_house://ACTIVITY_NUM = 0;
                        Intent intent1 = new Intent(context, HomeActivity.class);
                        context.startActivity(intent1);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_search://ACTIVITY_NUM = 1;
                        Intent intent2 = new Intent(context, SearchActivity.class);
                        context.startActivity(intent2);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_circle://ACTIVITY_NUM = 2;
                        Intent intent3 = new Intent(context, ShareActivity.class);
                        context.startActivity(intent3);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_alert://ACTIVITY_NUM = 3;
                        Intent intent4 = new Intent(context, LikesActivity.class);
                        context.startActivity(intent4);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                    case R.id.ic_android://ACTIVITY_NUM = 4;
                        Intent intent5 = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent5);
                        callingActivity.overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
    }
}
