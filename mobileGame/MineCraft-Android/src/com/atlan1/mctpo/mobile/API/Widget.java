package com.atlan1.mctpo.mobile.API;

import android.graphics.Canvas;

public interface Widget{

	public void render(Canvas g);
	public void tick();
	public void calcPosition();

}

