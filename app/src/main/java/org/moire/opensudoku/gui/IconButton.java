package org.moire.opensudoku.gui;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.button.MaterialButton;

/**
 * A button that displays an icon, may be checkable.
 *
 * The normal Material icon button assumes the button will still have text, and sizes the
 * icon to the size of the text.
 *
 * This button assumes there is no text, and sizes the icon to 2/3rds the height of the
 * button.
 */
public class IconButton extends MaterialButton {
    public IconButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int height = bottom - top;
        setIconSize((int) (height * (2.0/3.0)));
        super.onLayout(changed, left, top, right, bottom);
    }
}
