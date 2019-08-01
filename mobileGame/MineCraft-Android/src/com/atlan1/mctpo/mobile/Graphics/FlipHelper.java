package com.atlan1.mctpo.mobile.Graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class FlipHelper {
	
	public static Bitmap flip(Bitmap src) {
	    Matrix m = new Matrix();
	    m.preScale(-1, 1);
	    Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
	    return dst;
	}

	public static Bitmap flipAnimation(Bitmap animationTexture, int subImageWidth, int subImageHeight) {
		Bitmap animationTextureFlipped = Bitmap.createBitmap(animationTexture.getWidth(), animationTexture.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(animationTextureFlipped);
		Bitmap b;
		for (int i = 0; (i + 1) * subImageHeight <= animationTexture.getHeight(); i++) {
			for (int j = 0; (j + 1) * subImageWidth <= animationTexture.getWidth(); j++) {
				b = Bitmap.createBitmap(animationTexture, j * subImageWidth, i * subImageHeight, subImageWidth, subImageHeight);
				b = flip(b);
				canvas.drawBitmap(b, j * subImageWidth, i * subImageHeight, null);
			}
		}
		return animationTextureFlipped;
	}

}
