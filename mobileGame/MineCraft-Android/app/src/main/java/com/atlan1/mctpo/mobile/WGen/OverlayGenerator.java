package com.atlan1.mctpo.mobile.WGen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.atlan1.mctpo.mobile.Block;
import com.atlan1.mctpo.mobile.Material;

public class OverlayGenerator implements WorldGenerator {
private Map<Integer, Integer> materials = new HashMap<Integer, Integer>();
	
	public OverlayGenerator(Map<Integer, Integer> materials) {
		this.materials = materials;
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
		for(int x=0;x<blocks.length;x++){
			for(int y=0;y<blocks[0].length;y++){
				try{
					if(blocks[x][y].material!=Material.AIR && blocks[x][y-1].material==Material.AIR){
						int rand = r.nextInt(100);
						int lastProb = 0;
						for(int mat : materials.keySet()){
							if(rand>=lastProb && rand<=materials.get(mat)+lastProb){
								blocks[x][y].material = Material.getById(mat);
							}
							lastProb += materials.get(mat);
						}
					}
				}catch(Exception e){
					
				}
			}
		}
		return blocks;
	}

}
