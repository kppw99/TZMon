/**
 * 
 */
package com.learn.gamelearn;

import android.graphics.Canvas;

/**
 * @author Administrator
 *
 */
public class GameLoopThread extends Thread {

	private final long FPS = 20;
	private GameView view;
	private boolean running = false;
	Canvas c = null;
	public GameLoopThread(GameView view){
		this.view = view;
	}

	public void setRunning(boolean run){
		running = run;
	}

	@Override
	public void run(){
		long ticks = 1000/FPS;
		long startTime;
		long sleepTime;
		while (running){
			startTime = System.currentTimeMillis();
			try{

				c = view.getHolder().lockCanvas();
				if(c != null){
					synchronized (view.getHolder()) {
						view.onDraw(c);
					}
				}
			}finally{
				if (c != null){
					view.getHolder().unlockCanvasAndPost(c);
				}
			}
			sleepTime = ticks - (System.currentTimeMillis() - startTime);
			try {
				if (sleepTime > 0)
					sleep(sleepTime);
			} catch (Exception e) {}
		}
	}
}
