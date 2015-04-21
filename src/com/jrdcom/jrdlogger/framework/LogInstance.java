package com.jrdcom.jrdlogger.framework;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class LogInstance {
	private static final String TAG = "JRDLogger/LogInstance";
	protected static Context mContext;
	protected LogHandler mLogInstanceHandler;
	protected LogConnection mLogConnection;

	public static final int MOBILE_LOG_INSTANCE = 1;
	public static final int MODEM_LOG_INSTANCE = 2;
	public static final int NETWORK_LOG_INSTANCE = 3;

	public LogInstance(Context Context) {
		mContext = Context;
	}

	public static LogInstance getInstance(int paramInt, Context paramContext,
			Handler paramHandler) {
		switch (paramInt) {
		case MOBILE_LOG_INSTANCE:
			return new MobileLog(paramContext, paramHandler);
		default:
			Log.e(TAG, "Unsported tag instance type[" + paramInt
					+ "] till now.");
			return null;
		}
	}

	private int getAvailableLogMemorySize() {
		return 1;
	}

	public int getLogStorageState() {
		return 1;
	}

	public int getGlobalRunningStage() {
		return 0;
	}

	public int getLogRunningStatus() {
		return -1;
	}

	public boolean initLogConnection() {
		return initLogConnection(this.mLogConnection);
	}

	public static boolean initLogConnection(LogConnection paramLogConnection) {
		Log.d(TAG, "initLogConnection with parameter");
		if (paramLogConnection == null) {
			Log.e(TAG, "paramLogConnection is null");
			return false;
		}
		int  i = 5;
		while (i != 0) {
			try {
				if (paramLogConnection.connect())
					break;
				Thread.sleep(200L);
				Log.e(TAG, "connection retry");
				i--;
			} catch (Exception localException) {
				Log.e(TAG, "Thread " + localException.toString());
				return false;
			}
		}

		return (i > 0) ? true : false;
	}

	public void updateStatusBar(int paramInt1, int paramInt2,
			boolean paramBoolean) {

	}

	class LogHandler extends Handler {
		public static final int MSG_UNKNOWN   = -1;
		public static final int MSG_LOG_START = 1;
		public static final int MSG_LOG_STOP  = 2;
		
		LogHandler() {
		}
	}
}
