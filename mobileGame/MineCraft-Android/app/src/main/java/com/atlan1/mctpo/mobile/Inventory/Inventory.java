package com.atlan1.mctpo.mobile.Inventory;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.atlan1.mctpo.mobile.Character;
import com.atlan1.mctpo.mobile.ItemStack;
import com.atlan1.mctpo.mobile.MCTPO;
import com.atlan1.mctpo.mobile.Material;
import com.atlan1.mctpo.mobile.API.Widget;
import com.atlan1.mctpo.mobile.HUD.InventoryBar;

public class Inventory implements Widget {
	
	private boolean open = false;
	
	public static int invLength = 8;
	public static int invHeight = 5;
	public static int slotSize = 25;
	public static int slotSpace = 5;
	public static int borderSpace = 20;
	public static int itemBorder = 3;
	public static int maxStackSize = 64;
	public static int cursorSize = 20;
	
	
	public static Paint backgroundInvOpenPaint;
	
	
	
	static {
		
		backgroundInvOpenPaint = new Paint();
		backgroundInvOpenPaint.setARGB(130, 200, 200, 200);
		
	}
	
	public static float inventoryPixelSize = 1.5f;

	public static ItemStack dragStack;
	public static Slot startDragSlot;

	public static float dragX = -1;
	public static float dragY = -1;
	
	private InventoryBar invBar;
	public Slot[] slots = new Slot[invLength*invHeight];
	public int selected=0;
	
	
	public Inventory(Character c, InventoryBar invBar){
		this.invBar = invBar;
		for(int i=0;i<slots.length;i++){
			slots[i] = new Slot(new ItemStack(Material.AIR));
		}
		calcPosition();
	}
	
	public Inventory(Inventory inventory) {
		for(int i=0;i<slots.length;i++){
			slots[i] = inventory.slots[i];
		}
		calcPosition();
		selected = inventory.selected;
	}
	
	public void calcPosition() {
		int x=0, y=0;
		Rect posRect;
		for(int i=0;i<slots.length;i++){
			int xPos = (int) ((MCTPO.size.width/2) + (-((Inventory.invLength * (Inventory.slotSize + Inventory.slotSpace))/2)+((x * (Inventory.slotSize + Inventory.slotSpace)))) * Inventory.inventoryPixelSize);
			int yPos = (int) (MCTPO.size.height - (y + 2) * (Inventory.slotSize * Inventory.inventoryPixelSize + Inventory.slotSpace) - Inventory.borderSpace);
			posRect = new Rect(xPos, yPos, (int) (xPos + Inventory.slotSize * Inventory.inventoryPixelSize), (int) (yPos + Inventory.slotSize * Inventory.inventoryPixelSize));
			slots[i].setBounds(posRect);
			
			x++;
			if(x>=invLength){
				x=0;
				y++;
			}
		}

		
		
	}

	public void render(Canvas c){
		if(open){
			c.drawRect(new Rect(0, 0, MCTPO.size.width, MCTPO.size.height), backgroundInvOpenPaint);
			for(int i=0;i<slots.length;i++){
				slots[i].render(c, false);
			}
			if (dragStack.material != Material.AIR) {
				float offset = MCTPO.blockSize * inventoryPixelSize / 2;
				c.drawBitmap(Material.terrain.getSubImageById(dragStack.material.id), null, new Rect((int) (dragX - offset), (int) (dragY - offset), (int) (dragX + MCTPO.blockSize * inventoryPixelSize - offset), (int) (dragY + MCTPO.blockSize * inventoryPixelSize - offset)), null);
			}
		}	
		
	}

	public void tick() {
		for(Slot s : slots) {
			s.tick();
		}
		for(int i=0;i<Inventory.invLength;i++){
			if(!(slots[i].equals(invBar.slots[i]))){
				invBar.slots[i].itemstack = new ItemStack(slots[i].itemstack);
			}
		}

	}
	
	public boolean containsMaterial(Material m){
		for(Slot s : slots){
			if(m==s.itemstack.material) return true;
		}
		return false;
	}
	
	public Slot getSlot(Material m){
		for(Slot s : slots){
			if(m==s.itemstack.material) return s;
		}
		return null;
	}
	
	public Slot[] getSlotsContaining(Material m){
		List<Slot> slots2 = new ArrayList<Slot>();
		for(int i=0;i<slots.length;i++){
			if(slots[i].itemstack.material == m)
				slots2.add(slots[i]);
		}
		return slots2.toArray(new Slot[slots2.size()]);
	}
	
	public void clear(){
		for(Slot s : slots){
			s.itemstack.material = Material.AIR;
			s.itemstack.stacksize = 0;
		}
			
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public void setOpen(boolean o) {
		open = o;
	}

	
	
}
