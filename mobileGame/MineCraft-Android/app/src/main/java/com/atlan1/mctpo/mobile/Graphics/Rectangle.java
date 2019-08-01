package com.atlan1.mctpo.mobile.Graphics;

import android.graphics.Rect;
import android.graphics.RectF;

public class Rectangle {
	public int x = 0;
	public int y = 0;
	public int width = 0;
	public int height = 0;
	
	public Rectangle() {}
	
	public Rectangle(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public Rectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean contains(Point p) {
		return (p.x >= x && p.x <= x + width && p.y >= y && p.y <= y + height);
	}
	
	public boolean contains(int px, int py) {
		return (px >= x && px <= x + width && py >= y && py <= y + height);
	}
	
	public boolean contains(float px, float py) {
		return (px >= x && px <= x + width && py >= y && py <= y + height);
	}

	public RectF toRectF() {
		return new RectF(x, y, x + width, y + height);
	}

	public void setBounds(int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void setBounds(int width, int height){
		this.width = width;
		this.height = height;
		this.x = 0;
		this.y = 0;
	}
	
	public void setBounds(Rect r){
		this.width = r.width();
		this.height = r.height();
		this.x = r.left;
		this.y = r.top;
	}
	
	public void setBounds(Rectangle r){
		this.width = r.width;
		this.height = r.height;
		this.x = r.x;
		this.y = r.y;
	}
	
	public double getCenterX() {
		return x + width / 2;
	}
	
	public double getCenterY() {
		return y + height / 2;
	}
	
}
