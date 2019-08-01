package com.atlan1.mctpo.mobile.Physics.BlockPhysics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.atlan1.mctpo.mobile.Block;
import com.atlan1.mctpo.mobile.MCTPO;
import com.atlan1.mctpo.mobile.Material;
import com.atlan1.mctpo.mobile.World;

public class DecayPhysics extends AbstractBlockPhysics {

	private int tick = 0;
	private Material decayStopMat;
	private int prob;
	
	public DecayPhysics(int t, Material dsm, int p) {
		this.tick = t;
		this.decayStopMat = dsm;
		this.prob = p;
	}
	
	public boolean decay(Block b){
		addId(b);
		if(b!=null){
			if(MCTPO.thisTime - b.timeOfUpdate.get(getId(b))>=tick){
				Material m = b.material;
				boolean willDecay = false;
				if((new Random()).nextInt(100)<prob){
					int[] bc = {b.getGridX(), b.getGridY()};
					int[] bc2 = new int[2];
					List<Block> alreadyBlocks = new ArrayList<Block>();
					while(!willDecay){
						bc2 = new int[]{bc[0], bc[1]};
						try{
							if(World.blocks[bc[0]][bc[1]-1].material==decayStopMat){
								break;
							}else if(World.blocks[bc[0]][bc[1]-1].material==m && !alreadyBlocks.contains(World.blocks[bc[0]][bc[1]-1])){
								bc2[0] = bc[0];
								bc2[1] = bc[1]-1;
							}
						}catch(Throwable t){}
						try{
							if(World.blocks[bc[0]][bc[1]+1].material==decayStopMat){
								break;
							}else if(World.blocks[bc[0]][bc[1]+1].material==m && !alreadyBlocks.contains(World.blocks[bc[0]][bc[1]+1])){
								bc2[0] = bc[0];
								bc2[1] = bc[1]+1;
							}
						}catch(Throwable t){}
						try{
							if(World.blocks[bc[0]+1][bc[1]].material==decayStopMat){
								break;
							}else if(World.blocks[bc[0]+1][bc[1]].material==m && !alreadyBlocks.contains(World.blocks[bc[0]+1][bc[1]])){
								bc2[0] = bc[0]+1;
								bc2[1] = bc[1];
							}
						}catch(Throwable t){}
						try{
							if(World.blocks[bc[0]-1][bc[1]].material==decayStopMat){
								break;
							}else if(World.blocks[bc[0]-1][bc[1]].material==m && !alreadyBlocks.contains(World.blocks[bc[0]-1][bc[1]])){
								bc2[0] = bc[0]-1;
								bc2[1] = bc[1];
							}
						}catch(Throwable t){}
						if(Arrays.equals(bc, bc2)){
							willDecay = true;
						}
						try{
							alreadyBlocks.add(World.blocks[bc[0]][bc[1]]);
						}catch(Throwable t){}
						bc = bc2;
					}
				}
				if(willDecay)
					b.material = Material.AIR;
				else
					b.update(getId(b));
			}
		}
		return true;
	}

	@Override
	public boolean performPhysics(Block b) {
		return decay(b);
	}

}
