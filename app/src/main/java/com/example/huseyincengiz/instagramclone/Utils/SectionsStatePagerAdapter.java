package com.example.huseyincengiz.instagramclone.Utils;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HuseyinCengiz on 1.12.2017.
 */

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {

    private  final List<Fragment> mFragmentLists=new ArrayList<>();
    private  final HashMap<Fragment,Integer> mFragments=new HashMap<>();
    private  final HashMap<String,Integer> mFragmentNumbers=new HashMap<>();
    private  final HashMap<Integer,String> mFragmentNames=new HashMap<>();

    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentLists.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentLists.size();
    }
    public  void addFragment(Fragment fragment,String fragmentName)
    {
        mFragmentLists.add(fragment);
        mFragments.put(fragment,mFragmentLists.size()-1);
        mFragmentNumbers.put(fragmentName,mFragmentLists.size()-1);
        mFragmentNames.put(mFragmentLists.size()-1,fragmentName);
    }

    /**
     * return the fragment with
     * @param fragmentName
     * @return
     */
    public  Integer getFragmentNumber(String fragmentName)
    {
        if(mFragmentNumbers.containsKey(fragmentName)) {
            return mFragmentNumbers.get(fragmentName);
        }
        else {
            return  null;
        }
    }
    /**
     * return the fragment with
     * @param fragment
     * @return
     */
    public  Integer getFragmentNumber(Fragment fragment)
    {
        if(mFragments.containsKey(fragment)) {
            return mFragmentNumbers.get(fragment);
        }
        else {
            return  null;
        }
    }
    /**
     * return the fragment with
     * @param fragmentNumber
     * @return
     */
    public  String getFragmentName(Integer fragmentNumber)
    {
        if(mFragmentNames.containsKey(fragmentNumber)) {
            return mFragmentNames.get(fragmentNumber);
        }
        else {
            return  null;
        }
    }



}
