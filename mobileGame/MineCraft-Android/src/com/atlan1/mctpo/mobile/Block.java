package com.atlan1.mctpo.mobile;

import java.util.ArrayList;
import java.util.List;

import com.atlan1.mctpo.mobile.MCTPO;
import com.atlan1.mctpo.mobile.API.Thing;
import com.atlan1.mctpo.mobile.Graphics.Rectangle;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

public class Block extends Rectangle implements Thing{
	public Material material = Material.AIR;

	public List<Long> timeOfUpdate = new ArrayList<Long>();
	public List<Thing> collisions = new ArrayList<Thing>();
	
	public Block(Rect size, int id) {
		setBounds(size);
		this.material = Material.getById(id);
	}
	
	public Block(Rect size, Material m) {
		setBounds(size);
		this.material = m;
	}
	
	public Block(Rectangle size, int id) {
		setBounds(size);
		this.material = Material.getById(id);
	}
	
	public Block(Rectangle size, Material m) {
		setBounds(size);
		this.material = m;
	}
	
	
	public boolean intersects(DoubleRectangle rect){
		return intersects(new Rect((int)rect.x, (int)rect.y, (int) rect.x + (int)rect.width, (int) rect.y + (int)rect.height));
	}
	
	public boolean intersects(Rect rect)
	  {
	    int i = (int)this.width;
	    int j = (int)this.height;
	    int k = rect.width();
	    int m = rect.height();
	    if ((k <= 0) || (m <= 0) || (i <= 0) || (j <= 0))
	      return false;
	    int n = (int)this.x;
	    int i1 = (int)this.y;
	    int i2 = rect.top;
	    int i3 = rect.left;
	    k += i2;
	    m += i3;
	    i += n;
	    j += i1;
	    return ((k < i2) || (k > n)) && ((m < i3) || (m > i1)) && ((i < n) || (i > i2)) && ((j < i1) || (j > i3));
	  }
	
	public void render(Canvas c) {
		if(material != Material.AIR){
			/*Bitmap b = Material.terrain.getSubImageById(material.id);
			Log.d("materialid", String.valueOf(material.id));
			Log.d("b.width", String.valueOf(b.getWidth()));
			c.drawBitmap(b, 0, 0, null);
			Log.d("Block below x", String.valueOf(x - MCTPO.sX));
			Log.d("Block below y", String.valueOf(y - MCTPO.sY));
			Log.d("Block below x2", String.valueOf(x - MCTPO.sX + width));
			Log.d("Block below y2", String.valueOf(y - MCTPO.sY + height));
			Log.d("heigt", String.valueOf(height));
			Log.d("width", String.valueOf(width));*/
			c.drawBitmap(Material.terrain.getSubImageById(material.id), null, new RectF((float) (x - MCTPO.sX) * MCTPO.pixelSize,(float) (y - MCTPO.sY + MCTPO.blockSize) * MCTPO.pixelSize, (float) (x + width - MCTPO.sX) * MCTPO.pixelSize , (float) (y + height - MCTPO.sY + MCTPO.blockSize) * MCTPO.pixelSize), null);
		}
	}
	
	public void update(int index) {
		timeOfUpdate.set(index, MCTPO.thisTime);
	}

	public void tick(){
		if(!material.physics.isEmpty()){
			material.doPhysics(this);
			for(Thing t : new ArrayList<Thing>(collisions)){
				material.collide(this, t);
			}
		}
			
	}

	public Block addCollision(Thing ent) {
		this.collisions.add(ent);
		return this;
	}
	
	public Block removeCollision(Thing ent) {
		this.collisions.remove(ent);
		return this;
	}
	
	public synchronized int requestTimeId() {
		timeOfUpdate.add(MCTPO.thisTime);
		//return timeOfUpdate.get(timeOfUpdate.size()-1);
		return timeOfUpdate.size() - 1;
	}
	
	public int getGridX(){
		return x/MCTPO.blockSize;
	}
	
	public int getGridY(){
		return y/MCTPO.blockSize;
	}

	
	
}
