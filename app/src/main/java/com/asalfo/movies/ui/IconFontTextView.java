package com.asalfo.movies.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by asalfo on 24/02/16.
 */
public class IconFontTextView extends TextView {
    private static final String TAG = "IconFontTextView";
    private static Typeface iconTypeface;

    public IconFontTextView(Context context) {
        super(context);
        init();
    }

    public IconFontTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public IconFontTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        if (iconTypeface == null) {
            iconTypeface = Typeface.createFromAsset(getContext().getAssets(), "nf-icon.ttf");
        }
        setTypeface(iconTypeface);
    }

    public void setToIcon(IconFontGlyph iconFontGlyph, int i) {
        int dimensionPixelOffset = getResources().getDimensionPixelOffset(i);

        setText(String.valueOf(iconFontGlyph.getUnicodeChar()));
        setTextSize((float) dimensionPixelOffset);
    }
}
