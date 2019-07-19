package org.blockinger2.game.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.blockinger2.game.components.Board;

public class Row
{
    private Row below; // Positive x direction
    private Row above; // Negative x direction
    private Square[] elements;
    private Square emptySquare;
    private int width;
    private Animator animator;
    private int fillStatus;

    public Row(int width, Context context)
    {
        emptySquare = new Square(Square.TYPE_EMPTY, context);
        animator = new Animator(context, this);
        this.width = width;
        below = null;
        above = null;
        fillStatus = 0;
        elements = new Square[width];

        for (int i = 0; i < width; i++) {
            elements[i] = emptySquare;
        }
    }

    public void set(Square square, int i)
    {
        if (square.isEmpty()) {
            return;
        }

        if ((i >= 0) && (i < width)) {
            fillStatus++;
            elements[i] = square;
        }
    }

    public Square get(int i)
    {
        if ((i >= 0) && (i < width)) {
            return elements[i];
        }

        return null;
    }

    public void setAbove(Row row)
    {
        this.above = row;
    }

    public void setBelow(Row row)
    {
        this.below = row;
    }

    public Row getAbove()
    {
        return this.above;
    }

    public Row getBelow()
    {
        return this.below;
    }

    public void draw(int x, int y, int squareSize, Canvas c)
    {
        // Top left corner of row
        animator.draw(x, y, squareSize, c);
    }

    public Bitmap drawBitmap(int squareSize)
    {
        // Top left corner of row
        Bitmap bitmap = Bitmap.createBitmap(width * squareSize, squareSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        for (int i = 0; i < width; i++) {
            if (elements[i] != null) {
                elements[i].draw(i * squareSize, 0, squareSize, canvas, false);
            }
        }

        return bitmap;
    }

    public boolean isFull()
    {
        return fillStatus >= width;
    }

    public void cycle(long time, Board board)
    {
        animator.cycle(time, board);
    }

    public void clear(Board board, int currentDropInterval)
    {
        animator.start(board, currentDropInterval);
    }

    void finishClear(Board board)
    {
        // Clear this row
        fillStatus = 0;

        for (int i = 0; i < width; i++) {
            elements[i] = emptySquare;
        }

        Row topRow = board.getTopRow();

        // Disconnect tempRow
        getAbove().setBelow(getBelow());
        getBelow().setAbove(getAbove());

        // Insert tempRow on top
        setBelow(topRow);
        setAbove(topRow.getAbove());
        topRow.getAbove().setBelow(this);
        topRow.setAbove(this);

        board.finishClear(this);
    }

    public boolean interrupt(Board board)
    {
        return animator.finish(board);
    }
}
