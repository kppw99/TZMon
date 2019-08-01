package com.atlan1.mctpo.mobile.WGen;

import java.util.Random;

import com.atlan1.mctpo.mobile.Block;
import com.atlan1.mctpo.mobile.Material;

public class MountainGenerator implements WorldGenerator{

	private int yFrom, yTo;
	
	public MountainGenerator(int yFrom,int yTo){
		this.yFrom = yFrom;
		this.yTo = yTo;
	}

	@Override
	public Block[][] generate(Block[][] blocks) {
		Random r = new Random();
		if(yFrom>yTo){
			int temp;
			temp = yTo;
			yTo = yFrom;
			yFrom = temp;
		}
		for(int x=0;x<blocks.length;x++){
			for(int y=0;y<blocks[0].length;y++){
				if(y>yFrom && y<yTo){
					if(r.nextInt(100)>20){
						try{
							if(blocks[x-1][y-1].material == Material.MASK){
								blocks[x][y].material = Material.MASK;
							}
						}catch(Exception e){}
					}
					if(r.nextInt(100)>30){
						try{
							if(blocks[x+1][y-1].material == Material.MASK){
								blocks[x][y].material = Material.MASK;
							}
						}catch(Exception e){}
					}
					try{
						if(blocks[x][y-1].material == Material.MASK){
							blocks[x][y].material = Material.MASK;
						}
					}catch(Exception e){}
					if(r.nextInt(100)<2)
						blocks[x][y].material = Material.MASK;
				}
			}
		}
		return blocks;
	}
	

	
}
