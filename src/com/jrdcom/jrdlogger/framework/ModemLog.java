package com.jrdcom.jrdlogger.framework;

import com.jrdcom.jrdlogger.framework.LogInstance.JRDLogdResponseCode;
import com.jrdcom.jrdlogger.framework.LogInstance.LogHandler;

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
	private boolean bModemLogRunning = false;

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

	@Override
	public boolean getLogRunningStatus() {
		mLogInstanceHandler.sendMessage(mLogInstanceHandler.obtainMessage(LogHandler.MSG_LOG_GET_RUNNING_STATUS));
		waitfor();
		return bModemLogRunning;
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
			
            if (str.startsWith(String.valueOf(JRDLogdResponseCode.StartOkay))) {
            	bModemLogRunning = true;
            	return;
            }

            if (str.startsWith(String.valueOf(JRDLogdResponseCode.StopOkay))) {
            	bModemLogRunning = false;
            	return;
            }	
            
            if (str.startsWith(String.valueOf(JRDLogdResponseCode.GetRunningStatusRsp))) {
            	final String[] parsed = str.split(" ");
            	bModemLogRunning = parsed[2].startsWith("true");
            	notifyfor();
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
			case MSG_LOG_STOP:
				mLogConnection.sendCmd("modemlog", "stop");
				break;
			case MSG_LOG_GET_RUNNING_STATUS:
				mLogConnection.sendCmd("modemlog", "getstatus");
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
