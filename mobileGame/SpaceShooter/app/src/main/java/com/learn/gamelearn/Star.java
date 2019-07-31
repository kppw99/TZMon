package com.learn.gamelearn;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Star {

	int[] colors;
	Random rnd;
	Paint starPaint;
	private GameView view;
	int x = 0;
	int y = 0;
	int updateCounter = 0;


	public Star(GameView view, int y){

		rnd = new Random();
		this.view = view;
		//this.x = rnd.nextInt(view.getWidth());

		this.y = y;

	}





	public void update(){
		updateCounter++;
		if(updateCounter % 3 == 0){
			x = rnd.nextInt(view.getWidth());
			y--;
		}
		if (y <= 50)
			y = view.getHeight()-100;


	}
	public void onDraw(Canvas canvas){
		update();
		colors = new int[7];
		//rnd = new Random()
		colors[0] = Color.WHITE;
		colors[1] = Color.BLUE;
		colors[2] = Color.CYAN;
		colors[3] = Color.GREEN;
		colors[4] = Color.MAGENTA;
		colors[5] = Color.RED;
		colors[6] = Color.YELLOW;
		starPaint = new Paint();
		int color = rnd.nextInt(colors.length);
		starPaint.setColor(colors[color]);
		canvas.drawPoint((float) x, (float) y , starPaint);

	}
}
