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
import org.moire.opensudoku.gui.inputmethod.IMControlPanelStatePersister.StateBundle;
import org.moire.opensudoku.utils.ThemeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMNumpad extends InputMethod {

    private static final int MODE_EDIT_VALUE = 0;
    private static final int MODE_EDIT_CORNER_NOTE = 1;
    private static final int MODE_EDIT_CENTRE_NOTE = 2;

    private boolean moveCellSelectionOnPress = true;
    private boolean mHighlightCompletedValues = true;
    private boolean mShowNumberTotals = false;
    private Cell mSelectedCell;

    private int mEditMode = MODE_EDIT_VALUE;

    private Map<Integer, Button> mNumberButtons;
    // Conceptually these behave like RadioButtons. However, it's difficult to style a RadioButton
    // without re-implementing all the drawables, and they would require a custom parent layout
    // to work properly in a ConstraintLayout, so it's simpler and more consistent in the UI to
    // handle the toggle logic in the code here.
    private ImageButton mEnterNumberButton;
    private ImageButton mCornerNoteButton;
    private ImageButton mCentreNoteButton;

    private ImageButton mClearButton;

    private OnClickListener mNumberButtonClick = v -> {
        int selNumber = (Integer) v.getTag();
        Cell selCell = mSelectedCell;

        if (selCell != null) {
            switch (mEditMode) {
                case MODE_EDIT_VALUE:
                    if (selNumber >= 0 && selNumber <= 9) {
                        mGame.setCellValue(selCell, selNumber);
                        mBoard.setHighlightedValue(selNumber);
                        if (isMoveCellSelectionOnPress()) {
                            mBoard.moveCellSelectionRight();
                        }
                    }
                    break;
                case MODE_EDIT_CORNER_NOTE:
                    if (selNumber == 0) {
                        mGame.setCellCornerNote(selCell, CellNote.EMPTY);
                    } else if (selNumber > 0 && selNumber <= 9) {
                        mGame.setCellCornerNote(selCell, selCell.getCornerNote().toggleNumber(selNumber));
                    }
                    break;
                case MODE_EDIT_CENTRE_NOTE:
                    if (selNumber == 0) {
                        mGame.setCellCentreNote(selCell, CellNote.EMPTY);
                    } else if (selNumber > 0 && selNumber <= 9) {
                        mGame.setCellCentreNote(selCell, selCell.getCentreNote().toggleNumber(selNumber));
                    }
                    break;
            }
        }
    };

    private OnClickListener mModeButtonClicked = v -> {
        mEditMode = (Integer) v.getTag();
        update();
    };

    private OnChangeListener mOnCellsChangeListener = () -> {
        if (mActive) {
            update();
        }
    };

    public boolean isMoveCellSelectionOnPress() {
        return moveCellSelectionOnPress;
    }

    public void setMoveCellSelectionOnPress(boolean moveCellSelectionOnPress) {
        this.moveCellSelectionOnPress = moveCellSelectionOnPress;
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

    @Override
    protected void initialize(Context context, IMControlPanel controlPanel,
                              SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
        super.initialize(context, controlPanel, game, board, hintsQueue);

        game.getCells().addOnChangeListener(mOnCellsChangeListener);
    }

    @Override
    protected View createControlPanelView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View controlPanel = inflater.inflate(R.layout.im_numpad, null);

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
        //mNumberButtons.put(0, controlPanel.findViewById(R.id.button_clear));

        for (Integer num : mNumberButtons.keySet()) {
            View b = mNumberButtons.get(num);
            b.setTag(num);
            b.setOnClickListener(mNumberButtonClick);
        }

        mClearButton = controlPanel.findViewById(R.id.button_clear);
        mClearButton.setTag(0);
        mClearButton.setOnClickListener(mNumberButtonClick);

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

    @Override
    public int getNameResID() {
        return R.string.numpad;
    }

    @Override
    public int getHelpResID() {
        return R.string.im_numpad_hint;
    }

    @Override
    public String getAbbrName() {
        return mContext.getString(R.string.numpad_abbr);
    }

    @Override
    protected void onActivated() {
        onCellSelected(mBoard.isReadOnly() ? null : mBoard.getSelectedCell());
    }

    @Override
    protected void onCellSelected(Cell cell) {
        if (cell != null) {
            mBoard.setHighlightedValue(cell.getValue());
        } else {
            mBoard.setHighlightedValue(0);
        }

        mSelectedCell = cell;
        update();
    }

    // TODO: Maybe refactor the common code between this an IMSingleNumber.java in to a parent
    // class. Or re-think how communication between the keyboard and the game state works.
    private void update() {
        CellNote note;
        List<Integer> notedNumbers;

        switch (mEditMode) {
            case MODE_EDIT_VALUE:
                // Keyboard should highlight the current value, which may be empty
                ThemeUtils.applyIMButtonStateToImageButton(mEnterNumberButton, ThemeUtils.IMButtonStyle.ACCENT);
                ThemeUtils.applyIMButtonStateToImageButton(mCornerNoteButton, ThemeUtils.IMButtonStyle.DEFAULT);
                ThemeUtils.applyIMButtonStateToImageButton(mCentreNoteButton, ThemeUtils.IMButtonStyle.DEFAULT);

                int selectedNumber = mSelectedCell == null ? 0 : mSelectedCell.getValue();
                for (Button b : mNumberButtons.values()) {
                    if (b.getTag().equals(selectedNumber)) {
                        ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT);
                    } else {
                        ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.DEFAULT);
                    }
                }

                break;
            case MODE_EDIT_CORNER_NOTE:
                // Keyboard should highlight all the buttons corresponding to active corner notes
                ThemeUtils.applyIMButtonStateToImageButton(mEnterNumberButton, ThemeUtils.IMButtonStyle.DEFAULT);
                ThemeUtils.applyIMButtonStateToImageButton(mCornerNoteButton, ThemeUtils.IMButtonStyle.ACCENT);
                ThemeUtils.applyIMButtonStateToImageButton(mCentreNoteButton, ThemeUtils.IMButtonStyle.DEFAULT);

                note = mSelectedCell == null ? new CellNote() : mSelectedCell.getCornerNote();
                notedNumbers = note.getNotedNumbers();
                for (Button b : mNumberButtons.values()) {
                    if (notedNumbers.contains(b.getTag())) {
                        ThemeUtils.applyIMButtonStateToView((TextView) b, ThemeUtils.IMButtonStyle.ACCENT);
                    } else {
                        ThemeUtils.applyIMButtonStateToView((TextView) b, ThemeUtils.IMButtonStyle.DEFAULT);
                    }
                }

                break;
            case MODE_EDIT_CENTRE_NOTE:
                // Keyboard should highlight all the buttons corresponding to active centre notes
                ThemeUtils.applyIMButtonStateToImageButton(mEnterNumberButton, ThemeUtils.IMButtonStyle.DEFAULT);
                ThemeUtils.applyIMButtonStateToImageButton(mCornerNoteButton, ThemeUtils.IMButtonStyle.DEFAULT);
                ThemeUtils.applyIMButtonStateToImageButton(mCentreNoteButton, ThemeUtils.IMButtonStyle.ACCENT);

                note = mSelectedCell == null ? new CellNote() : mSelectedCell.getCentreNote();
                notedNumbers = note.getNotedNumbers();
                for (Button b : mNumberButtons.values()) {
                    if (notedNumbers.contains(b.getTag())) {
                        ThemeUtils.applyIMButtonStateToView((TextView) b, ThemeUtils.IMButtonStyle.ACCENT);
                    } else {
                        ThemeUtils.applyIMButtonStateToView((TextView) b, ThemeUtils.IMButtonStyle.DEFAULT);
                    }
                }
                break;
        }

        Map<Integer, Integer> valuesUseCount = null;
        if (mHighlightCompletedValues || mShowNumberTotals)
            valuesUseCount = mGame.getCells().getValuesUseCount();

        if (mHighlightCompletedValues && mEditMode == MODE_EDIT_VALUE) {
            int selectedNumber = mSelectedCell == null ? 0 : mSelectedCell.getValue();
            for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                boolean highlightValue = entry.getValue() >= CellCollection.SUDOKU_SIZE;
                boolean selected = entry.getKey() == selectedNumber;
                View b = mNumberButtons.get(entry.getKey());
                if (highlightValue && !selected) {
                    ThemeUtils.applyIMButtonStateToView((TextView) b, ThemeUtils.IMButtonStyle.ACCENT_HIGHCONTRAST);
                }
            }
        }

        if (mShowNumberTotals) {
            for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                View b = mNumberButtons.get(entry.getKey());
                ((TextView) b).setText(entry.getKey() + " (" + entry.getValue() + ")");
            }
        }
    }

    @Override
    protected void onSaveState(StateBundle outState) {
        outState.putInt("editMode", mEditMode);
    }

    @Override
    protected void onRestoreState(StateBundle savedState) {
        mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
        if (isInputMethodViewCreated()) {
            update();
        }
    }
}
