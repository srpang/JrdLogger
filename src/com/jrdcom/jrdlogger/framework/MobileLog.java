package com.jrdcom.jrdlogger.framework;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class MobileLog extends LogInstance {
	private static final String TAG = "JRDLogger/MobileLog";

	private Handler mServicehandler = null;
	private MobileLogThread mMobileLogThread;
	private final String mSocketName = "jrdlogd";
	private boolean bConnected = false;

	public MobileLog(Context paramContext, Handler paramHandler) {
		super(paramContext);
		mServicehandler = paramHandler;
		createMobileLogThread();
		mLogConnection = new MobileLogConnection(mSocketName, mLogInstanceHandler);
	}

	private void createMobileLogThread() {
		mMobileLogThread = new MobileLogThread();
		mMobileLogThread.start();
		waitForHandlerCreation();
	}

	private void waitForHandlerCreation() {
		synchronized (MobileLog.this) {
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

	class MobileLogConnection extends LogConnection {
		public MobileLogConnection(String paramString, Handler localHandler) {
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

	class MobileLogHandler extends LogInstance.LogHandler {
		public MobileLogHandler () {
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
				mLogConnection.sendCmd("mobilelog", "start");
				break;
			default:
				break;
			}

		}
	}

	class MobileLogThread extends Thread {
		MobileLogThread() {
			super("MobileLogThread");
		}

		@Override
		public void run() {
			Looper.prepare();

			synchronized (MobileLog.this) {
				mLogInstanceHandler = new MobileLogHandler();

				// Notify that the handler has been created
				MobileLog.this.notify();
			}

			// Listen for volume change requests that are set by VolumePanel
			Looper.loop();
		}
	}
}
