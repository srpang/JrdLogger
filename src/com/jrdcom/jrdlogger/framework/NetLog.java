package com.jrdcom.jrdlogger.framework;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class NetLog extends LogInstance {
	private static final String TAG = "JRDLogger/NetLog";

	private Handler mServicehandler = null;
	private NetLogThread mNetLogThread;
	private final String mSocketName = "jrdlogd";
	private boolean bConnected = false;

	public NetLog(Context paramContext, Handler paramHandler) {
		super(paramContext);
		mServicehandler = paramHandler;
		createNetLogThread();
		mLogConnection = new NetLogConnection(mSocketName, mLogInstanceHandler);
	}

	private void createNetLogThread() {
		mNetLogThread = new NetLogThread();
		mNetLogThread.start();
		waitForHandlerCreation();
	}

	private void waitForHandlerCreation() {
		synchronized (NetLog.this) {
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

	class NetLogConnection extends LogConnection {
		public NetLogConnection(String paramString, Handler localHandler) {
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

	class NetLogHandler extends LogInstance.LogHandler {
		public NetLogHandler () {
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
				mLogConnection.sendCmd("netlog", "start");
				break;
			default:
				break;
			}

		}
	}

	class NetLogThread extends Thread {
		NetLogThread() {
			super("NetLogThread");
		}

		@Override
		public void run() {
			Looper.prepare();

			synchronized (NetLog.this) {
				mLogInstanceHandler = new NetLogHandler();

				// Notify that the handler has been created
				NetLog.this.notify();
			}

			// Listen for volume change requests that are set by VolumePanel
			Looper.loop();
		}
	}
}
