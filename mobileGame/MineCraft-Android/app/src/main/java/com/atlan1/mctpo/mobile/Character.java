package com.atlan1.mctpo.mobile;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.atlan1.mctpo.mobile.MCTPO;
import com.atlan1.mctpo.mobile.HUD.HUD;
import com.atlan1.mctpo.mobile.HUD.HealthBar;
import com.atlan1.mctpo.mobile.HUD.InventoryBar;
import com.atlan1.mctpo.mobile.Inventory.Inventory;
import com.atlan1.mctpo.mobile.Inventory.Slot;
import com.atlan1.mctpo.mobile.Texture.TextureLoader;
import com.atlan1.mctpo.mobile.API.LivingThing;
import com.atlan1.mctpo.mobile.API.Thing;
import com.atlan1.mctpo.mobile.Graphics.FlipHelper;
import com.atlan1.mctpo.mobile.Graphics.Line2d;
import com.atlan1.mctpo.mobile.Graphics.Point;

public class Character extends DoubleRectangle implements LivingThing{
	private static Bitmap animationTexture;
	private static Bitmap animationTextureFlipped;
	static Bitmap buildOnlyButton;
	static Bitmap destroyOnlyButton;
	static Bitmap buildDestroyButton;
	
	public static int modeButtonSize = 50;
	
	public static enum BuildMode {
		BUILD_DESTROY, BUILD_ONLY, DESTROY_ONLY;
		public BuildMode getNext() {
			return values()[(ordinal()+1) % values().length];
		}
	}

	static public Paint redFilter;
	
	private static int[]  character = {0, 0};
	static{
		animationTexture = TextureLoader.loadImage("images/animation.png");
		animationTextureFlipped = FlipHelper.flipAnimation(animationTexture, MCTPO.blockSize, MCTPO.blockSize * 2);
		//Red filter
		redFilter = new Paint(Color.RED);
		ColorFilter filter = new LightingColorFilter(Color.RED, 1);
		redFilter.setColorFilter(filter);
		

		buildOnlyButton = TextureLoader.loadImage("images/buildonlybutton.png");
		destroyOnlyButton = TextureLoader.loadImage("images/destroyonlybutton.png");
		buildDestroyButton = TextureLoader.loadImage("images/builddestroybutton.png");
	}
	
	private List<Thing> collisions = new ArrayList<Thing>();
	
	public double fallingSpeed = 5d;//blocks per second
	public double jumpingSpeed = 5d;//blocks per second
	public double movementSpeed = 2d; //blocks per second
	//public double sprintSpeed = 0.5d;//blocks per second
	public boolean isMoving = false;
	public boolean wouldJump = false;
	public boolean isJumping = false;
	public boolean setBlockBelow = false;
	public boolean isSprinting = false;
	public float jumpHeight = 1f; //in Blocks
	public float actualJumpHeight = 0;
	public double dir = 1;
	public int animation = 0;
	public long startAnimation;
	public int animationTime = 500; //ms
	public int sprintAnimationTime = 8;
	public double buildRange = 4; //in blocks
	public boolean isFalling = false;
	public double startFalling = 0;
	public HUD hud = new HUD(new HealthBar(this), new InventoryBar());
	public Inventory inv = new Inventory(this, hud.getWidget(InventoryBar.class));

	public int maxHealth = 100;
	public int health = 100;
	public boolean damaged = false;
	public int damageTime=400; // ms
	public long destroyTime=0;
	public boolean buildOn = true;
	private boolean building = false;
	public BuildMode buildMode = BuildMode.BUILD_DESTROY;
	public Block currentBlock;
	public Block lastBlock;
	public final int bUP = 0, bDOWN = 1, bRIGHT = 2, bLEFT = 3;
	public com.atlan1.mctpo.mobile.Graphics.Line.Double[] bounds = new com.atlan1.mctpo.mobile.Graphics.Line.Double[4];
	private long startDestroyTime = 0;
	private long damageStartTime = 0;
	
	public Character(double width, double height) {
		
		/*damageAnimationTexture = Bitmap.createBitmap(animationTexture.getWidth(), animationTexture.getHeight(), Bitmap.Config.RGB_565);
		Canvas damageCanvas = new Canvas(damageAnimationTexture);
		damageCanvas.drawBitmap(animationTexture, 0, 0, p);*/
		
		
		setBounds(width, height, (MCTPO.pixel.width / 2) - (width / 2), (MCTPO.pixel.height / 2) - (height / 2));
		calcBounds();
	}
	
	//TODO: Nur gerenderte Blocks durchlaufen!!!
	
	public boolean isCollidingWithAnyBlock(Line2d line) {
		for(int x=(int)(this.x/MCTPO.blockSize);x<(int)(this.x/MCTPO.blockSize+3);x++)
			for(int y=(int)(this.y/MCTPO.blockSize);y<(int)(this.y/MCTPO.blockSize+3);y++)
				if(x >= 0 && y >= 0 && x < World.worldW && y < World.worldH){
					boolean collide = World.blocks[x][y].contains(line.getP1())|| World.blocks[x][y].contains(line.getP2());
					if(collide)
						collisions.add(World.blocks[x][y].addCollision(this));
					if(!World.blocks[x][y].material.nonSolid&&collide)
							return true;
				}
		return false;
	}
	
	public boolean isCollidingWithBlock(Block t) {
		RectF blockRect = t.toRectF();
		for(com.atlan1.mctpo.mobile.Graphics.Line.Double line : bounds){
			if (blockRect.contains((float) line.getP1().x, (float) line.getP1().y) || blockRect.contains((float) line.getP2().x, (float) line.getP2().y)) {
				return true;
			}
		}
		return false;
	}
	
	public void render(Canvas c){
		/*Log.d("Block below x", String.valueOf((int) Math.round(x) - (int) MCTPO.sX));
		Log.d("Block below y", String.valueOf((int) y - 1 - (int) MCTPO.sY));
		Log.d("Block below x2", String.valueOf((int)Math.round((x + width)) - (int) MCTPO.sX));
		Log.d("Block below y2", String.valueOf((int)(y + height) - 1 - (int) MCTPO.sY));
		c.drawBitmap(Material.terrain.getSubImageById(1), new Rect(0, 0, MCTPO.tileSize, MCTPO.tileSize), new Rect((int)x - (int) MCTPO.sX, (int)y - (int) MCTPO.sY, (int)(x + width) - (int) MCTPO.sX, (int)(y + height) - (int) MCTPO.sY), null);*/
		if(dir>=0) 
			c.drawBitmap(animationTexture, new Rect((character[0] * MCTPO.blockSize)+(MCTPO.blockSize * animation), (character[1] * MCTPO.blockSize), (character[0] * MCTPO.blockSize)+((animation + 1) * MCTPO.blockSize), ((character[1] + 2) * MCTPO.blockSize)), new Rect((int)((x -  MCTPO.sX) * MCTPO.pixelSize), (int) ((y - MCTPO.sY + MCTPO.blockSize) * MCTPO.pixelSize), (int) (((x + width) - MCTPO.sX) * MCTPO.pixelSize), (int) ((int)((y + height) - (int) MCTPO.sY + MCTPO.blockSize) * MCTPO.pixelSize)), damaged?redFilter:null);
			//c.drawBitmap(animationTexture, (int)(x + width) - (int) MCTPO.sX, (int)y - (int) MCTPO.sY, damaged?redFilter:null);
			/*Rect src = new Rect((character[0] * MCTPO.tileSize)+(MCTPO.tileSize * animation), (character[1] * MCTPO.tileSize), (character[0] * MCTPO.tileSize)+(animation * MCTPO.tileSize)+ (int) width, (character[1] * MCTPO.tileSize) + (int) height);
			Rect dst = new Rect((int)x - (int) MCTPO.sX, (int)y - (int) MCTPO.sY, (int)(x + width) - (int) MCTPO.sX, (int)(y + height) - (int) MCTPO.sY);
			c.drawBitmap(animationTexture, src, dst, damaged?redFilter:null);*/
		else
			c.drawBitmap(animationTextureFlipped, new Rect((character[0] * MCTPO.blockSize)+(MCTPO.blockSize * animation), (character[1] * MCTPO.blockSize), (character[0] * MCTPO.blockSize)+((animation + 1) * MCTPO.blockSize), ((character[1] + 2) * MCTPO.blockSize)), new Rect((int)((x -  MCTPO.sX) * MCTPO.pixelSize), (int) ((y - MCTPO.sY + MCTPO.blockSize) * MCTPO.pixelSize), (int) (((x + width) - MCTPO.sX) * MCTPO.pixelSize), (int) ((int)((y + height) - (int) MCTPO.sY + MCTPO.blockSize) * MCTPO.pixelSize)), damaged?redFilter:null);
			//c.drawBitmap(animationTextureFlipped, (int)(x + width) - (int) MCTPO.sX, (int)y - (int) MCTPO.sY, damaged?redFilter:null);
			/*Rect src = new Rect((character[0] * MCTPO.tileSize)+(MCTPO.tileSize * animation), (character[1] * MCTPO.tileSize), (character[0] * MCTPO.tileSize)+(animation * MCTPO.tileSize)+ (int) width, (character[1] * MCTPO.tileSize) + (int) height);
			Rect dst = new Rect((int)(x + width) - (int) MCTPO.sX, (int)y - (int) MCTPO.sY, (int)x - (int) MCTPO.sX, (int)(y + height) - (int) MCTPO.sY);
			c.drawBitmap(animationTextureFlipped, src, dst, damaged?redFilter:null);*/
		
	}
	
	public void tick(){
		calcBounds();
		clearCollisions();
		
		calcMovement();
		
		Log.d("jumping", String.valueOf(isJumping));
		Log.d("falling", String.valueOf(isFalling));
		
		int firstHealth = health;
		boolean noGroundCollision = !isCollidingWithAnyBlock(bounds[bDOWN]);
		if(!isJumping && noGroundCollision){
			if(!isFalling && noGroundCollision){
				startFalling=this.y;
				isFalling = true;
			}
			
			double fall = fallingSpeed * MCTPO.blockSize * MCTPO.deltaTime / 1000;
			Log.d("fall", String.valueOf(fall));
			y += fall;
			MCTPO.sY += fall;
			
			calcBounds();
			
			if (isCollidingWithAnyBlock(bounds[bDOWN])) {
				MCTPO.sY -= y % MCTPO.blockSize;
				y -= y % MCTPO.blockSize;
				isFalling = false;
				calcBounds();
				clearCollisions();
			}
			
			
		}else{
			if(wouldJump)
				isJumping = true;
		}
		
		if(!noGroundCollision && isFalling){
			int deltaFallBlocks = (int) ((this.y-startFalling)/MCTPO.blockSize);
			if(deltaFallBlocks>3)
				health-=deltaFallBlocks;
			startFalling=0;
			isFalling = false;
		}
		if(isJumping){
			boolean canJump = !isCollidingWithAnyBlock(bounds[bUP]);
			if(canJump){
				Log.d("actualJumpHeight", String.valueOf(actualJumpHeight));
				if(jumpHeight<=actualJumpHeight + 0.05){ // + 0.05 to allow small difference, elsewise: often not falling.
					isJumping = false;
					actualJumpHeight = 0;
					if (setBlockBelow && !inv.slots[inv.selected].itemstack.material.nonSolid/*!hud.getWidget(InventoryBar.class).slots[hud.getWidget(InventoryBar.class).selected].itemstack.material.nonSolid*/) {
						
						if(inv.slots[inv.selected].itemstack.stacksize>0)
							inv.slots[inv.selected].itemstack.stacksize--;
						else{
							inv.slots[inv.selected].itemstack.stacksize = 0;
							inv.slots[inv.selected].itemstack.material = Material.AIR;
						}
						World.blocks[(int) Math.round(x / MCTPO.blockSize)][(int) Math.round(y / 20 + 2)].material = inv.slots[inv.selected].itemstack.material;
						setBlockBelow = false;
					} else if (setBlockBelow) {
						setBlockBelow = false;
					}
				}else{
					double jump = jumpingSpeed * MCTPO.blockSize * MCTPO.deltaTime / 1000;
					/*if (jump > jumpHeight - actualJumpHeight) {
						jump = (jumpHeight - actualJumpHeight) * MCTPO.blockSize;
					}*/
					y -= jump;
					MCTPO.sY -= jump;
					actualJumpHeight += jump / MCTPO.blockSize;
					
					if (isCollidingWithAnyBlock(bounds[bUP])) {
						MCTPO.sY += y % MCTPO.blockSize;
						y += y % MCTPO.blockSize;
						calcBounds();
					}
				}
			}else{
				isJumping = false;
				actualJumpHeight = 0;
				setBlockBelow = false;
			}
		}		
		if(isMoving){
			boolean canMove = false;
			
			if(dir == 1){
				canMove = !isCollidingWithAnyBlock(bounds[bRIGHT]);
			}else if (dir == -1){
				canMove = !isCollidingWithAnyBlock(bounds[bLEFT]);
			}
			
			if(MCTPO.thisTime - startAnimation >= (isSprinting?sprintAnimationTime:animationTime)) {
				if(animation<3){
					animation++;
					startAnimation = MCTPO.thisTime;
				}else{
					animation=0;
					startAnimation = MCTPO.thisTime;
				}
			}else{
				
			}
			
			if(canMove){
				double move = dir * movementSpeed * MCTPO.blockSize * MCTPO.deltaTime / 1000;
				x += move;
				MCTPO.sX += move;
				
				/*if (isCollidingWithAnyBlock(bounds[dir > 0?bRIGHT:bLEFT])) {
					MCTPO.sX -= dir * (x % MCTPO.blockSize);
					x -= dir *(x % MCTPO.blockSize);
					calcBounds();
				}*/
			} else if (!isJumping && !noGroundCollision && MCTPO.fingerDown) {
				isJumping = true;
			}
		}else{
			animation = 1;
		}
		if(firstHealth-health>0){
			damaged = true;
		}
		if(damaged){
			if(damageTime<=MCTPO.thisTime - damageStartTime ){
				damageStartTime = 0;
				damaged= false;
			}else{
				
			}
		}
		/*if(currentBlock!=null&&currentBlock.material.nonSolid&&isBlockInBuildRange(currentBlock))
			MCTPO.mctpo.setCursor(MCTPO.buildCursor);
		else if(currentBlock!=null&&!currentBlock.material.nonSolid&&isBlockInBuildRange(currentBlock)){
			MCTPO.mctpo.setCursor(MCTPO.destroyCursor);
		}else if(currentBlock!=null&&!isBlockInBuildRange(currentBlock)){
			MCTPO.mctpo.setCursor(MCTPO.crossHair);
		}*/
		if(currentBlock!=null&&lastBlock!=null&&MCTPO.fingerBuildDown&&lastBlock.equals(currentBlock)&&isBlockInBuildRange(currentBlock)){
			destroyTime = MCTPO.thisTime - startDestroyTime ;
		}else{
			destroyTime=0;
			startDestroyTime = 0;
		}
		if (!MCTPO.fingerBuildDown) {
			if (!buildOn) {
				buildOn = true;
			}
			if (building) {
				building = false;
			}
				
		}
		lastBlock = currentBlock;
		currentBlock = getCurrentBlock();
		if(currentBlock!=null)
			build();
		if(health<=0){
			inv.clear();
			this.respawn();
			health = maxHealth;
		}
		if(this.y/MCTPO.blockSize>World.worldH){
			this.teleport((int) this.x, 0);
		}
		inv.tick();
		hud.tick();
	}
	
	private void calcMovement() {
		if (MCTPO.fingerDown) {
			if (MCTPO.fingerP.x <= (MCTPO.size.width) / 2 - 30) {
				isMoving = true;
				dir = -1;
			} else if (MCTPO.fingerP.x >= (MCTPO.size.width) / 2 + 30) {
				isMoving = true;
				dir = 1;
			} else if (!isJumping && (MCTPO.fingerP.y <= (MCTPO.size.height) / 2 - 50) && isCollidingWithAnyBlock(bounds[bDOWN])) {
				isJumping = true;
			} else if (!isJumping && (MCTPO.fingerP.y >= (MCTPO.size.height) / 2 + 50) && isCollidingWithAnyBlock(bounds[bDOWN])) {
				isJumping = true;
				setBlockBelow = true;
			}
		} else if (this.isMoving) {
			this.isMoving = false;
		}
		//Log.d("fingerDownY", String.valueOf(MCTPO.fingerDownP.y));
		//Log.d("fingerY", String.valueOf(MCTPO.fingerP.y));
		if (MCTPO.fingerDownP.y - MCTPO.fingerP.y  > 70 && MCTPO.fingerDown && !isJumping && isCollidingWithAnyBlock(bounds[bDOWN])) {
			isJumping = true;
		} /*else if (isJumping) {
			isJumping = false;
		}*/
		
	}

	public void calcBounds(){
		bounds[bUP] = new com.atlan1.mctpo.mobile.Graphics.Line.Double(new Point((int)(x+2), (int) (y+1)), new Point((int)(x + width -2), (int)(y+1)));
		bounds[bDOWN] = new com.atlan1.mctpo.mobile.Graphics.Line.Double(new Point((int)(x+2), (int) (y+height)), new Point((int)(x+width-2), (int)(y+height)));
		bounds[bRIGHT] = new com.atlan1.mctpo.mobile.Graphics.Line.Double(new Point((int)(x + width -1), (int) y), new Point((int)(x + width), (int) (y + (height-2))));
		bounds[bLEFT] = new com.atlan1.mctpo.mobile.Graphics.Line.Double(new Point((int)x-1, (int) y), new Point((int)x-1, (int) (y + (height-2))));
		
	}
	
	public void clearCollisions() {
		for(Thing t: new ArrayList<Thing>(collisions)) {
			if(!isCollidingWithBlock((Block) t)){
				this.removeCollision(t);
				t.removeCollision(this);
			}
		}
	}
	
	public Block getCurrentBlock(){
		if (MCTPO.fingerBuildDown) {
			//Block[][] blocks = World.blocks;
			/*int camX=(int)MCTPO.sX;
			int camY=(int)MCTPO.sY;
			int renW=(MCTPO.pixel.width / MCTPO.tileSize) + 2;
			int renH=(MCTPO.pixel.height / MCTPO.tileSize) + 2;*/
			/*for(int x=(camX/MCTPO.tileSize);x<(camX/MCTPO.tileSize) + renW;x++){
				for(int y=(camY/MCTPO.tileSize);y<(camY/MCTPO.tileSize) + renH;y++){
					if(x>=0 && y>=0 && x<World.worldW && y<World.worldH){
						if(blocks[x][y].contains(new Point(MCTPO.fingerBuildP.x + (int)MCTPO.sX, MCTPO.fingerBuildP.y + (int)MCTPO.sY))){
							return blocks[x][y];
						}
					}
				}
			}*/
			try {
				Block b = getBlockIncluding(MCTPO.fingerBuildP.x / MCTPO.pixelSize, MCTPO.fingerBuildP.y / MCTPO.pixelSize);
				/*Log.d("bx", String.valueOf(b.x));
				Log.d("by", String.valueOf(b.y));
				Log.d("px", String.valueOf(x));
				Log.d("py", String.valueOf(y));*/
				return b;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/*else {
				double m = (MCTPO.fingerBuildDownP.y - y)/(MCTPO.fingerBuildDownP.x - x);
				double xIterate = MCTPO.size.width/2;
				double yIterate = MCTPO.size.height/2;
				double halfScreenWidth = MCTPO.size.width / 2;
				double halfScreenHeight = MCTPO.size.height / 2;
				int xdirection = (MCTPO.fingerBuildP.x > 0) ? 1 : -1;
				Block b = null;
				do {
					xIterate = xdirection * Math.sqrt(1 / (1 + m * m));
					yIterate = m * xIterate;
					b =  getBlockIncluding(xIterate - halfScreenWidth, yIterate - halfScreenHeight);
				} while (b != null?isBlockInBuildRange(b) && b.material == Material.AIR:true);
				if (isBlockInBuildRange(b)) {
					return b;
				}
			}*/
			
		return null;
	}
	
	public Block getBlockIncluding(double x, double y) {
		return World.blocks[(int) ((x + MCTPO.sX) / MCTPO.blockSize)][(int) ((y + MCTPO.sY) / MCTPO.blockSize) - 1]; // -1 only in mobile version
		//return World.blocks[(int) ((x + MCTPO.sX - (MCTPO.size.width - MCTPO.pixel.width) / 2) / (MCTPO.tileSize))][(int) ((y + MCTPO.sY - (MCTPO.size.height - MCTPO.pixel.height) / 2) / (MCTPO.tileSize) - 1)];
	}
	
	public boolean isBlockInBuildRange(Block block) {
		//Log.d("range", String.valueOf(Math.sqrt((Math.pow(((MCTPO.fingerBuildP.x / MCTPO.tileSize + MCTPO.sX + (MCTPO.size.width - MCTPO.pixel.width) / 2 - (int)(this.x+width/2))), 2) + Math.pow(((MCTPO.fingerBuildP.y / MCTPO.tileSize + MCTPO.sY + (MCTPO.size.height - MCTPO.pixel.height) / 2) - (int)(this.y+height/2)) , 2)))));
		//Log.d("rangeValue", String.valueOf(buildRange * MCTPO.pixelSize));
		return Math.sqrt(Math.pow(block.getCenterX() - (int)(this.x+width/2), 2) + Math.pow(block.getCenterY() - (int)(this.y+height/2) , 2)) <= buildRange*MCTPO.blockSize;
		//return Math.sqrt((Math.pow(((MCTPO.fingerBuildP.x + MCTPO.sX - (MCTPO.size.width - MCTPO.pixel.width) / 2 - (int)(this.x+width/2))), 2) + Math.pow(((MCTPO.fingerBuildP.y + MCTPO.sY - (MCTPO.size.height - MCTPO.pixel.height) / 2) - (int)(this.y+height/2)) , 2))) <= buildRange * MCTPO.pixelSize;
	}
	
	public void build(){
		if(isBlockInBuildRange(currentBlock) /*&& currentBlock != lastBlock*/){
			//Log.d("build", "inRange");
			Material m = currentBlock.material;
			if(MCTPO.fingerBuildDown && m.nonSolid && buildOn && buildMode != BuildMode.DESTROY_ONLY && MCTPO.fingerBuildP.x != -1 && MCTPO.fingerBuildP.y != -1/* && !this.asRectF().contains((float) MCTPO.fingerBuildP.x, (float) MCTPO.fingerBuildP.y)*/){
				if (!building) {
					building = true;
				}
				if(inv.slots[inv.selected].itemstack.material != Material.AIR){
					if(inv.slots[inv.selected].itemstack.stacksize>0)
						inv.slots[inv.selected].itemstack.stacksize--;
					else{
						inv.slots[inv.selected].itemstack.stacksize = 0;
						inv.slots[inv.selected].itemstack.material = Material.AIR;
					}
					currentBlock.material = inv.slots[inv.selected].itemstack.material;
					//buildOn = false;
					return;
				}
			} else if(MCTPO.fingerBuildDown && !building && buildMode != BuildMode.BUILD_ONLY && MCTPO.fingerBuildP.x != -1 && MCTPO.fingerBuildP.y != -1){
				if (buildOn) {
					buildOn = false;
				}
				if(destroyTime>=m.hardness&&!(m.hardness<0)){
					if(inv.containsMaterial(m)){
						boolean check = false;
						Slot[] slots = inv.getSlotsContaining(m);
						for(Slot s : slots){
						if(s.itemstack.stacksize<Inventory.maxStackSize){
								s.itemstack.stacksize++;
								check = true;
								break;
							}
						}
						if(!check && inv.containsMaterial(Material.AIR)){
							Slot s2 = inv.getSlot(Material.AIR);
							s2.itemstack.material = m;
							s2.itemstack.stacksize++;
						}
					}else if(inv.containsMaterial(Material.AIR)){
						Slot s3 = inv.getSlot(Material.AIR);
						s3.itemstack.material = m;
						s3.itemstack.stacksize++;
					}
					currentBlock.material = Material.AIR;
					destroyTime = 0;
				}
				return;
			}
		}
	}
	
	public void respawn(){
		teleport(MCTPO.world.spawnPoint.x, MCTPO.world.spawnPoint.y);
	}
	
	public void teleport(double x, double y){
		this.y = y;
		this.x = x;
		MCTPO.sY = y - (MCTPO.pixel.height / 2) + (height / 2);
		MCTPO.sX = x - (MCTPO.pixel.width / 2) + (width / 2);
	}

	@Override
	public int getHealth() {
		return health;
	}

	@Override
	public void setHealth(int i) {
		health = i;
	}

	@Override
	public Thing addCollision(Thing t) {
		collisions.add(t);
		return this;
	}

	@Override
	public Thing removeCollision(Thing t) {
		collisions.remove(t);
		return this;
	}
}
