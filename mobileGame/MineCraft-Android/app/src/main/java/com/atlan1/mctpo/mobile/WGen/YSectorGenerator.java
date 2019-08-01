package com.atlan1.mctpo.mobile.WGen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.atlan1.mctpo.mobile.Block;
import com.atlan1.mctpo.mobile.Material;

public class YSectorGenerator implements WorldGenerator {

	private int yFrom, yTo;
	private Map<Integer, Integer> materials = new HashMap<Integer, Integer>();
	private boolean fillMask;
	
	public YSectorGenerator(Map<Integer, Integer> materials, int yFrom, int yTo, boolean fillMask) {
		this.yFrom = yFrom;
		this.yTo = yTo;
		this.materials = materials;
		this.fillMask = fillMask;
		int sum = 0;
		for(int i : materials.values()){
			sum+=i;
		}
		if(sum!=100){
			System.out.println("ERROR: Probabilities must add up to 100 percent!");
		}
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
				if(y > yFrom && y < yTo){
					if((fillMask && blocks[x][y].material==Material.MASK)||!fillMask){
						int rand = r.nextInt(100);
						int lastProb = 0;
						for(int mat : materials.keySet()){
							if(rand>=lastProb && rand<=materials.get(mat)+lastProb){
								blocks[x][y].material = Material.getById(mat);
							}
							lastProb += materials.get(mat);
						}
					}
				}
			}
		}
		return blocks;
	}

}
