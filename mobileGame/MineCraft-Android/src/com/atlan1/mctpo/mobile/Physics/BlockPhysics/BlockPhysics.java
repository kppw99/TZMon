package com.atlan1.mctpo.mobile.Physics.BlockPhysics;

import com.atlan1.mctpo.mobile.Block;
import com.atlan1.mctpo.mobile.API.Thing;

public interface BlockPhysics {

	public boolean performPhysics(Block b);

	public boolean performCollision(Block b, Thing ent);
	
}
