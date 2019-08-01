package com.atlan1.mctpo.mobile.HUD;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.atlan1.mctpo.mobile.Character;
import com.atlan1.mctpo.mobile.MCTPO;
import com.atlan1.mctpo.mobile.API.Widget;
import com.atlan1.mctpo.mobile.Inventory.Inventory;
import com.atlan1.mctpo.mobile.Texture.TextureLoader;

public class HealthBar implements Widget {

	private static Bitmap hIcon;
	private static Bitmap hblackIcon;
	private Character c;
	
	public int maxHearts = 10;
	public int invBorder = 10;
	public int heartSpace = 4;
	public int heartSize = 20;
	
	public HealthBar(Character c){
		this.c = c;
		hIcon = TextureLoader.loadImage("images/heart.png");
		hblackIcon = TextureLoader.loadImage("images/heart_black.png");
	}
	
	public void tick(){
		
	}
	
	public void render(Canvas canvas) {
		if (MCTPO.character.hud.getWidget(InventoryBar.class).inflated) {
			int heartsLeft = c.health/(c.maxHealth/maxHearts);
			int xH;
			int yH;
			for(int x=0;x<maxHearts;x++){
				boolean black = heartsLeft-x<0;
				xH = (int) ((MCTPO.size.width/2)-((((maxHearts * (heartSize + heartSpace))/2) - ((x * (heartSize + heartSpace)))) * Inventory.inventoryPixelSize));
				yH = (int) (MCTPO.size.height - (Inventory.slotSize + invBorder + heartSize) * Inventory.inventoryPixelSize - Inventory.borderSpace);
				canvas.drawBitmap(black?hblackIcon:hIcon, null, new Rect(xH, yH, (int) (xH + heartSize * Inventory.inventoryPixelSize), (int) (yH + heartSize * Inventory.inventoryPixelSize)), HUD.transparentPaint);
			}
		}
	}

	@Override
	public void calcPosition() {
		//is already done in render()
		
	}
}
