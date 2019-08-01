package com.atlan1.mctpo.mobile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.atlan1.mctpo.mobile.Inventory.Inventory;
import com.atlan1.mctpo.mobile.Texture.BitmapHelper;

import android.app.*;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.*;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class GameActivity extends Activity implements OnSeekBarChangeListener {
	
    MainGamePanel panel;
    FrameLayout layout;
    
    LayoutInflater inflater;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		
		layout = new FrameLayout(this);
		
		panel = new MainGamePanel(this);
		layout.addView(panel);
		
	    setContentView(layout);
    }
	
	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (layout.getChildCount() == 1) {
				RelativeLayout menuLayout = (RelativeLayout) inflater.inflate(R.layout.menu, null);
				layout.addView(menuLayout);
				
				SeekBar inventoryBar = (SeekBar) layout.findViewById(R.id.inventory_size_bar);
				SeekBar worldBar = (SeekBar) layout.findViewById(R.id.world_size_bar);
				
				worldBar.setProgress((int) (MCTPO.pixelSize * 10));
				inventoryBar.setProgress((int) (Inventory.inventoryPixelSize * 10));
					
				worldBar.setOnSeekBarChangeListener(this);
				inventoryBar.setOnSeekBarChangeListener(this);
				
				return true;
			} else {
				layout.removeViewAt(1);
				return true;
			}
			
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (layout.getChildCount() > 1) {
				layout.removeViewAt(1);
				return true;
			} else if (MCTPO.character.inv.isOpen()) {
				MCTPO.character.inv.setOpen(false);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);

	}
	
	public void screenshot(View v) {
		Bitmap  bitmap = Bitmap.createBitmap( panel.getWidth(), panel.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		MCTPO.mctpo.render(canvas);
		int i = 1;
		String name = "screenshot" + i + ".jpg";
		File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Screenshots");
		if (!dir.exists()) {
			dir.mkdir();
		}
		String[] fileArray = dir.list();
		if (fileArray != null && fileArray.length != 0) {
			List<String> dirList = Arrays.asList(fileArray);
			while (dirList.contains(name)) {
				i++;
				name = "screenshot" + i + ".jpg";
			}
		}
		BitmapHelper.saveBitmapToSdcard(bitmap, "Screenshots" + File.separator + name);
		
		/*for (int j = 0; j <= 40; j++) {
			BitmapHelper.saveBitmapToSdcard(Material.terrain.getSubImageById(j), "Screenshots/terrain" + j + ".jpg");
		}*/
		
		
		Toast.makeText(this, "Screenshot saved to: " + "Screenshots/" + name, Toast.LENGTH_LONG).show();
	}
	
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
	*/
	
	@Override
	public void onProgressChanged(SeekBar seekBar,
			int progress, boolean fromUser) {
		switch (seekBar.getId()) {
		case R.id.world_size_bar:
			MCTPO.setPixelSize(seekBar.getProgress() / 10f);
			break;
		case R.id.inventory_size_bar:
			Inventory.inventoryPixelSize = seekBar.getProgress() / 10f;
			//MCTPO.character.inventory = new Inventory(MCTPO.character.inventory);
			MCTPO.character.hud.calcPosition();
			MCTPO.character.inv.calcPosition();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}

}
