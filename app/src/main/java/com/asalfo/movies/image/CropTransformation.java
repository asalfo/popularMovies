package com.asalfo.movies.image;

/**
 * Created by asalfo on 15/01/16.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

public class CropTransformation implements Transformation {

    private int mWidth;
    private int mHeight;
    private CropType mCropType = CropType.CENTER;
    public CropTransformation() {
    }

    public CropTransformation(int width, int height) {
        this(width, height, CropType.CENTER);
    }

    public CropTransformation(int width, int height, CropType cropType) {
        mWidth = width;
        mHeight = height;
        mCropType = cropType;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        mWidth = mWidth == 0 ? source.getWidth() : mWidth;
        mHeight = mHeight == 0 ? source.getHeight() : mHeight;

        float scaleX = (float) mWidth / source.getWidth();
        float scaleY = (float) mHeight / source.getHeight();
        float scale = Math.max(scaleX, scaleY);

        float scaledWidth = scale * source.getWidth();
        float scaledHeight = scale * source.getHeight();
        float left = (mWidth - scaledWidth) / 2;
        float top = getTop(scaledHeight);
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, source.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(source, null, targetRect, null);
        source.recycle();

        return bitmap;
    }

    @Override
    public String key() {
        return "CropTransformation(width=" + mWidth + ", height=" + mHeight + ", cropType=" + mCropType
                + ")";
    }

    private float getTop(float scaledHeight) {
        switch (mCropType) {
            case TOP:
                return 0;
            case CENTER:
                return (mHeight - scaledHeight) / 2;
            case BOTTOM:
                return mHeight - scaledHeight;
            default:
                return 0;
        }
    }

    public enum CropType {
        TOP,
        CENTER,
        BOTTOM
    }
}