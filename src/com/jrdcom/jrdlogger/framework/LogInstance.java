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
		case MODEM_LOG_INSTANCE:
			return new ModemLog(paramContext, paramHandler);
		case NETWORK_LOG_INSTANCE:
			return new NetLog(paramContext, paramHandler);
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

	public boolean getLogRunningStatus() {
		return false;
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
	
    class JRDLogdResponseCode {
        // 100 series - Requestion action was initiated; expect another reply
        // before proceeding with a new command.
        public static final int ActionInitiated           = 100;
        
        // 200 series - Requested action has been successfully completed
        public static final int CommandOkay               = 200;
        public static final int StartOkay                 = 201;
        public static final int StopOkay                  = 202;

        // 400 series - The command was accepted but the requested action
        // did not take place.
        public static final int OperationFailed           = 400;
        public static final int StartFailed               = 401;
        public static final int StopFailed                = 402;

        // 500 series - The command was not accepted and the requested
        // action did not take place.
        public static final int CommandSyntaxError        = 500;
        public static final int CommandParameterError     = 501;
        
        // 600 series - self defined errors
        public static final int FileAccessFailed          = 600;
        public static final int FileOpenFailed            = 601;
        public static final int ThreadStartFailed         = 602;
        public static final int ThreadStopFailed          = 602;
        public static final int LogAlreadyStartFailed     = 603;

        public static final int ImpossibleFailed          = 700;
    }
}
