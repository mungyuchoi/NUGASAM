package com.moon.nugasam.utils;

public class NugaUtils {

    public static String getPositionString(String data, int position){
        String[] temp = data.split("\\s+");
        return temp[position];
    }

}
