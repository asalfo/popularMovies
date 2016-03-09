package com.asalfo.movies.ui;

/**
 * Created by asalfo on 24/02/16.
 */
public enum IconFontGlyph {
    PLAY_W_RING(Character.valueOf('\ue647'));

    private final Character unicodeChar;

    private IconFontGlyph(Character ch) {
        this.unicodeChar = ch;
    }

    public Character getUnicodeChar() {
        return this.unicodeChar;
    }
}
