package com.learn.gamelearn;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class TempSprite {
	
	private int BMP_COLUMNS = 0;
	private int BMP_ROWS = 0;
	private int x = 0;
	private int y = 0;
	private Bitmap bmp;
	@SuppressWarnings("unused")
	private GameView view;
	private int currentFrame = -1;
	private int width = 0;
	private int height = 0;
	private int life = 8;
	private int updateCounter = 0;
	private List<TempSprite> temps;

	public TempSprite(List<TempSprite> temps, GameView view, Bitmap bmp,int BMP_ROWS, int BMP_COLUMNS, int x, int y){
		
		this.temps = temps;
		this.view = view;
		this.bmp = bmp;
		this.BMP_COLUMNS = BMP_COLUMNS;
		this.BMP_ROWS = BMP_ROWS;
		this.x = x;
		this.y = y;
		this.width = bmp.getWidth()/BMP_COLUMNS;
		this.height = bmp.getHeight() / this.BMP_ROWS;
		
	}
	
	public void update(){
		updateCounter++;
		if(updateCounter % 2 == 0){
			currentFrame = ++currentFrame % BMP_COLUMNS;
		}
		
		if(--life < 1){
			temps.remove(this);
			life = 16;
		}
	}
	
	public void onDraw(Canvas canvas){
		
		update();
		
		int srcX = currentFrame * width;
		int srcY = 0;
		
		Rect src = new Rect(srcX, srcY, srcX + width, srcY + height);
		Rect dst = new Rect(x, y, x + width, y + height);

		canvas.drawBitmap(bmp, src, dst, null);
	}
}
