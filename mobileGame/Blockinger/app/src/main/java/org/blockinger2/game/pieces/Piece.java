package org.blockinger2.game.pieces;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.blockinger2.game.R;
import org.blockinger2.game.components.Board;
import org.blockinger2.game.engine.Square;

public abstract class Piece
{
    static final int TYPE_J = 1; // Blue
    static final int TYPE_L = 2; // Orange
    static final int TYPE_O = 3; // Yellow
    static final int TYPE_Z = 4; // Red
    static final int TYPE_S = 5; // Green
    static final int TYPE_T = 6; // Magenta
    static final int TYPE_I = 7; // Cyan

    private boolean active;
    int x; // Pattern position
    int y; // Pattern position
    int dim; // Maximum dimensions for square matrix, so all rotations fit inside!
    private int squareSize;
    protected Square pattern[][]; // Square matrix
    Square rotated[][]; // Square matrix
    private Square emptySquare;
    private Bitmap bitmap;
    private Bitmap bitmapPhantom;
    private boolean isPhantom;

    protected Piece(Context context, int dimension)
    {
        this.dim = dimension;
        squareSize = 1;
        x = context.getResources().getInteger(R.integer.piece_start_x);
        y = 0;
        active = false;
        isPhantom = false;

        emptySquare = new Square(Square.TYPE_EMPTY, context);

        pattern = new Square[dim][dim]; // Empty piece
        rotated = new Square[dim][dim];

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                pattern[i][j] = emptySquare;
                rotated[i][j] = emptySquare;
            }
        }
    }

    public void reset(Context context)
    {
        x = context.getResources().getInteger(R.integer.piece_start_x);
        y = 0;
        active = false;

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                pattern[i][j] = emptySquare;
            }
        }
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;

        reDraw();
    }

    public void place(Board board)
    {
        active = false;

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (pattern[i][j] != null) {
                    board.set(x + j, y + i, pattern[i][j]);
                }
            }
        }
    }

    /*
     * Returns true if movement was successfull.
     */
    public boolean setPosition(int x, int y, boolean noInterrupt, Board board)
    {
        boolean collision;
        int leftOffset;
        int rightOffset;
        int bottomOffset;

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (pattern[i][j] != null) {
                    leftOffset = -(x + j);
                    rightOffset = (x + j) - (board.getWidth() - 1);
                    bottomOffset = (y + i) - (board.getHeight() - 1);

                    if (!pattern[i][j].isEmpty() && (leftOffset > 0)) {
                        // Left border violation
                        return false;
                    }

                    if (!pattern[i][j].isEmpty() && (rightOffset > 0)) {
                        // Right border violation
                        return false;
                    }

                    if (!pattern[i][j].isEmpty() && (bottomOffset > 0)) {
                        // Bottom border violation
                        return false;
                    }

                    if (board.get(x + j, y + i) != null) {
                        collision = (!pattern[i][j].isEmpty() && !board.get(x + j, y + i).isEmpty()); // Collision

                        if (collision) {
                            if (noInterrupt) {
                                return false;
                            }

                            // Try to avoid collision by interrupting all running clear animations.
                            board.interruptClearAnimation();
                            collision = !board.get(x + j, y + i).isEmpty(); // Still not empty?

                            if (collision) {
                                return false; // All hope is lost.
                            }
                        }
                    }
                }
            }
        }

        this.x = x;
        this.y = y;

        return true;
    }

    public abstract void turnLeft(Board board);

    public abstract void turnRight(Board board);

    /*
     * Returns true if movement to the left was successful.
     */
    public boolean moveLeft(Board board)
    {
        if (!active) {
            return true;
        }

        return setPosition(x - 1, y, false, board);
    }

    /*
     * Returns true if movement to the right was successful.
     */
    public boolean moveRight(Board board)
    {
        if (!active) {
            return true;
        }

        return setPosition(x + 1, y, false, board);
    }

    /*
     * Returns true if drop was successful. Otherwise the ground or other pieces was hit.
     */
    public boolean drop(Board board)
    {
        if (!active) {
            return true;
        }

        return setPosition(x, y + 1, false, board);
    }

    public int hardDrop(boolean noInterrupt, Board board)
    {
        int i = 0;

        while (setPosition(x, y + 1, noInterrupt, board)) {
            if (i >= board.getHeight()) {
                throw new RuntimeException("Hard Drop Error: dropped too far.");
            }

            i++;
        }

        return i;
    }

    protected void reDraw()
    {
        bitmap = Bitmap.createBitmap(squareSize * dim, squareSize * dim, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (pattern[i][j] != null && !pattern[i][j].isEmpty()) {
                    pattern[i][j].draw(j * squareSize, i * squareSize, squareSize, canvas, false);
                }
            }
        }

        bitmapPhantom = Bitmap.createBitmap(squareSize * dim, squareSize * dim, Bitmap.Config.ARGB_8888);
        Canvas canvasPhantom = new Canvas(bitmapPhantom);

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (pattern[i][j] != null && !pattern[i][j].isEmpty()) {
                    pattern[i][j].draw(j * squareSize, i * squareSize, squareSize, canvasPhantom, true);
                }

            }
        }
    }

    /*
     * Draw on current position.
     */
    public void drawOnBoard(int xOffset, int yOffset, int squareSize, Canvas canvas)
    {
        if (!active) {
            return;
        }

        if (squareSize != this.squareSize) {
            this.squareSize = squareSize;
            reDraw();
        }

        if (isPhantom) {
            canvas.drawBitmap(bitmapPhantom, x * this.squareSize + xOffset, y * this.squareSize + yOffset, null);
        } else {
            canvas.drawBitmap(bitmap, x * this.squareSize + xOffset, y * this.squareSize + yOffset, null);
        }
    }

    /*
     * Draw on preview position.
     */
    public void drawOnPreview(int x, int y, int squareSize, Canvas canvas)
    {
        if (squareSize != this.squareSize) {
            this.squareSize = squareSize;
            reDraw();
        }

        canvas.drawBitmap(bitmap, x, y, null);
    }

    public int getDim()
    {
        return dim;
    }

    public void setPhantom(boolean isPhantom)
    {
        this.isPhantom = isPhantom;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public void setPositionSimple(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public boolean setPositionSimpleCollision(int x, int y, Board board)
    {
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (pattern[i][j] != null) {
                    if (board.get(x + j, y + i) == null) {
                        if (!pattern[i][j].isEmpty()) {
                            return false;
                        }
                    } else {
                        if (!pattern[i][j].isEmpty() && !board.get(x + j, y + i).isEmpty()) {
                            return false;
                        }
                    }

                }
            }
        }

        this.x = x;
        this.y = y;

        return true;
    }
}
