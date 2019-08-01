package com.atlan1.mctpo.mobile.Physics.BlockPhysics;

import com.atlan1.mctpo.mobile.Block;
import com.atlan1.mctpo.mobile.Material;
import com.atlan1.mctpo.mobile.API.Thing;

public class LeavesPhysics implements BlockPhysics{

	private AbstractBlockPhysics abs;
	
	public LeavesPhysics() {
		abs = new DecayPhysics(3000, Material.WOOD, 40);
	}

	@Override
	public boolean performPhysics(Block b) {
		return abs.performPhysics(b);
	}


	@Override
	public boolean performCollision(Block b, Thing ent) {
		return false;
	}

}
