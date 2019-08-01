package com.atlan1.mctpo.mobile.Texture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;

public class BitmapHelper {
	/*
	 * Class to make working with Bitmaps easier. 
	 */
	
	public static Bitmap getSubimage(Bitmap b, Rect copyRect) {
		// Extracts a part of a Bitmap defined by copyRect.
		Bitmap subImage = Bitmap.createBitmap(copyRect.width(), copyRect.height(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(subImage);
		c.drawBitmap(b, copyRect, new Rect(0, 0, copyRect.width(), copyRect.height()),  null);
		return subImage;
	}
	
	public static void saveBitmapToSdcard(Bitmap b, String subpath) {
		FileOutputStream fo = null;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Bitmap copyBitmap = Bitmap.createBitmap(b);
		copyBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		File f = new File(Environment.getExternalStorageDirectory(), subpath);
		try {
			f.createNewFile();
			fo = new FileOutputStream(f);
			fo.write(bytes.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
