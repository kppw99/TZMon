package com.atlan1.mctpo.mobile;

import android.graphics.Canvas;

public class Sky {

	public int dayLength=12000;
	public long startTime = System.currentTimeMillis();
	private int dayR=60, dayG=110, dayB=255;
	private int nightR=50, nightG=40, nightB=145;
	private int nowR, nowG, nowB;
	private boolean isDay = true;
	
	public Sky(){
		nowR=dayR; nowG=dayG; nowB=dayB;
	}
	
	public void tick(){
		if(MCTPO.thisTime - startTime > dayLength){
			if(isDay){
				isDay=false;
			}else{
				isDay=true;
			}
			startTime=0;
		}else{
			startTime++;
		}
		if(isDay){
			if(!(nowR==nightR&&nowG==nightG&&nowB==nightB)){
				if(nowR<nightR)
					nowR++;
				else
					nowR--;
				if(nowG<nightG)
					nowG++;
				else
					nowG--;
				if(nowB<nightB)
					nowB++;
				else
					nowB--;
			}
		}else{
			if(!(nowR==dayR&&nowG==dayG&&nowB==dayB)){
				if(nowR<dayR)
					nowR++;
				else
					nowR--;
				if(nowG<dayG)
					nowG++;
				else
					nowG--;
				if(nowB<dayB)
					nowB++;
				else
					nowB--;
			}
		}
	}
	
	public void render(Canvas c) {
		c.drawARGB(255, nowR, nowG, nowB);
	}
}
