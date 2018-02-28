package com.android.launcher3;

import android.content.Context;
import android.content.SharedPreferences;

public class SalesUtil {
	private SharedPreferences mPreferences;
	private Context mContext;

	public SalesUtil(Context context) {
		mContext = context;
        //Modify chenshu 20160906 start
		//mPreferences = context.getSharedPreferences(SalesString.MY_SHARED_PREFFERENCES, 0);
        mPreferences = context.getSharedPreferences(SalesString.MY_SHARED_PREFFERENCES, 2);
        //Modify chenshu 20160906 end
	}

	public void setMode(Boolean mode) {
		mPreferences.edit().putBoolean(SalesString.MODE, mode).apply();
	}

	public boolean getMode() {
		return mPreferences.getBoolean(SalesString.MODE, true);
	}

	public void setBlockMCC460(boolean block) {
		mPreferences.edit().putBoolean(SalesString.BLOCK_MCC_460, block)
				.apply();
	}

	public boolean getBlockMCC460() {
		return mPreferences.getBoolean(SalesString.BLOCK_MCC_460, true);
	}

	/*public void setServer(String server) {
		mPreferences.edit().putString(SalesString.SERVER, server).apply();
	}

	public String getServer() {
		return mPreferences.getString(SalesString.SERVER, mContext
				.getResources().getString(R.string.def_server));
	}

	public void setTime(long time) {
		mPreferences.edit().putLong(SalesString.TIME_SEND_MMS, time).apply();
	}

	public long getTime() {
		return mPreferences.getLong(
				SalesString.TIME_SEND_MMS,
				Long.parseLong(mContext.getResources().getString(
						R.string.def_send_sms_time)));
	}*/

	public void setFirstBootWithSIM(boolean firstBootWithSIM) {
		mPreferences.edit()
				.putBoolean(SalesString.FIRST_BOOT_WITH_SIM, firstBootWithSIM)
				.apply();
	}

	public boolean getFirstBootWithSIM() {
		return mPreferences.getBoolean(SalesString.FIRST_BOOT_WITH_SIM, true);
	}

	/*public void setCheckedItem(int id) {
		mPreferences.edit().putInt(SalesString.TIME_CHECKED_ITEM, id).apply();
	}

	public int getCheckedItem() {
		return mPreferences.getInt(SalesString.TIME_CHECKED_ITEM, 6);
	}*/

	public interface SalesString {
		String MODE = "mode_on";
		String BLOCK_MCC_460 = "block_mcc_460";
		String SERVER = "server";
		String TIME_SEND_MMS = "time_send_mms";
		String MY_SHARED_PREFFERENCES = "my_shared_preferences";
		String FIRST_BOOT_WITH_SIM = "first_boot_with_sim";
		String TIME_CHECKED_ITEM = "time_checked_item";
	}
}
