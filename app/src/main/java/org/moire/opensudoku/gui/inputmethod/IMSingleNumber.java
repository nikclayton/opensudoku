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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.moire.opensudoku.R;
import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.CellCollection.OnChangeListener;
import org.moire.opensudoku.game.CellNote;
import org.moire.opensudoku.game.SudokuGame;
import org.moire.opensudoku.gui.HintsQueue;
import org.moire.opensudoku.gui.SudokuBoardView;
import org.moire.opensudoku.gui.SudokuPlayActivity;
import org.moire.opensudoku.gui.inputmethod.IMControlPanelStatePersister.StateBundle;
import org.moire.opensudoku.utils.ThemeUtils;

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
    private static final int MODE_EDIT_CENTRE_NOTE = 2;

    private boolean mHighlightCompletedValues = true;
    private boolean mShowNumberTotals = false;
    private boolean mBidirectionalSelection = true;
    private boolean mHighlightSimilar = true;

    private int mSelectedNumber = 1;
    private int mEditMode = MODE_EDIT_VALUE;

    private Handler mGuiHandler;
    private Map<Integer, View> mNumberButtons;

    // Conceptually these behave like RadioButtons. However, it's difficult to style a RadioButton
    // without re-implementing all the drawables, and they would require a custom parent layout
    // to work properly in a ConstraintLayout, so it's simpler and more consistent in the UI to
    // handle the toggle logic in the code here.
    private ImageButton mEnterNumberButton;
    private ImageButton mCornerNoteButton;
    private ImageButton mCentreNoteButton;

    private SudokuPlayActivity.OnSelectedNumberChangedListener mOnSelectedNumberChangedListener = null;
    private View.OnTouchListener mNumberButtonTouched = (view, motionEvent) -> {
        mSelectedNumber = (Integer) view.getTag();
        onSelectedNumberChanged();
        update();
        return true;
    };
    private OnClickListener mNumberButtonClicked = v -> {
        mSelectedNumber = (Integer) v.getTag();
        onSelectedNumberChanged();
        update();
    };
    private OnChangeListener mOnCellsChangeListener = () -> {
        if (mActive) {
            update();
        }
    };

    private OnClickListener mModeButtonClicked = v -> {
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
        mNumberButtons.put(0, controlPanel.findViewById(R.id.button_clear));

        for (Integer num : mNumberButtons.keySet()) {
            View b = mNumberButtons.get(num);
            b.setTag(num);
            b.setOnClickListener(mNumberButtonClicked);
            b.setOnTouchListener(mNumberButtonTouched);
        }

        mEnterNumberButton = controlPanel.findViewById(R.id.enter_number);
        mEnterNumberButton.setTag(MODE_EDIT_VALUE);
        mEnterNumberButton.setOnClickListener(mModeButtonClicked);

        mCornerNoteButton = controlPanel.findViewById(R.id.corner_note);
        mCornerNoteButton.setTag(MODE_EDIT_CORNER_NOTE);
        mCornerNoteButton.setOnClickListener(mModeButtonClicked);

        mCentreNoteButton = controlPanel.findViewById(R.id.centre_note);
        mCentreNoteButton.setTag(MODE_EDIT_CENTRE_NOTE);
        mCentreNoteButton.setOnClickListener(mModeButtonClicked);

        return controlPanel;
    }

    private void update() {
        switch (mEditMode) {
            case MODE_EDIT_VALUE:
                ThemeUtils.applyIMButtonStateToImageButton(mEnterNumberButton, ThemeUtils.IMButtonStyle.ACCENT);
                ThemeUtils.applyIMButtonStateToImageButton(mCornerNoteButton, ThemeUtils.IMButtonStyle.DEFAULT);
                ThemeUtils.applyIMButtonStateToImageButton(mCentreNoteButton, ThemeUtils.IMButtonStyle.DEFAULT);
                break;
            case MODE_EDIT_CORNER_NOTE:
                ThemeUtils.applyIMButtonStateToImageButton(mEnterNumberButton, ThemeUtils.IMButtonStyle.DEFAULT);
                ThemeUtils.applyIMButtonStateToImageButton(mCornerNoteButton, ThemeUtils.IMButtonStyle.ACCENT);
                ThemeUtils.applyIMButtonStateToImageButton(mCentreNoteButton, ThemeUtils.IMButtonStyle.DEFAULT);
                break;
            case MODE_EDIT_CENTRE_NOTE:
                ThemeUtils.applyIMButtonStateToImageButton(mEnterNumberButton, ThemeUtils.IMButtonStyle.DEFAULT);
                ThemeUtils.applyIMButtonStateToImageButton(mCornerNoteButton, ThemeUtils.IMButtonStyle.DEFAULT);
                ThemeUtils.applyIMButtonStateToImageButton(mCentreNoteButton, ThemeUtils.IMButtonStyle.ACCENT);
                break;
        }

        // TODO: sometimes I change background too early and button stays in pressed state
        // this is just ugly workaround
        mGuiHandler.postDelayed(() -> {
            for (View b : mNumberButtons.values()) {
                Integer tag = (Integer) b.getTag();
                if (b.getTag().equals(mSelectedNumber)) {
                    if (mSelectedNumber == 0) {
                        ThemeUtils.applyIMButtonStateToImageButton((ImageButton)b, ThemeUtils.IMButtonStyle.ACCENT);
                    } else {
                        ((Button) b).setTextAppearance(mContext, ThemeUtils.getCurrentThemeStyle(mContext, android.R.attr.textAppearanceLarge));
                        ThemeUtils.applyIMButtonStateToView((TextView) b, ThemeUtils.IMButtonStyle.ACCENT);
                    }
                    b.requestFocus();
                } else {
                    if (tag.equals(0)) {
                        ThemeUtils.applyIMButtonStateToImageButton((ImageButton) b, ThemeUtils.IMButtonStyle.DEFAULT);
                    } else {
                        ((Button) b).setTextAppearance(mContext, ThemeUtils.getCurrentThemeStyle(mContext, android.R.attr.textAppearanceButton));
                        ThemeUtils.applyIMButtonStateToView((TextView) b, ThemeUtils.IMButtonStyle.DEFAULT);
                    }
                }
            }

            Map<Integer, Integer> valuesUseCount = null;
            if (mHighlightCompletedValues || mShowNumberTotals)
                valuesUseCount = mGame.getCells().getValuesUseCount();

            if (mHighlightCompletedValues) {
                for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                    boolean highlightValue = entry.getValue() >= CellCollection.SUDOKU_SIZE;
                    boolean selected = entry.getKey() == mSelectedNumber;
                    if (highlightValue && !selected) {
                        ThemeUtils.applyIMButtonStateToView((TextView) mNumberButtons.get(entry.getKey()), ThemeUtils.IMButtonStyle.ACCENT_HIGHCONTRAST);
                    }
                }
            }

            if (mShowNumberTotals) {
                for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                    View b = mNumberButtons.get(entry.getKey());
                    if (!b.getTag().equals(mSelectedNumber) && mSelectedNumber != 0)
                        ((Button) b).setText(entry.getKey() + " (" + entry.getValue() + ")");
                    else
                        ((Button) b).setText("" + entry.getKey());
                }
            }

            mBoard.setHighlightedValue(mBoard.isReadOnly() ? 0 : mSelectedNumber);
        }, 100);
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
                    mGame.setCellNote(cell, CellNote.EMPTY);
                } else if (selNumber > 0 && selNumber <= 9) {
                    CellNote newNote = cell.getNote().toggleNumber(selNumber);
                    mGame.setCellNote(cell, newNote);
                    // if we toggled the note off we want to de-select the cell
                    if (!newNote.hasNumber(selNumber)) {
                        mBoard.clearCellSelection();
                    }
                }
                break;
            case MODE_EDIT_VALUE:
                if (selNumber >= 0 && selNumber <= 9) {
                    if (!mNumberButtons.get(selNumber).isEnabled()) {
                        // Number requested has been disabled but it is still selected. This means that
                        // this number can be no longer entered, however any of the existing fields
                        // with this number can be deleted by repeated touch
                        if (selNumber == cell.getValue()) {
                            mGame.setCellValue(cell, 0);
                            mBoard.clearCellSelection();
                        }
                    } else {
                        // Normal flow, just set the value (or clear it if it is repeated touch)
                        if (selNumber == cell.getValue()) {
                            selNumber = 0;
                            mBoard.clearCellSelection();
                        }
                        mGame.setCellValue(cell, selNumber);
                    }
                }
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
