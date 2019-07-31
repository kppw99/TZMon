package com.learn.gamelearn;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Bullet {
	
	/**
	 * Declaring Variables
	 */

	private GameView view;
	private Bitmap bmp;
	private int x = 0;
	private int y = 0;
	private boolean isFired = false;
	private Sprite[][] sprite;
	private int[][] collision;
	private List<TempSprite> temps;
	private SoundManager sManager;
	
	/**
	 * Defining the Object
	 * @param context
	 * @param temps
	 * @param view
	 * @param bmp
	 * @param x
	 * @param y
	 * @param sprite
	 * @param collision
	 */
	public Bullet(Context context, List<TempSprite> temps, GameView view, Bitmap bmp, int x, int y, Sprite[][] sprite, int[][] collision){

		this.temps = temps;
		this.view = view;
		this.bmp = bmp;
		this.x = x;
		this.y = y;
		this.sprite = sprite;
		this.collision = collision;
		sManager = new SoundManager(context);
		sManager.addSound(1, R.raw.fire);
		sManager.addSound(2, R.raw.killenemy);
		sManager.addSound(3, R.raw.fire);

	}
	/**
	 * On draw method draws the sprites
	 * @param canvas
	 */
	public void onDraw(Canvas canvas){
		updateBullet();
		isCollision(sprite, collision);
		canvas.drawBitmap(bmp, x, y, null);

	}
	/**
	 * Update the state of the sprite.
	 */

	public void updateBullet(){
		if(isFired){
			if(y >= 50)
				y -= 25;
			else{
				x = view.getStarshipX() + 8;
				y = view.getStarshipY() + 10;
				sManager.playSound(3);
			}
		}
		else{
			if(y < view.getStarshipY() + 10){
				if(y >= 50)
					y -= 30;
				else{
					x = view.getStarshipX() + 8;
					y = view.getStarshipY() + 10;
				}
			}
			else{
				x = view.getStarshipX() + 8;
				y = view.getStarshipY() + 10;
			}
		}
	}
	
	/**
	 * Get & Set methods
	 * @param isFired
	 */

	public void setBulletFired(boolean isFired){
		this.isFired = isFired;
		if(x == view.getStarshipX() + 8 && y == view.getStarshipY() + 10)
			sManager.playSound(1);
	}

	public int getY(){
		return y;
	}

	public int getX(){
		return x;
	}

	public void setX(int x){
		this.x = x;
	}

	public void setY(int y){
		this.y = y;
	}

	public boolean isFired(){
		return isFired;
	}
	/**
	 * Get & set methods end.
	 */
	
	
	/**
	 * Collision Detection
	 * 
	 * @param sprite
	 * @param collision
	 */
	public void isCollision(Sprite[][] sprite, int[][] collision){

		for(int i =4; i>=0; i--){
			for(int j=7; j>=0; j--){
				if(collision[i][j] == 1){
					if(y <= sprite[i][j].getY() + sprite[i][j].getHeight() && y>= sprite[i][j].getY() && x>= sprite[i][j].getX()+6 && x<= sprite[i][j].getX() + sprite[i][j].getWidth()-6){

						synchronized (view.getHolder()) {
							view.setCollision(i, j, 0);
							x = view.getStarshipX() + 8;
							y = view.getStarshipY() + 10;
							sManager.playSound(2);
							temps.add(new TempSprite(temps, view, view.explosion1, 1, 4, sprite[i][j].getX() - 10, sprite[i][j].getY() + 40));
							if(i>=3)
								view.setScore(10);
							else if(i>=1 && i<3)
								view.setScore(20);
							else 
								view.setScore(40);
							
							if (view.isGameOver()){
								view.resetAliens();
							}
						}
					}
				}
			}
		}
	}

}
