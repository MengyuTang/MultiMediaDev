package com.tang.alex.multimediadev.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static Bitmap loadImageFromFile(Context mContext,String path){
        Bitmap bitmap = null;
        try {
            InputStream open = mContext.getAssets().open(path);
            if (null != open){
                bitmap = BitmapFactory.decodeStream(open);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
