package com.jrdcom.jrdlogger.framework;

import com.jrdcom.jrdlogger.framework.LogInstance.JRDLogdResponseCode;
import com.jrdcom.jrdlogger.framework.LogInstance.LogHandler;

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
	private boolean bNetLogRunning = false;
	
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

	@Override
	public boolean getLogRunningStatus() {
		mLogInstanceHandler.sendMessage(mLogInstanceHandler.obtainMessage(LogHandler.MSG_LOG_GET_RUNNING_STATUS));
		waitfor();
		return bNetLogRunning;
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

            if (str.startsWith(String.valueOf(JRDLogdResponseCode.StartOkay))) {
            	bNetLogRunning = true;
            	return;
            }

            if (str.startsWith(String.valueOf(JRDLogdResponseCode.StopOkay))) {
            	bNetLogRunning = false;
            	return;
            }

            if (str.startsWith(String.valueOf(JRDLogdResponseCode.GetRunningStatusRsp))) {
            	final String[] parsed = str.split(" ");
            	bNetLogRunning = parsed[2].startsWith("true");
            	notifyfor();
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
			case MSG_LOG_STOP:
				mLogConnection.sendCmd("netlog", "stop");
				break;
			case MSG_LOG_GET_RUNNING_STATUS:
				mLogConnection.sendCmd("netlog", "getstatus");
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
