package org.blockinger2.game.components;

import android.R.color;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;

import org.blockinger2.game.R;
import org.blockinger2.game.activities.GameActivity;
import org.blockinger2.game.engine.Row;
import org.blockinger2.game.pieces.Piece;

public class Display extends Component
{
    private int prevPhantomY;
    private boolean dropPhantom;
    private Paint paint;
    private int gridRowBorder;
    private int gridColumnBorder;
    private int squaresize;
    private int rowOffset;
    private int rows;
    private int columnOffset;
    private int columns;
    private boolean landscapeInitialized;
    private int prevTop;
    private int prevBottom;
    private int prevLeft;
    private int prevRight;
    private int textLeft;
    private int textTop;
    private int textRight;
    private int textBottom;
    private int textLines;
    private int textSizeH;
    private int textEmptySpacing;
    private Paint textPaint;
    private Rect textRect;
    private int textHeight;
    private Paint popupTextPaint;

    public Display(GameActivity gameActivity)
    {
        super(gameActivity);

        invalidatePhantom();
        prevPhantomY = 0;
        landscapeInitialized = false;
        paint = new Paint();
        rows = host.getResources().getInteger(R.integer.rows);
        columns = host.getResources().getInteger(R.integer.columns);

        squaresize = 1; // Unknown at this point
        prevTop = 1; // Unknown at this point
        prevBottom = 1; // Unknown at this point
        prevLeft = 1; // Unknown at this point
        prevRight = 1; // Unknown at this point

        rowOffset = host.getResources().getInteger(R.integer.rows_offset);
        columnOffset = host.getResources().getInteger(R.integer.columns_offset);

        textPaint = new Paint();
        textRect = new Rect();
        textPaint.setColor(host.getResources().getColor(color.white));
        textPaint.setAlpha(host.getResources().getInteger(R.integer.text_alpha));
        textPaint.setAntiAlias(true);
        popupTextPaint = new Paint();
        popupTextPaint.setColor(host.getResources().getColor(color.white));
        popupTextPaint.setAntiAlias(true);
        popupTextPaint.setTextSize(120);
        textSizeH = 1;
        textHeight = 2;

        if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_show_fps", true)) {
            textLines = 10;
        } else {
            textLines = 8;
        }
    }

    public void doDraw(Canvas canvas, int fps)
    {
        if (canvas == null) {
            return;
        }

        if (!landscapeInitialized) {
            int fpsenabled = 0;

            if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_show_fps", true)) {
                fpsenabled = 1;
            }

            host.game.getBoard().invalidate();
            landscapeInitialized = true;
            squaresize = (((canvas.getHeight() - 1) - 2 * rowOffset) / rows);
            int size2 = (((canvas.getHeight() - 1) - 2 * columnOffset) / (columns + 4 + host.getResources()
                .getInteger(R.integer.padding_columns)));

            if (size2 < squaresize) {
                squaresize = size2;
                rowOffset = (((canvas.getHeight() - 1) - squaresize * rows) / 2);
            } else {
                columnOffset = (((canvas.getWidth() - 1) - squaresize * (host.getResources()
                    .getInteger(R.integer.padding_columns) + 4 + columns)) / 2);
            }

            gridRowBorder = rowOffset + squaresize * rows;
            gridColumnBorder = columnOffset + squaresize * columns;
            prevTop = rowOffset;
            prevBottom = rowOffset + 4 * squaresize;
            prevLeft = gridColumnBorder + host.getResources()
                .getInteger(R.integer.padding_columns) * squaresize;
            prevRight = prevLeft + 4 * squaresize;
            textLeft = prevLeft;
            textTop = prevBottom + 2 * squaresize;
            textRight = (canvas.getWidth() - 1) - columnOffset;
            textBottom = (canvas.getHeight() - 1) - rowOffset - squaresize;
            textSizeH = 1;

            // Adaptive text size setup
            textPaint.setTextSize(textSizeH + 1);

            while (textPaint.measureText("00:00:00") < (textRight - textLeft)) {
                textPaint.getTextBounds("Level:32", 0, 6, textRect);
                textHeight = textRect.height();
                textEmptySpacing = ((textBottom - textTop) - (textLines * (textHeight + 3))) / (3 + fpsenabled);

                if (textEmptySpacing < 10) {
                    break;
                }

                textSizeH++;
                textPaint.setTextSize(textSizeH + 1);
            }

            textPaint.setTextSize(textSizeH);
            textPaint.getTextBounds("Level:32", 0, 6, textRect);
            textHeight = textRect.height() + 3;
            textEmptySpacing = ((textBottom - textTop) - (textLines * (textHeight))) / (3 + fpsenabled);
        }

        // Background
        canvas.drawColor(Color.argb(0, 0, 0, 0), android.graphics.PorterDuff.Mode.CLEAR);

        host.game.getBoard().draw(columnOffset, rowOffset, squaresize, canvas);

        drawActive(columnOffset, rowOffset, squaresize, canvas);

        if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_phantom", false)) {
            drawPhantom(columnOffset, rowOffset, squaresize, canvas);
        }

        drawGrid(columnOffset, rowOffset, gridColumnBorder, gridRowBorder, canvas);

        drawPreview(prevLeft, prevTop, prevRight, prevBottom, canvas);

        drawTextFillBox(canvas, fps);

        if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_popup", true)) {
            drawPopupText(canvas);
        }
    }

    private void drawGrid(int x, int y, int xBorder, int yBorder, Canvas canvas)
    {
        paint.setColor(host.getResources().getColor(color.holo_blue_dark));

        for (int zeilePixel = 0; zeilePixel <= rows; zeilePixel++) {
            canvas.drawLine(x, y + zeilePixel * squaresize, xBorder, y + zeilePixel * squaresize, paint);
        }

        for (int spaltePixel = 0; spaltePixel <= columns; spaltePixel++) {
            canvas.drawLine(x + spaltePixel * squaresize, y, x + spaltePixel * squaresize, yBorder, paint);
        }

        // Draw border
        paint.setColor(host.getResources().getColor(color.background_light));
        canvas.drawLine(x, y, x, yBorder, paint);
        canvas.drawLine(x, y, xBorder, y, paint);
        canvas.drawLine(xBorder, yBorder, xBorder, y, paint);
        canvas.drawLine(xBorder, yBorder, x, yBorder, paint);
    }

    private void drawPreview(int left, int top, int right, int bottom, Canvas canvas)
    {
        // Piece
        drawPreview(left, top, squaresize, canvas);

        // Grid lines
        paint.setColor(host.getResources().getColor(color.holo_blue_dark));

        for (int zeilePixel = 0; zeilePixel <= 4; zeilePixel++) {
            canvas.drawLine(left, top + zeilePixel * squaresize, right, top + zeilePixel * squaresize, paint);
        }
        for (int spaltePixel = 0; spaltePixel <= 4; spaltePixel++) {
            canvas.drawLine(left + spaltePixel * squaresize, top, left + spaltePixel * squaresize, bottom, paint);
        }

        // Border
        paint.setColor(host.getResources().getColor(color.background_light));
        canvas.drawLine(left, top, right, top, paint);
        canvas.drawLine(left, top, left, bottom, paint);
        canvas.drawLine(right, bottom, right, top, paint);
        canvas.drawLine(right, bottom, left, bottom, paint);
    }

    private void drawTextFillBox(Canvas canvas, int fps)
    {
        // Draw level text
        canvas.drawText(host.getResources()
            .getString(R.string.level), textLeft, textTop + textHeight, textPaint);
        canvas.drawText(host.game.getLevelString(), textLeft, textTop + 2 * textHeight, textPaint);

        // Draw score text
        canvas.drawText(host.getResources()
            .getString(R.string.score), textLeft, textTop + 3 * textHeight + textEmptySpacing, textPaint);
        canvas.drawText(host.game.getScoreString(), textLeft, textTop + 4 * textHeight + textEmptySpacing, textPaint);

        // Draw time text
        canvas.drawText(host.getResources()
            .getString(R.string.time), textLeft, textTop + 5 * textHeight + 2 * textEmptySpacing, textPaint);
        canvas.drawText(host.game.getTimeString(), textLeft, textTop + 6 * textHeight + 2 * textEmptySpacing, textPaint);

        // Draw APM text
        canvas.drawText(host.getResources()
            .getString(R.string.apm), textLeft, textTop + 7 * textHeight + 3 * textEmptySpacing, textPaint);
        canvas.drawText(host.game.getAPMString(), textLeft, textTop + 8 * textHeight + 3 * textEmptySpacing, textPaint);

        // Draw FPS text
        if (PreferenceManager.getDefaultSharedPreferences(host).getBoolean("pref_show_fps", true)) {
            canvas.drawText(host.getResources()
                .getString(R.string.fps), textLeft, textTop + 9 * textHeight + 4 * textEmptySpacing, textPaint);
            canvas.drawText("" + fps, textLeft, textTop + 10 * textHeight + 4 * textEmptySpacing, textPaint);
        }
    }

    private void drawActive(int spaltenOffset, int zeilenOffset, int spaltenAbstand, Canvas canvas)
    {
        host.game.getActivePiece().drawOnBoard(spaltenOffset, zeilenOffset, spaltenAbstand, canvas);
    }

    private void drawPhantom(int spaltenOffset, int zeilenOffset, int spaltenAbstand, Canvas canvas)
    {
        Piece active = host.game.getActivePiece();
        int y = active.getY();
        int x = active.getX();
        active.setPhantom(true);

        if (dropPhantom) {
            int backup_currentRowIndex = host.game.getBoard().getCurrentRowIndex();
            Row backup_currentRow = host.game.getBoard().getCurrentRow();
            int cnt = y + 1;

            while (active.setPositionSimpleCollision(x, cnt, host.game.getBoard())) {
                cnt++;
            }

            host.game.getBoard().setCurrentRowIndex(backup_currentRowIndex);
            host.game.getBoard().setCurrentRow(backup_currentRow);
        } else {
            active.setPositionSimple(x, prevPhantomY);
        }

        prevPhantomY = active.getY();
        active.drawOnBoard(spaltenOffset, zeilenOffset, spaltenAbstand, canvas);
        active.setPositionSimple(x, y);
        active.setPhantom(false);
        dropPhantom = false;
    }

    private void drawPreview(int spaltenOffset, int zeilenOffset, int spaltenAbstand, Canvas canvas)
    {
        host.game.getPreviewPiece().drawOnPreview(spaltenOffset, zeilenOffset, spaltenAbstand, canvas);
    }

    private void drawPopupText(Canvas canvas)
    {
        final int offset = 6;
        final int diagonaloffset = 6;

        String text = host.game.getPopupString();
        popupTextPaint.setTextSize(host.game.getPopupSize());
        popupTextPaint.setColor(host.getResources().getColor(color.black));
        popupTextPaint.setAlpha(host.game.getPopupAlpha());

        int left = columnOffset + (columns * squaresize / 2) - ((int) popupTextPaint.measureText(text) / 2); // Middle minus half text width
        int top = canvas.getHeight() / 2;

        canvas.drawText(text, offset + left, top, popupTextPaint); // Right
        canvas.drawText(text, diagonaloffset + left, diagonaloffset + top, popupTextPaint); // Bottom right
        canvas.drawText(text, left, offset + top, popupTextPaint); // Bottom
        canvas.drawText(text, -diagonaloffset + left, diagonaloffset + top, popupTextPaint); // Bottom left
        canvas.drawText(text, -offset + left, top, popupTextPaint); // Left
        canvas.drawText(text, -diagonaloffset + left, -diagonaloffset + top, popupTextPaint); // Top left
        canvas.drawText(text, left, -offset + top, popupTextPaint); // Top
        canvas.drawText(text, diagonaloffset + left, -diagonaloffset + top, popupTextPaint); // Top right

        popupTextPaint.setColor(host.game.getPopupColor());
        popupTextPaint.setAlpha(host.game.getPopupAlpha());
        canvas.drawText(text, left, top, popupTextPaint);
    }

    void invalidatePhantom()
    {
        dropPhantom = true;
    }
}
