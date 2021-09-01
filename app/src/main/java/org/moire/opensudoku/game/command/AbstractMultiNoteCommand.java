package org.moire.opensudoku.game.command;

import org.moire.opensudoku.game.CellCollection;
import org.moire.opensudoku.game.CellNote;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class AbstractMultiNoteCommand extends AbstractCellCommand {

    protected List<NoteEntry> mOldCornerNotes = new ArrayList<>();
    protected List<NoteEntry> mOldCentreNotes = new ArrayList<>();

    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        data.append(mOldCornerNotes.size()).append("|");

        for (NoteEntry ne : mOldCornerNotes) {
            data.append(ne.rowIndex).append("|");
            data.append(ne.colIndex).append("|");
            ne.note.serialize(data);
        }

        data.append(mOldCentreNotes.size()).append("|");
        for (NoteEntry ne: mOldCentreNotes) {
            data.append(ne.rowIndex).append("|");
            data.append(ne.colIndex).append("|");
            ne.note.serialize(data);
        }
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        int notesSize = Integer.parseInt(data.nextToken());
        for (int i = 0; i < notesSize; i++) {
            int row = Integer.parseInt(data.nextToken());
            int col = Integer.parseInt(data.nextToken());

            mOldCornerNotes.add(new NoteEntry(row, col, CellNote.deserialize(data.nextToken())));
        }

        // Might be no more tokens if deserializing data from before centre notes existed.
        if (!data.hasMoreTokens()) {
            return;
        }

        notesSize = Integer.parseInt(data.nextToken());
        for (int i = 0; i < notesSize; i++) {
            int row = Integer.parseInt(data.nextToken());
            int col = Integer.parseInt(data.nextToken());

            mOldCentreNotes.add(new NoteEntry(row, col, CellNote.deserialize(data.nextToken())));
        }
    }

    @Override
    void undo() {
        CellCollection cells = getCells();

        for (NoteEntry ne : mOldCornerNotes) {
            cells.getCell(ne.rowIndex, ne.colIndex).setCornerNote(ne.note);
        }

        for (NoteEntry ne: mOldCentreNotes) {
            cells.getCell(ne.rowIndex, ne.colIndex).setCentreNote(ne.note);
        }
    }

    protected void saveOldNotes() {
        CellCollection cells = getCells();
        for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
            for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
                mOldCornerNotes.add(new NoteEntry(r, c, cells.getCell(r, c).getCornerNote()));
                mOldCentreNotes.add(new NoteEntry(r, c, cells.getCell(r, c).getCentreNote()));
            }
        }
    }

    protected static class NoteEntry {
        public int rowIndex;
        public int colIndex;
        public CellNote note;

        public NoteEntry(int rowIndex, int colIndex, CellNote note) {
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
            this.note = note;
        }
    }
}
