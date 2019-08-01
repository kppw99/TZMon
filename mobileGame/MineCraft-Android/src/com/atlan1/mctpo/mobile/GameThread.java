package com.atlan1.mctpo.mobile;

import android.view.*;
import android.graphics.*;

public class GameThread extends Thread {
	
	private boolean running; 
	
	private SurfaceHolder surfaceHolder; 
	private MainGamePanel panel;
	
	MCTPO mctpo;
	
	private Canvas canvas;
	
	int fps = 0;

	public GameThread(SurfaceHolder surfaceHolder, MainGamePanel gamePanel) {
		super();
		this.surfaceHolder = surfaceHolder;
		this.panel = gamePanel; 
	}
	
	public void setRunning(boolean running) {
		this.running = running; 
	}

	@Override 
	public void run() {
		
		mctpo = new MCTPO(panel.getContext());
		
		while (running) {
			
			/*synchronized (panel) {
				panel.notify();
			}
			Thread.yield();*/
			
			mctpo.tick();
			
			canvas = null; 
			// try locking the canvas for exclusive pixel editing on the surface
			try {
				canvas = this.surfaceHolder.lockCanvas();
				
				mctpo.render(canvas);
				
				synchronized (surfaceHolder) {
					// update game state 
					// draws the canvas on the panel 
					this.panel.onDraw(canvas);
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				// in case of an exception the surface is not left in 
				// an inconsistent state
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
			
			
		}
	} 
	
}
