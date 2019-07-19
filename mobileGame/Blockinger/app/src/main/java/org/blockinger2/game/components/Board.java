package org.blockinger2.game.components;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.blockinger2.game.R;
import org.blockinger2.game.activities.GameActivity;
import org.blockinger2.game.engine.Row;
import org.blockinger2.game.engine.Square;

public class Board extends Component
{
    private int height;
    private int width;
    private Row topRow; // Index 0
    private Row currentRow;
    private int currentIndex;
    private Row tempRow;
    private boolean valid;
    private Bitmap blockBitmap;

    Board(GameActivity gameActivity)
    {
        super(gameActivity);

        width = host.getResources().getInteger(R.integer.columns);
        height = host.getResources().getInteger(R.integer.rows);
        valid = false;

        // Initialise board
        topRow = new Row(width, host);
        currentIndex = 0;
        tempRow = topRow;
        currentRow = topRow;

        for (int i = 1; i < height; i++) {
            currentRow = new Row(width, host);
            currentIndex = i;
            currentRow.setAbove(tempRow);
            tempRow.setBelow(currentRow);
            tempRow = currentRow;
        }

        topRow.setAbove(currentRow);
        currentRow.setBelow(topRow);
    }

    void draw(int x, int y, int squareSize, Canvas canvas)
    {
        // Top left corner of game board
        if (topRow == null) {
            throw new RuntimeException("BlockBoard was not initialized!");
        }

        if (valid) {
            canvas.drawBitmap(blockBitmap, x, y, null);

            return;
        }

        // Prevent the "java.lang.OutOfMemoryError: bitmap size exceeds VM budget" crash.
        try {
            blockBitmap = Bitmap.createBitmap(width * squareSize, height * squareSize, Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            valid = false;
            tempRow = topRow;

            for (int i = 0; i < height; i++) {
                if (tempRow != null) {
                    canvas.drawBitmap(tempRow.drawBitmap(squareSize), x, y + i * squareSize, null);
                    tempRow = tempRow.getBelow();
                }
            }

            return;
        }

        Canvas blockCanvas = new Canvas(blockBitmap);
        valid = true;
        tempRow = topRow;

        for (int i = 0; i < height; i++) {
            if (tempRow != null) {
                tempRow.draw(0, i * squareSize, squareSize, blockCanvas);
                tempRow = tempRow.getBelow();
            }
        }

        canvas.drawBitmap(blockBitmap, x, y, null);
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public Square get(int x, int y)
    {
        if (x < 0 || x > (width - 1)) {
            return null;
        }

        if (y < 0 || y > (height - 1)) {
            return null;
        }

        if (currentIndex == y) {
            return currentRow.get(x);
        } else if (currentIndex < y) {
            if (currentRow.getBelow() == null) {
                return null;
            } else {
                currentRow = currentRow.getBelow();
                currentIndex++;

                return get(x, y);
            }
        } else {
            if (currentRow.getAbove() == null) {
                return null;
            } else {
                currentRow = currentRow.getAbove();
                currentIndex--;

                return get(x, y);
            }
        }
    }

    public void set(int x, int y, Square square)
    {
        if (x < 0 || x > (width - 1)) {
            return;
        }

        if (y < 0 || y > (height - 1)) {
            return;
        }

        if (square == null || square.isEmpty()) {
            return;
        }

        valid = false;

        if (currentIndex == y) {
            currentRow.set(square, x);
        } else if (currentIndex < y) {
            currentRow = currentRow.getBelow();
            currentIndex++;
            set(x, y, square);
        } else {
            currentRow = currentRow.getAbove();
            currentIndex--;
            set(x, y, square);
        }
    }

    public void cycle(long time)
    {
        // Begin at bottom line
        if (topRow == null) {
            throw new RuntimeException("BlockBoard was not initialized!");
        }

        tempRow = topRow.getAbove();

        for (int i = 0; i < height; i++) {
            tempRow.cycle(time, this);
            tempRow = tempRow.getAbove();

            if (tempRow == null) {
                return;
            }
        }
    }

    int clearLines(int dim)
    {
        valid = false;
        Row clearPointer = currentRow;
        int clearCounter = 0;

        for (int i = 0; i < dim; i++) {
            if (clearPointer.isFull()) {
                clearCounter++;
                clearPointer.clear(this, host.game.getAutoDropInterval());
            }
            clearPointer = clearPointer.getAbove();
        }

        currentRow = topRow;
        currentIndex = 0;

        return clearCounter;
    }

    public Row getTopRow()
    {
        return topRow;
    }

    public void finishClear(Row row)
    {
        valid = false;
        topRow = row;
        currentIndex++;
        host.display.invalidatePhantom();
    }

    public void interruptClearAnimation()
    {
        // Begin at bottom line
        if (topRow == null) {
            throw new RuntimeException("BlockBoard was not initialized!");
        }

        Row interator = topRow.getAbove();

        for (int i = 0; i < height; i++) {
            if (interator.interrupt(this)) {
                interator = topRow.getAbove();
                i = 0;
                valid = false;
            } else {
                interator = interator.getAbove();
            }

            if (interator == null) {
                return;
            }
        }

        host.display.invalidatePhantom();
    }

    public void invalidate()
    {
        valid = false;
    }

    int getCurrentRowIndex()
    {
        return currentIndex;
    }

    Row getCurrentRow()
    {
        return currentRow;
    }

    void setCurrentRowIndex(int index)
    {
        currentIndex = index;
    }

    void setCurrentRow(Row row)
    {
        currentRow = row;
    }
}
