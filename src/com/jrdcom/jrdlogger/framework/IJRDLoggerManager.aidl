package com.jrdcom.jrdlogger.framework;

interface IJRDLoggerManager
{
	int getCurrentRunningStage();
	int getLogRunningStatus(int paramInt);
	boolean runCommand(String paramString);
	boolean setAutoStart(int paramInt, boolean paramBoolean);
	boolean setLogSize(int paramInt1, int paramInt2);
	boolean setTotalLogSize(int paramInt1, int paramInt2);
	boolean startLog(int paramInt, String paramString);
	boolean stopCommand();
	boolean stopLog(int paramInt, String paramString);  
	
}
