package org.blockinger2.game.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.blockinger2.game.R;
import org.blockinger2.game.components.Board;

public class Animator
{
    private static final int ANIMATION_STAGE_IDLE = 0;
    private static final int ANIMATION_STAGE_FLASH = 1;

    // Config
    private long flashInterval;
    private long flashFinishTime;
    private int squareSize;

    // State
    private long startTime;
    private int stage;
    private boolean drawEnable;
    private long nextFlash;

    // Data
    private Row row;
    private Bitmap bitmapRow;
    private int flashCount;
    private int rawFlashInterval;

    Animator(Context context, Row row)
    {
        rawFlashInterval = context.getResources().getInteger(R.integer.clear_animation_flash_interval);
        flashCount = context.getResources().getInteger(R.integer.clear_animation_flash_count);
        stage = ANIMATION_STAGE_IDLE;
        this.row = row;
        drawEnable = true;
        startTime = 0;
        flashFinishTime = 0;
        nextFlash = 0;
        flashInterval = 0;
        squareSize = 0;
    }

    void cycle(long time, Board board)
    {
        if (stage == ANIMATION_STAGE_IDLE) {
            return;
        }

        if (time >= flashFinishTime) {
            finish(board);
        } else if (time >= nextFlash) {
            nextFlash += flashInterval;
            drawEnable = !drawEnable;
            board.invalidate();
        }
    }

    public void start(Board board, int currentDropInterval)
    {
        bitmapRow = row.drawBitmap(squareSize);
        stage = ANIMATION_STAGE_FLASH;
        startTime = System.currentTimeMillis();
        flashInterval = Math.min(
            // Choose base flash interval on slow levels and shorter interval on fast levels.
            rawFlashInterval,
            (int) ((float) currentDropInterval / (float) flashCount)
        );
        flashFinishTime = startTime + 2 * flashInterval * flashCount;
        nextFlash = startTime + flashInterval;
        drawEnable = false;
        board.invalidate();
    }

    boolean finish(Board board)
    {
        if (ANIMATION_STAGE_IDLE == stage) {
            return false;
        }

        stage = ANIMATION_STAGE_IDLE;
        row.finishClear(board);
        drawEnable = true;

        return true;
    }

    void draw(int x, int y, int squareSize, Canvas canvas)
    {
        this.squareSize = squareSize;

        if (drawEnable) {
            if (stage == ANIMATION_STAGE_IDLE) {
                bitmapRow = row.drawBitmap(squareSize);
            }

            if (bitmapRow != null) {
                canvas.drawBitmap(bitmapRow, x, y, null);
            }
        }
    }
}
