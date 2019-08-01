package com.atlan1.mctpo.mobile.Physics.BlockPhysics;

import com.atlan1.mctpo.mobile.Block;
import com.atlan1.mctpo.mobile.API.Thing;

public class WaterPhysics implements BlockPhysics{

	private AbstractBlockPhysics abs;
	
	public WaterPhysics() {
		abs = new FlowPhysics(500, true);
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
