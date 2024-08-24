package com.chasearchive.radarImageCli;

public class DebugLogger {
	DebugLoggerLevel programLevel;
	
	public DebugLogger(DebugLoggerLevel programLevel) {
		this.programLevel = programLevel;
	}

	public void println(Object obj, DebugLoggerLevel strLevel) {
		if(programLevel == DebugLoggerLevel.SILENT) {
			return;
		} else if (programLevel == DebugLoggerLevel.BRIEF) {
			if(strLevel == DebugLoggerLevel.BRIEF) {
				System.out.println(obj);
			} else {
				return;
			}
		} else {
			System.out.println(obj);
		}
	}
}
