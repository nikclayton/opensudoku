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

package org.moire.opensudoku.game.command;

import org.moire.opensudoku.game.Cell;
import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.CellNote;

public class ClearAllNotesCommand extends AbstractMultiNoteCommand {

    public ClearAllNotesCommand() {
    }

    @Override
    void execute() {
        CellCollection cells = getCells();

        mOldCornerNotes.clear();
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                Cell cell = cells.getCell(r, c);
                CellNote cornerNote = cell.getCornerNote();
                CellNote centerNote = cell.getCenterNote();
                if (!cornerNote.isEmpty()) {
                    mOldCornerNotes.add(new NoteEntry(r, c, cornerNote));
                    mOldCenterNotes.add(new NoteEntry(r, c, centerNote));
                    cell.setCornerNote(new CellNote());
                    cell.setCenterNote(new CellNote());
                }
            }
        }
    }
}
