package com.sixsimplex.treasurehunt.revelocore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

public class VectorDrawableUtils {
    public static Drawable getDrawable(Context context, int drawableResId) {
        Drawable drawable = VectorDrawableCompat.create(context.getResources(), drawableResId, context.getTheme());
        Drawable mDrawableNew = drawable.getConstantState().newDrawable().mutate();
        return mDrawableNew;
    }

    public static Drawable getDrawable(Context context, int drawableResId, int colorFilter) {
        Drawable drawable = getDrawable(context, drawableResId);
        Drawable mDrawableNew = drawable.getConstantState().newDrawable().mutate();
        mDrawableNew.setColorFilter(colorFilter, PorterDuff.Mode.SRC_IN);
        return mDrawableNew;
    }

    public static Drawable getDrawable(Context context, int drawableResId, int colorFilter,int alpha) {
        Drawable drawable = getDrawable(context, drawableResId);
        Drawable mDrawableNew = drawable.getConstantState().newDrawable().mutate();
        mDrawableNew.setColorFilter(colorFilter, PorterDuff.Mode.SRC_IN);
        mDrawableNew.setAlpha(alpha);
        return mDrawableNew;
    }

    public static Drawable getDrawable(Context context, Drawable drawable, int colorFilter) {
        Drawable mDrawableNew = drawable.getConstantState().newDrawable().mutate();
        mDrawableNew.setColorFilter(colorFilter, PorterDuff.Mode.SRC_IN);
        return mDrawableNew;
    }
    public static BitmapDrawable writeOnDrawable(Context context, int drawableId, String text, int colorFilter){

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, 0, bm.getHeight()/2, paint);

        BitmapDrawable bitmapDrawable = new BitmapDrawable(bm);
        bitmapDrawable.setColorFilter(colorFilter,PorterDuff.Mode.SRC_IN);
        return bitmapDrawable;
    }
}
