package com.learn.gamelearn;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Starship {

	private int x = 0;
	private int y = 0;
	private GameView view;
	private Bitmap bmp;
	private int left = 0;
	private int right = 0;
	private boolean move = false;
	//private boolean isFired = false;


	public Starship(GameView view, Bitmap bmp, int x, int y){

		this.view = view;
		this.bmp = bmp;
		this.x = x;
		this.y = y;
		this.left = x;
		this.right = y;

	}



	public void update(Bitmap bmp){
		this.bmp = bmp;
	}

	public void updateShip(int left, int right, boolean move){
		if(move){
			if(x>10 && left == 1 && right == 0)
				x-=5;
			else if(x < view.getWidth() - bmp.getWidth() - 10 && left == 0 && right == 1)
				x+=5;
		}			
	}

	public void onDraw(Canvas canvas){


		updateShip(left, right, move);
		
		canvas.drawBitmap(bmp, x, y, null);
	}

	public void setLeftRight(int left, int right, boolean move){
		this.left = left;
		this.right = right;
		this.move = move;
	}	

	
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
}
