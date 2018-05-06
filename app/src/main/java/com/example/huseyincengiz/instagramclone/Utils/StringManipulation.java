package com.example.huseyincengiz.instagramclone.Utils;

import android.util.Log;

/**
 * Created by HuseyinCengiz on 29.01.2018.
 */

public class StringManipulation {

    //huseyin.cengiz i alir huseyin cengiz yapar
    public static String expandUsername(String value) {
        return value.replace('.', ' ');
    }

    //stringteki boşluğun yerine . koyar huseyin.cengiz gibi
    public static String condenseUsername(String value) {
        return value.replace(' ', '.');
    }

    /*
    In -> some description #tag1 #tag2 some words
    Out-> #tag1,#tag2
     */
    public static String getTags(String text) {
        if (text.indexOf("#") > 0) {
            StringBuilder sb = new StringBuilder();
            char[] charArray = text.toCharArray();
            boolean foundword = false;
            for (char c : charArray) {
                if (c == '#') {
                    foundword = true;
                    sb.append(c);
                } else {
                    if (foundword) {
                        sb.append(c);
                    }
                }
                if (c == ' ') {
                    foundword = false;
                }
            }
            String s = sb.toString().replace(" ", "").replace("#", ",#");
            return s.substring(1, s.length());
        } else {
            return "";
        }
    }
}
