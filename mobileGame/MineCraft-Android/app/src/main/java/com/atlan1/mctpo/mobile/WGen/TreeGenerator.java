package com.atlan1.mctpo.mobile.WGen;

import java.util.Random;

import com.atlan1.mctpo.mobile.Block;
import com.atlan1.mctpo.mobile.Material;

public class TreeGenerator implements WorldGenerator {

	private int maxTreeHeight, minTreeHeight, prob1, prob2;
	
	public TreeGenerator(int maxTreeHeight, int minTreeHeight, int prob1,int prob2){
		this.maxTreeHeight = maxTreeHeight;
		this.minTreeHeight = minTreeHeight;
		this.prob1 = prob1;
		this.prob2 = prob2;
	}
	
	@Override
	public Block[][] generate(Block[][] blocks) {
		Random r = new Random();
		for(int x=0;x<blocks.length;x++){
			for(int y=0;y<blocks[0].length;y++){
			if(blocks[x][y].material == Material.GRASS && r.nextInt(100) < prob1){
				//Check if generating tree is possible
					boolean minBlocksReached = true;
					for(int n=1;n<=minTreeHeight;n++){
						minBlocksReached &= blocks[x][y-n].material == Material.AIR;
					}
					if(minBlocksReached){
						//Generate min tree height
						for(int n=1;n<=minTreeHeight;n++){
							blocks[x][y-n].material = Material.WOOD;
						}
						//Generate additional Blocks
						int addBlocks = 0;
						for(int l = minTreeHeight+1;l<=maxTreeHeight;l++){
							try{
								if(blocks[x][y-l].material == Material.AIR && r.nextInt(100) < prob2){
									blocks[x][y-l].material = Material.WOOD;
									addBlocks++;
								}else{
									break;
								}
							}catch(Exception e){e.printStackTrace();}
						}
						//Generate Leaves.
						int treeHeight = minTreeHeight + addBlocks;
						if(blocks[x][y-treeHeight-1].material == Material.AIR)
							blocks[x][y-treeHeight-1].material = Material.LEAVES;
						for(int o=x-(treeHeight/2);o<=x+(treeHeight/2);o++){
							for(int p=y-treeHeight;p<y-(treeHeight/2);p++){
								try{
									if(blocks[o][p].material == Material.AIR)
										blocks[o][p].material = Material.LEAVES;
								}catch(Exception e){}
							}
						}
					}
				}
			}
		}
		return blocks;
	}

}
