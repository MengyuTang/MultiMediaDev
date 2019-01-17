package com.tang.alex.multimediadev.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tang.alex.multimediadev.R;
import com.tang.alex.multimediadev.utils.ImageUtils;

import java.io.File;

public class CustomView extends View{

    private Paint mPaint;

    private Bitmap mBitmap;

    private Drawable vectorDrawable;
    public CustomView(Context context) {
        this(context,null);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomView,defStyleAttr,0);
        int attr = ta.getIndex(0);
        if (attr == R.styleable.CustomView_image){
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
                vectorDrawable = context.getDrawable(ta.getResourceId(attr,0));
                mBitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                        vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(mBitmap);
                vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                vectorDrawable.draw(canvas);
            }else {
                mBitmap = BitmapFactory.decodeResource(getResources(),ta.getResourceId(attr,0));
            }
        }
        Log.e("CustomView","mBitmap == null"+(mBitmap == null?"true":"false"));
        ta.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//设置抗锯齿
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap != null) {
            if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
                vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                vectorDrawable.draw(canvas);
            }else{
                canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            }
        }
    }
}
