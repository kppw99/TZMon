package org.blockinger2.game.engine;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import org.blockinger2.game.R;
import org.blockinger2.game.activities.GameActivity;

public class WorkThread extends Thread
{
    private final SurfaceHolder surfaceHolder;
    private boolean runFlag = false;
    private boolean firstTime = true;
    private long lastFrameStartingTime = 0;
    private int fpslimit;
    private long lastDelay;
    private GameActivity host;

    public WorkThread(GameActivity gameActivity, SurfaceHolder surfaceHolder)
    {
        host = gameActivity;
        this.surfaceHolder = surfaceHolder;
        fpslimit = host.getResources().getInteger(R.integer.fpslimit);
        lastDelay = 100;
    }

    public void setRunning(boolean run)
    {
        this.runFlag = run;
    }

    @Override
    public void run()
    {
        Canvas canvas;
        long tempTime = System.currentTimeMillis();

        long fpsUpdateTime = tempTime + 200;
        int frames = 0;
        int frameCounter[] = {0, 0, 0, 0, 0};
        int i = 0;

        while (this.runFlag) {
            if (firstTime) {
                firstTime = false;

                continue;
            }

            // FPS CONTROL
            tempTime = System.currentTimeMillis();

            // FPS limit
            long lastFrameDuration = tempTime - lastFrameStartingTime;
            //lastFrameDuration = lastFrameDuration / 10;

            if (lastFrameDuration > (1000.0f / fpslimit)) {
                lastDelay = Math.max(0, lastDelay - 25);
            } else {
                lastDelay += 25;
            }

            if (lastDelay != 0) {
                try {
                    Thread.sleep(lastDelay);
                } catch (InterruptedException e) {
                    //
                }
            }

            lastFrameStartingTime = tempTime;

            if (tempTime >= fpsUpdateTime) {
                i = (i + 1) % 5;
                fpsUpdateTime += 200;
                frames = frameCounter[0] + frameCounter[1] + frameCounter[2] + frameCounter[3] + frameCounter[4];
                frameCounter[i] = 0;
            }
            frameCounter[i]++;
            // END OF FPS CONTROL

            if (host.game.cycle(tempTime)) {
                host.controls.cycle();
            }

            host.game.getBoard().cycle(tempTime);

            canvas = null;

            try {
                canvas = this.surfaceHolder.lockCanvas(null);

                synchronized (this.surfaceHolder) {
                    host.display.doDraw(canvas, frames);
                }
            } finally {
                if (canvas != null) {
                    this.surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void setFirstTime(boolean firstTime)
    {
        this.firstTime = firstTime;
    }
}
