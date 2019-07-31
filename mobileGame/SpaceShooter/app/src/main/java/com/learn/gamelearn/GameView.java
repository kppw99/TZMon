/**
 * 
 */
package com.learn.gamelearn;
/**
 * Package imports.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * @author Administrator
 *
 */
public class GameView extends SurfaceView {

	private SurfaceHolder holder;
	Bitmap leftbutton;
	Bitmap touchedleftbutton;
	Bitmap rightbutton;
	Bitmap touchedrightbutton;
	Bitmap alien;
	Bitmap ship;
	Bitmap shootbutton;
	Bitmap shoottouched;
	Bitmap shootbullet;
	Bitmap explosion;
	Bitmap explosion1;
	private GameLoopThread loop;
	private Sprite[][] sprite;
	private Starship leftButton;
	private Starship rightButton;
	private Starship starship;
	private Starship shootButton;
	private Bullet bullet;
	private List<TempSprite> temps = new ArrayList<TempSprite>();
	private ArrayList<Integer> highScore = new ArrayList<Integer>();
	private Context context;
	String serializedCollision = "";
	int x = 50;
	int y = 30;
	int score = 0;
	int level = 1;
	String displayScore;
	private Star[][] star;
	
	int[][] collision;
	Random rnd;
	int touchX[];
	int touchY[];
	Paint paint;

	//private Sprite alienSprite;
	public GameView(Context context) {
		super(context);
		loop = new GameLoopThread(this);
		this.context = context;

		// TODO Auto-generated constructor stub
		holder = getHolder();
		holder.addCallback(new Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				boolean retry = true;
				loop.setRunning(false);
				while (retry){
					try{
						loop.join();
						retry = false;
					} catch (InterruptedException e) {
					}
				}
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// TODO Auto-generated method stub

				createStar();

				loop.setRunning(true);
				loop.start();
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
				// TODO Auto-generated method stub

			}
		});
		rnd = new Random();
		alien = BitmapFactory.decodeResource(getResources(), R.drawable.aliens);
		leftbutton = BitmapFactory.decodeResource(getResources(), R.drawable.leftbutton);
		touchedleftbutton = BitmapFactory.decodeResource(getResources(), R.drawable.leftbuttonclicked);
		rightbutton = BitmapFactory.decodeResource(getResources(), R.drawable.rightbutton);
		touchedrightbutton = BitmapFactory.decodeResource(getResources(), R.drawable.rightbuttonclickedd);
		ship = BitmapFactory.decodeResource(getResources(), R.drawable.starship);
		shootbutton = BitmapFactory.decodeResource(getResources(), R.drawable.shoot);
		shoottouched = BitmapFactory.decodeResource(getResources(), R.drawable.shootclicked);
		shootbullet = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);
		explosion = BitmapFactory.decodeResource(getResources(), R.drawable.alienexplode);
		explosion1 = BitmapFactory.decodeResource(getResources(), R.drawable.alienexplode1);
		
		sprite = new Sprite[5][8];
		for(int i = 0; i<5; i++){
			for(int j = 0; j<8; j++){

				sprite[i][j] = new Sprite(this, alien, 4, 2, x + j*20, y + i*20);

			}
		}
		collision = new int[5][8];
		for(int i = 0; i<5; i++){
			for(int j = 0; j<8; j++){

				collision[i][j] = 1;

			}
		}
	}

	/**
	 * onDraw method of the extended SurfaceView class.
	 */

	@Override
	protected void onDraw(Canvas canvas) {

		paint.setColor(Color.RED);
		canvas.drawColor(Color.BLACK);
		canvas.drawLine(0, this.getHeight()- 90, this.getWidth(), this.getHeight() - 90, paint);
		paint.setColor(Color.GREEN);
		canvas.drawLine(0, 50, this.getWidth(), 50, paint);
		paint.setTextSize(15);
		paint.setFakeBoldText(true);
		paint.setTextScaleX(2);
		canvas.drawText(new String("Score : ").concat(Integer.toString(score)), 10, 20, paint);
		paint.setColor(Color.CYAN);
		canvas.drawText(new String("HighScore : "), 10, 40, paint);
		if(highScore.size() != 0)
			canvas.drawText(new String(Integer.toString(highScore.get(0))), 170, 40, paint);
		else
			canvas.drawText(new String("0"), 170, 40, paint);

		for (int i = 0; i<5; i++){
			for (int j = 0; j<8; j++){
				if(collision[i][j] == 1)
					sprite[i][j].onDraw(canvas, i, j);
			}
		}

		for(int i=50; i<this.getHeight()-100;i += 10){
			star[i][0].onDraw(canvas);
			star[i][1].onDraw(canvas);
		}

		for (int i = temps.size() - 1; i >= 0; i--) {
			temps.get(i).onDraw(canvas);
		}
		leftButton.onDraw(canvas);	
		rightButton.onDraw(canvas);
		shootButton.onDraw(canvas);
		bullet.onDraw(canvas);
		starship.onDraw(canvas);



	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event){

		int action = event.getActionMasked();//event.getAction() & MotionEvent.ACTION_MASK;
		int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			int pointerID = event.getPointerId(pointerIndex);
			int pointerCount = event.getPointerCount();

			touchX = new int[pointerCount];
			touchY = new int[pointerCount];

			for(int i = 0; i<pointerCount; i++){

				touchX[i] = (int) event.getX(i);
				touchY[i] = (int) event.getY(i);

			}
			switch(action){

			case MotionEvent.ACTION_DOWN:
				if(touchX[0] >= 0 && touchX[0] <= leftbutton.getWidth() && touchY[0] >= this.getHeight() - 100 && touchY[0] <= this.getHeight()){
					starship.setLeftRight(1, 0, true);
					leftButton.update(touchedleftbutton);

				}
				else if (touchX[0] >= 20 + leftbutton.getWidth() && touchX[0] <= 20 + leftbutton.getWidth() + rightbutton.getWidth() && touchY[0] >= this.getHeight() - 100 && touchY[0] <= this.getHeight()){

					starship.setLeftRight(0, 1, true);
					rightButton.update(touchedrightbutton);
				}
				else if(touchX[0] >= this.getWidth() - shootbutton.getWidth() && touchY[0] >+ this.getHeight() - 100 && touchY[0] < this.getHeight()){
					shootButton.update(shoottouched);
					bullet.setBulletFired(true);
				}

				break;

			case MotionEvent.ACTION_UP:
				if(touchX[0] >= 0 && touchX[0] <= leftbutton.getWidth() && touchY[0] >= this.getHeight() - 100 && touchY[0] <= this.getHeight()){
					starship.setLeftRight(0, 0, false);
					leftButton.update(leftbutton);

				}
				else if (touchX[0] >= 20 + leftbutton.getWidth() && touchX[0] <= 20 + leftbutton.getWidth() + rightbutton.getWidth() && touchY[0] >= this.getHeight() - 100 && touchY[0] <= this.getHeight()){

					starship.setLeftRight(0, 0, false);
					rightButton.update(rightbutton);
				}
				else if(touchX[0] >= this.getWidth() - shootbutton.getWidth() && touchY[0] >+ this.getHeight() - 100 && touchY[0] < this.getHeight()){
					shootButton.update(shootbutton);
					bullet.setBulletFired(false);
				}

				break;

			case MotionEvent.ACTION_POINTER_DOWN:
				if(touchX[pointerID] >= 0 && touchX[pointerID] <= leftbutton.getWidth() && touchY[pointerID] >= this.getHeight() - 100 && touchY[pointerID] <= this.getHeight()){
					starship.setLeftRight(1, 0, true);
					leftButton.update(touchedleftbutton);

				}
				else if (touchX[pointerID] >= 20 + leftbutton.getWidth() && touchX[pointerID] <= 20 + leftbutton.getWidth() + rightbutton.getWidth() && touchY[pointerID] >= this.getHeight() - 100 && touchY[pointerID] <= this.getHeight()){

					starship.setLeftRight(0, 1, true);
					rightButton.update(touchedrightbutton);
				}
				else if(touchX[pointerID] >= this.getWidth() - shootbutton.getWidth() && touchY[pointerID] >+ this.getHeight() - 100 && touchY[pointerID] < this.getHeight()){
					shootButton.update(shoottouched);
					bullet.setBulletFired(true);
				}

				break;

			case MotionEvent.ACTION_POINTER_UP:
				if(touchX[pointerID] >= 0 && touchX[pointerID] <= leftbutton.getWidth() && touchY[pointerID] >= this.getHeight() - 100 && touchY[pointerID] <= this.getHeight()){
					starship.setLeftRight(0, 0, false);
					leftButton.update(leftbutton);

				}
				else if (touchX[pointerID] >= 20 + leftbutton.getWidth() && touchX[pointerID] <= 20 + leftbutton.getWidth() + rightbutton.getWidth() && touchY[pointerID] >= this.getHeight() - 100 && touchY[pointerID] <= this.getHeight()){

					starship.setLeftRight(0, 0, false);
					rightButton.update(rightbutton);
				}
				else if(touchX[pointerID] >= this.getWidth() - shootbutton.getWidth() && touchY[pointerID] >+ this.getHeight() - 100 && touchY[pointerID] < this.getHeight()){
					shootButton.update(shootbutton);
					bullet.setBulletFired(false);
				}

				break;
			}
			return true;		
	}

	public void createStar(){


		star = new Star[this.getHeight()][2];
		for(int i =50; i< this.getHeight()- 100; i++){
			star[i][0] = new Star(this, i);
			star[i][1] = new Star(this, i);
		}


		leftButton = new Starship(this, leftbutton, 10, this.getHeight() - 75);
		rightButton = new Starship(this, rightbutton, 20 + leftbutton.getWidth(), this.getHeight() -75);
		starship = new Starship(this, ship, this.getWidth()/2, this.getHeight() - 110);
		shootButton = new Starship(this, shootbutton, this.getWidth() - shootbutton.getWidth() - 10, this.getHeight() - 75);
		bullet = new Bullet(context, temps,this, shootbullet, starship.getX() + 8, starship.getY() + 10, sprite, collision);
		paint = new Paint();
		
	}

	public int getInitialX(){
		int x = 0;
		int j =0;
		outerloopX:
			for(int i=0; i<5; i++){
				for(j=0; j<8; j++){
					if(collision[i][j] == 1){
						x = sprite[i][j].getX();
						break outerloopX;
					}
				}
			}

		return x - j*20;
	}

	public int getInitialY(){
		int y = 0;
		int i = 0;
		outerloopY:
			for(i=0; i<5; i++){
				for(int j=0; j<8; j++){
					if(collision[i][j] == 1){
						y = sprite[i][j].getY();
						break outerloopY;
					}
				}
			}
		return y - i*20;
	}
	public int getX(int i, int j){
		return sprite[i][j].getX();
	}

	public int getY(int i, int j){
		return sprite[i][j].getY();
	}

	public void putX(int valueX){
		for(int i = 0; i < 5; i++){
			for (int j = 0; j < 8; j++){

				sprite[i][j].putX(valueX + j*20);
			}
		}
	}

	public void putY(int valueY){
		for(int i = 0; i < 5; i++){
			for (int j = 0; j < 8; j++){

				sprite[i][j].putY(valueY + i*20);
			}
		}
	}

	public int getSpeed(){
		int i,j =0;
		loop:
			for(i=0; i<5; i++){
				for(j=0; j<8; j++){
					if(collision[i][j] == 1)
						break loop;
				}
			}
		return sprite[i][j].getSpeed();
	}

	public void putSpeed(int speed){

		for(int i = 0; i <5; i++){
			for (int j =0; j<8; j++){
				sprite[i][j].putSpeed(speed);
			}
		}
	}

	public int getStarshipX(){
		return starship.getX();
	}

	public int getStarshipY(){
		return starship.getY();
	}

	public void setCollision(int i, int j, int value){
		collision[i][j] = value;
	}
	public void resetAliens(){
		
		this.x = 50;
		this.y = 30;
		
		for(int i=0;i<5;i++){
			for(int j=0;j<8;j++){
				collision[i][j] = 1;
				//sprite[i][j].putX(x + j*20);
				//sprite[i][j].putY(y + i*20);
				sprite[i][j] = new Sprite(this, alien, 4, 2, x + j*20, y + i*20);
			}
		}
	}
	public int getScore(){
		return score;
	}

	public void setScore(int score){
		this.score += score;
	}

	public String getSerializedCollision(){
		for(int i=0;i<5;i++){
			for(int j=0;j<8;j++){
				serializedCollision = serializedCollision.concat(Integer.toString(collision[i][j]));
			}
		}

		return serializedCollision;
	}

	public void setSerializedCollision(String serializedCollision){
		int[] collision1D = new int[40];
		int row = 0;
		for(int i =0; i<serializedCollision.length(); i++){
			collision1D[i] = Integer.parseInt(serializedCollision.substring(i, i+1));
		}

		for(int i=0; i<5; i++){
			for(int j=0; j<8; j++){
				collision[i][j] = collision1D[row+j];
			}
			row += 8;
		}
	}

	public int getCurrentScore(){
		return score;
	}

	public void setCurrentScore(int score){
		this.score = score;
	}

	public void nextLevel(){
		level++;
		for(int i=0; i<5; i++){
			for(int j=0; j<8; j++){
				collision[i][j] = 1;
			}
		}
	}
	
	public boolean isGameOver(){
		for(int i=0; i<5; i++){
			for(int j=0; j<8; j++){
				if(collision[i][j] == 1)
					return false;
			}
		}
		
		return true;
	}
}
