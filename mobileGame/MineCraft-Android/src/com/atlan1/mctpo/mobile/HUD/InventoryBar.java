package com.atlan1.mctpo.mobile.HUD;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.atlan1.mctpo.mobile.MCTPO;
import com.atlan1.mctpo.mobile.Material;
import com.atlan1.mctpo.mobile.API.Widget;
import com.atlan1.mctpo.mobile.Graphics.Rectangle;
import com.atlan1.mctpo.mobile.Inventory.Inventory;
import com.atlan1.mctpo.mobile.Inventory.Slot;
import com.atlan1.mctpo.mobile.Texture.TextureLoader;
import com.atlan1.mctpo.mobile.ItemStack;

public class InventoryBar implements Widget{

	public int selected = 0;
	public Slot[] slots = new Slot [Inventory.invLength];
	public boolean inflated = true;
	
	public static Bitmap inflateButton;
	public static Bitmap openButton;
	public static Rect inflateButtonRect;
	public static Rect openButtonRect;
	
	static {	
		inflateButton = TextureLoader.loadImage("images/lol.png");
		openButton = TextureLoader.loadImage("images/vignette_2.png");
	}
	
	
	public InventoryBar() {
		for(int i=0;i<slots.length;i++){
			slots[i] = new Slot(new Rectangle((MCTPO.pixel.width/2)-((Inventory.invLength * (Inventory.slotSize + Inventory.slotSpace))/2)+((i * (Inventory.slotSize + Inventory.slotSpace))), MCTPO.pixel.height - (Inventory.slotSize + Inventory.borderSpace), Inventory.slotSize, Inventory.slotSize), new ItemStack(Material.AIR));
		}
		calcPosition();
	}
	
	@Override
	public void render(Canvas c) {
		if (inflated) {
			for(int i=0;i<slots.length;i++){
				slots[i].render(c, i==MCTPO.character.inv.selected);
			}
		}
		c.drawBitmap(inflateButton, null, inflateButtonRect, HUD.transparentPaint);
		c.drawBitmap(openButton, null, openButtonRect, HUD.transparentPaint);
	}

	@Override
	public void tick() {
		for(Slot s : slots){
			s.tick();
		}
	}

	@Override
	public void calcPosition() {
		Rect posRect;
		for(int i=0;i<slots.length;i++){
			int x = (int) ((MCTPO.size.width/2) + (-((Inventory.invLength * (Inventory.slotSize + Inventory.slotSpace))/2)+((i * (Inventory.slotSize + Inventory.slotSpace)))) * Inventory.inventoryPixelSize);
			int y = (int) (MCTPO.size.height - (Inventory.slotSize * Inventory.inventoryPixelSize + Inventory.borderSpace));
			posRect = new Rect(x, y, (int) (x + Inventory.slotSize * Inventory.inventoryPixelSize), (int) (y + Inventory.slotSize * Inventory.inventoryPixelSize));
			slots[i].setBounds(posRect);
			//slots[i].setBounds(new Rectangle((int)((MCTPO.pixel.width/2)-((Inventory.invLength * (Inventory.slotSize + Inventory.slotSpace))/2)+((i * (Inventory.slotSize + Inventory.slotSpace)) * Inventory.inventoryPixelSize)), (int) ((MCTPO.pixel.height - (Inventory.slotSize + Inventory.borderSpace)) * Inventory.inventoryPixelSize) , (int) (Inventory.slotSize * Inventory.inventoryPixelSize), (int) (Inventory.slotSize * Inventory.inventoryPixelSize)));
		}
		initializeButtons();
	}
	
	public void initializeButtons() {
		int inflateX = (int) ((MCTPO.size.width/2) + (-((Inventory.invLength * (Inventory.slotSize + Inventory.slotSpace))/2)+((- 1 * (Inventory.slotSize + Inventory.slotSpace)))) * Inventory.inventoryPixelSize);
		int inflateY = (int) (MCTPO.size.height - (Inventory.slotSize * Inventory.inventoryPixelSize + Inventory.borderSpace));
		inflateButtonRect = new Rect(inflateX, inflateY, (int) (inflateX + Inventory.slotSize * Inventory.inventoryPixelSize), (int) (inflateY + Inventory.slotSize * Inventory.inventoryPixelSize));
		
		inflateX = (int) ((MCTPO.size.width/2) + (-((Inventory.invLength * (Inventory.slotSize + Inventory.slotSpace))/2)+(((Inventory.invLength) * (Inventory.slotSize + Inventory.slotSpace)))) * Inventory.inventoryPixelSize);
		//inflateY = (int) (MCTPO.size.height - (Inventory.slotSize * Inventory.inventoryPixelSize + Inventory.borderSpace));
		openButtonRect = new Rect(inflateX, inflateY, (int) (inflateX + Inventory.slotSize * Inventory.inventoryPixelSize), (int) (inflateY + Inventory.slotSize * Inventory.inventoryPixelSize));
	}
	
	

}