package com.atlan1.mctpo.mobile.Physics.BlockPhysics;

import com.atlan1.mctpo.mobile.Block;
import com.atlan1.mctpo.mobile.MCTPO;
import com.atlan1.mctpo.mobile.Material;
import com.atlan1.mctpo.mobile.World;

public class FlowPhysics extends AbstractBlockPhysics {

	private int tick = 0; //time between physic updates
	private boolean infinite = false; //infinite fluid source from one block
	
	public FlowPhysics(int tick, boolean inf){
		this.tick = tick;
		this.infinite = inf;
	}
	
	//Calculates fluids movement
	public Boolean flow(Block b){
		addId(b);
		if(b!=null){
			if(MCTPO.thisTime - b.timeOfUpdate.get(getId(b))>=tick){
				Material from = b.material;
				//infinite fluid
				if(infinite){
					//Flow down
					Block to = null;
					try{
						if(to==null&&World.blocks[b.getGridX()][b.getGridY()+1].material.nonSolid && World.blocks[b.getGridX()][b.getGridY()+1].material != from){
							to = World.blocks[b.getGridX()][b.getGridY()+1];
							World.blocks[b.getGridX()][b.getGridY()+1].update(getId(b));
						}
					}catch(Throwable t){}
					//Flow right/left
					try{
						if(to==null&&World.blocks[b.getGridX()][b.getGridY()+1].material!=from){
							try{
								if(to==null&&World.blocks[b.getGridX()-1][b.getGridY()].material.nonSolid && World.blocks[b.getGridX()-1][b.getGridY()].material != from){
									to = World.blocks[b.getGridX()-1][b.getGridY()];
									World.blocks[b.getGridX()-1][b.getGridY()].update(getId(b));
								}
							}catch(Throwable t){}
							try{
								if(to==null&&World.blocks[b.getGridX()+1][b.getGridY()].material.nonSolid && World.blocks[b.getGridX()+1][b.getGridY()].material != from){
									to = World.blocks[b.getGridX()+1][b.getGridY()];
									World.blocks[b.getGridX()+1][b.getGridY()].update(getId(b));
								}
							}catch(Throwable t){}
						}
					}catch(Throwable t){}
					if(to != null){
						if((from==Material.WATER&&to.material==Material.LAVA)||(to.material==Material.WATER&&from==Material.LAVA)){
							to.material = Material.STONE;
						}else if(to.material.nonSolid){
							to.material = from;
						}
					}
					return true;
				//finite fluid
				}else{
					Block to = null;
					try{
						if(to==null&&World.blocks[b.getGridX()][b.getGridY()+1].material.nonSolid && World.blocks[b.getGridX()][b.getGridY()+1].material != from){
							to = World.blocks[b.getGridX()][b.getGridY()+1];
							World.blocks[b.getGridX()][b.getGridY()+1].update(getId(b));
						}
					}catch(Throwable t){}
					//Check if the block next to and one down is nonSolid
					try{
						if(to==null&&World.blocks[b.getGridX()][b.getGridY()+1].material!=from){
							try{
								if(to==null&&(World.blocks[b.getGridX()-1][b.getGridY()].material.nonSolid &&World.blocks[b.getGridX()-1][b.getGridY()].material != from)&&(World.blocks[b.getGridX()-1][b.getGridY()+1].material.nonSolid && World.blocks[b.getGridX()-1][b.getGridY()+1].material != from)){
									to = World.blocks[b.getGridX()-1][b.getGridY()];
									World.blocks[b.getGridX()-1][b.getGridY()].update(getId(b));
								}
							}catch(Throwable t){}
							try{
								if(to==null&&(World.blocks[b.getGridX()+1][b.getGridY()].material.nonSolid &&World.blocks[b.getGridX()+1][b.getGridY()].material != from)&&(World.blocks[b.getGridX()+1][b.getGridY()+1].material.nonSolid && World.blocks[b.getGridX()+1][b.getGridY()+1].material != from)){
									to = World.blocks[b.getGridX()+1][b.getGridY()];
									World.blocks[b.getGridX()+1][b.getGridY()].update(getId(b));
								}
							}catch(Throwable t){}
						}
					}catch(Throwable t){}
					if(to != null){
						if((from==Material.WATER&&to.material==Material.LAVA)||(to.material==Material.WATER&&from==Material.LAVA)){
							to.material = Material.STONE;
							from = Material.AIR;
						}else if(to.material.nonSolid){
							to.material = from;
							from = Material.AIR;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean performPhysics(Block b) {
		return flow(b);
	}

}
