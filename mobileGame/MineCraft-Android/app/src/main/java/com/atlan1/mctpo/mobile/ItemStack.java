package com.atlan1.mctpo.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.atlan1.mctpo.mobile.HUD.HUD;
import com.atlan1.mctpo.mobile.Inventory.Inventory;
import com.atlan1.mctpo.mobile.Inventory.Slot;


public class ItemStack {
	
	public static Paint fontPaint;
	
	static {
		fontPaint = new Paint();
		fontPaint.setARGB(255, 255, 255, 200);
		fontPaint.setTypeface(Typeface.createFromAsset(MCTPO.context.getAssets(),
                "fonts/tahoma.ttf"));
	}
	
	public Material material;
	public int stacksize=0;

	public ItemStack(Material mat) {
			material = mat;
	}

	public ItemStack(Material mat, int stack) {
		material = mat;
		stacksize = stack;
	}

	public ItemStack(ItemStack i){
		material = i.material;
		stacksize = i.stacksize;
	}

	public void render(Canvas canvas, Slot s) {
		if(stacksize>0&&material!=Material.AIR){
			canvas.drawBitmap(Material.terrain.getSubImageById(material.id), null, new Rect((int)((s.x+Inventory.itemBorder)), (int)((s.y+Inventory.itemBorder)), (int)((s.x + s.width-Inventory.itemBorder)), (int)((s.y +s.height-Inventory.itemBorder))), HUD.transparentPaint);
			canvas.drawText(String.valueOf(stacksize), (s.x+s.width-8), (s.y+s.height), fontPaint);
		}else if(stacksize<=0||material==Material.AIR){
			material = Material.AIR;
			stacksize = 0 ;
		}
	}

	public boolean equals(Object o){
		if(!(o instanceof ItemStack)) {
			return false;
		}
		ItemStack i = (ItemStack) o;
		if(i.material == material&&i.stacksize==stacksize){
			return true;
		}
		return false;
	}
}
