package org.blockinger2.game.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import org.blockinger2.game.R;

public class Square
{
    public static final int TYPE_EMPTY = 0;
    private static final int TYPE_BLUE = 1;
    private static final int TYPE_ORANGE = 2;
    private static final int TYPE_YELLOW = 3;
    private static final int TYPE_RED = 4;
    private static final int TYPE_GREEN = 5;
    private static final int TYPE_MAGENTA = 6;
    private static final int TYPE_CYAN = 7;

    private int type;
    private Paint paint;
    private Bitmap bitmap;
    private Bitmap phantomBitmap;
    private int squareSize;
    private int phantomAlpha;

    public Square(int type, Context context)
    {
        this.type = type;
        paint = new Paint();
        phantomAlpha = context.getResources().getInteger(R.integer.phantom_alpha);
        squareSize = 0;

        switch (type) {
            case TYPE_BLUE:
                paint.setColor(context.getResources().getColor(R.color.square_blue));
                break;

            case TYPE_ORANGE:
                paint.setColor(context.getResources().getColor(R.color.square_orange));
                break;

            case TYPE_YELLOW:
                paint.setColor(context.getResources().getColor(R.color.square_yellow));
                break;
            case TYPE_RED:
                paint.setColor(context.getResources().getColor(R.color.square_red));
                break;

            case TYPE_GREEN:
                paint.setColor(context.getResources().getColor(R.color.square_green));
                break;

            case TYPE_MAGENTA:
                paint.setColor(context.getResources().getColor(R.color.square_magenta));
                break;
            case TYPE_CYAN:
                paint.setColor(context.getResources().getColor(R.color.square_cyan));
                break;

            case TYPE_EMPTY:
                return;

            default:
                // error: white
                paint.setColor(context.getResources().getColor(R.color.square_error));
                break;
        }
    }

    public void reDraw(int ss)
    {
        if (type == TYPE_EMPTY) {
            return;
        }

        squareSize = ss;
        bitmap = Bitmap.createBitmap(ss, ss, Bitmap.Config.ARGB_8888);
        phantomBitmap = Bitmap.createBitmap(ss, ss, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Canvas phantomCanvas = new Canvas(phantomBitmap);

        paint.setAlpha(255);
        canvas.drawRect(0, 0, squareSize, squareSize, paint);
        paint.setAlpha(phantomAlpha);
        phantomCanvas.drawRect(0, 0, squareSize, squareSize, paint);
    }

    public boolean isEmpty()
    {
        return type == TYPE_EMPTY;
    }

    public void draw(int x, int y, int squareSize, Canvas canvas, boolean isPhantom)
    {
        // Top left corner of square
        if (type == TYPE_EMPTY) {
            return;
        }

        if (this.squareSize != squareSize) {
            reDraw(squareSize);
        }

        if (isPhantom) {
            canvas.drawBitmap(phantomBitmap, x, y, null);
        } else {
            canvas.drawBitmap(bitmap, x, y, null);
        }
    }
}
