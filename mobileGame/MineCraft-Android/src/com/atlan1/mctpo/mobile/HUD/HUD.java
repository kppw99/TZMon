package com.atlan1.mctpo.mobile.HUD;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.atlan1.mctpo.mobile.API.Widget;

public class HUD{
	
	public static Paint transparentPaint;
	
	static {
		transparentPaint = new Paint();
		transparentPaint.setColor(Color.WHITE);
		transparentPaint.setAlpha(200);
	}

	private List<Widget> widgets = new ArrayList<Widget>();

	public HUD(Widget...widgets){
		for(Widget w : widgets){
			addWidget(w);
		}
	}
	
	public void render(Canvas c) {
		for(Widget w : widgets){
			w.render(c);
		}
	}

	public void tick() {
		for(Widget w : widgets) {
			w.tick();
		}
	}

	public void calcPosition() {
		for(Widget w : widgets){
			w.calcPosition();
		}
	}
	
	public void addWidget(Widget w){
		widgets.add(w);
	}
	
	public void removeWidget(Widget w){
		widgets.remove(w);
	}
	
	public void removeAll() {
		widgets = new ArrayList<Widget>();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Widget> T getWidget(Class<T> c) {
		for(Widget w : widgets){
			if(c.isAssignableFrom(w.getClass()))
				return (T)w;
		}
		return null;
	}
}