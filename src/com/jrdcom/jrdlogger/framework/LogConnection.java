package com.jrdcom.jrdlogger.framework;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class LogConnection {
	private static final String TAG = "JRDLogger/LogConnection";
	private int mInstanceIndex = -1;

	private Handler mHandler = null;
	private LocalSocket mSocket;
	private LocalSocketAddress address;

	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private Thread mListenThread = null;

	static private AtomicInteger mSequenceNumber = new AtomicInteger(0);;
	private final Object mCmdLock = new Object();

	public LogConnection(int paramInt, String paramString,
			LocalSocketAddress.Namespace paramNamespace, Handler paramHandler) {
		this(paramString, paramNamespace, paramHandler);
		this.mInstanceIndex = paramInt;
	}

	public LogConnection(String paramString,
			LocalSocketAddress.Namespace paramNamespace, Handler paramHandler) {
		this.mHandler = paramHandler;
		this.mSocket = new LocalSocket();
		this.address = new LocalSocketAddress(paramString, paramNamespace);
	}

	public LogConnection(String paramString, Handler paramHandler) {
		this(paramString, LocalSocketAddress.Namespace.RESERVED, paramHandler);
	}

	public boolean connect() {
		Log.d(TAG, "connect, socketname = " + address.getName());
		try {
			mSocket.connect(address);
			mOutputStream = mSocket.getOutputStream();
			mInputStream = mSocket.getInputStream();
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

	public void dealWithResponse(byte[] paramArrayOfByte, Handler paramHandler) {
		
	}

	public boolean isConnected() {
		return (this.mSocket != null) && (this.mSocket.isConnected());
	}

	public void listen() {
		byte[] responseBuf1 = new byte[100];
		Log.d(TAG, "Monitor thread running");
		while (true)
		{
            try
		    {
		        int i = this.mInputStream.read(responseBuf1, 0, 100);
		        if (i < 0)
		        {
		        	Log.e(TAG, "Get a empty response from native layer, stop listen.");
	                return;
	            }

		        Log.v(TAG, "Response from native byte size=" + i);
		        byte[] responseBuf2 = new byte[i];
		        System.arraycopy(responseBuf1, 0, responseBuf2, 0, i);

		        dealWithResponse(responseBuf2, this.mHandler);
		    }
	        catch (IOException localIOException)
	        {
	        	Log.e(TAG, "read failed", localIOException);
		        break;
	        }
	    }
	}

	public boolean sendCmd(String cmd, Object... args) {
		
        final StringBuilder rawBuilder = new StringBuilder();
        final StringBuilder logBuilder = new StringBuilder();
        
        final int sequenceNumber = mSequenceNumber.incrementAndGet();
        makeCommand(rawBuilder, logBuilder, sequenceNumber, cmd, args);
        final String rawCmd = rawBuilder.toString();
        final String logCmd = logBuilder.toString();

        Log.d(TAG, "SND -> {" + logCmd + "}");

        synchronized (mCmdLock) {
            if (mOutputStream == null) {
                throw new AssertionError("missing output stream");
            } else {
                try {
                    mOutputStream.write(rawCmd.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException("problem sending command", e);
                }
            }
        }
		return false;
	}
	
	public void stop() {

	}

	private void makeCommand(StringBuilder rawBuilder, StringBuilder logBuilder, int sequenceNumber, 
														 String cmd, Object... args) {
        if (cmd.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Unexpected command: " + cmd);
        }
        if (cmd.indexOf(' ') >= 0) {
            throw new IllegalArgumentException("Arguments must be separate from command");
        }
        
        rawBuilder.append(sequenceNumber).append(' ').append(cmd);
        logBuilder.append(sequenceNumber).append(' ').append(cmd);
        
        for (Object arg : args) {
            final String argString = String.valueOf(arg);
            if (argString.indexOf('\0') >= 0) {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }

            rawBuilder.append(' ');
            logBuilder.append(' ');

            appendEscaped(rawBuilder, argString);
            appendEscaped(logBuilder, argString);
        }

        rawBuilder.append('\0');
	}

    private void appendEscaped(StringBuilder builder, String arg) {
        final boolean hasSpaces = arg.indexOf(' ') >= 0;
        if (hasSpaces) {
            builder.append('"');
        }

        final int length = arg.length();
        for (int i = 0; i < length; i++) {
            final char c = arg.charAt(i);

            if (c == '"') {
                builder.append("\\\"");
            } else if (c == '\\') {
                builder.append("\\\\");
            } else {
                builder.append(c);
            }
        }

        if (hasSpaces) {
            builder.append('"');
        }
    }

}
