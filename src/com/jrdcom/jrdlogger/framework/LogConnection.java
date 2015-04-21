package com.jrdcom.jrdlogger.framework;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.InputStream;
import java.io.OutputStream;

public class LogConnection {
	private static final String TAG = "JRDLogger/LogConnection";
	private int mInstanceIndex = -1;

	private Handler mHandler = null;
	private LocalSocket socket;
	private LocalSocketAddress address;

	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private Thread mListenThread = null;

	public LogConnection(int paramInt, String paramString,
			LocalSocketAddress.Namespace paramNamespace, Handler paramHandler) {
		this(paramString, paramNamespace, paramHandler);
		this.mInstanceIndex = paramInt;
	}

	public LogConnection(String paramString,
			LocalSocketAddress.Namespace paramNamespace, Handler paramHandler) {
		this.mHandler = paramHandler;
		this.socket = new LocalSocket();
		this.address = new LocalSocketAddress(paramString, paramNamespace);
	}

	public LogConnection(String paramString, Handler paramHandler) {
		this(paramString, LocalSocketAddress.Namespace.ABSTRACT, paramHandler);
	}

	public boolean connect() {
		Log.d(TAG, "connect, socketname = " + address.getName());
		try {
			socket.connect(address, 5000);
			mOutputStream = socket.getOutputStream();
			mInputStream = socket.getInputStream();
			mListenThread = new Thread() {
				public void run() {
					listen();
				}
			};
			mListenThread.start();
			return true;
		} catch (Exception localException) {
			Log.e(TAG, "Communication error: " + localException.toString());
		}
		return false;
	}

	public boolean isConnected() {
		return (this.socket != null) && (this.socket.isConnected());
	}

	public void listen() {

	}

	public boolean sendCmd(String paramString) {
		return false;
	}

	public void stop() {

	}
}
