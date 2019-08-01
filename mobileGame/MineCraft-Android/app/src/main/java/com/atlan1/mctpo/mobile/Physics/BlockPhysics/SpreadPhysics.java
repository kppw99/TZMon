package com.atlan1.mctpo.mobile.Physics.BlockPhysics;

import com.atlan1.mctpo.mobile.Block;

public class SpreadPhysics extends AbstractBlockPhysics {

	
	
	public SpreadPhysics(int t) {
		
	}
	
	public boolean spread(Block b){
		return false;
	}

	@Override
	public boolean performPhysics(Block b) {
		return spread(b);
	}

}
