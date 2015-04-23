package com.jrdcom.jrdlogger.framework;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class LogConnection {
	private static final String TAG = "JRDLogger/LogConnection";
	private int mInstanceIndex = -1;

	private Handler mHandler = null;
	private LocalSocket socket;
	private LocalSocketAddress address;

	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private Thread mListenThread = null;

	private final Object mCmdLock = new Object();

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

	public boolean sendCmd(String cmd, Object... args) {
		
        final StringBuilder rawBuilder = new StringBuilder();
        final StringBuilder logBuilder = new StringBuilder();

        makeCommand(rawBuilder, logBuilder, cmd, args);
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

	private void makeCommand(StringBuilder rawBuilder, StringBuilder logBuilder, String cmd, Object... args) {
        if (cmd.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Unexpected command: " + cmd);
        }
        if (cmd.indexOf(' ') >= 0) {
            throw new IllegalArgumentException("Arguments must be separate from command");
        }
        
        rawBuilder.append(cmd);
        logBuilder.append(cmd);
        
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
