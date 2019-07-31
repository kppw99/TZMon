package com.learn.gamelearn;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Sprite {

	@SuppressWarnings("unused")
	private int BMP_ROWS = 0;
	private int BMP_COLUMNS = 0;
	int updateCounter = 0;

	private int x = 50;
	private int y = 0;
	private int xSpeed = 2;
	private GameView gameView;
	private Bitmap bmp;
	private int currentFrame = 0;
	private int width = 0;
	private int height = 0;
	//private Bullet[] bullet;
	//private int[][] collision;
	int i,j =0;

	public Sprite(GameView gameView, Bitmap bmp, int BMP_ROWS, int BMP_COLUMNS, int x, int y){
		this.gameView = gameView;
		this.bmp = bmp;
		this.BMP_ROWS = BMP_ROWS;
		this.BMP_COLUMNS = BMP_COLUMNS;
		this.width = bmp.getWidth() / BMP_COLUMNS;
		this.height = bmp.getHeight() / BMP_ROWS;
		this.x = x;
		this.y = y;
		//this.bullet = bullet;
		//this.collision = colylision;
	}

	private void update(int i, int j) {

		updateCounter++;
		if (x > gameView.getWidth() - width - xSpeed - 150 + j*20) {
			xSpeed = -1;
			y+=2;
		}

		if (x + xSpeed < 10 +j*20) {
			xSpeed = 1;
			y+=2;
		}
		x = x + xSpeed;
		if(updateCounter % 5 == 0)
			currentFrame = ++currentFrame % BMP_COLUMNS;
	}

	public void onDraw(Canvas canvas,int i, int j) {

		this.i = i;
		this.j = j;
		update(i, j);
		int srcX = currentFrame * width;
		int srcY = (i %4)* height;
		//int srcY = 1 * height;
		Rect src = new Rect(srcX, srcY, srcX + width, srcY + height);
		Rect dst = new Rect(x , y +50, x + width, y + height+ 50);

		canvas.drawBitmap(bmp, src, dst, null);



	}

	



	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}

	public void putX(int value){
		x = value;
	}

	public void putY(int value){
		y = value;
	}

	public int getSpeed(){
		return xSpeed;
	}

	public void putSpeed(int speed){
		xSpeed = speed;
	}

	public int getHeight(){
		return height;
	}

	public int getWidth(){
		return width;
	}

	public void setBitmap(Bitmap bmp){
		this.bmp = bmp;
	}
}
