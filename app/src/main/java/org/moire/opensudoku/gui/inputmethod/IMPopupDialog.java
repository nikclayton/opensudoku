/*
 * Copyright (C) 2009 Roman Masek
 *
 * This file is part of OpenSudoku.
 *
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.moire.opensudoku.gui.inputmethod;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.button.MaterialButton;

import org.moire.opensudoku.R;
import org.moire.opensudoku.gui.NumberButton;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dialog for selecting and entering numbers and notes.
 *
 * When entering a number, the dialog automatically closes.
 *
 * When entering a note the dialog remains open, to allow multiple notes to be entered at once.
 */
public class IMPopupDialog extends Dialog {

    private Context mContext;
    private LayoutInflater mInflater;

    // TODO: These are common across all input methods
    private static final int MODE_EDIT_VALUE = 0;
    private static final int MODE_EDIT_CORNER_NOTE = 1;
    private static final int MODE_EDIT_CENTER_NOTE = 2;

    private int mEditMode = MODE_EDIT_VALUE;

    private final Map<Integer, NumberButton> mNumberButtons = new HashMap<>();

    // selected number on "Select number" tab (0 if nothing is selected).
    private int mSelectedNumber;
    private final Set<Integer> mCornerNoteSelectedNumbers = new HashSet<>();

    private final Set<Integer> mCenterNoteSelectedNumbers = new HashSet<>();

    private final MaterialButton mEnterNumberButton;
    private final MaterialButton mCornerNoteButton;
    private final MaterialButton mCenterNoteButton;

    private OnNumberEditListener mOnNumberEditListener;
    private OnNoteEditListener onNoteEditListener;

    private Map<Integer, Integer> mValueCount = new HashMap<>();

    private final View.OnClickListener mNumberButtonClicked = v -> {
        int number = (Integer) v.getTag();

        switch (mEditMode) {
            case MODE_EDIT_VALUE:
                mSelectedNumber = number;
                syncAndDismiss();
                break;
            case MODE_EDIT_CORNER_NOTE:
                if (((MaterialButton)v).isChecked()) {
                    mCornerNoteSelectedNumbers.add(number);
                } else {
                    mCornerNoteSelectedNumbers.remove(number);
                }
                break;
            case MODE_EDIT_CENTER_NOTE:
                if (((MaterialButton)v).isChecked()) {
                    mCenterNoteSelectedNumbers.add(number);
                } else {
                    mCenterNoteSelectedNumbers.remove(number);
                }
                break;
        }
    };

    /**
     * Occurs when user presses "Clear" button.
     */
    private final View.OnClickListener clearButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((MaterialButton) v).setChecked(false);
            switch (mEditMode) {
                case MODE_EDIT_VALUE:
                    mSelectedNumber = 0;
                    syncAndDismiss();
                    break;
                case MODE_EDIT_CORNER_NOTE:
                    // Clear the corner notes. Dialog should stay visible
                    setCornerNotes(Collections.emptyList());
                    break;
                case MODE_EDIT_CENTER_NOTE:
                    // Clear the center notes. Dialog should stay visible
                    setCenterNotes(Collections.emptyList());
                    break;

            }
            update();
        }
    };

    /**
     * Occurs when user presses "Close" button.
     */
    private final View.OnClickListener closeButtonListener = v -> syncAndDismiss();

    /** Synchronises state with the hosting activity and dismisses the dialog */
    private void syncAndDismiss() {
        if (mOnNumberEditListener != null) {
            mOnNumberEditListener.onNumberEdit(mSelectedNumber);
        }

        if (onNoteEditListener != null) {
            Integer[] numbers = new Integer[mCornerNoteSelectedNumbers.size()];
            onNoteEditListener.onCornerNoteEdit(mCornerNoteSelectedNumbers.toArray(numbers));
            numbers = new Integer[mCenterNoteSelectedNumbers.size()];
            onNoteEditListener.onCenterNoteEdit(mCenterNoteSelectedNumbers.toArray(numbers));
        }

        dismiss();
    }

    public IMPopupDialog(Context context) {
        super(context);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View keypad = mInflater.inflate(R.layout.im_popup_edit_value, null);

        mNumberButtons.put(1, keypad.findViewById(R.id.button_1));
        mNumberButtons.put(2, keypad.findViewById(R.id.button_2));
        mNumberButtons.put(3, keypad.findViewById(R.id.button_3));
        mNumberButtons.put(4, keypad.findViewById(R.id.button_4));
        mNumberButtons.put(5, keypad.findViewById(R.id.button_5));
        mNumberButtons.put(6, keypad.findViewById(R.id.button_6));
        mNumberButtons.put(7, keypad.findViewById(R.id.button_7));
        mNumberButtons.put(8, keypad.findViewById(R.id.button_8));
        mNumberButtons.put(9, keypad.findViewById(R.id.button_9));

        for (Integer num : mNumberButtons.keySet()) {
            View b = mNumberButtons.get(num);
            b.setTag(num);
            b.setOnClickListener(mNumberButtonClicked);
        }

        View clearButton = keypad.findViewById(R.id.button_clear);
        clearButton.setTag(0);
        clearButton.setOnClickListener(clearButtonListener);

        mEnterNumberButton = keypad.findViewById(R.id.enter_number);
        mEnterNumberButton.setTag(MODE_EDIT_VALUE);

        /* Switch mode, and update the UI */
        View.OnClickListener modeButtonClicked = v -> {
            mEditMode = (Integer) v.getTag();
            update();
        };

        mEnterNumberButton.setOnClickListener(modeButtonClicked);

        mCornerNoteButton = keypad.findViewById(R.id.corner_note);
        mCornerNoteButton.setTag(MODE_EDIT_CORNER_NOTE);
        mCornerNoteButton.setOnClickListener(modeButtonClicked);

        mCenterNoteButton = keypad.findViewById(R.id.center_note);
        mCenterNoteButton.setTag(MODE_EDIT_CENTER_NOTE);
        mCenterNoteButton.setOnClickListener(modeButtonClicked);

        View closeButton = keypad.findViewById(R.id.button_close);
        closeButton.setOnClickListener(closeButtonListener);

        setContentView(keypad);
    }

    private void update() {
        switch (mEditMode) {
            case MODE_EDIT_VALUE:
                mEnterNumberButton.setChecked(true);
                mCornerNoteButton.setChecked(false);
                mCenterNoteButton.setChecked(false);

                for (MaterialButton b: mNumberButtons.values()) {
                    b.setChecked(mSelectedNumber == (Integer) b.getTag());
                }
                break;
            case MODE_EDIT_CORNER_NOTE:
                mEnterNumberButton.setChecked(false);
                mCornerNoteButton.setChecked(true);
                mCenterNoteButton.setChecked(false);

                for (MaterialButton b: mNumberButtons.values()) {
                    b.setChecked(mCornerNoteSelectedNumbers.contains(b.getTag()));
                }
                break;
            case MODE_EDIT_CENTER_NOTE:
                mEnterNumberButton.setChecked(false);
                mCornerNoteButton.setChecked(false);
                mCenterNoteButton.setChecked(true);
                for (MaterialButton b: mNumberButtons.values()) {
                    b.setChecked(mCenterNoteSelectedNumbers.contains(b.getTag()));
                }
                break;
        }

        if (! mValueCount.isEmpty()) {
            for (NumberButton b: mNumberButtons.values()) {
                b.setNumbersPlaced(mValueCount.get(b.getTag()));
            }
        }
    }

    /**
     * Registers a callback to be invoked when number is selected.
     *
     * @param l
     */
    public void setOnNumberEditListener(OnNumberEditListener l) {
        mOnNumberEditListener = l;
    }

    /**
     * Register a callback to be invoked when note is edited.
     *
     * @param l
     */
    public void setOnNoteEditListener(OnNoteEditListener l) {
        onNoteEditListener = l;
    }

    /**
     * Reset most of the state of the dialog (selected values, notes, etc).
     *
     * DO NOT reset the edit mode, for compatibility with the previous code that used a tab.
     * The selected tab (which was the edit mode) was retained if the dialog was dismissed on
     * one cell and opened on another.
     */
    public void resetState() {
        mSelectedNumber = 0;
        mCornerNoteSelectedNumbers.clear();
        mCenterNoteSelectedNumbers.clear();
        mValueCount.clear();
        update();
    }

    // TODO: vsude jinde pouzivam misto number value
    public void setNumber(int number) {
        mSelectedNumber = number;
        update();
    }

    public void setCornerNotes(List<Integer> numbers) {
        mCornerNoteSelectedNumbers.clear();
        mCornerNoteSelectedNumbers.addAll(numbers);
        update();
    }

    public void setCenterNotes(List<Integer> numbers) {
        mCenterNoteSelectedNumbers.clear();
        mCenterNoteSelectedNumbers.addAll(numbers);
        update();
    }

    public void highlightNumber(int number) {
        NumberButton b = mNumberButtons.get(number);
        b.setChecked(number == mSelectedNumber);
    }

    public void setValueCount(Map<Integer, Integer> count) {
        mValueCount.clear();
        mValueCount.putAll(count);
        update();
    }

    /**
     * Interface definition for a callback to be invoked, when user selects a number, which
     * should be entered in the sudoku cell.
     */
    public interface OnNumberEditListener {
        boolean onNumberEdit(int number);
    }

    /**
     * Interface definition for a callback to be invoked, when user selects new note
     * content.
     */
    public interface OnNoteEditListener {
        boolean onCornerNoteEdit(Integer[] number);
        boolean onCenterNoteEdit(Integer[] number);
    }

}
