package com.android.launcher3;

import java.io.IOException;
import com.android.launcher3.WallpaperPickerActivity.ResourceWallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import android.util.Pair;
import android.content.res.Resources;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.Random;
import android.provider.Settings;
public class ChangerWallpaperProvider /*extends BroadcastReceiver*/ {
	private WallpaperManager wallpaperManager;
	protected static ArrayList<ResourceWallpaperInfo> wallpapers = null;
	private Context context;
	private int lastResourceId;	
	public ChangerWallpaperProvider(Context context){
       this.context=context;
	}
	/*@Override
	public void onReceive(Context context, Intent arg1) {
		// TODO Auto-generated method stub
		wallpaperManager = WallpaperManager.getInstance(context);
		this.context=context;
		wallpapers = findBundledWallpapers();
		Random random=new Random();
		int ResourceId=random.nextInt(wallpapers.size());
		//lastResourceId=ResourceId;	
		lastResourceId=Settings.Secure.getInt(context.getContentResolver(),"com.android.launcher3.lastnumber",0);
		Log.d("LUORAN","************"+lastResourceId);
		if(ResourceId==lastResourceId){
		  if(ResourceId==0){
		  ResourceId+=1;
		  }else{		  
		  ResourceId-=1;
		  }
		}
		try {
			wallpaperManager.setResource(wallpapers.get(ResourceId).getmResId());
			Settings.Secure.putInt(context.getContentResolver(),"com.android.launcher3.lastnumber",ResourceId);
			lastResourceId=ResourceId;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	public ArrayList<ResourceWallpaperInfo> getAllWallpapers(){
	
	 return findBundledWallpapers();
	
	}
	protected ArrayList<ResourceWallpaperInfo> findBundledWallpapers() {
		ArrayList<ResourceWallpaperInfo> bundledWallpapers = new ArrayList<ResourceWallpaperInfo>(
				24);

		Pair<ApplicationInfo, Integer> r = getWallpaperArrayResourceId();
		if (r != null) {
			try {
				Resources wallpaperRes = context.getPackageManager()
						.getResourcesForApplication(r.first);
				bundledWallpapers = addWallpapers(wallpaperRes,
						r.first.packageName, r.second);
			} catch (PackageManager.NameNotFoundException e) {
			}
		}

		return bundledWallpapers;
	}
	
	public Pair<ApplicationInfo, Integer> getWallpaperArrayResourceId() {
		// Context.getPackageName() may return the "original" package name,
		// com.android.launcher3; Resources needs the real package name,
		// com.android.launcher3. So we ask Resources for what it thinks the
		// package name should be.
		final String packageName = context.getResources().getResourcePackageName(
				R.array.wallpapers);
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(
					packageName, 0);
			return new Pair<ApplicationInfo, Integer>(info, R.array.wallpapers);
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}
	}
	
	private ArrayList<ResourceWallpaperInfo> addWallpapers(Resources res,
			String packageName, int listResId) {
		ArrayList<ResourceWallpaperInfo> bundledWallpapers = new ArrayList<ResourceWallpaperInfo>(
				24);
		final String[] extras = res.getStringArray(listResId);
		for (String extra : extras) {
			int resId = res.getIdentifier(extra, "drawable", packageName);
			if (resId != 0) {
				final int thumbRes = res.getIdentifier(extra + "_small",
						"drawable", packageName);

				if (thumbRes != 0) {
					ResourceWallpaperInfo wallpaperInfo = new ResourceWallpaperInfo(
							res, resId, res.getDrawable(thumbRes));
					bundledWallpapers.add(wallpaperInfo);
					// Log.d(TAG, "add: [" + packageName + "]: " + extra + " ("
					// + res + ")");
				}
			}
		}
		return bundledWallpapers;
	}

}
