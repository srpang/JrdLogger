package com.jrdcom.jrdlogger.framework;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.jrdcom.jrdlogger.framework.IJRDLoggerManager;

public class JRDLoggerService extends Service {
	private static final String TAG = "JRDLogger/JRDLoggerManager";

	public IJRDLoggerManager.Stub mBind = new IJRDLoggerManager.Stub() {
		public boolean clearLog() {
			Log.d(TAG, "MTKLogger/MTKLoggerService -->clearLog()");
			return true;
		}

		public int getCurrentRunningStage() {
			return JRDLoggerService.this.getGlobalRunningStage();
		}

		public int getLogRunningStatus(int paramInt) {
			return JRDLoggerService.this.getLogInstanceRunningStatus(paramInt);
		}

		public boolean runCommand(String paramString) {

			return false;
		}

		public boolean setAutoStart(int paramInt, boolean paramBoolean) {
			return JRDLoggerService.this
					.setLogAutoStart(paramInt, paramBoolean);
		}

		public boolean setLogSize(int paramInt1, int paramInt2) {
			return JRDLoggerService.this.setEachLogSize(paramInt1, paramInt2);
		}

		public boolean setTotalLogSize(int paramInt1, int paramInt2) {
			return JRDLoggerService.this.setEachTotalLogSize(paramInt1,
					paramInt2);
		}

		public boolean startLog(int paramInt, String paramString) {
			return JRDLoggerService.this.startRecording(paramInt, paramString);
		}

		public boolean stopCommand() {
			return false;
		}

		public boolean stopLog(int paramInt, String paramString) {
			return JRDLoggerService.this.stopRecording(paramInt, paramString);
		}

	};

	private SparseArray<LogInstance> mLogInstanceMap = new SparseArray<LogInstance>();
	private Handler mNativeStateHandler = null;

	private LogInstance getLogInstance(int paramInt) {
		if (mLogInstanceMap.indexOfKey(paramInt) < 0) {
			LogInstance localLogInstance = LogInstance.getInstance(paramInt,
					this, mNativeStateHandler);
			if (localLogInstance != null)
				mLogInstanceMap.put(paramInt, localLogInstance);
		}
		return (LogInstance) mLogInstanceMap.get(paramInt);
	}

	private int getGlobalRunningStage() {
		return 1;
	}

	public int getLogInstanceRunningStatus(int paramInt) {
		return 1;
	}

	public boolean setLogAutoStart(int paramInt, boolean paramBoolean) {
		return false;
	}

	public boolean setEachLogSize(int paramInt1, int paramInt2) {
		return false;
	}

	public boolean setEachTotalLogSize(int paramInt1, int paramInt2) {
		return false;
	}

	public boolean startRecording(int paramInt, String paramString) {
		LogInstance localLogInstance = getLogInstance(LogInstance.MOBILE_LOG_INSTANCE);
		LogInstance.LogHandler localLogHandler = localLogInstance.mLogInstanceHandler;
		localLogHandler.sendMessageDelayed(localLogHandler.obtainMessage(
				LogInstance.LogHandler.MSG_LOG_START, paramString), 300);

		return true;
	}

	public boolean stopRecording(int paramInt, String paramString) {
		return false;
	}

	public IBinder onBind(Intent paramIntent) {
		Log.d(TAG, "onBind()");
		return this.mBind;
	}

	@Override
	public void onCreate() {

	}

	@Override
	public void onDestroy() {

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, ">>> JRDLoggerService.onStartCommand intent: " + intent
				+ " startId: " + startId);
		int iRet = super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "<<< JRDLoggerService.onStartCommand: " + iRet);
		return START_NOT_STICKY;
	}
}