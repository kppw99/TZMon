package com.atlan1.mctpo.mobile.Texture;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.atlan1.mctpo.mobile.MCTPO;

public class TextureLoader {

	public static SpriteImage loadSpriteImage(String assetsPath) {
		SpriteImage image = null;
		InputStream is = null;
		try {
			is = MCTPO.context.getResources().getAssets().open(assetsPath);
			image = new SpriteImage(BitmapFactory.decodeStream(is), MCTPO.blockSize);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return image;
	}
	
	public static Bitmap loadImage(String assetsPath) {
		try{
			return BitmapFactory.decodeStream(MCTPO.context.getResources().getAssets().open(assetsPath));
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
}
