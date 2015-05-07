package com.jrdcom.jrdlogger.framework;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ModemLog extends LogInstance {
	private static final String TAG = "JRDLogger/ModemLog";

	private Handler mServicehandler = null;
	private ModemLogThread mModemLogThread;
	private final String mSocketName = "jrdlogd";
	private boolean bConnected = false;

	public ModemLog(Context paramContext, Handler paramHandler) {
		super(paramContext);
		mServicehandler = paramHandler;
		createModemLogThread();
		mLogConnection = new ModemLogConnection(mSocketName, mLogInstanceHandler);
	}

	private void createModemLogThread() {
		mModemLogThread = new ModemLogThread();
		mModemLogThread.start();
		waitForHandlerCreation();
	}

	private void waitForHandlerCreation() {
		synchronized (ModemLog.this) {
			while (mLogInstanceHandler == null) {
				try {
					// Wait for mAudioHandler to be set by the other thread
					wait();
				} catch (InterruptedException e) {
					Log.e(TAG, "Interrupted while waiting on volume handler.");
				}
			}
		}
	}

	class ModemLogConnection extends LogConnection {
		public ModemLogConnection(String paramString, Handler localHandler) {
			super(paramString, localHandler);
		}

		@Override
		public void dealWithResponse(byte[] rspbuff, Handler rspHandler) {
			super.dealWithResponse(rspbuff, rspHandler);
			String str = new String(rspbuff);
			Log.d(TAG, "-->dealWithResponse(), rsp=" + str);
			if ((rspbuff == null) || (rspbuff.length == 0)) {
				 Log.d(TAG, "Get an empty response from native, ignore it.");
				 return;
			}
			
			
			
		}

	}

	class ModemLogHandler extends LogInstance.LogHandler {
		public ModemLogHandler () {
			super();
		}

		@Override
		public void handleMessage(Message paramMessage) {
			if (!bConnected) {
				bConnected = initLogConnection();
			}

			if (!bConnected) {
				Log.e(TAG, "cannot connect with the mobilelog native service");
			}

			switch (paramMessage.what) {
			case MSG_LOG_START:
				mLogConnection.sendCmd("modemlog", "start");
				break;
			default:
				break;
			}

		}
	}

	class ModemLogThread extends Thread {
		ModemLogThread() {
			super("ModemLogThread");
		}

		@Override
		public void run() {
			Looper.prepare();

			synchronized (ModemLog.this) {
				mLogInstanceHandler = new ModemLogHandler();

				// Notify that the handler has been created
				ModemLog.this.notify();
			}

			// Listen for volume change requests that are set by VolumePanel
			Looper.loop();
		}
	}
}
