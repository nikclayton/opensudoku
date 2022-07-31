package org.moire.opensudoku.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.google.android.material.button.MaterialButton;

import org.moire.opensudoku.R;
import org.moire.opensudoku.utils.ThemeUtils;

/**
 * A button that displays a number the user can enter in to the grid.
 *
 * The display of the number on the button varies depending on the current edit mode.
 */
public class NumberButton extends MaterialButton {
    /** Paint when entering main numbers */
    private final Paint mEnterNumberPaint;

    /** Paint when entering center notes */
    private final Paint mCenterNotePaint;

    /** Paint when entering corner notes */
    private final Paint mCornerNotePaint;

    /** Paint for "numbers placed" count */
    private final Paint mNumbersPlacedPaint;

    // TODO: Repeats definitions in
    public static final int MODE_EDIT_VALUE = 0;
    public static final int MODE_EDIT_CORNER_NOTE = 1;
    public static final int MODE_EDIT_CENTER_NOTE = 2;

    /** Mode used to display numbers */
    private int mMode = MODE_EDIT_VALUE;

    /** Count of the number of times this number is placed in the puzzle */
    private String mNumbersPlaced = null;

    public NumberButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        mEnterNumberPaint = new Paint();
        mCenterNotePaint = new Paint();
        mCornerNotePaint = new Paint();
        mNumbersPlacedPaint = new Paint();

        mEnterNumberPaint.setAntiAlias(true);
        mCenterNotePaint.setAntiAlias(true);
        mCornerNotePaint.setAntiAlias(true);
        mNumbersPlacedPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int third = h / 3;
        mEnterNumberPaint.setTextSize(third * 2);
        mCornerNotePaint.setTextSize(third);
        mCenterNotePaint.setTextSize(third);
        mNumbersPlacedPaint.setTextSize(third / 2.0f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Rect bounds = new Rect();

        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getWidth() - getPaddingRight();
        int bottom = getHeight() - getPaddingBottom();

        float midX = (float) ((right-left)/2.0 + left);
        float midY = (float) ((bottom+top)/2.0);

        float textHeight;
        float textWidth;

        String number = getTag().toString();

        // On text measuring.
        //
        // The code uses getTextBounds() and measureText() to determine where to place the numbers.
        //
        // getTextBounds() gives the size of the minimum bounding rectangle, without any extra
        // top or bottom space for ascenders and descenders. This is necessary for vertical
        // centering of the numbers.
        //
        // The width of the minimum bounding rectangle may not reflect the actual width of the
        // number when used. This becomes apparent when trying to horizontally center the
        // numbers. measureText() returns a width that can be used for horizontal measurement,
        // and appears to be visually consistent with e.g., the dial pad, or calculator apps.
        switch (this.mMode) {
            case MODE_EDIT_VALUE:
                // Large numbers, vertically/horizontally centered, with optional small number at
                // the bottom right showing the placed count.
                if ("9".equals(mNumbersPlaced) && ! this.isChecked()) {
                    mEnterNumberPaint.setColor(ThemeUtils.getCurrentThemeColor(this.getContext(), R.attr.colorPrimary));
                } else {
                    mEnterNumberPaint.setColor(getCurrentTextColor());
                }
                mEnterNumberPaint.getTextBounds(number, 0, 1, bounds);
                textHeight = bounds.height();
                textWidth = mEnterNumberPaint.measureText(number, 0, 1);
                canvas.drawText(number, 0, 1, midX - (textWidth/2), midY+(textHeight/2), mEnterNumberPaint);

                if (mNumbersPlaced != null) {
                    if ("9".equals(mNumbersPlaced) && ! this.isChecked()) {
                        mNumbersPlacedPaint.setColor(ThemeUtils.getCurrentThemeColor(this.getContext(), R.attr.colorPrimary));
                    } else {
                        mNumbersPlacedPaint.setColor(getCurrentTextColor());
                    }
                    mNumbersPlacedPaint.getTextBounds(mNumbersPlaced, 0, 1, bounds);
                    textHeight = bounds.height();
                    textWidth = mNumbersPlacedPaint.measureText(mNumbersPlaced, 0, 1);
                    canvas.drawText(mNumbersPlaced, right - textWidth, bottom - textHeight, mNumbersPlacedPaint);
                }
                break;

            case MODE_EDIT_CORNER_NOTE:
                mCornerNotePaint.setColor(getCurrentTextColor());
                // Small numbers, vertically/horizontally centered, then offset based on col/row
                mCornerNotePaint.getTextBounds(number, 0, 1, bounds);
                textHeight = bounds.height();
                textWidth = mCornerNotePaint.measureText(number, 0, 1);

                // Move each number's location along the X/Y axis based on the col/row it is in.
                Integer tag = (Integer) getTag();
                int col = (tag - 1) % 3;
                int row = (tag - 1) / 3;

                // How far to move each number from the middle of the cell (as a percentage).
                float offsetPct = 0.25f;

                // Compute the offset. Results in offsets of 1 - offsetPct for the first column
                // and row, an offset of 1 for the center cell, and offsets of 1 + offsetPct for
                // the last column and row.
                float offsetX = ((col - 1) * offsetPct) + 1;
                float offsetY = ((row - 1) * offsetPct) + 1;
                canvas.drawText(number, 0, 1, (midX * offsetX) - (textWidth / 2),
                        (midY * offsetY) + (textHeight/2), mCornerNotePaint);

                break;
            case MODE_EDIT_CENTER_NOTE:
                mCenterNotePaint.setColor(getCurrentTextColor());
                // Small numbers, vertically/horizontally centered.
                mCenterNotePaint.getTextBounds(number, 0, 1, bounds);
                textHeight = bounds.height();
                textWidth = mCenterNotePaint.measureText(number, 0, 1);
                canvas.drawText(number, 0, 1, midX - (textWidth/2), midY+(textHeight/2), mCenterNotePaint);
                break;
        }
    }

    /** Sets the mode used for displaying numbers */
    public void setMode(int mode) {
        if (mMode == mode)
            return;

        mMode = mode;
        invalidate();
    }

    /** Sets the value to use for the count of placed numbers */
    public void setNumbersPlaced(int numbersRemaining) {
        mNumbersPlaced = String.valueOf(numbersRemaining);
        invalidate();
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        invalidate();
    }
}
