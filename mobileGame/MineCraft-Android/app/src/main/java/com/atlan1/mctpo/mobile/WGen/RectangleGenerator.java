package com.atlan1.mctpo.mobile.WGen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.atlan1.mctpo.mobile.Block;
import com.atlan1.mctpo.mobile.Material;

public class RectangleGenerator implements WorldGenerator{

	private int yFrom, yTo;
	private Map<Integer, Integer> materials = new HashMap<Integer, Integer>();
	private int probability, prob;
	private int maxHeight, maxWidth, minHeight, minWidth;
	private boolean roundCorners;
	
	public RectangleGenerator(int yF, int yT, int p, int exP, int mH, int mW, int minH, int minW, boolean rC, Map<Integer, Integer> materials) {
		this.yFrom = yF;
		this.yTo = yT;
		this.materials = materials;
		this.probability = p;
		this.prob = exP;
		this.maxHeight = mH;
		this.maxWidth = mW;
		this.minHeight = minH;
		this.minWidth = minW;
		this.roundCorners = rC;
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
		for(int y=0;y<blocks[0].length;y++){
			if(y > yFrom && y < yTo){
				for(int x=0;x<blocks.length;x++){
					if(r.nextInt(1000)<probability){
						int rand = r.nextInt(100);
						int lastProb = 0;
						for(int mat : materials.keySet()){
							if(rand>=lastProb && rand<=materials.get(mat)+lastProb){
								int w, h;
								try {
									for(w=0;w<minWidth;w++){
										for(h=0;h<minHeight;h++){
											if(r.nextInt(100)<prob)
												blocks[x+w][y+h].material = Material.getById(mat);
										}
									}
									int extW = r.nextInt(minWidth+maxWidth);
									int extH = r.nextInt(minHeight+maxHeight);
									for(w=minWidth;w<minWidth+extW;w++){
										for(h=minHeight;h<minHeight+extH;h++){
											if(r.nextInt(100)<prob)
												blocks[x+w][y+h].material = Material.getById(mat);
										}
									}
								}catch(Throwable t){}
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
