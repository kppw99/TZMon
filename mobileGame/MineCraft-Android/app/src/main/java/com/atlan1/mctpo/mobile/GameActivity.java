package com.atlan1.mctpo.mobile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.atlan1.mctpo.mobile.Inventory.Inventory;
import com.atlan1.mctpo.mobile.Texture.BitmapHelper;

import android.app.*;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.*;
import android.util.Log;
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

	//==============================================================================================
	// Start to add source code for using TZMON JNI Library by kevin
	//==============================================================================================

	public static final boolean tzmonUse = true;

	static {
		System.loadLibrary("tzMonJNI");
	}

	public native void tzmonHello();
	public native boolean tzmonInitKeyNFlag();
	public native boolean tzmonCheckAppHash();
	public native boolean tzmonSecureUpdate();
	public native boolean tzmonAbusingDetection();
	public native boolean tzmonSyncTimer();

	public native boolean tzmonHidingSetup();
	public native int tzmonGetHKey(String data);

	public static void alertDialog(final Activity a, String message){
		AlertDialog.Builder alert = new AlertDialog.Builder(a);
		alert.setCancelable(false);
		alert.setPositiveButton("종료", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				a.moveTaskToBack(true);
				a.finish();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		alert.setMessage(message);
		alert.create().show();
	}

	//==============================================================================================
	// End to add source code for using TZMON JNI Library by kevin
	//==============================================================================================

	@Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		//==========================================================================================
		// Start to add source code for checking app integrity by kevin
		//==========================================================================================

		// After enable just one time to get usage access, never use this function.
		// startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 1);

		if (tzmonUse) {
			Boolean res;
			res = tzmonInitKeyNFlag();
			Log.d("[LOGD] Init Key and Flag ", String.valueOf(res));
			if (!res) {
				alertDialog(this, "tzMon 초기화에 실패하였습니다.");
			}

			res = tzmonCheckAppHash();
			Log.d("[LOGD] App Integrity checking ", String.valueOf(res));
			if (!res) {
				alertDialog(this, "APP 위변조가 탐지되었습니다!");
			}

			res = tzmonSecureUpdate();
			Log.d("[LOGD] Secure Update ", String.valueOf(res));
			if (!res) {
				alertDialog(this, "Secure Update에 실패하였습니다.");
			}

			res = tzmonAbusingDetection();
			Log.d("[LOGD] Abusing Detection ", String.valueOf(res));
			if (!res) {
				alertDialog(this, "Abusing package가 발견되었습니다.");
			}

			res = tzmonSyncTimer();
			Log.d("[LOGD] Sync Timer ", String.valueOf(res));
			if (!res) {
				alertDialog(this, "Timer Sync에 실패하였습니다.");
			}

			res = tzmonHidingSetup();
			Log.d("[LOGD] Hiding Setup ", String.valueOf(res));
			if (!res) {
				alertDialog(this, "Hiding Setup에 실패하였습니다.");
			}
		} else {
			Boolean res;
			res = tzmonInitKeyNFlag();
			Log.d("[LOGD] Init Key and Flag ", String.valueOf(res));
			if (!res) {
				alertDialog(this, "tzMon 초기화에 실패하였습니다.");
			}
		}
		//==========================================================================================
		// Enc to add source code for checking app integrity by kevin
		//==========================================================================================

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
