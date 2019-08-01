package com.atlan1.mctpo.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.atlan1.mctpo.mobile.Character.BuildMode;
import com.atlan1.mctpo.mobile.Graphics.Dimension;
import com.atlan1.mctpo.mobile.Graphics.Point;


public class MCTPO {
	public static MCTPO mctpo;
	//public static JFrame frame;
	
	public static float pixelSize = 1.5f;
	//private Image screen;
	
	static public Paint blackLine;
	
	static {
		blackLine = new Paint();
		blackLine.setARGB(255, 0, 0, 0);
	}
	
	public static final int blockSize = 20;
	
	public static Dimension size;
	public static Dimension pixel;
	public static String name = "Minecraft Two Point o.O";
	public static double sX = 0, sY = 0;
	
	public static Point fingerDownP = new Point(-1, -1);
	public static Point fingerP = new Point(-1, -1);
	//public static Point lastFingerP = new Point(-1, -1);
	public static boolean fingerDown = false;
	public static Point fingerBuildDownP = new Point(-1, -1);
	public static Point fingerBuildP = new Point(-1, -1);
	public static boolean fingerBuildDown = false;
	public static boolean fingerBuildMoved;
	
	/*public static Point mouse = new Point(0, 0);
	public static boolean mouseLeftDown = false;
	public static boolean mouseRightDown = false;
	public boolean controlDown = false;
	*/
	
	public static World world;
	public static Sky sky;
	public static Character character;

	public static Context context;
	
	//for frame independent movement
	public static long thisTime;
	static long deltaTime;
	static long lastTime;

	
	/*public static Cursor destroyCursor = Toolkit.getDefaultToolkit().createCustomCursor(TextureLoader.loadImage("res/DestroyCursor.png"), mouse, "DestroyCursor");
	public static Cursor buildCursor = Toolkit.getDefaultToolkit().createCustomCursor(TextureLoader.loadImage("res/BuildCursor.png"), mouse, "BuildCursor");
	public static Cursor crossHair = Toolkit.getDefaultToolkit().createCustomCursor(TextureLoader.loadImage("res/CrossHair.png"), mouse, "CrosshairCursor");
	*/
	
	public MCTPO(Context c) {
		MCTPO.mctpo = this;
		MCTPO.context = c;
		Display d = ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		size = new Dimension(d.getWidth(), d.getHeight());
		pixel = new Dimension(Math.round(size.width/pixelSize), Math.round(size.height/pixelSize));
		start();
		//setPreferredSize(size);	
	}

	public void start() {
		//setCursor(buildCursor);
		//requestFocus();

		character = new Character(MCTPO.blockSize, MCTPO.blockSize * 2);
		world = new World(character);
		sky = new Sky();
		lastTime = System.currentTimeMillis() - 1;
		
		/*MouseListener ml = new MouseListener(character);
		this.addMouseListener(ml);
		this.addMouseMotionListener(ml);
		this.addMouseWheelListener(ml);
		this.addKeyListener(new KeyListening(character));*/
		
		//isRunning = true;
		//new Thread(this).start();
	}
	
	/*public void stop() {
	}*/
	
	
	/*public static void main(String args[]) {
		MCTPO c = new MCTPO();
		frame = new JFrame(name);
		frame.add(c);
		frame.setSize(size);
		frame.pack();
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setIconImage(TextureLoader.loadImage("res/icon.png"));
		frame.setAlwaysOnTop(true);
		c.start();
	}*/
	
	public void tick() {
		thisTime = System.currentTimeMillis();
		deltaTime = thisTime - lastTime;
		
		character.tick();
		sky.tick();
		world.tick((int)sX, (int)sY, (pixel.width / blockSize) + 8, (pixel.height / blockSize) + 8);
	}
	
	public void render(Canvas canvas) {
		
		sky.render(canvas);
		
		character.render(canvas);
		world.render(canvas, (int)sX, (int)sY, (pixel.width / blockSize) + 2, (pixel.height / blockSize) + 2);
		character.hud.render(canvas);
		character.inv.render(canvas);
		
		canvas.drawBitmap(character.buildMode == BuildMode.BUILD_DESTROY?Character.buildDestroyButton:character.buildMode == BuildMode.BUILD_ONLY?Character.buildOnlyButton:Character.destroyOnlyButton, null, new Rect(MCTPO.size.width - Character.modeButtonSize, 0, MCTPO.size.width, Character.modeButtonSize), null);
		
		lastTime = thisTime;
		
		/*if (MCTPO.fingerBuildDown) {
			canvas.drawLine(MCTPO.size.width / 2, MCTPO.size.height / 2 - tileSize, (float) MCTPO.fingerBuildP.x, (float) MCTPO.fingerBuildP.y, blackLine);
		}*/
	}

	public static void setPixelSize(float i) {
		if (i != 0) {
			pixelSize = i;
			pixel.width = (int) (size.width / i);
			pixel.height = (int) (size.height / i);
			moveCamera(character.x, character.y);
		}
		
	}
	
	public static void moveCamera(double x, double y) {
		MCTPO.sX = x - (MCTPO.pixel.width / 2) + character.width / 2;
		MCTPO.sY = y - (MCTPO.pixel.height / 2) + character.height;
	}

	/*public static BufferedImage toBufferedImage(Image image) {
	    if (image instanceof BufferedImage) {
	        return (BufferedImage)image;
	    }
	
	    // This code ensures that all the pixels in the image are loaded
	image = new ImageIcon(image).getImage();
	
	// Determine if the image has transparent pixels; for this method's
	// implementation, see Determining If an Image Has Transparent Pixels
	boolean hasAlpha = hasAlpha(image);
	
	// Create a buffered image with a format that's compatible with the screen
	BufferedImage bimage = null;
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	try {
	    // Determine the type of transparency of the new buffered image
	    int transparency = Transparency.OPAQUE;
	    if (hasAlpha) {
	        transparency = Transparency.BITMASK;
	    }
	
	    // Create the buffered image
	    GraphicsDevice gs = ge.getDefaultScreenDevice();
	    GraphicsConfiguration gc = gs.getDefaultConfiguration();
	    bimage = gc.createCompatibleImage(
	        image.getWidth(null), image.getHeight(null), transparency);
	} catch (HeadlessException e) {
	    // The system does not have a screen
	}
	
	if (bimage == null) {
	    // Create a buffered image using the default color model
	    int type = BufferedImage.TYPE_INT_RGB;
	    if (hasAlpha) {
	        type = BufferedImage.TYPE_INT_ARGB;
	    }
	    bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
	}
	
	// Copy image to buffered image
	Graphics g = bimage.createGraphics();
	
	// Paint the image onto the buffered image
	    g.drawImage(image, 0, 0, null);
	    g.dispose();
	
	    return bimage;
	}
	
	public static boolean hasAlpha(Image image) {
	    // If buffered image, the color model is readily available
	    if (image instanceof BufferedImage) {
	        BufferedImage bimage = (BufferedImage)image;
	        return bimage.getColorModel().hasAlpha();
	    }

	    // Use a pixel grabber to retrieve the image's color model;
	    // grabbing a single pixel is usually sufficient
	     PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
	    try {
	        pg.grabPixels();
	    } catch (InterruptedException e) {
	    }

	    // Get the image's color model
	    ColorModel cm = pg.getColorModel();
	    return cm.hasAlpha();
	}*/
	
//	public static boolean extractFile(String regex) {
//		boolean found = false;
//		try {
//			String path = Component.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//			String decodedPath = URLDecoder.decode(path, "UTF-8");
//			JarFile jar = new JarFile(new File(decodedPath));
//			for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
//				JarEntry entry = (JarEntry) entries.nextElement();
//				String name = entry.getName();
//				if (name.matches(regex)) {
//					if (!new File(Component.comp.getCodeBase().getFile()).exists()) {
//						new File(Component.comp.getCodeBase().getFile()).mkdir();
//					}
//					try {
//						File file = new File(new File(Component.comp.getCodeBase().getFile()), name);
//						if (!file.exists()) {
//							InputStream is = jar.getInputStream(entry);
//							FileOutputStream fos = new FileOutputStream(file);
//							while (is.available() > 0) {
//								fos.write(is.read());
//							}
//							fos.close();
//							is.close();
//							found = true;
//						}
//					} catch (Exception e) {
//					}
//				}
//			}
//		} catch (Exception e) {
//		}
//		return found;
//	}
}
