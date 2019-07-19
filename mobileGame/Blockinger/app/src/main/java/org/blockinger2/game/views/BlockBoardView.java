package org.blockinger2.game.views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import org.blockinger2.game.activities.GameActivity;


public class BlockBoardView extends SurfaceView implements Callback
{
    private GameActivity host;

    public BlockBoardView(Context context)
    {
        super(context);
    }

    public BlockBoardView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
    }

    public BlockBoardView(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);
    }

    public void setHost(GameActivity gameActivity)
    {
        host = gameActivity;
    }

    public void init()
    {
        setZOrderOnTop(true);
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height)
    {
        //
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder)
    {
        host.startGame(this);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder)
    {
        host.destroyWorkThread();
    }
}

