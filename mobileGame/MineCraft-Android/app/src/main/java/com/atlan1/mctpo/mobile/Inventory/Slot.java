package com.atlan1.mctpo.mobile.Inventory;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.atlan1.mctpo.mobile.ItemStack;
import com.atlan1.mctpo.mobile.Graphics.Rectangle;
import com.atlan1.mctpo.mobile.HUD.HUD;
import com.atlan1.mctpo.mobile.Texture.TextureLoader;

public class Slot extends Rectangle {
	
	public static Bitmap slotNormal= TextureLoader.loadImage("images/slot_normal.png");
	public static Bitmap slotSelected= TextureLoader.loadImage("images/slot_selected.png");
	
	
	public ItemStack itemstack;
	
	public Slot(Rectangle rectangle,  ItemStack i){
		setBounds(rectangle);
		itemstack = i;
	}
	
	public Slot(ItemStack i){
		itemstack = i;
	}

	
	public void render(Canvas c, boolean selected){
		/*Log.d("slotx", String.valueOf((x)));
		Log.d("sloty", String.valueOf((y)));*/
		c.drawBitmap(slotNormal, null, new Rect((int) (x), (int) (y), (int) ((x + width)), (int) ((y + height))), HUD.transparentPaint);
		if(selected){
			c.drawBitmap(slotSelected, null, new Rect((int) ((x-1)), (int) ((y-1)), (int) ((x + width+2)), (int) ((y + height+2))), HUD.transparentPaint);
		}
		itemstack.render(c, this);
	}
	
	public void tick() {
		
	}	


}
