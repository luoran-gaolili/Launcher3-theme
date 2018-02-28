package com.android.launcher3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import android.os.SystemProperties;
import android.os.IBinder;
import android.os.Build;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * 
 * @author yulong.liang
 * @date 2012-5-21 9:52:11
 */
public class ActivityUtils {
	public static final int quency_days = 3;// update system,Frequency

	private static final int AP_CFG_REEB_PRODUCT_NEW_INFO_LID = 71; // 92 kk
																	// should be
																	// 45
	private static final String TAG = "ActivityUtils";

	public static OnRecordChangeLister lister;

	// add download statistics 20121023 yulong.liang end
	// Read data from nvram
	public static int readNVData(int flag) {
		IBinder binder = ServiceManager.getService("NvRAMAgent");
		int b = -1;
		if (binder != null) {
			NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
			byte[] buff = null;
			try {
				buff = agent.readFile(AP_CFG_REEB_PRODUCT_NEW_INFO_LID);// read
				// buffer from nvra
				if (buff != null) {
					b = buff[flag];
					if (b == -1)
						b = 0;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("b:" + b);
		return b;
	}

	// Write data to nvram
	public static int writeNVData(int indexNum, int indexValue) {
		IBinder binder = ServiceManager.getService("NvRAMAgent");
		if (binder != null) {
			NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
			byte[] buff = null;
			try {
				buff = agent.readFile(AP_CFG_REEB_PRODUCT_NEW_INFO_LID);// read
				buff[indexNum] = (byte) indexValue;
			} catch (Exception e) {
				e.printStackTrace();
			}
			int flag = 0;
			try {
				flag = agent.writeFile(AP_CFG_REEB_PRODUCT_NEW_INFO_LID, buff);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("flag=" + flag);
			if (lister != null && indexNum == 253) {
				lister.onRecordChanged(indexValue == 1);
			}
			return flag;
		}
		return 0;
	}

	public static int whetherRegister() {
		int regParm = readNVData(253);
		return regParm;
	}

	public static void setOnRecordChangeLister(OnRecordChangeLister l) {
		lister = l;
	}

	public interface OnRecordChangeLister {
		void onRecordChanged(boolean registered);
	}
}
