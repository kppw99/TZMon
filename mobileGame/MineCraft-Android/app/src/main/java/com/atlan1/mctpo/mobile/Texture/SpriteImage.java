package com.atlan1.mctpo.mobile.Texture;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;


public class SpriteImage{
	private int spriteSize;
	private Bitmap image;
	private List<Bitmap> subImages = new ArrayList<Bitmap>();
	
	public SpriteImage(Bitmap image, int spriteSize) {
		this.image = image;
		this.spriteSize = spriteSize;
		calcSubImages();
	}
	
	public void calcSubImages(){
		int count = 0;
//		for (int y = (image.getHeight() / spriteSize)-1; y > 1; y--) {
//			for (int x = 1; x < (image.getWidth() / spriteSize); x++) {
//				subImages.add(count, image.getSubimage(x, y, x * spriteSize, y * spriteSize));
//				count++;
//			}
//		}
		
		for(int y = 0; y < image.getHeight();y+=spriteSize){
			for(int x = 0; x < image.getWidth();x+=spriteSize){
				subImages.add(count, Bitmap.createBitmap(image, x, y, spriteSize, spriteSize));
				count++;
			}
		}
	}

	public int getSpriteSize() {
		return spriteSize;
	}
	
	public Bitmap getSubImageById(int id){
		return subImages.get(id);
	}
	
	public Bitmap getImage(){
		return image;
	}
}
