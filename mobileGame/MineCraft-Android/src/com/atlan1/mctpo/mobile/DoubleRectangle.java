package com.atlan1.mctpo.mobile;

import android.graphics.Rect;
import android.graphics.RectF;

public class DoubleRectangle {

	protected double width;
	protected double height;
	protected double x;
	protected double y;
	
	public DoubleRectangle() {
		this(0, 0, 0, 0);
	}
	
	public DoubleRectangle(double width, double height, double x, double y) {
		setBounds(width, height, x, y);
	}
	
	public void setBounds(double width, double height, double x, double y){
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
	}
	
	public void setBounds(double width, double height){
		this.width = width;
		this.height = height;
		this.x = 0;
		this.y = 0;
	}
	
	public boolean intersects(DoubleRectangle rect){
		return intersects(new Rect((int)rect.x, (int)rect.y, (int) rect.x + (int)rect.width, (int) rect.y + (int)rect.height));
	}
	
	public boolean intersects(Rect rect)
	{
	    int i = (int)this.width;
	    int j = (int)this.height;
	    int k = rect.width();
	    int m = rect.height();
	    if ((k <= 0) || (m <= 0) || (i <= 0) || (j <= 0))
	      return false;
	    int n = (int)this.x;
	    int i1 = (int)this.y;
	    int i2 = rect.top;
	    int i3 = rect.left;
	    k += i2;
	    m += i3;
	    i += n;
	    j += i1;
	    return ((k < i2) || (k > n)) && ((m < i3) || (m > i1)) && ((i < n) || (i > i2)) && ((j < i1) || (j > i3));
	}
	
	public RectF asRectF() {
		return new RectF((float)x, (float)y, (float)(x + width), (float)(y + height));
	}
	
	public double getCenterX() {
		return x + width / 2;
	}
	
	public double getCenterY() {
		return y + height / 2;
	}
	
}
