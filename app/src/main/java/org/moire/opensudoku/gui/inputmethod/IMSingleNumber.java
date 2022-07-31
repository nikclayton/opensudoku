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

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.material.button.MaterialButton;

import org.moire.opensudoku.R;
import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.CellCollection.OnChangeListener;
import org.moire.opensudoku.game.CellNote;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.gui.HintsQueue;
import org.moire.opensudoku.gui.NumberButton;
import org.moire.opensudoku.gui.SudokuBoardView;
import org.moire.opensudoku.gui.SudokuPlayActivity;
import org.moire.opensudoku.gui.inputmethod.IMControlPanelStatePersister.StateBundle;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents following type of number input workflow: Number buttons are displayed
 * in the sidebar, user selects one number and then fill values by tapping the cells.
 *
 * @author romario
 */
public class IMSingleNumber extends InputMethod {

    private static final int MODE_EDIT_VALUE = 0;
    private static final int MODE_EDIT_CORNER_NOTE = 1;
    private static final int MODE_EDIT_CENTER_NOTE = 2;

    private boolean mHighlightCompletedValues = true;
    private boolean mShowNumberTotals = false;
    private boolean mBidirectionalSelection = true;
    private boolean mHighlightSimilar = true;

    private int mSelectedNumber = 1;
    private int mEditMode = MODE_EDIT_VALUE;

    private Handler mGuiHandler;
    private Map<Integer, NumberButton> mNumberButtons;

    // Conceptually these behave like RadioButtons. However, it's difficult to style a RadioButton
    // without re-implementing all the drawables, and they would require a custom parent layout
    // to work properly in a ConstraintLayout, so it's simpler and more consistent in the UI to
    // handle the toggle logic in the code here.
    private MaterialButton mEnterNumberButton;
    private MaterialButton mCornerNoteButton;
    private MaterialButton mCenterNoteButton;
    private MaterialButton mClearButton;

    private SudokuPlayActivity.OnSelectedNumberChangedListener mOnSelectedNumberChangedListener = null;
    private final View.OnTouchListener mNumberButtonTouched = (view, motionEvent) -> {
        view.performClick();
        mSelectedNumber = (Integer) view.getTag();
        onSelectedNumberChanged();
        update();
        return true;
    };
    private final OnClickListener mNumberButtonClicked = v -> {
        mSelectedNumber = (Integer) v.getTag();
        onSelectedNumberChanged();
        update();
    };
    private final OnChangeListener mOnCellsChangeListener = () -> {
        if (mActive) {
            update();
        }
    };

    private final OnClickListener mModeButtonClicked = v -> {
        mEditMode = (Integer) v.getTag();
        update();
    };

    public IMSingleNumber() {
        super();

        mGuiHandler = new Handler();
    }

    public boolean getHighlightCompletedValues() {
        return mHighlightCompletedValues;
    }

    /**
     * If set to true, buttons for numbers, which occur in {@link CellCollection}
     * more than {@link CellCollection#SUDOKU_SIZE}-times, will be highlighted.
     *
     * @param highlightCompletedValues
     */
    public void setHighlightCompletedValues(boolean highlightCompletedValues) {
        mHighlightCompletedValues = highlightCompletedValues;
    }

    public boolean getShowNumberTotals() {
        return mShowNumberTotals;
    }

    public void setShowNumberTotals(boolean showNumberTotals) {
        mShowNumberTotals = showNumberTotals;
    }

    public boolean getBidirectionalSelection() {
        return mBidirectionalSelection;
    }

    public void setBidirectionalSelection(boolean bidirectionalSelection) {
        mBidirectionalSelection = bidirectionalSelection;
    }

    public boolean getHighlightSimilar() {
        return mHighlightSimilar;
    }

    public void setHighlightSimilar(boolean highlightSimilar) {
        mHighlightSimilar = highlightSimilar;
    }

    public void setmOnSelectedNumberChangedListener(SudokuPlayActivity.OnSelectedNumberChangedListener l) {
        mOnSelectedNumberChangedListener = l;
    }

    @Override
    protected void initialize(Context context, IMControlPanel controlPanel,
                              SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
        super.initialize(context, controlPanel, game, board, hintsQueue);

        game.getCells().addOnChangeListener(mOnCellsChangeListener);
    }

    @Override
    public int getNameResID() {
        return R.string.single_number;
    }

    @Override
    public int getHelpResID() {
        return R.string.im_single_number_hint;
    }

    @Override
    public String getAbbrName() {
        return mContext.getString(R.string.single_number_abbr);
    }

    @Override
    protected View createControlPanelView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View controlPanel = inflater.inflate(R.layout.im_single_number, null);

        mNumberButtons = new HashMap<>();
        mNumberButtons.put(1, controlPanel.findViewById(R.id.button_1));
        mNumberButtons.put(2, controlPanel.findViewById(R.id.button_2));
        mNumberButtons.put(3, controlPanel.findViewById(R.id.button_3));
        mNumberButtons.put(4, controlPanel.findViewById(R.id.button_4));
        mNumberButtons.put(5, controlPanel.findViewById(R.id.button_5));
        mNumberButtons.put(6, controlPanel.findViewById(R.id.button_6));
        mNumberButtons.put(7, controlPanel.findViewById(R.id.button_7));
        mNumberButtons.put(8, controlPanel.findViewById(R.id.button_8));
        mNumberButtons.put(9, controlPanel.findViewById(R.id.button_9));

        for (Integer num : mNumberButtons.keySet()) {
            View b = mNumberButtons.get(num);
            b.setTag(num);
            b.setOnClickListener(mNumberButtonClicked);
            b.setOnTouchListener(mNumberButtonTouched);
        }

        mClearButton = controlPanel.findViewById(R.id.button_clear);
        mClearButton.setTag(0);
        mClearButton.setOnClickListener(mNumberButtonClicked);

        mEnterNumberButton = controlPanel.findViewById(R.id.enter_number);
        mEnterNumberButton.setTag(MODE_EDIT_VALUE);
        mEnterNumberButton.setOnClickListener(mModeButtonClicked);

        mCornerNoteButton = controlPanel.findViewById(R.id.corner_note);
        mCornerNoteButton.setTag(MODE_EDIT_CORNER_NOTE);
        mCornerNoteButton.setOnClickListener(mModeButtonClicked);

        mCenterNoteButton = controlPanel.findViewById(R.id.center_note);
        mCenterNoteButton.setTag(MODE_EDIT_CENTER_NOTE);
        mCenterNoteButton.setOnClickListener(mModeButtonClicked);

        return controlPanel;
    }

    private void update() {
        switch (mEditMode) {
            case MODE_EDIT_VALUE:
                mEnterNumberButton.setChecked(true);
                mCornerNoteButton.setChecked(false);
                mCenterNoteButton.setChecked(false);
                break;
            case MODE_EDIT_CORNER_NOTE:
                mEnterNumberButton.setChecked(false);
                mCornerNoteButton.setChecked(true);
                mCenterNoteButton.setChecked(false);
                break;
            case MODE_EDIT_CENTER_NOTE:
                mEnterNumberButton.setChecked(false);
                mCornerNoteButton.setChecked(false);
                mCenterNoteButton.setChecked(true);
                break;
        }

        for (NumberButton b : mNumberButtons.values()) {
            Integer tag = (Integer) b.getTag();
            b.setMode(mEditMode);
            if (b.getTag().equals(mSelectedNumber)) {
                b.setChecked(true);
                b.requestFocus();
            } else {
                b.setChecked(false);
            }
        }

        mClearButton.setChecked(mSelectedNumber == 0);

        // TODO: This is identical across IMSingleNumber.java and IMNumberPad.java
        Map<Integer, Integer> valuesUseCount = null;
        if (mHighlightCompletedValues || mShowNumberTotals)
            valuesUseCount = mGame.getCells().getValuesUseCount();

        if (mHighlightCompletedValues) {
            for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                boolean highlightValue = entry.getValue() >= CellCollection.SUDOKU_SIZE;
                boolean selected = entry.getKey() == mSelectedNumber;
                // TODO: This should probably set the disabled state on the button
                //ThemeUtils.applyIMButtonStateToView((TextView) mNumberButtons.get(entry.getKey()), ThemeUtils.IMButtonStyle.ACCENT_HIGHCONTRAST);
            }
        }

        // TODO: This is identical across IMSingleNumber.java and IMNumberPad.java
        if (mShowNumberTotals) {
            for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                NumberButton b = mNumberButtons.get(entry.getKey());
                b.setNumbersPlaced(entry.getValue());
            }
        }

        mBoard.setHighlightedValue(mBoard.isReadOnly() ? 0 : mSelectedNumber);
    }

    @Override
    protected void onActivated() {
        update();
    }

    @Override
    protected void onCellSelected(Cell cell) {
        super.onCellSelected(cell);

        if (mBidirectionalSelection && cell != null) {
            int v = cell.getValue();
            if (v != 0 && v != mSelectedNumber) {
                mSelectedNumber = cell.getValue();
                update();
            }
        }

        mBoard.setHighlightedValue(mSelectedNumber);
    }

    private void onSelectedNumberChanged() {
        if (mBidirectionalSelection && mHighlightSimilar && mOnSelectedNumberChangedListener != null && !mBoard.isReadOnly()) {
            mOnSelectedNumberChangedListener.onSelectedNumberChanged(mSelectedNumber);
            mBoard.setHighlightedValue(mSelectedNumber);
        }
    }

    @Override
    protected void onCellTapped(Cell cell) {
        int selNumber = mSelectedNumber;

        switch (mEditMode) {
            case MODE_EDIT_CORNER_NOTE:
                if (selNumber == 0) {
                    mGame.setCellCornerNote(cell, CellNote.EMPTY);
                } else if (selNumber > 0 && selNumber <= 9) {
                    CellNote newNote = cell.getCornerNote().toggleNumber(selNumber);
                    mGame.setCellCornerNote(cell, newNote);
                    // if we toggled the note off we want to de-select the cell
                    if (!newNote.hasNumber(selNumber)) {
                        mBoard.clearCellSelection();
                    }
                }
                break;
            case MODE_EDIT_CENTER_NOTE:
                if (selNumber == 0) {
                    mGame.setCellCenterNote(cell, CellNote.EMPTY);
                } else if (selNumber > 0 && selNumber <= 9) {
                    CellNote newNote = cell.getCenterNote().toggleNumber(selNumber);
                    mGame.setCellCenterNote(cell, newNote);
                    if (!newNote.hasNumber(selNumber)) {
                        mBoard.clearCellSelection();
                    }
                }
                break;
            case MODE_EDIT_VALUE:
                // Normal flow, just set the value (or clear it if it is repeated touch)
                if (selNumber == cell.getValue()) {
                    selNumber = 0;
                    mBoard.clearCellSelection();
                }
                mGame.setCellValue(cell, selNumber);

                break;
        }

    }

    @Override
    protected void onSaveState(StateBundle outState) {
        outState.putInt("selectedNumber", mSelectedNumber);
        outState.putInt("editMode", mEditMode);
    }

    @Override
    protected void onRestoreState(StateBundle savedState) {
        mSelectedNumber = savedState.getInt("selectedNumber", 1);
        mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
        if (isInputMethodViewCreated()) {
            update();
        }
    }

}
