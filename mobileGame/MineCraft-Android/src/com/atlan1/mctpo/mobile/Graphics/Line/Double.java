package com.atlan1.mctpo.mobile.Graphics.Line;

import com.atlan1.mctpo.mobile.Graphics.Line2d;
import com.atlan1.mctpo.mobile.Graphics.Point;

public class Double extends Line2d {
		
	private double x1;
	private double x2;
	private double y1;
	private double y2;
	
	public Double(Point point1, Point point2) {
		x1 = point1.x;
		x2 = point2.x;
		y1 = point1.y;
		y2 = point2.y;
	}
	
	public Double(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}
	
	public Double() {
		this.x1 = 0;
		this.x2 = 0;
		this.y1 = 0;
		this.y2 = 0;
	}
	
	public Point getP1() {
		return new Point(x1, y1);
	}
	
	public Point getP2() {
		return new Point(x2, y2);
	}
	
}

